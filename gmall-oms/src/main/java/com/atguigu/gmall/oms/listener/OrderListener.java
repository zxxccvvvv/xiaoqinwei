package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = "ORDER_DEAD_QUEUE")
    public void close(String orderToken, Channel channel, Message message){
        try {
            if (StringUtils.isBlank(orderToken)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
            //1.更新订单状态为无效订单
            if (orderMapper.updateStatus(orderToken, 0, 4) == 1){
                //2.更新成功，发送消息给wms，解锁库存
                rabbitTemplate.convertAndSend("order_exchange", "stock.unlock", orderToken);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
