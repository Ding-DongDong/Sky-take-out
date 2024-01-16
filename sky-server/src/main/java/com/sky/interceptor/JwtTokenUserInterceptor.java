package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取当前线程id
        System.out.println("当前线程id"+Thread.currentThread().getId());
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            //这个方法是自定义的 JWT 解析方法，接受两个参数，第一个是用于解密的密钥
            //第二个是待解析的 JWT 令牌 (token)。该方法返回一个 Claims 对象，其中包含了 JWT 中的声明信息。
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            //这一行代码从 Claims 对象中获取用户ID的声明，并将其转换成Long类型。在JWT的声明中，通常会包含一些声明（比如过期时间、签发者等）
            // 同时也会包含自定义的声明。在这里，通过 JwtClaimsConstant.USER_ID 来获取用户ID的声明。
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：", userId);
            //在拦截器中存入
            //这行代码的作用是将用户ID存入线程上下文。线程上下文是一个与线程关联的数据存储区域，可以在整个线程的生命周期内共享数据。
            //在这个具体的场景中，当用户的 JWT 令牌验证通过后，将用户的ID存入线程上下文，以便在后续的业务逻辑中能够方便地获取当前用户的ID，
            //而不必在每个方法参数中传递用户ID或者从其他地方再次获取。这样做的好处是简化了代码，提高了代码的可读性和可维护性。
            //举例来说，如果有其他地方需要使用当前用户的ID，可以通过 BaseContext.getCurrentId() 获取，
            //而不必传递用户ID的参数。这在涉及多个方法、类之间需要传递用户ID的情况下，可以减少重复代码，提高开发效率。
            BaseContext.setCurrentId(userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
