package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate RedisTemplateredis;

    @GetMapping("status")
    @ApiOperation("用户页面获取页面状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer)RedisTemplateredis.opsForValue().get("Shop_Status");
        log.info("当前营业状态为:{}", status == 1 ? "营业中" : "未营业");
        return Result.success(status);
    }
}
