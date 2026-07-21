package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
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
                    setmealDish.setSetmealId(setmealId));

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
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.Page(setmealPageQueryDTO);
        List<SetmealVO> result = page.getResult();
        long total = page.getTotal();
        return new PageResult(total, result);
    }

    /**
     * 根据id批量删除套餐
     *
     * @param ids
     */
    @Transactional
    public void DeleteByIds(List<Long> ids) {
        //删除套餐需要时停售状态
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.SelectById(id);
            if (setmeal.getStatus() != StatusConstant.DISABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //先删除setmeal表
        setmealMapper.DeleteByIds(ids);
        //在删除套餐菜品表
        setmealDishMapper.DeleteBySetmealIds(ids);

    }

    /**
     * 根据id查询套餐(回显套餐数据)
     *
     * @param id
     * @return
     */
    public SetmealVO SelectByIdWithDish(Long id) {
        //先查询套餐表
        Setmeal setmeal = setmealMapper.SelectById(id);

        //查询套餐菜品表
        List<SetmealDish> setmealDish = setmealDishMapper.SelectBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();

        //拷贝套餐表数据
        BeanUtils.copyProperties(setmeal, setmealVO);

        setmealVO.setSetmealDishes(setmealDish);
        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public void Update(SetmealDTO setmealDTO) {
        //修改套餐要修改两部分一个是套餐表另一个是菜品表
        //1.修改口菜品表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.Update(setmeal);

        //修改口味表

        //1.先删除口味
        //单个删除
        setmealDishMapper.DeleteBySetmealId(setmealDTO.getId());

        //2.在添加口味
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            //为setmealDishes中的setmealId属性赋值
            setmealDishes.forEach(setmealDish ->
                    setmealDish.setSetmealId(setmealDTO.getId()));
            //插入n条口味表
            setmealDishMapper.insertBatch(setmealDishes);

        }


    }

    /**
     * 起售停售设置
     *
     * @param status
     * @param id
     */
    public void StartOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();

        setmealMapper.Update(setmeal);
    }


}
