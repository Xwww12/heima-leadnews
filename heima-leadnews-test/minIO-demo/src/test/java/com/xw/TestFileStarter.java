package com.xw;

import com.heima.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest(classes = Main.class)
@RunWith(SpringRunner.class)
public class TestFileStarter {
    @Resource
    private FileStorageService fileStorageService;

    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("d:\\list.html");
        String url = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(url);
    }
}
