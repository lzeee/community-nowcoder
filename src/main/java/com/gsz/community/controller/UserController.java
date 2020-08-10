package com.gsz.community.controller;

import com.gsz.community.annotation.LoginRequired;
import com.gsz.community.entity.User;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CommunityUtil;
import com.gsz.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path="/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error", "没有选择图片");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        //生成文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常" + e.getMessage());
        }
        //更新当前用户的头像的路径
        User user = hostHolder.getUser();
        String headerUrl = domain + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //解析文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os =  response.getOutputStream();){
            //响应头像
            response.setContentType("image/"+suffix);

            byte[] buffer = new byte[1024];
            int b=0;
            while((b=fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("响应头像失败" + e.getMessage());
            e.printStackTrace();
        }
    }

    @RequestMapping(path="/changepw", method = RequestMethod.POST)
    public String changePassword(Model model, String oldPassword,  String newPassword, String confirmPassword){
        //判断旧密码是否为空
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPwMsg", "旧密码为空");
            return "/site/setting";
        }
        //判断旧密码是否等于账号的密码
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            model.addAttribute("oldPwMsg", "旧密码不正确");
            return "/site/setting";
        }
        //判断新密码是否为空
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPwMsg", "新密码不能为空");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confPwMsg", "两次密码输入不一致");
            return "/site/setting";
        }
        //上面判断都通过之后，可以修改密码
        userService.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));
        return "redirect:/index";
    }

}