package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 在职员工账号 数据层
 * <p>
 * 用药执行人、疫苗负责人、回访人、登记人、手术医生这类「谁做的」字段，
 * 不再接受自由文本，一律从系统账号里选，避免出现「助理小李」这种对不上人的名字。
 *
 * @author ruoyi
 */
public interface CatStaffMapper
{
    /**
     * 候选人员列表
     *
     * @param roleKeys 角色标识（空 = 全部在职账号）
     * @return userId / userName / nickName
     */
    List<Map<String, Object>> selectStaffOptions(@Param("roleKeys") List<String> roleKeys);

    /**
     * 按昵称在候选范围内反查账号
     *
     * @param nickName 昵称
     * @param roleKeys 角色标识（空 = 全部在职账号）
     */
    Map<String, Object> selectStaffByNickName(@Param("nickName") String nickName, @Param("roleKeys") List<String> roleKeys);
}
