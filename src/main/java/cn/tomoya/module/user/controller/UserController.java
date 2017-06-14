package cn.tomoya.module.user.controller;

import cn.tomoya.config.base.BaseController;
import cn.tomoya.module.user.entity.User;
import cn.tomoya.module.user.service.UserService;
import cn.tomoya.util.FileUploadEnum;
import cn.tomoya.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Created by tomoya.
 * Copyright (c) 2016, All Rights Reserved.
 * http://tomoya.cn
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

  @Autowired
  private UserService userService;
  @Autowired
  private FileUtil fileUtil;

  /**
   * 个人资料
   *
   * @param username
   * @param model
   * @return
   */
  @GetMapping("/{username}")
  public String profile(@PathVariable String username, Model model) {
    model.addAttribute("username", username);
    return render("/front/user/info");
  }

  /**
   * 用户发布的所有话题
   *
   * @param username
   * @return
   */
  @GetMapping("/{username}/topics")
  public String topics(@PathVariable String username, Integer p, Model model) {
    model.addAttribute("username", username);
    model.addAttribute("p", p);
    return render("/front/user/topics");
  }

  /**
   * 用户发布的所有回复
   *
   * @param username
   * @return
   */
  @GetMapping("/{username}/replies")
  public String replies(@PathVariable String username, Integer p, Model model) {
    model.addAttribute("username", username);
    model.addAttribute("p", p);
    return render("/front/user/replies");
  }

  /**
   * 用户收藏的所有话题
   *
   * @param username
   * @return
   */
  @GetMapping("/{username}/collects")
  public String collects(@PathVariable String username, Integer p, Model model) {
    model.addAttribute("username", username);
    model.addAttribute("p", p);
    return render("/front/user/collects");
  }

  /**
   * 进入用户个人设置页面
   *
   * @param model
   * @return
   */
  @GetMapping("/setting")
  public String setting(Model model) {
    model.addAttribute("user", getUser());
    return render("/front/user/setting");
  }

  /**
   * 更新用户的个人设置
   *
   * @param email
   * @param url
   * @param signature
   * @param response
   * @return
   */
  @PostMapping("/setting")
  public String updateUserInfo(String email, String url, String signature, @RequestParam("avatar") MultipartFile avatar,
                               HttpServletResponse response) throws Exception {
    User user = getUser();
    if (user.isBlock())
      throw new Exception("你的帐户已经被禁用，不能进行此项操作");
    user.setEmail(email);
    user.setSignature(signature);
    user.setUrl(url);
    String requestUrl = fileUtil.uploadFile(avatar, FileUploadEnum.AVATAR, getUsername());
    if (!StringUtils.isEmpty(requestUrl)) {
      user.setAvatar(requestUrl);
    }
    userService.save(user);
    return redirect(response, "/user/" + user.getUsername());
  }

  /**
   * 修改密码
   *
   * @param oldPassword
   * @param newPassword
   * @param model
   * @return
   */
  @PostMapping("/changePassword")
  public String changePassword(String oldPassword, String newPassword, Model model) throws Exception {
    User user = getUser();
    if (user.isBlock())
      throw new Exception("你的帐户已经被禁用，不能进行此项操作");
    if (new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
      user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
      userService.save(user);
      model.addAttribute("changePasswordErrorMsg", "修改成功，请重新登录");
    } else {
      model.addAttribute("changePasswordErrorMsg", "旧密码不正确");
    }
    model.addAttribute("user", getUser());
    return render("/front/user/setting");
  }

  /**
   * 刷新token
   *
   * @param response
   * @return
   */
  @GetMapping("/refreshToken")
  public String refreshToken(HttpServletResponse response) {
    User user = getUser();
    user.setToken(UUID.randomUUID().toString());
    userService.save(user);
    return redirect(response, "/user/setting");
  }

}
