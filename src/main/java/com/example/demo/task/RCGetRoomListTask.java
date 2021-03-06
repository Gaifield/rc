package com.example.demo.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.MQConstant;
import com.example.demo.po.AVRoomInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component(value=RCGetRoomListTask.taskType)
@Scope("prototype")
public class RCGetRoomListTask extends SimpleTask implements Runnable {
    private static Logger log = LoggerFactory.getLogger(RCGetRoomListTask.class);
    public final static String taskType = "get_roomlist_request";
    public final static String taskResType = "get_roomlist_response";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Override
    @Transactional
    public void run() {
        log.info("execute RCGetRoomListTask at {}", new Date());
        JSONObject jsonObject = JSON.parseObject(msg);
        String client_id = jsonObject.getString("client_id");
        //根据用户ID获得所在会议室集合键：(key: AV_User_Room_[UserID])
        String userRoomKey = MQConstant.REDIS_USER_ROOM_KEY_PREFIX+client_id;
        Set<Object> avRoomInfoSet = RedisUtils.sGet(redisTemplate, userRoomKey);
        List<Map<String,Object>> roominfo_list = new ArrayList<>();
        for(Object obj : avRoomInfoSet){
            AVRoomInfo avRoomInfo = (AVRoomInfo)obj;
            Map<String,Object> room_info = new HashMap<>();
            room_info.put("room_id", avRoomInfo.getRoom_id());
            room_info.put("room_name", avRoomInfo.getRoom_name());
            room_info.put("creator_id", avRoomInfo.getCreator_id());
            room_info.put("create_time", avRoomInfo.getCreate_time());
            roominfo_list.add(room_info);
        }
        JSONArray room_list_array = JSONArray.parseArray(JSONObject.toJSONString(roominfo_list));
        JSONObject responseMsg = new JSONObject();
        responseMsg.put("type",RCCreateRoomTask.taskResType);
        responseMsg.put("room_list",room_list_array);
        String send_routekey = MQConstant.MQ_CLIENT_KEY_PREFIX+client_id;
        log.info("mq send response {}: {}",send_routekey,responseMsg);
        rabbitTemplate.convertAndSend(MQConstant.MQ_EXCHANGE, send_routekey, responseMsg);
    }
}
