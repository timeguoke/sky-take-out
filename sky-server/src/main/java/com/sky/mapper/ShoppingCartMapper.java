package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 根据userId,dishId/setmealId查找商品
     *
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     *  修改商品数量
     *
     * @param shoppingCart
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入商品
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name,user_id,dish_id,setmeal_id,dish_flavor,number,amount,image,create_time)" +
            " values(#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     *
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id =#{userId} ")
    void clean(Long userId);

    /**
     * 根据id删除商品
     * @param id
     */
    @Delete("delete from shopping_cart where  id =#{id} ")
    void deleteById(long id);
}
