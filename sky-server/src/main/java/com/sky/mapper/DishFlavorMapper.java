package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量插入口味
     * @param flavors
     */

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 删除菜品时删除对应的口味
     * @param dishids
     */
    void DeleteByDishIds(List<Long> dishids);
}
