package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @Transactional
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //先判断加入该购物车的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断商品是否已存在
        if (list != null && list.size() > 0) {
            //若存在了将商品个数+1
            //将商品取出来
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);

            shoppingCartMapper.updateNumberById(shoppingCart1);

        } else {
            //若不存在将商品添加到购物车中
            //先判断添加的商品是套餐还是菜品
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (setmealId != null) {
                //查询套餐信息
                Setmeal setmeal = setmealMapper.SelectById(setmealId);
                //添加套餐
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            } else {
                Long dishId = shoppingCartDTO.getDishId();
                //查询菜品信息
                Dish dish = dishMapper.SelectById(dishId);
                //添加菜品
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /**
     * 查看购物车
     *
     * @return
     */
    public List<ShoppingCart> showShppingCart() {
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }

    /**
     * 删除购物车的一个商品
     *
     * @param shoppingCartDTO
     */
    @Transactional
    public void delete(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询商品数量
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();

            //根据number的大小分类进行处理
            if (number == 1) {
                shoppingCartMapper.deleteById(cart.getId());

            } else {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }
}
