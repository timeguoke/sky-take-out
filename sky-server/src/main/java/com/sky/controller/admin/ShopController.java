package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Key;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作接口")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("店铺状态设置")
    public Result setStatus(@PathVariable Integer status) {
        log.info("店铺操作接口,店铺状态:{}", status == 1 ? "营业中" : "打样中");
        //开始设置状态
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> getStatus() {
        //获取店铺状态
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("店铺操作接口,店铺状态:{}", status == 1 ? "营业中" : "打样中");

        return Result.success(status);
    }

}
