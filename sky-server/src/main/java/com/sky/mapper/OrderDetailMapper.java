package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单详情表
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单详情表
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> getByOrderId(long orderId);

    /**
     * 查询销量排名top10
     * @param
     * @return
     */
    List<GoodsSalesDTO> getTop10(LocalDateTime begin, LocalDateTime end);
}
