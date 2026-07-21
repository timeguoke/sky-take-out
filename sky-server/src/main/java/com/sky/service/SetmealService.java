package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService {
    /**
     * 添加套餐
     * @param setmealDTO
     * @return
     */
    void save(SetmealDTO setmealDTO);
    /**
     * 套餐分页
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult Page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id批量删除
     * @param ids
     */
    void DeleteByIds(List<Long> ids);

    /**
     * 根据id查询套餐(回显套餐数据)
     * @param id
     * @return
     */
    SetmealVO SelectByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void Update(SetmealDTO setmealDTO);
    /**
     * 起售停售设置
     * @param status
     * @param id
     */
    void StartOrStop(Integer status, Long id);
}
