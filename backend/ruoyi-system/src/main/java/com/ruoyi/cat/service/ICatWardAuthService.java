package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatWardAuth;

/**
 * 病房授权 服务层
 * 
 * @author ruoyi
 */
public interface ICatWardAuthService
{
    /**
     * 查询病房授权列表
     */
    public List<CatWardAuth> selectCatWardAuthList(CatWardAuth wardAuth);

    /**
     * 查询某用户授权的病房id列表
     */
    public List<Long> selectWardIdsByUserId(Long userId);

    /**
     * 查询某病房授权的用户id列表
     */
    public List<Long> selectUserIdsByWardId(Long wardId);

    /**
     * 当前登录用户是否需要按病房授权收敛数据范围。
     *
     * 规则：超管、管理员(manager)、医生(doctor) 一律不收敛；
     * 只有「助理(assistant)」角色、且在病房授权表里确实配置了病房时才收敛。
     * 助理若一条授权都没有，视为未启用病房隔离（否则会出现整站 0 条数据）。
     */
    public boolean isWardScopedUser();

    /**
     * 保存某用户的病房授权（先删后增）
     */
    public int saveUserWardAuth(Long userId, Long[] wardIds);

    /**
     * 新增单条授权
     */
    public int insertCatWardAuth(CatWardAuth wardAuth);

    /**
     * 删除某用户全部病房授权
     */
    public int deleteByUserId(Long userId);

    /**
     * 删除某病房全部用户授权
     */
    public int deleteByWardId(Long wardId);
}
