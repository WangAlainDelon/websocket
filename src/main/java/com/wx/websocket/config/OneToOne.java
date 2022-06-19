package com.wx.websocket.config;

/**
 * Created by wangxiang on 2022/6/18
 */
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Daniel
 * @Description 客户端与客户端之前相互发送消息
 **/
@Slf4j//日志
@ServerEndpoint(value = "/oneToOne")//前端通过此URL和后端交互，建立连接
@Component//实例化pojo
public class OneToOne {

    //在线人数
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    //在线客户端
    private static Map<String, Session> clients = new ConcurrentHashMap<>();

    //成功建立连接时
    @OnOpen
    public void onOpen(Session session) {
        //在线人数加1
        onlineCount.incrementAndGet();
        //将连接信息放入客户端
        clients.put(session.getId(), session);
        log.info("新用户{}加入，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    //关闭连接时
    @OnClose
    public void onClose(Session session) {
        //在线人数减1
        onlineCount.decrementAndGet();
        clients.remove(session.getId());
        log.info("用户{}退出，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    //客户端发送过来的消息
    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = session.getId();
        String[] messages = message.split(" ");
        //将客户端的消息转换为json字符串
        String json = "{\"message\":\"" + messages[0] + "\", \"userId\":" + messages[1] + "}";
        log.info("服务端收到客户端[{}]的消息[{}]", sessionId, json);
        try {
            MyMessage myMessage = JSON.parseObject(json, MyMessage.class);
            if (myMessage != null) {
                Session toSession = clients.get(myMessage.getUserId());
                if (toSession != null) {
                    this.sendMessage(myMessage.getMessage(), toSession, sessionId);
                }
            }
        } catch (Exception e) {
            log.error("----------------发生错误----------------", e);
        }
    }

    //发生错误时
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("----------------发生错误----------------");
        error.printStackTrace();
    }

    //服务端发送消息给客户端
    public void sendMessage(String message, Session toSession, String sessionId) {
        try {
            log.info("服务端转发客户端[{}]的消息给客户端[{}]，内容：{}", sessionId, toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("----------------发生错误----------------", e);
        }
    }

}
