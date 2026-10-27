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
