package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 添加套餐
     *
     * @param setmealDTO
     * @return
     */
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //添加套餐也要分两部分

        //先添加setmeal表部分
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //新增套餐状态为停售
        setmeal.setStatus(StatusConstant.DISABLE);
        //获取insert sql语句生成的id
        Long setmealId = setmeal.getId();


        //添加套餐菜品表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            //为setmealDishes中的dishId属性赋值
            setmealDishes.forEach(setmealDish ->
                    setmealDish.setDishId(setmealId));
            //插入n条套餐菜品表
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 套餐分页
     *
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult Page(SetmealPageQueryDTO setmealPageQueryDTO) {
        //使用PageHelper进行分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page =setmealMapper.Page(setmealPageQueryDTO);
        List<SetmealVO> result = page.getResult();
        long total = page.getTotal();
        return new PageResult(total,result);
    }
}
