# 上线增量脚本

这个目录**只放生产环境要执行的增量 SQL**，按文件名顺序执行。

## 规则速查

- 命名：`YYYYMMDD_序号_描述.sql`，例 `20261101_01_add_chip_no_to_cat_profile.sql`
- 只加不改：`CREATE TABLE IF NOT EXISTS` / `ALTER TABLE ... ADD` / `INSERT` / 带条件的 `UPDATE`
- 禁止：`DROP` / `TRUNCATE` / 不带 `WHERE` 的 `UPDATE`、`DELETE`
- 必须幂等，重复执行不报错、不重复插入
- **已执行过的脚本冻结**，要修正就再追加一个新脚本
- 文件头写清：变更目的、影响表、能否回滚、回滚语句

## 执行

```bash
for f in backend/sql/upgrade/*.sql; do
  echo ">> $f"
  mysql -u root -p cat_rescue < "$f" || { echo "失败，中断"; break; }
done

redis-cli --scan --pattern 'sys_config:*' | xargs -r redis-cli del
redis-cli --scan --pattern 'sys_dict:*'   | xargs -r redis-cli del
```

模板与完整规范见 [docs/DATABASE.md](../../docs/DATABASE.md) 第五节。

---

## 本地怎么办

本地**不用**这个目录。本地直接改库，改完重新导出
`backend/sql/cat_rescue.sql` 覆盖基线（导出命令见 docs/DATABASE.md 第四节），
这样本地永远是"一份完整文件"。

增量脚本在本地也执行一遍，保证本地与线上结构一致，然后再重新导出基线。
