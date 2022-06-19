package com.wx.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Daniel
 * @Description 客户端给其他客户端群发消息
 **/
@Slf4j//日志
@ServerEndpoint(value = "/oneToMany")//前端通过此URL和后端交互，建立连接
@Component//实例化pojo
public class OneToMany {

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

    //接收到消息时
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        this.sendMessage(message, session);
    }

    //发生错误时
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("----------------发生错误----------------");
        error.printStackTrace();
    }

    //群发消息
    public void sendMessage(String message, Session session) {
        //遍历客户端在线人数
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
            //拿到消息
            String sessionId = session.getId();
            Session toSession = sessionEntry.getValue();
            //排除自己
            if (!sessionId.equals(toSession.getId())) {
                log.info("服务端转发客户端[{}]的群发消息给客户端[{}]，内容：{}", sessionId, toSession.getId(), message);
                toSession.getAsyncRemote().sendText(message);
            }
        }
    }
}
