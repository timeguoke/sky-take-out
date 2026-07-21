package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查找对应的套餐
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐对应的菜品
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐菜品
     * @param SetmealIds
     */
    void DeleteBySetmealIds(List<Long> SetmealIds);

    /**
     * 根据套餐id查询套餐菜品
     * @param setmealId
     */
    @Select("select * from setmeal_dish where setmeal_id =#{setmealId}")
    List<SetmealDish> SelectBySetmealId(Long setmealId);

    /**
     * 单个删除(修改套餐)
     * @param SetmealId
     */
    @Delete("delete from setmeal_dish where Setmeal_id =#{SetmealId}")
    void DeleteBySetmealId(Long SetmealId);
}
