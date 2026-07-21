package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {
    /**
     * 新增菜品和口味表
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页操作
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void DeleteByIds(List<Long> ids);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void Update(DishDTO dishDTO);

    /**
     * 根据id查找菜品数据(回显数据)
     * @param id
     * @return
     */
    DishVO SelectByIdWithFlavor(Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> SelectByCategoryId(Long categoryId);

    /**
     * 起售停售设置
     * @param status
     * @param id
     */
    void StartOrStop(Integer status, Long id);
}

