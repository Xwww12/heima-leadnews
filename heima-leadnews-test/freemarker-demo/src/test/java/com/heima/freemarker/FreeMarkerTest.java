package com.heima.freemarker;

import com.heima.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FreeMarkerTest {
    @Resource
    private Configuration configuration;

    @Test
    public void test() throws IOException, TemplateException {
        Template template = configuration.getTemplate("01-basic.ftl");

        template.process(getData(), new FileWriter("d:/list.html"));
    }

    private Map getData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "freemarker");
        map.put("date", new Date());
        Student student = new Student();
        student.setName("zs");
        student.setAge(18);
        map.put("stu", student);
        return map;
    }
}
