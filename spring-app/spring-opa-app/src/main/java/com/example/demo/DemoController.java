package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
class DemoController {

    @RequestMapping("/health")
    public String health() {
        return "OK";
    }

    @RequestMapping("/**")
    public String main(HttpServletRequest request) {
        return String.format("Success: User %s is authorized", request.getUserPrincipal().getName());
    }

}
