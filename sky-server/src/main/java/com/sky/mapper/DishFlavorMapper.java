package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量插入口味
     *
     * @param flavors
     */

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 删除菜品时删除对应的口味
     * 批量删除
     *
     * @param dishids
     */
    void DeleteByDishIds(List<Long> dishids);

    /**
     * 单个删除
     * @param dishId
     */

    @Delete("DELETE FROM dish_flavor where  dish_id =#{dishId}")
    void DeleteByDishId(Long dishId);

    /**
     * 根据菜品id查询口味表
     *
     * @param dish_id
     * @return
     */
    @Select("select * from dish_flavor where dish_id =#{dish_id}")
    List<DishFlavor> SelectByDishId(Long dish_id);
}
