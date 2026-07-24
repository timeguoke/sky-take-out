package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页操作
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //使用PageHelper进行分页
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);

        List result = page.getResult();
        long total = page.getTotal();

        return new PageResult(total, result);

    }

    /**
     * 菜品的批量删除
     *
     * @param ids
     */
    @Transactional
    public void DeleteByIds(List<Long> ids) {
        //先判断所属菜品是否符合删除要求
        //1.菜品是否是停售状态
        for (Long id : ids) {
            Dish dish = dishMapper.SelectById(id);
            if (dish.getStatus() != 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.套餐关联的菜品能不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品和其对应的口味
        dishMapper.DeleteByIds(ids);
        dishFlavorMapper.DeleteByDishIds(ids);
    }

    /**
     * 根据id查找数据
     *
     * @param id
     * @return
     */
    @Transactional
    public DishVO SelectByIdWithFlavor(Long id) {
        //先查询dish表
        Dish dish = dishMapper.SelectById(id);
        //查询口味表
        List<DishFlavor> dishFlavor = dishFlavorMapper.SelectByDishId(id);

        DishVO dishVO = new DishVO();
        //进行数据拷贝
        BeanUtils.copyProperties(dish, dishVO);

        //将口味数据添加到dishVO中
        dishVO.setFlavors(dishFlavor);

        return dishVO;
    }


    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Transactional
    public void Update(DishDTO dishDTO) {
        //修改菜品要修改两部分一个是dish表另一个是口味表
        //1.修改口菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.Update(dish);

        //修改口味表

        //1.先删除口味

        dishFlavorMapper.DeleteByDishId(dishDTO.getId());

        //2.在添加口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //为flavors中的dishId属性赋值
            flavors.forEach(dishFlavor ->
                    dishFlavor.setDishId(dishDTO.getId()));
            //插入n条口味表
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    public List<Dish> SelectByCategoryId(Long categoryId) {

        //添加菜品中展示的是起售中的菜品
        //两个条件,categoryId和status=1
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.list(dish);
        return dishes;
    }

    /**
     *起售停售设置
     * @param status
     * @param id
     */
    public void StartOrStop(Integer status, Long id) {
        Dish dish =Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.Update(dish);
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.SelectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
