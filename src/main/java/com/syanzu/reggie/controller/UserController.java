package com.syanzu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.entity.User;
import com.syanzu.reggie.service.UserService;
import com.syanzu.reggie.utils.SMSUtils;
import com.syanzu.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        // 获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            // 生成四位的手机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);

            // 调用阿里云提供的短息服务API发送短信
            //SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            // 需要将生成的验证码保存到session
            //session.setAttribute(phone, code);  // 手机号是key，值是验证码code

            // 将生成的验证码保存到Redis中,并且设置有效期为5分钟
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(phone, code, 5, TimeUnit.MINUTES);


            return R.success("手机验证码发送成功！");
        }

        return R.error("短信发送失败！");
    }


    /**
     * 移动端用户登陆功能
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        // 获取手机号
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从session中获取保存的验证码
        // Object codeInSession = session.getAttribute(phone);

        // 从redis中获取验证码
        ValueOperations operations = redisTemplate.opsForValue();
        Object codeInSession = operations.get(phone);


        // 进行验证码比对（页面提交的验证码和session中保存的验证码进行比对）
        if(codeInSession != null && codeInSession.equals(code)){
            // 如果能够比对成功，说明登陆成功
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);

            User user = service.getOne(lqw);
            if (user == null){
                // 判断当前手机号是否为新用户，如果是新用户，则自动完成注册
                user = new User();
                user.setPhone(phone);
                service.save(user);
            }

            // 在session中存储一份id，防止过滤器校验失败
            session.setAttribute("user", user.getId());   // key是user，把用户的id存进去

            // 如果用户登陆成功，则可以删除redis中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }


        return R.error("登陆失败！");
    }
}
