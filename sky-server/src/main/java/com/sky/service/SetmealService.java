package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

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
}
