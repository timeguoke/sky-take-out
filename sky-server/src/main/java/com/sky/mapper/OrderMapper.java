package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入一条订单
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页条件查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @return
     */
    @Select("select * from orders where id =#{id}")
    Orders getById(Long id);

    /**
     * 各个订单状态数量统计
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 超时订单处理
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status =#{status} and order_time< #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 查询每日营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 订单统计
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
