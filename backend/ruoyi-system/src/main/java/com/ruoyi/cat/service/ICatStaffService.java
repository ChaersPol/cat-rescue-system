package com.ruoyi.cat.service;

import java.util.List;
import java.util.Map;

/**
 * 在职员工账号 服务层
 * <p>
 * 统一处理「这条记录是谁做的」这类人员字段：不再让前端自由填写姓名，
 * 而是从系统账号中选，保证名字和人对得上。
 *
 * @author ruoyi
 */
public interface ICatStaffService
{
    /** 角色范围：医生 */
    String SCOPE_DOCTOR = "doctor";

    /** 角色范围：助理（含物资管理员、回访专员等助理岗位） */
    String SCOPE_ASSISTANT = "assistant";

    /** 角色范围：全部在职账号 */
    String SCOPE_ALL = "all";

    /**
     * 候选人员下拉
     *
     * @param scope doctor / assistant / all
     */
    List<Map<String, Object>> selectOptions(String scope);

    /**
     * 新增时确定人员
     * <p>
     * 1. 登录人本身属于该角色范围 → 一律记本人，忽略前端传值，防止冒填别人；
     * 2. 其他角色（超管 / 管理员）→ 只能是候选账号里的姓名，否则报错。
     *
     * @param label    字段中文名，用于报错提示（如「用药执行人」）
     * @param scope    角色范围
     * @param name     前端传来的姓名
     * @param required 是否必填
     * @return 最终写入的姓名
     */
    String requireForInsert(String label, String scope, String name, boolean required);

    /**
     * 修改时的处理：能匹配到账号就保留姓名，匹配不到（历史手填）也不阻断超管纠错。
     *
     * @param scope 角色范围
     * @param name  前端传来的姓名
     * @return 最终写入的姓名
     */
    String normalizeForUpdate(String scope, String name, boolean required);

    /**
     * 主数据里的固定人名字段（如基地负责人）：只校验非空值必须是系统账号，
     * 不涉及「记本人」——负责人是基地的属性，不是谁操作的留痕。
     * <p>
     * 用于「新增」和「改了名字的修改」；改名没变时调用方应跳过校验，
     * 否则历史手填的老数据会被卡住无法保存其它字段。
     *
     * @param label 字段中文名，用于报错提示
     * @param scope 角色范围
     * @param name  前端传来的姓名
     * @return 最终写入的姓名（空值返回 null）
     */
    String validateOptional(String label, String scope, String name);

    /**
     * 当前登录人的展示名（优先昵称，没有昵称退回登录名）
     * <p>
     * 流转记录这类系统自动写入的「操作人」统一用它，避免出现 admin / assist01 这种登录名。
     */
    String currentStaffName();

    /**
     * 当前登录人是否拥有某个角色标识（精确匹配）
     * <p>
     * 不能用 {@code SecurityUtils.hasRole}：它把 admin 当通配角色，对任何 role 都返回 true。
     * 用于「助理不能新增诊断/就诊」这类硬性业务限制。
     */
    boolean hasCurrentRole(String roleKey);

    /**
     * 当前登录人是否属于管理岗（超管 / 管理员），管理岗不受岗位级限制
     */
    boolean isCurrentManager();
}
