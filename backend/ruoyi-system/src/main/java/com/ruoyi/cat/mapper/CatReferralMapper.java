package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatReferral;

/**
 * 外送医院转诊记录 数据层
 * 
 * @author ruoyi
 */
public interface CatReferralMapper
{
    public CatReferral selectCatReferralById(Long referralId);

    public List<CatReferral> selectCatReferralList(CatReferral referralId);

    public int insertCatReferral(CatReferral referralId);

    public int updateCatReferral(CatReferral referralId);

    public int deleteCatReferralById(Long referralId);

    public int deleteCatReferralByIds(Long[] referralIds);
}
