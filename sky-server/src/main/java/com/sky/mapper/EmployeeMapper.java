package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import springfox.documentation.service.Operation;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * 数据库变量中默认kebab下划线命名方式（此为历史惯例），在Mybatis中需要通过yml进行驼峰命名转换符合变量命名习惯
     * @param employee
     */

    @Insert("insert into employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据主键动态修改属性
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    EmployeeDTO getById(Long id);
}
