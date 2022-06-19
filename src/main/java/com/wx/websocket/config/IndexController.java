package com.wx.websocket.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by wangxiang on 2022/6/18
 */
@Controller
public class IndexController {

    @GetMapping
    public String test() {
        return "/index";
    }
}
