package com.heiying.heiyingmail.order;

import com.heiying.heiyingmail.order.entity.OrderItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class HeiyingmailOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    void sendMessage1(){
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderId(1L);
        orderItemEntity.setSkuName("java");
        rabbitTemplate.convertAndSend("java-exchange","e.news",orderItemEntity);
    }

    @Test
    void sendMessage(){
        rabbitTemplate.convertAndSend("java-exchange","heiying.news","hello java");
    }

    @Test
    void contextLoads() {
        TopicExchange topicExchange = new TopicExchange("java-exchange",true,false);
        amqpAdmin.declareExchange(topicExchange);
        log.info("交换机创建成功{}",topicExchange);
    }

    @Test
    void binding(){
        Binding heiying = new Binding("heiying.news", Binding.DestinationType.QUEUE, "java-exchange", "#.news", null);
        amqpAdmin.declareBinding(heiying);
    }


}
