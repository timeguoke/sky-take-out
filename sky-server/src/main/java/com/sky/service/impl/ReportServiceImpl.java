package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    WorkspaceService workspaceService;


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

    /**
     * 导出Excel表格接
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //获取时间数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        //整理时间数据
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //先查询表格上的概览数据
        BusinessDataVO businessDataVo = workspaceService.getBusinessData(beginTime, endTime);
        //通过POI将数据写到Excel表格中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //基于模版创建一个新的Excel文件
        XSSFWorkbook excel = null;
        try {
            excel = new XSSFWorkbook(in);
            //填充数据---时间数据
            //创建标签页
            XSSFSheet sheet1 = excel.getSheet("sheet1");
            //向表格(2,2)添加数据
            sheet1.getRow(1).getCell(1).setCellValue("时间" + begin + "至" + end);

            //开始添加概览数据
            //获取第四行
            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessDataVo.getTurnover());
            row.getCell(4).setCellValue(businessDataVo.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVo.getNewUsers());
            //获取第五行
            XSSFRow row1 = sheet1.getRow(4);
            row1.getCell(2).setCellValue(businessDataVo.getValidOrderCount());
            row1.getCell(4).setCellValue(businessDataVo.getUnitPrice());

            //开始查询明细数据
            for (int i = 1; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                LocalDateTime dateBegin = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dateEnd = LocalDateTime.of(date, LocalTime.MAX);
                BusinessDataVO businessData = workspaceService.getBusinessData(dateBegin, dateEnd);

                //开始添加明细数据
                //获取所在行
                XSSFRow row2 = sheet1.getRow(7+i);
                row2.getCell(1).setCellValue(String.valueOf(date));
                row2.getCell(2).setCellValue(businessData.getTurnover());
                row2.getCell(3).setCellValue(businessData.getValidOrderCount());
                row2.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row2.getCell(5).setCellValue(businessData.getUnitPrice());
                row2.getCell(6).setCellValue(businessData.getNewUsers());

            }
            //通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
