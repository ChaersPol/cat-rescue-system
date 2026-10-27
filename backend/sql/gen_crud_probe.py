#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全模块 CRUD 回归探针：
1. 解析每个 Mapper XML 的 insert 语句得到表名 + 插入列
2. 从 information_schema 取列类型 / 是否可空
3. 生成最小可用 payload（必填列 + 少量业务列），走 新增->查询->修改->删除 全流程
输出：每个模块 PASS/FAIL 与失败原因
"""
import json
import os
import re
import subprocess
import sys
import urllib.request

BASE = "http://localhost:8080"
PROBE_CAT = None          # 探针专用临时档案，避免污染演示数据
baselines = {}
PROBE_ARCHIVE = "RT-PROBE-%d" % int(__import__("time").time())
MAPPER_DIR = "/Users/chaers/Documents/jiedan/myProjects/261023-dou-救助站-2000-1000/backend/ruoyi-system/src/main/resources/mapper/cat"

MODULES = {
    "base":         ("CatBaseMapper.xml",           "/cat/base", "baseId"),
    "ward":         ("CatWardMapper.xml",           "/cat/ward", "wardId"),
    "tag":          ("CatTagMapper.xml",            "/cat/tag", "tagId"),
    "profile":      ("CatProfileMapper.xml",        "/cat/profile", "catId"),
    "vaccine":      ("CatVaccineMapper.xml",        "/cat/vaccine", "vaccineId"),
    "deworm":       ("CatDewormMapper.xml",         "/cat/deworm", "dewormId"),
    "diagnosis":    ("CatDiagnosisMapper.xml",      "/cat/diagnosis", "diagnosisId"),
    "medication":   ("CatMedicationMapper.xml",     "/cat/medication", "medicationId"),
    "photo":        ("CatPhotoMapper.xml",          "/cat/photo", "photoId"),
    "flow":         ("CatFlowMapper.xml",           "/cat/flow", "flowId"),
    "tnr":          ("CatTnrMapper.xml",            "/cat/tnr", "tnrId"),
    "adoption":     ("CatAdoptionMapper.xml",       "/cat/adoption", "adoptionId"),
    "release":      ("CatReleaseMapper.xml",        "/cat/release", "releaseId"),
    "death":        ("CatDeathMapper.xml",          "/cat/death", "deathId"),
    "referral":     ("CatReferralMapper.xml",       "/cat/referral", "referralId"),
    "visit":        ("CatVisitMapper.xml",          "/cat/visit", "visitId"),
    "expense":      ("CatExpenseMapper.xml",        "/cat/expense", "expenseId"),
    "feecategory":  ("CatFeeCategoryMapper.xml",    "/cat/feecategory", "categoryId"),
    "material":     ("CatMaterialMapper.xml",       "/cat/material", "materialId"),
    "materialcategory": ("CatMaterialCategoryMapper.xml", "/cat/materialcategory", "categoryId"),
    "stockrecord":  ("CatStockRecordMapper.xml",    "/cat/stockrecord", "recordId"),
    "reminder":     ("CatReminderMapper.xml",       "/cat/reminder", "reminderId"),
    "config":       ("CatConfigMapper.xml",         "/cat/config", "configId"),
}


def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True)


def q(sql):
    p = sh("mysql -h127.0.0.1 -uroot -p123456 cat_rescue -N -B -e \"%s\" 2>/dev/null" % sql)
    rows = [l.split("\t") for l in p.stdout.strip().split("\n") if l.strip()]
    return rows


def parse_insert(xml_file):
    txt = open(os.path.join(MAPPER_DIR, xml_file), encoding="utf-8").read()
    m = re.search(r"<insert[^>]*>(.*?)</insert>", txt, re.S)
    if not m:
        return None, []
    body = re.sub(r"<[^>]*>", " ", m.group(1))  # 去掉 <if>/<trim> 等动态标签再解析
    body = re.sub(r"\s+", " ", body)
    # 兼容两种写法： insert into t (a,b) values / insert into t <trim prefix="("> a,b </trim> values
    tm = re.search(r"insert\s+into\s+(\w+)\s*\((.*?)\)\s*values", body, re.S | re.I)
    if not tm:
        tm2 = re.search(r"insert\s+into\s+(\w+)\s+(.*?)\s+values", body, re.S | re.I)
        if not tm2:
            return None, []
        table = tm2.group(1)
        inner = tm2.group(2).strip().strip("()")
        cols = [c.strip() for c in inner.split(",") if c.strip()]
        return table, cols
    table = tm.group(1)
    cols = [c.strip() for c in tm.group(2).replace("\n", " ").split(",") if c.strip()]
    return table, cols


def col_meta(table):
    rows = q("select column_name, data_type, is_nullable, column_default, extra, "
             "ifnull(character_maximum_length,0) "
             "from information_schema.columns where table_schema='cat_rescue' and table_name='%s'" % table)
    meta = {}
    for r in rows:
        if len(r) < 5:
            continue
        default = None if r[3] in ("NULL", "") else r[3]
        meta[r[0]] = {"type": r[1], "nullable": r[2], "default": default, "extra": r[4],
                      "len": int(r[5]) if len(r) > 5 and r[5].isdigit() else 0}
    return meta


SKIP = {"create_by", "create_time", "update_by", "update_time", "remark", "del_flag",
        "qr_code", "params"}

# 业务值提示：列名 -> 生成函数
def val_for(col, meta):
    t = meta.get("type", "varchar")
    if col.endswith("_id") or col == "id":
        return None  # 交给下面按主键自动取
    if t in ("int", "bigint", "smallint", "tinyint", "decimal", "double", "float"):
        return 1
    if t in ("char",):
        return "1"
    if t == "varchar" and 0 < meta.get("len", 0) <= 4:
        return "1"
    if t == "date":
        return "2026-10-26"
    if t in ("datetime", "timestamp"):
        return "2026-10-26 10:00:00"
    if t in ("mediumtext", "text", "longtext"):
        return "回归测试"
    return "回归测试"


def pick_fk(col, table):
    """为 xxx_id 取一个真实存在的值，避免脏关联"""
    mapping = {
        "cat_id": "cat_profile", "base_id": "cat_base", "ward_id": "cat_ward",
        "tag_id": "cat_tag", "category_id": None, "material_id": "cat_material",
        "adoption_id": "cat_adoption", "hospital_id": None, "user_id": "sys_user",
        "parent_id": None, "tnr_id": "cat_tnr", "referral_id": "cat_referral",
        "flow_id": "cat_flow", "expense_id": "cat_expense",
    }
    if col == "cat_id" and PROBE_CAT:
        return PROBE_CAT
    if table == "cat_ward" and col == "base_id":
        return sel("select base_id from cat_base order by base_id limit 1")
    if table in ("cat_profile",) and col == "base_id":
        return sel("select base_id from cat_base order by base_id limit 1")
    if table in ("cat_profile",) and col == "ward_id":
        return sel("select ward_id from cat_ward order by ward_id limit 1")
    tgt = mapping.get(col)
    if tgt:
        key = {"cat_profile": "cat_id", "cat_base": "base_id", "cat_ward": "ward_id",
               "cat_tag": "tag_id", "cat_material": "material_id", "sys_user": "user_id",
               "cat_adoption": "adoption_id", "cat_tnr": "tnr_id",
               "cat_referral": "referral_id", "cat_flow": "flow_id",
               "cat_expense": "expense_id"}.get(tgt)
        if key:
            v = sel("select %s from %s order by %s limit 1" % (key, tgt, key))
            if v:
                return v
    return 1


def sel(sql):
    r = q(sql)
    return r[0][0] if r and r[0] and r[0][0] != "NULL" else None


def snake(name):
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def camel(name):
    parts = name.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def build_payload(table, cols):
    meta = col_meta(table)
    payload = {}
    for c in cols:
        if c in SKIP:
            continue
        m = meta.get(c, {})
        required = (m.get("nullable") == "NO" and m.get("default") is None
                    and "auto_increment" not in (m.get("extra") or ""))
        if c.endswith("_id"):
            v = pick_fk(c, table)
            if v is not None:
                payload[camel(c)] = v
            continue
        v = val_for(c, m)
        if v is None:
            continue
        if required:
            payload[camel(c)] = v
    return payload


def req(method, path, token, data=None):
    url = BASE + path
    body = json.dumps(data).encode() if data is not None else None
    r = urllib.request.Request(url, data=body, method=method)
    r.add_header("Authorization", "Bearer " + token)
    r.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(r, timeout=25) as resp:
            return resp.status, json.loads(resp.read().decode() or "{}")
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode() or "{}")
        except Exception:
            return e.code, {}


def main():
    token = sh("cat /tmp/cat_token.txt").stdout.strip()
    if not token:
        print("缺少 token")
        return
    # 创建探针专用档案（跑完即删，避免污染演示数据）
    global PROBE_CAT
    req("POST", "/cat/profile", token, {"catName": "回归探针猫", "archiveNo": PROBE_ARCHIVE,
                                        "baseId": sel("select base_id from cat_base order by base_id limit 1"),
                                        "wardId": sel("select ward_id from cat_ward order by ward_id limit 1")})
    PROBE_CAT = sel("select cat_id from cat_profile where archive_no='%s'" % PROBE_ARCHIVE)
    print("探针档案 catId=%s\n" % PROBE_CAT)

    # 记录各表运行前主键基线，供结束时精准回收
    global baselines
    for name, (xml, path, idcol) in MODULES.items():
        t, _c = parse_insert(xml)
        if t:
            v = sel("select max(%s) from %s" % (snake(idcol), t))
            baselines[t] = int(v) if v and v.isdigit() else 0
    # 探针猫本身也要纳入基线回收范围
    baselines["cat_profile"] = int(PROBE_CAT) - 1 if PROBE_CAT else baselines.get("cat_profile", 0)

    results = []
    for name, (xml, path, idcol) in MODULES.items():
        table, cols = parse_insert(xml)
        rec = {"module": name, "table": table, "list": "", "add": "", "detail": "", "update": "", "delete": "", "note": ""}
        # list
        st, body = req("GET", path + "/list?pageNum=1&pageSize=5", token)
        rec["list"] = "%s/%s" % (st, body.get("total"))
        if st != 200:
            rec["note"] = "list失败:" + str(body.get("msg"))
            results.append(rec)
            continue
        if not table:
            rec["note"] = "无insert语句"
            results.append(rec)
            continue
        payload = build_payload(table, cols)
        st, body = req("POST", path, token, payload)
        rec["add"] = str(st)
        if st != 200 or body.get("code") != 200:
            rec["note"] = "add失败:" + str(body.get("msg"))[:120] + " payload=" + json.dumps(payload, ensure_ascii=False)[:200]
            results.append(rec)
            continue
        newid = sel("select max(%s) from %s" % (snake(idcol), table))
        if newid is None:
            rec["note"] = "取新主键失败"
            results.append(rec)
            continue
        st, body = req("GET", "%s/%s" % (path, newid), token)
        rec["detail"] = "%s/%s" % (st, (body.get("data") or {}).get(idcol))
        # update：改一个字符串字段
        upd = dict(payload)
        for k, v in list(upd.items()):
            if isinstance(v, str) and v == "回归测试":
                upd[k] = "回归测试-已修改"
                break
        upd[idcol] = int(newid) if newid.isdigit() else newid
        st, body = req("PUT", path, token, upd)
        rec["update"] = str(st) + "/" + str(body.get("code"))
        # delete
        st, body = req("DELETE", "%s/%s" % (path, newid), token)
        rec["delete"] = str(st) + "/" + str(body.get("code"))
        results.append(rec)

    print("%-18s %-24s %-10s %-6s %-12s %-8s %-8s" % ("模块", "表", "list", "add", "detail", "update", "delete"))
    bad = 0
    for r in results:
        ok = all(x.startswith("200") for x in [r["list"], r["add"]] if x) and r["note"] == ""
        if not ok:
            bad += 1
        print("%-18s %-24s %-10s %-6s %-12s %-8s %-8s %s" % (
            r["module"], r["table"], r["list"], r["add"], r["detail"], r["update"], r["delete"],
            ("<< " + r["note"]) if r["note"] else ""))
    # 回收探针数据：按运行前主键基线硬删除本模块表新增行（RuoYi 逻辑删除的行也一并清掉）
    tables = []
    for name, (xml, path, idcol) in MODULES.items():
        t, _c = parse_insert(xml)
        if t and t in baselines:
            tables.append((t, snake(idcol), baselines[t]))
    # 先删子表再删主表，避免留下孤儿数据
    order = ["cat_flow", "cat_visit", "cat_photo", "cat_vaccine", "cat_deworm",
             "cat_diagnosis", "cat_medication", "cat_tnr", "cat_adoption", "cat_release",
             "cat_death", "cat_referral", "cat_stock_record", "cat_reminder",
             "cat_material", "cat_material_category", "cat_expense", "cat_fee_category",
             "cat_ward", "cat_tag", "cat_base", "cat_profile"]
    tables.sort(key=lambda x: order.index(x[0]) if x[0] in order else 99)
    for t, idc, base in tables:
        q("delete from %s where %s > %s" % (t, idc, base))
    if PROBE_CAT:
        q("delete from cat_profile_tag where cat_id > %s" % baselines.get("cat_profile", 0))
    left = sel("select count(*) from cat_profile where cat_id > %s" % baselines.get("cat_profile", 0))
    print("探针数据回收: %s" % ("已清理" if left in (None, "0") else "仍有 %s 行残留" % left))

    print("\n失败模块数: %d / %d" % (bad, len(results)))


if __name__ == "__main__":
    main()
