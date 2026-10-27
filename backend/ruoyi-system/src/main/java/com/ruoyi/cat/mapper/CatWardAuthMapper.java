package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatWardAuth;

/**
 * 病房授权 数据层
 * 
 * @author ruoyi
 */
public interface CatWardAuthMapper
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
     * 新增病房授权
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

    /**
     * 批量新增（用户授权多个病房）
     */
    public int batchInsert(@Param("userId") Long userId, @Param("wardIds") Long[] wardIds, @Param("operator") String operator);
}
