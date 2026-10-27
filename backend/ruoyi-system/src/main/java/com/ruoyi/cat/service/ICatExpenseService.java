package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatExpense;

/**
 * 财务支出 服务层
 * 
 * @author ruoyi
 */
public interface ICatExpenseService
{
    public CatExpense selectCatExpenseById(Long expenseId);

    public List<CatExpense> selectCatExpenseList(CatExpense expenseId);

    public int insertCatExpense(CatExpense expenseId);

    public int updateCatExpense(CatExpense expenseId);

    public int deleteCatExpenseByIds(Long[] expenseIds);
}
