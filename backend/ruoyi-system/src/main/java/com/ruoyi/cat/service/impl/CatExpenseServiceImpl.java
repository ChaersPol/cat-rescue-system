package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatExpense;
import com.ruoyi.cat.mapper.CatExpenseMapper;
import com.ruoyi.cat.service.ICatExpenseService;

/**
 * 财务支出 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatExpenseServiceImpl implements ICatExpenseService
{
    @Autowired
    private CatExpenseMapper expenseIdMapper;

    @Override
    public CatExpense selectCatExpenseById(Long expenseId)
    {
        return expenseIdMapper.selectCatExpenseById(expenseId);
    }

    @Override
    public List<CatExpense> selectCatExpenseList(CatExpense expenseId)
    {
        return expenseIdMapper.selectCatExpenseList(expenseId);
    }

    @Override
    public int insertCatExpense(CatExpense expenseId)
    {
        return expenseIdMapper.insertCatExpense(expenseId);
    }

    @Override
    public int updateCatExpense(CatExpense expenseId)
    {
        return expenseIdMapper.updateCatExpense(expenseId);
    }

    @Override
    public int deleteCatExpenseByIds(Long[] expenseIds)
    {
        return expenseIdMapper.deleteCatExpenseByIds(expenseIds);
    }
}
