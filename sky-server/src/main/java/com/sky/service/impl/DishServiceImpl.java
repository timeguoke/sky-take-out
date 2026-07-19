package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味
     *
     * @param dishDTO
     */
    //因为要操作两个表,所以需要开启事务保证准确性
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //插入一条菜品表
        Dish dish = new Dish();
        //1.进行拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        //2.添加到表中
        dishMapper.insert(dish);
        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        //获取口味集合
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //为flavors中的dishId属性赋值
            flavors.forEach(dishFlavor ->
                    dishFlavor.setDishId(dishId));
            //插入n条口味表
            dishFlavorMapper.insertBatch(flavors);

        }

    }
}
