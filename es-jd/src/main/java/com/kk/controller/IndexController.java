package com.kk.controller;

/*
@author kzj
@date 2020/4/14 - 22:50
*/

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "index";
    }

}
