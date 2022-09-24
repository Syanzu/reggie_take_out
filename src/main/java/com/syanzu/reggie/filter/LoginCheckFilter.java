package com.syanzu.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.syanzu.reggie.common.BaseContext;
import com.syanzu.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*过滤器：检查用户是否已经完成登陆
WebFilter：过滤器注解
filterName：过滤器名称，可以随意起，但是和类名一致更好
urlPatterns：需要拦截的路径
/* ：表示所有路径都拦截*/
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
 //过滤器需要实现 Filter接口
public class LoginCheckFilter implements Filter {

    // 路径匹配，支持通配符器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1 获取本次请求的URI
        String uri = request.getRequestURI();

        log.info("拦截到请求：{}", uri);

        // 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login", // 登陆按钮请求的地址
                "/employee/logout", // 退出按钮请求的地址
                "/backend/**", // 静态页面
                "/front/**",  // 静态页面
                "/common/**",   // 不用登陆也可以访问 上传功能的controller方法
                "/user/sendMsg",   // 移动端发送端溪
                "/user/login",   // 移动端登陆
                // 以下是Swagger相关
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };


        // 2 判断本次请求是否需要处理
        boolean check = check(urls, uri);

        // 3 如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理", uri);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1 判断登陆状态，如果已登陆，则直接放行
        if(request.getSession().getAttribute("employee") != null){ // 判断session里面的employee属性是否为空
            log.info("用户已登陆，用户id为：{}", request.getSession().getAttribute("employee"));

            // 获取当前登陆用户的Id
            Long empId = (Long) request.getSession().getAttribute("employee");
            // 将id存储到Thread变量，即ThreadLocal中
            BaseContext.setCurrentId(empId);


            // 为登陆状态，直接放行
            filterChain.doFilter(request, response);
            return;
        }


        // 4-2 判断登陆状态，如果已登陆，则直接放行（移动端用户）
        if(request.getSession().getAttribute("user") != null){ // 判断session里面的user属性是否为空
            log.info("用户已登陆，用户id为：{}", request.getSession().getAttribute("user"));

            // 获取当前登陆用户的Id
            Long userId = (Long) request.getSession().getAttribute("user");
            // 将id存储到Thread变量，即ThreadLocal中
            BaseContext.setCurrentId(userId);


            // 为登陆状态，直接放行
            filterChain.doFilter(request, response);
            return;
        }




        log.info("用户未登陆");
        // 5 如果未登陆则返回登陆结果（通过输出流的方式，向前段响应数据）
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            // 参数一：数组中的元素    参数二：访问的url
            boolean match = PATH_MATCHER.match(url, requestURI); // true则表示匹配上
            if(match){
                return true;
            }
        }
        return false;
    }
}
