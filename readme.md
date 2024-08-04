# 项目的叙述

版块讲解多为后端内容，欢迎互相学习交流。





首先说明一下这个项目的存放规则

sky-pojo ：存放实体，DTO，VO

Entity：实体 和数据库对应

DTO：数据传输对象，通常在各层之间传输

VO：试图对象，用于展现到前端

POJO：普通对象，只用getter和setter

我讲该项目分为三部分：公共部分、实体类、业务服务

首先对于一些常用到的常量 例如一个订单的状态（待接单，待取消）等，当前线程的id，将json的时间转为yy-mm-dd的形式，像这些常用到的放到这一个版块里。

实体类就是将该项目所有有关的实体类放到这里。

业务服务就是 需要实现相应的接口。



# 代码实现规范

下面以员工为例来讲解一下代码过程

![QQ图片20240804155245](E:\BaiduNetdiskDownload\资料\资料\day01\后端初始工程\sky-take-out\img\QQ图片20240804155245.png)

首先对于员工来说，我们实现的接口分别是增删改查。

我们一般在controller层中实现逻辑，根据文档来判断是Get请求还是Post、Put请求

列如@GetMapping("")来进行注解



以员工新增为例子(EmployeeController)

```java
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }
```

可以看到controller是进行一个控制的作用，降低耦合度，之后在service层中实现相应的操作

```java
@Override
public void save(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeDTO, employee);
    // 设置状态 密码 时间
    employee.setStatus(StatusConstant.ENABLE);
    employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
    employee.setCreateTime(LocalDateTime.now());
    employee.setUpdateTime(LocalDateTime.now());

    // TODO 需要修改当前登录的id
    employee.setCreateUser(BaseContext.getCurrentId());
    employee.setUpdateUser(BaseContext.getCurrentId());
    employeeMapper.insert(employee);
}
```

一般而言，从前端接受的为DTO类，传给前端的为VO类，由于一些传的时候的安全性或者没必要，自然会有一些属性没有，所以这个时候接受过来还要根据实际情况再添加一下（当然后续可能在公共部分自动填充中实现了），自然 需要操作数据库的时候在mapper层中实现。

```java
    @Insert("INSERT INTO employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)" +
            "values "+
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

```

## mybatis相关

mybatis是进行对数据库的相关操作，无非就是进行curd，在mapper层中去实现，而如果涉及到动态查询，则需要再mapper.xml中进行扩展。

批量插入

```xml
    <insert id="insertBatch">
        insert into order_detail (name, image, order_id, dish_id, setmeal_id, dish_flavor, amount)
            VALUES
        <foreach collection="orderDetailList" item="it" separator=",">
            (#{it.name}, #{it.image},#{it.orderId},#{it.dishId},#{it.setmealId},#{it.dishFlavor},#{it.amount})
        </foreach>
     </insert>
```



返回主键

当进行完插入处理，我们不知道所对应的id，就无法在想对插入的数据进行之后的操作。可以在xml中返回主键并赋值到id上

```xml
    <insert id="insertBatch">
        insert into order_detail (name, image, order_id, dish_id, setmeal_id, dish_flavor, amount)
            VALUES
        <foreach collection="orderDetailList" item="it" separator=",">
            (#{it.name}, #{it.image},#{it.orderId},#{it.dishId},#{it.setmealId},#{it.dishFlavor},#{it.amount})
        </foreach>
    </insert>
```

