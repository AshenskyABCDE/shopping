package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate RedisTemplateredis;

    @PutMapping("/{status}")
    @ApiOperation("设置点铺的营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("当前营业状态为:{}", status == 1 ? "营业中" : "未营业");
        RedisTemplateredis.opsForValue().set("Shop_Status",status);
        return Result.success();
    }

    @GetMapping("status")
    @ApiOperation("管理页面获取页面状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer)RedisTemplateredis.opsForValue().get("Shop_Status");
        log.info("当前营业状态为:{}", status == 1 ? "营业中" : "未营业");
        return Result.success(status);
    }
}
