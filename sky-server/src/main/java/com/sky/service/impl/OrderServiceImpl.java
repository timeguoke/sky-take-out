package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //查看传来的订单信息问题
        //1.查看地址是否存在

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //检查用户的收货地址是否超出配送范围
        // checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //查看购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartlist = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartlist == null || shoppingCartlist.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        //插入n条订单详情表
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartlist) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            //给OrderId属性赋值
            orderDetail.setOrderId(orders.getId());
            //加入集合
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        //清空购物车数据
        shoppingCartMapper.clean(userId);

        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;

    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        //自己new一个JSONObject
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket想浏览器推送消息 type orderId content

        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);


    }

    /**
     *
     * 历史订单查询
     *
     * @param pageNum  页面
     * @param pageSize 页面记录数
     * @param status   订单状态   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    public PageResult historyOrders(int pageNum, int pageSize, Integer status) {
        //查询历史订单分为两个部分1.订单表  2.订单详情表

        //1.订单表查询
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        //上面使用了PageHelper进行分页就不用将pageNum, pageSize赋值给DTO了
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> result = page.getResult();
        long total = page.getTotal();
        //2.订单详情表查询
        List<OrderVO> list = new ArrayList<>();
        if (page != null && total > 0) {
            for (Orders orders : result) {

                Long ordersId = orders.getId();
                List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(ordersId);
                //将订单和订单详情都给存到orderVO中传给前端
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetail);

                list.add(orderVO);
            }
        }
        return new PageResult(total, list);
    }

    /**
     * 查询订单详情信息
     *
     * @param orderId
     * @return
     */
    @Transactional
    public OrderVO getOrderDetail(Long orderId) {
        //订单详情信息所需要的有三个表的信息
        // orders 订单表、address_book 地址表、user 用户表，

        Orders orders = orderMapper.getById(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderDetailList(orderDetails);
        BeanUtils.copyProperties(orders, orderVO);
        return orderVO;

    }


    /**
     * 取消订单
     *
     * @param id
     */
    public void cancelOrder(Long id) throws Exception {
        //先查询订单是否存在

        Orders orderDB = orderMapper.getById(id);

        if (orderDB == null) {
            //订单不存在
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单存在,根据订单的不同状态,进行不同的处理

        //1.待支付和待配送的可直接取消
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //3已接单 4派送中 5已完成 6已取消
        if (orderDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //用于修改订单状态
        Orders orders = new Orders();
        orders.setId(orderDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            /*weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);


    }


    /**
     * 再来一单
     *
     * @param id
     */
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Transactional
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页操作
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> result = page.getResult();
        long total = page.getTotal();

        //为返回信息进行赋值
        //返回信息分为Order表信息和OrderDetail表
        List<OrderVO> orderVOS = new ArrayList<>();
        //先查询Order表信息
        for (Orders orders : result) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            //在查询OrderDetail表
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            //前端需要的是将详情信息转化为字符串
            // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
            List<String> orderDishList = orderDetailList.stream().map(s -> {
                String orderDetail = s.getName() + "*" + s.getNumber();
                return orderDetail;
            }).collect(Collectors.toList());

            orderVO.setOrderDishes(orderDishList.toString());
            orderVOS.add(orderVO);
        }
        return new PageResult(total, orderVOS);


    }

    /**
     * 各个订单状态数量统计
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     *
     * @return
     */
    public OrderStatisticsVO statistics() {

        OrderStatisticsVO orderStatisticsVO = orderMapper.statistics();
        return orderStatisticsVO;

    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO details(long id) {
        //详情表需要信息需要order表和orderDetail表
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;

    }

    /**
     * 接单(就是修改订单状态)
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);

    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    //    业务规则：
    //
    //- 商家拒单其实就是将订单状态修改为“已取消”
    //- 只有订单处于“待接单”状态时可以执行拒单操作
    //- 商家拒单时需要指定拒单原因
    //- 商家拒单时，如果用户已经完成了支付，需要为用户退款
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //先整理拒单的前提条件
        //查看订单状态
        Orders orderDB = orderMapper.getById(ordersRejectionDTO.getId());
        //查看订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //- 只有订单处于“待接单”状态时可以执行拒单操作
        if (orderDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //进行拒单
        //查看支付状态
        Integer payStatus = orderDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付,进行退款操作
            //调用微信支付退款接口
            /*weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orderDB.setPayStatus(Orders.REFUND);
        }
        //拒单
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);


    }

    /**
     * 取消订单(管理端取消订单)(商家)
     *
     * @param ordersCancelDTO
     */
    //业务规则：
    //
    //- 取消订单其实就是将订单状态修改为“已取消”
    //- 商家取消订单时需要指定取消原因
    //- 商家取消订单时，如果用户已经完成了支付，需要为用户退款
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //先整理取消的前提条件
        //查看订单状态
        Orders orderDB = orderMapper.getById(ordersCancelDTO.getId());
        //查看订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查看支付状态
        Integer payStatus = orderDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付,进行退款操作
            //调用微信支付退款接口
          /*  weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orderDB.setPayStatus(Orders.REFUND);
        }
        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    //业务规则：
    //
    //- 派送订单其实就是将订单状态修改为“派送中”
    //- 只有状态为“待派送”的订单可以执行派送订单操作
    public void delivery(Long id) {
        //查询订单状态
        Orders orderDB = orderMapper.getById(id);
        if (orderDB.getStatus() != Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //进行派送
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        //查询订单状态
        Orders orderDB = orderMapper.getById(id);
        if (!orderDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //进行完成
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address", address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

    /**
     * 用户催单
     *
     * @param id
     */
    public void reminder(Long id) {
        //查看订单状态
        Orders orderDB = orderMapper.getById(id);
        //查看订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号" + orderDB.getNumber());

        String json = JSON.toJSONString(map);
        //通过websocket想浏览器发送催单消息
        webSocketServer.sendToAllClient(json);
    }

}
