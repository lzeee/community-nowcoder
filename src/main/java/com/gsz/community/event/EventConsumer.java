package com.gsz.community.event;

import com.alibaba.fastjson.JSONObject;
import com.gsz.community.entity.Event;
import com.gsz.community.entity.Message;
import com.gsz.community.service.MessageService;
import com.gsz.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    //消费者需要记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //消息
    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息内容为空");
            return;
        }
        else{
            Event event = JSONObject.parseObject(record.value().toString(), Event.class);
            if(event == null){
                logger.error("消息格式错误");
            }
            else{
                //发送消息，主要是构造message对象
                Message message = new Message();
                message.setFromId(SYSTEM_USERID);
                message.setToId(event.getEntityUserId());
                message.setConversationId(event.getTopic());
                message.setStatus(0);
                message.setCreateTime(new Date());
                //每条信息记录：谁 对什么 干了什么
                Map<String, Object> content = new HashMap<>();
                content.put("userId", event.getUserId());
                content.put("entityType", event.getEntityType());
                content.put("entityId", event.getEntityId());
                if(!event.getData().isEmpty()){
                    //不为空把数据存过来
                    for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                        content.put(entry.getKey(),entry.getValue());
                    }
                }
                message.setContent(JSONObject.toJSONString(content));
                messageService.addMessage(message);
                //把消息存到数据库中
            }
        }
    }

}
