package com.wx.websocket.config;

/**
 * Created by wangxiang on 2022/6/18
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Daniel
 * @Description 客户端与服务端交互
 **/
@Slf4j//日志
@ServerEndpoint(value = "/oneWebSocket")//前端通过此URL和后端交互，建立连接
@Component
public class OneWebSocket {

    //在线人数
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    //成功建立连接时
    @OnOpen
    public void onOpen(Session session) {
        //在线人数加1
        onlineCount.incrementAndGet();
        //将连接信息放入客户端
        log.info("新用户{}加入，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    //关闭连接时
    @OnClose
    public void onClose(Session session) {
        //在线人数减1
        onlineCount.decrementAndGet();
        log.info("用户{}退出，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    //客户端发送过来的消息
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        //给客户端回消息
        this.sendMessage("Hello, " + message, session);
    }

    //发生错误时
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("----------------发生错误----------------");
        error.printStackTrace();
    }

    //回消息
    public void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息，内容：{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("----------------发生错误----------------", e);
        }
    }
}
