package com.gsz.community.controller;

import com.gsz.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello Spring Boot!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, Integer age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    @RequestMapping(path="/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mv = new ModelAndView();
        mv.addObject("name", "张三");
        mv.addObject("age","30");
        mv.setViewName("/demo/teacher");
        return mv;
    }

    @RequestMapping(path="/school", method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", "120");
        return "/demo/teacher";
    }

    //响应json数据，通常是处理异步请求
    //比如说当前网页不刷新，但是也向服务器发送了请求
    //比如注册的时候检查用户名是否存在
    //将java对象转为json对象，给浏览器的js调用
    @RequestMapping(path="emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp(){
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", "23");
        emp.put("salary", 12392);
        return emp;
    }

}
