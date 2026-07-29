package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;


    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //获取起始日期到结束日期之间所有日期（包含首尾日期）
        List<LocalDate> dateList = getDateList(begin, end);

        List<Double> turnoverList = new ArrayList<>();
        //整理营业额数据
        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据，营业额是指：状态为"已完成"的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//每日的00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//每日的23:59:59.999999999

            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnOver = orderMapper.sumByMap(map);
            turnOver = turnOver == null ? 0.0 : turnOver;
            turnoverList.add(turnOver);

        }


        //将数据添加到返回结果中去
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();

        return turnoverReportVO;

    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //获取起始日期到结束日期之间所有日期（包含首尾日期）
        List<LocalDate> dateList = getDateList(begin, end);
        //用户总量查询
        List<Integer> totalUserList = new ArrayList<>();
        //每日新增用户
        List<Integer> newUserList = new ArrayList<>();

        //整理营业额数据
        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据，营业额是指：状态为"已完成"的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//每日的00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//每日的23:59:59.999999999

            //用户总量 select count(*) from user where  creat_time < ?
            //新增用户 select count(*) from user where  creat_time < ? and order_time < ?
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }


        //将数据添加到返回结果中去
        UserReportVO userReportVO = UserReportVO.builder()
                .totalUserList(StringUtils.join(totalUserList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();


        return userReportVO;
    }

    /**
     * 获取起始日期到结束日期之间所有日期（包含首尾日期）
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 有序日期集合
     */
    private static List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //将时间数据按顺序包装到集合中去
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算,计算到指定日期的最后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //获取起始日期到结束日期之间所有日期（包含首尾日期）
        List<LocalDate> dateList = getDateList(begin, end);

        //每日订单数
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        //整理营业额数据
        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据，营业额是指：状态为"已完成"的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//每日的00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//每日的23:59:59.999999999

            //每日订单数 select count(*) from order where  creat_time < ? and order_time < ?
            //新增用户 select count(*) from order where  creat_time < ? and order_time < ? and status =?
            Map map = new HashMap();
            map.put("end", endTime);
            map.put("begin", beginTime);
            //每日订单数
            Integer orderCount = orderMapper.countByMap(map);

            map.put("status", Orders.COMPLETED);
            //每日有效订单数
            Integer validOrderCount = orderMapper.countByMap(map);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);

        }
//        // ========== 2. 单独查询【整个区间】汇总数据 ==========
//        LocalDateTime totalBegin = LocalDateTime.of(begin, LocalTime.MIN);//起始日期
//        LocalDateTime totalEnd = LocalDateTime.of(end, LocalTime.MAX);//结束日期
//
//        //订单总数 select count(*) from order where and order_time < ?
//        //有效订单总数 select count(*) from order where   and order_time < ? and  status =?
//
//        Map  totalMap = new HashMap<>();
//        totalMap.put("begin", totalBegin);
//        totalMap.put("end", totalEnd);
//        //区间总订单数
//        Integer totalOrderCount = orderMapper.countByMap(totalMap);
//
//        //区间有效订单数
//        totalMap.put("status", Orders.COMPLETED);
//        Integer totalValidOrderCount = orderMapper.countByMap(totalMap);
        //循环拿到每一天orderCountList、validOrderCountList
        Integer totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);

        //3.计算完成率
        double orderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();


    }

    /**
     * 查询销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {

//        //商品名称列表，以逗号分隔，例如：鱼香肉丝,宫保鸡丁,水煮鱼
//        private String nameList;
//
//        //销量列表，以逗号分隔，例如：260,215,200
//        private String numberList;
        LocalDateTime totalBegin = LocalDateTime.of(begin, LocalTime.MIN);//起始日期
        LocalDateTime totalEnd = LocalDateTime.of(end, LocalTime.MAX);//结束日期

        List<GoodsSalesDTO> goodsSalesDTOList = orderDetailMapper.getTop10(totalBegin, totalEnd);

        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        //封装数据
        return SalesTop10ReportVO.builder()
                .numberList(numberList)
                .nameList(nameList)
                .build();

    }
}
