package com.example.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component(value=RCUserExpiredTask.taskType)
@Scope("prototype")
public class RCUserExpiredTask extends SimpleTask implements Runnable{
    private static Logger log = LoggerFactory.getLogger(RCUserExpiredTask.class);
    public final static String taskType = "user_expired_report";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //可以遍历查看用户所属会议室，如果在此会议室，主动发送exit_room消息即可
    @Override
    @Transactional
    public void run() {
        log.info("execute RCUserExpiredTask");
    }
}
