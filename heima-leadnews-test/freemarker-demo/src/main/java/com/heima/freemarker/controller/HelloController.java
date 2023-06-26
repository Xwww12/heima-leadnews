package com.heima.freemarker.controller;

import com.heima.freemarker.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class HelloController {
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "freemarker");
        model.addAttribute("date", new Date());
        Student student = new Student();
        student.setName("zs");
        student.setAge(18);
        model.addAttribute("stu", student);
        return "01-basic";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("name", "freemarker");

        ArrayList<Student> stus = new ArrayList<>();
        Student s1 = new Student();
        s1.setName("zs");
        s1.setAge(18);
        s1.setMoney(1000.0f);
        Student s2 = new Student();
        s2.setName("ls");
        s2.setAge(18);
        s2.setMoney(1000.0f);
        stus.add(s1);
        stus.add(s2);

        model.addAttribute("stus", stus);
        return "02-list";
    }
}
