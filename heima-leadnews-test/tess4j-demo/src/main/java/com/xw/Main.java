package com.xw;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            // 本地图片文件
            File file = new File("C:\\Users\\ASUS\\Desktop\\aaa.jpg");
            // 创建Tesseract对象
            Tesseract tesseract = new Tesseract();
            // 设置字体库路径
            tesseract.setDatapath("D:\\download\\");
            // 设置具体字体库
            tesseract.setLanguage("chi_sim");
            // 识别图片文字
            String result = tesseract.doOCR(file);
            result = result.replace("\\r|\\n", "-");
            System.out.println("识别结果：" + result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
