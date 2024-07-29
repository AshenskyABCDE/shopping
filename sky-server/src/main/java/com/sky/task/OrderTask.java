package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单");
        LocalDateTime plus = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orderByStatusAndOrderTime = orderMapper.getOrderByStatusAndOrderTime(Orders.PENDING_PAYMENT, plus);
        if(orderByStatusAndOrderTime == null || orderByStatusAndOrderTime.isEmpty()) {
            return ;
        }
        for(Orders orders : orderByStatusAndOrderTime) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时，自动删掉订单~");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }

    }


    // 处理一直处于派送中的订单
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliverOrder() {
        log.info("定时处理派送中的订单");
        LocalDateTime plus = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderByStatusAndOrderTime = orderMapper.getOrderByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, plus);
        if(orderByStatusAndOrderTime == null || orderByStatusAndOrderTime.isEmpty()) {
            return ;
        }
        for(Orders orders : orderByStatusAndOrderTime) {
            orders.setStatus(Orders.COMPLETED);
            orders.setCancelReason("订单超时，自动删掉订单~");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }
}
