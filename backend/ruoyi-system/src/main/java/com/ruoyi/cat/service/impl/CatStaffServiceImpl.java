package com.ruoyi.cat.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.mapper.CatStaffMapper;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 在职员工账号 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CatStaffServiceImpl implements ICatStaffService
{
    /** scope → 角色标识 */
    private static final Map<String, List<String>> SCOPE_ROLES = new HashMap<>();

    static
    {
        SCOPE_ROLES.put(SCOPE_DOCTOR, Arrays.asList("doctor"));
        SCOPE_ROLES.put(SCOPE_ASSISTANT, Arrays.asList("assistant"));
        SCOPE_ROLES.put(SCOPE_ALL, null);
    }

    @Autowired
    private CatStaffMapper staffMapper;

    @Override
    public List<Map<String, Object>> selectOptions(String scope)
    {
        return staffMapper.selectStaffOptions(rolesOf(scope));
    }

    @Override
    public String requireForInsert(String label, String scope, String name, boolean required)
    {
        SysUser user = currentUser();
        if (mustUseSelf(scope, user))
        {
            // 医生 / 助理本人录入：直接记本人，不采信前端传的名字
            return displayName(user);
        }
        if (StringUtils.isEmpty(name))
        {
            if (required)
            {
                throw new ServiceException("请选择" + label);
            }
            return null;
        }
        if (staffMapper.selectStaffByNickName(name, rolesOf(scope)) == null)
        {
            throw new ServiceException(label + "「" + name + "」不在系统账号列表中，请从下拉选择");
        }
        return name;
    }

    @Override
    public String normalizeForUpdate(String scope, String name, boolean required)
    {
        if (StringUtils.isEmpty(name))
        {
            if (required)
            {
                throw new ServiceException("请选择人员");
            }
            return null;
        }
        // 历史手填的名字（如「助理小李」）没有对应账号，允许原样保留，不阻断管理员修正其它字段
        return name;
    }

    @Override
    public String validateOptional(String label, String scope, String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        if (staffMapper.selectStaffByNickName(name, rolesOf(scope)) == null)
        {
            throw new ServiceException(label + "「" + name + "」不在系统账号列表中，请从下拉选择");
        }
        return name;
    }

    /**
     * 是否必须记本人：
     * 医生字段看是不是医生、助理字段看是不是助理；
     * 通用字段（操作人、登记人、领用人）除超管/管理员外都记本人。
     * <p>
     * 注意不能直接用 {@link SecurityUtils#hasRole(String)} 判断：该方法的实现里
     * 「admin」会被当成通配角色，对任何 role 都返回 true，超管会被误判成医生/助理。
     */
    private boolean mustUseSelf(String scope, SysUser user)
    {
        if (user == null || user.getUserId() == null)
        {
            return false;
        }
        if (isManager(user))
        {
            // 超管 / 管理员是代录角色，可以指定别人
            return false;
        }
        if (SCOPE_DOCTOR.equals(scope))
        {
            return hasExactRole(user, SCOPE_DOCTOR);
        }
        if (SCOPE_ASSISTANT.equals(scope))
        {
            return hasExactRole(user, SCOPE_ASSISTANT);
        }
        return true;
    }

    /** 超管 / 管理员：可以替别人代录 */
    private boolean isManager(SysUser user)
    {
        return SecurityUtils.isAdmin(user.getUserId())
                || hasExactRole(user, "admin")
                || hasExactRole(user, "manager");
    }

    /** 精确匹配角色标识（不走通配） */
    private boolean hasExactRole(SysUser user, String roleKey)
    {
        if (user.getRoles() == null)
        {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(r -> r != null && roleKey.equals(r.getRoleKey()));
    }

    private List<String> rolesOf(String scope)
    {
        if (StringUtils.isEmpty(scope) || SCOPE_ALL.equals(scope))
        {
            return null;
        }
        List<String> roles = SCOPE_ROLES.get(scope);
        if (roles == null)
        {
            throw new ServiceException("未知的人员范围：" + scope);
        }
        return roles;
    }

    @Override
    public String currentStaffName()
    {
        SysUser user = currentUser();
        if (user == null)
        {
            return null;
        }
        return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    @Override
    public boolean hasCurrentRole(String roleKey)
    {
        SysUser user = currentUser();
        return user != null && hasExactRole(user, roleKey);
    }

    @Override
    public boolean isCurrentManager()
    {
        SysUser user = currentUser();
        return user != null && isManager(user);
    }

    private SysUser currentUser()
    {
        try
        {
            return SecurityUtils.getLoginUser().getUser();
        }
        catch (Exception e)
        {
            // 定时任务 / 内部调用没有登录上下文，交给调用方按必填规则处理
            return null;
        }
    }

    private String displayName(SysUser user)
    {
        return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
    }
}
