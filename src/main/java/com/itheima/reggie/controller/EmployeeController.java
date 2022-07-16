package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request , @RequestBody Employee employee){
        //从前端获取密码,并进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据前端获取的username从数据库中进行查询
        LambdaQueryWrapper<Employee> lwq = new LambdaQueryWrapper();
        lwq.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lwq);
        //如果查询不到则登陆失败
        if (emp == null){
            return R.error("登录失败");
        }
        //查询到但密码不匹配登录失败
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //查询到且密码匹配,而状态被禁用则登录失败
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //登录成功,并将id保存到session中
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清除session中保存的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工信息
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee){
        log.info("新增员工信息{}",employee);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("新增员工信息成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();

        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        lqw.orderByDesc(Employee::getCreateTime);

        employeeService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    /**
     * 修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        log.info(employee.toString());

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询信息id:{}",id);

        Employee emp = employeeService.getById(id);

        if(emp != null){
            return R.success(emp);
        }

        return R.error("没有查询到对应的员工信息");

    }


}
