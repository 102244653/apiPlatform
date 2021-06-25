package com.vantop.apitest.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
public class ViewController {

    @GetMapping(value = {"", "/login"})
    public String main(Model model){
        return "login";
    }

    @GetMapping(value = "/logout")
    public String logout(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        return "login";
    }

    @GetMapping(value = "/{url}")
    public String redirect(@PathVariable("url") String url, Model model) {
        return url;
    }

}
