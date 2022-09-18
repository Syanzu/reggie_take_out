package com.syanzu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syanzu.reggie.common.R;
import com.syanzu.reggie.entity.Employee;
import com.syanzu.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService service;


    /**
     * 员工登陆
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,
                             @RequestBody Employee employee){ // 与前段的json属性一致才能封装成Employee对象
        // 1 将页面提交的密码进行MD5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2 根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // eq：等值查询
        // 参数一：类中的属性/Database中的字段， 参数二：网页端获取的值
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        // 调用service进行数据库的select查询
        // 将查询结果封装成Employee对象
        Employee emp = service.getOne(queryWrapper);// 数据库中username字段是unique唯一的，所以用getOne

        // 3 如果没有查询到则返回登陆失败结果
        if(emp == null){
            // 将返回结果封装成R对象
            // 调用R类的静态方法直接封装数据
            return R.error("登陆失败");
        }

        // 4 进行密码的比对，如果不一致则返回登陆失败结果
        // getPassword获取的结果为数据库端的密码
        // 参数password为网页端传回的密码，已经进行过MD5加密
        // 前面加 "！"表示"不等于"，即密码比对不成功
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }

        // 5 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        // 因为是数值，直接用 == 来比对
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        // 6 登陆成功，将员工的id存入Session并返回成功结果
        // 参数一：Session的属性（key），参数二：数据库查询出来的emp的id（values）
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    // R的类型为String，只需要返回字符串
    public R<String> logout(HttpServletRequest request){ // 并不需要接收参数，前端也没有提交参数
        // 清理Session中保存的当前登陆员工的ID
        // 属性值：登陆时存入时候定义的名字
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }


    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){

        log.info("新增员工，员工信息：{}", employee.toString());

        // 设置初始密码123456，但是要进行MD5加密处理
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

        /*
            以下这些：更新创建人，创建时间的操作，交给MetaObjectHandler来处理
         */
        /*// 设置 CreateTime（创建时间）和 UpdateTime（更新时间）
        // LocalDateTime.now() ==> 获取当前系统时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置 CreateUser（创建人），即当前登陆的用户ID
        // 通过request获取session，再提取出ID
        Long empID = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empID);
        // 设置 UpdateUser（最后的更新人），也是当前登陆的用户ID
        employee.setUpdateUser(empID);*/


        // 将 Employee对象存进数据库
        service.save(employee);


        return R.success("新增员工成功");
    }


    /**
     * 员工信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    // Page是MybatisPlus封装的类，里面有Page分页相关的属性
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 1 构造 分页构造器：构造分页查询条件
        Page pageInfo = new Page(page, pageSize);

        // 2 构造 条件构造器：构造条件查询的条件
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        // 添加过滤条件（先判断是否为空，再决定是否将条件添加进SQL语句）
        lqw.like(StringUtils.isNotEmpty(name), Employee::getName, name); // apache的工具类StringUtils
        // 添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime); // 根据更新时间来排序，Desc降序排序

        // 3 执行查询
        service.page(pageInfo, lqw);   // 并不需要返回，处理好之后会自动给page对象封装好数据

        // pageInfo在前面代表 分页构造器， 在后面执行之后代表 封装好的page对象
        return R.success(pageInfo);
    }


    /**
     * 修改员工信息
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request){

        /*
                注意：因为前端js对Long型数据会有精度误差，造成 ID不匹配，所以要扩展MVC框架中的 "消息转换器"，
                    在里面添加"对象映射器"，将employee对象转换成特定的Json数据，将id的Long型改成String类型
         */


        /*
            以下这些：更新创建人，创建时间的操作，交给MetaObjectHandler来处理
         */
        /*// 1 设置 修改者的ID
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);

        // 2 设置 更新的时间
        employee.setUpdateTime(LocalDateTime.now());*/

        // 3 执行sql，根据ID修改信息
        service.updateById(employee);

        // 4 返回结果
        return R.success("修改成功！");
    }


    /**
     * 通过ID查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee emp = service.getById(id);

        if(emp != null){
            return R.success(emp);
        }

        return R.error("没有该用户的信息");
    }
}
