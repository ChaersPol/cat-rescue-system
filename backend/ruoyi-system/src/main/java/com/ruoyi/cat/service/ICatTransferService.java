package com.ruoyi.cat.service;

import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;

/**
 * 猫咪流转核心业务服务
 * 负责：生成流转记录 + 联动更新猫咪状态/所在基地
 * 
 * @author ruoyi
 */
public interface ICatTransferService
{
    /**
     * 执行流转：写入流转记录，并同步更新猫咪状态与所在基地/病房
     * 
     * @param profile 猫咪档案（需含 catId、新状态 status、目标基地 baseId、病房 wardId）
     * @param flow 流转记录（类型、原因、医院等）
     * @return 结果
     */
    public int transfer(CatProfile profile, CatFlow flow);

    /**
     * 按流转类型自动推导猫咪状态与所在位置，再执行流转
     * 供「流转记录」页面直接发起流转使用
     *
     * @param flow 流转记录（需含 catId、flowType、目标基地/病房、医院名称等）
     * @return 结果
     */
    public int transferByFlow(CatFlow flow);
}
