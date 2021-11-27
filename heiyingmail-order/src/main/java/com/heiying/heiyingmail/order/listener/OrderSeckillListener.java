package com.heiying.heiyingmail.order.listener;

import com.heiying.common.to.mq.SeckillOrderTO;
import com.heiying.heiyingmail.order.entity.OrderEntity;
import com.heiying.heiyingmail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;


    @RabbitHandler
    public void listener(SeckillOrderTO seckillOrder, Channel channel, Message message) throws IOException {
        System.out.println("准备创建秒杀订单...");
        try {
            orderService.createSeckillOrder(seckillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
