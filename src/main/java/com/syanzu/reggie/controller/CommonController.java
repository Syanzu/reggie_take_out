package com.syanzu.reggie.controller;


import com.syanzu.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 进行文件的上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    // 注入basePath的值
    @Value("${reggie.path}")  // 获取yml文件中自定义的reggie的path属性值
    private String basePath;


    /**
     * 客户端文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){  // 参数名字要和前端页面定义的名字保持一致，否则不能接收
        // file是个临时文件，需要转存到指定位置，否则本次请求完成之后临时文件会自动删除
        log.info(file.toString());

        // 获取原始的文件名
        String filename = file.getOriginalFilename();
        // 获取文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成的覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象（以防止图片存储路径不存在导致图片存储失败）
        File dir = new File(basePath);
        // 判断当前目录是否存在
        if(!dir.exists()){
            // 目录不存在：需要创建该目录
            dir.mkdirs();  // 创建目录
        }


        // 调用 transferTo方法，将文件保存在指定位置
        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回文件名给前端
        return R.success(fileName);
    }


    /**
     * 客户端文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    // 返回值是void，直接通过输出流将文件传过去
    public void download(String name, HttpServletResponse response){   // 需要response来向网页响应图片文件

        try {
            // 输入流，用于读取文件的内容（操作File对象）
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流，用于将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            // 设置响应回去的文件格式
            response.setContentType("image/jepg");  // 表示图片文件，固定的表达

            int len;
            byte[] bytes = new byte[1024];   // 数组作为每次读取的容器
            // 每次读取一个字节数组返回，如果字节没有可读的，返回-1
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);  // 读取长度为len
                outputStream.flush();   // 刷新
            }


            // 关闭流
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
