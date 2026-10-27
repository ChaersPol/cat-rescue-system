package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatReferral;

/**
 * 外送医院转诊记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatReferralService
{
    public CatReferral selectCatReferralById(Long referralId);

    public List<CatReferral> selectCatReferralList(CatReferral referralId);

    public int insertCatReferral(CatReferral referralId);

    public int updateCatReferral(CatReferral referralId);

    public int deleteCatReferralByIds(Long[] referralIds);
}
