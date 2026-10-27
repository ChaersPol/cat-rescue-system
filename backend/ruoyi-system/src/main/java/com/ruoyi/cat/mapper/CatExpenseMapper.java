package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatExpense;

/**
 * 财务支出 数据层
 * 
 * @author ruoyi
 */
public interface CatExpenseMapper
{
    public CatExpense selectCatExpenseById(Long expenseId);

    public List<CatExpense> selectCatExpenseList(CatExpense expenseId);

    public int insertCatExpense(CatExpense expenseId);

    public int updateCatExpense(CatExpense expenseId);

    public int deleteCatExpenseById(Long expenseId);

    public int deleteCatExpenseByIds(Long[] expenseIds);
}
