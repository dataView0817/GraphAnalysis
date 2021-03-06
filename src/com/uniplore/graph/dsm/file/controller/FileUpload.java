package com.uniplore.graph.dsm.file.controller;

import com.alibaba.fastjson.JSON;
import com.uniplore.graph.util.fileoperation.FileOperation;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;



@Controller
@RequestMapping(value = "/file/Upload")
public class FileUpload {

  /**  
  * 功能描述:实现文件上传保存操作.
  * @param file   上传的文件参数
  * @throws Exception  抛出异常
  */
  @RequestMapping(value = "/Text",method = RequestMethod.POST)
  public void fileUploadText(@RequestParam(value = "file",required = false) 
      MultipartFile file)throws Exception  {
    System.out.println("从客户端接收到文件" + file.getOriginalFilename());
    
  }

  /**
   * 功能描述：将JSON文件上传之后，跳转到此链接，该部分完成将JSON文件保存的过程，保存之后将文件名、上传时间、id保存到数据库中.
   * 接着将该唯一的id返回给客户端，使用apache的fileUpload组件实现文件的上传保存过程，还需要当上传文件同名时，避免覆盖
   * @param file 文件参数
   * @return  返回值为集合类型，加上@ResponseBody注解之后，将集合类型转换成JSON格式返回
   * @throws Exception  抛出异常
  */
  @RequestMapping(value = "/Json",method = RequestMethod.POST)
  public @ResponseBody Map<String, Object> fileUploadJson(
      @RequestParam(value = "file",required = true)  MultipartFile file,
            HttpServletRequest request)throws Exception {
    //如果用的是tomcat服务器，则文件会上传到\\%TOMCAT_HOME%\\webapps\\graphanalysis\\WEB-INF\\upload\\文件夹中
    String realPath = request.getSession().getServletContext()
        .getRealPath("/WEB-INF/upload"); //路径中的/代表tomcat中当前项目路径
    //避免文件被覆盖，也就是重名问题
    String id = UUID.randomUUID().toString();  
    String fileOriginalName = file.getOriginalFilename();   //得到上传的原始文件名
    System.out.println(fileOriginalName);
    String saveFileName = id + "#" + fileOriginalName;  //保存到服务器的文件名由两部分组成：生成的唯一id和原始的文件名
    //这里不必处理IO流关闭的问题，因为FileUtils.copyInputStreamToFile()方法内部会自动把用到的IO流关掉，我是看它的源码才知道的  
    FileUtils.copyInputStreamToFile(file.getInputStream(), new File(realPath, saveFileName)); 

    //将文件的唯一id(避免文件重名)和原始文件名以JSON形式返回给服务器端
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id", id);   //将每个文件的唯一id编号传递给客户端
    map.put("fileName", fileOriginalName);   //将原始的文件名传递给客户端
    return map;
  }

  /**
   * 功能描述: 实现数据集的预览.
   * @param request 请求参数
   * @return  返回值 
   * @throws Exception 抛出异常
   */
  @RequestMapping(value = "/FindData",method = RequestMethod.POST)
  public @ResponseBody String viewData(HttpServletRequest request)throws Exception {
    String id = request.getParameter("id");
    String fileName = request.getParameter("fileName");
    
    String realPath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");
    //System.out.println("拼接的路径为:"+realPath+"\\"+id+"#"+fileName);  //查看拼接的路径是否正确
    File file = new File(realPath + "\\" + id + "#" + fileName);    //获取到指定路径下的文件
    //System.out.println("读取到的文件大小为:"+file.length());

    FileInputStream fileInputStream = new FileInputStream(file); //建立数据通道
    String jsonContent = FileOperation.readFileContent(fileInputStream);
    System.out.println("--原始JSON字符串--:");
    System.out.println(jsonContent);
    
    //使用fastjson将字符串构造成标准的JSON对象对应的字符串
    Object formatJson = JSON.parse(jsonContent);   //将字符串解析为一个JSON对象
    String outputJson = formatJson.toString();   //将得到的JSON对象格式化为标准的字符串，无需做美化的操作
    System.out.println("--格式化JSON字符串--");
    System.out.println(outputJson);
    return outputJson;   //返回标准的JSON字符串
  }
}
