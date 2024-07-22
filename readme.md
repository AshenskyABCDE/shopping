# Day1 项目的准备

## 项目的划分：

sky-common：存放的是公共部分



sky-pojp ：存放实体，DTO，VO

Entity：实体 和数据库对应

DTO：数据传输对象，通常在各层之间传输

VO：试图对象，用于展现到前端

POJO：普通对象，只用getter和setter



sky-server存放的是配置文件 controller、service、mapper

## 前后端联调

当前端相应时，在controller层接受并封装参数，然后利用service方法查询数据库，service层则是通过mapper来查询数据库。

用nginx进行负载均衡

//localhost/api/employee/login 转换为 //localhost//admin/employee/login

使用Swagger去定义接口及接口相关的信息，knife4j为java mvc框架集成Swagger生成Api文档的增强解决方案。

Yapi和Swagger的区别

一个是设计时用的接口，管理和维护接口

一个是开发阶段使用的框架，帮助后端开发人员做后端的接口测试

## Swagger

![image-20240716234222523](C:\Users\hp\AppData\Roaming\Typora\typora-user-images\image-20240716234222523.png)

# Day2：

## 新增员工：

对于前端的数据，我们采用注解@RequestBody 获取，由于前端的用DTO去获取，在处理的时候我们应该先转换成对应的类

```java
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
```

之后采用mybatis进行数据库的增删改查

我们只需要在对应mapper层写入对应的sql语句即可

```java
    @Insert("INSERT INTO employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)" +
            "values "+
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);
```



## 记录员工的id

一般有jwt，cookie，session，redisson，由于之前的业务我们已经接触过redisson这次采用jwt，我们在拦截器进行事物可以看成同一个线程，可以用当前线程来作为id，注意在拦截器之后要删除，否则会内存泄漏。



## 对员工查询进行分页

一般来说，分页需要page 页数还有 当前页面数据的数量pagesize来进行分页，运用pageHelper插件可以

```java
    public PageResult PageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        // select * from employee limit
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page =  employeeMapper.pageQuery(employeePageQueryDTO);
        System.out.println(page);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total,records);
    }
```

其中对应的信息可以用sql语句来查询

```mysql
select * from employee order by desc
```

对应的mapper是

```xml
    <select id="pageQuery" resultType="com.sky.entity.Employee">
        select * from employee
        <where>
            <if test="name != null and name != ''">
                name like concat('%',#{name},'%')
            </if>
        </where>
        order by create_time desc
    </select>
```



# Day 3

## 自动填补公共字段

这个问题主要出现在有一些公共属性，会经常出现一些属性要增加和修改，这个时候可以用自动填补公共字段。

### MybatisPlus方法 

首先要进行实体类加上TableFiled的注解

```java
    @TableId(value = "id", type = IdType.AUTO)
    private String id;
    /**
     * 创建人
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人
     */
    @TableField(value = "create_user_code", fill = FieldFill.INSERT)
    private String createUserCode;

    /**
     * 创建时间
     */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;
```

之后

实现一个metaObjectHandler类的接口即可

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入操作时自动填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", String.class, getCurrentUser()); // 获取当前用户的方法
        this.strictInsertFill(metaObject, "updateUser", String.class, getCurrentUser());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作时自动填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", String.class, getCurrentUser());
    }

    // 获取当前用户的方法
    private String getCurrentUser() {
        // 可以根据实际情况从上下文中获取当前用户信息
        return "system";
    }
}
```

### Mybatis方法

首先自定义AutoFill注解

```java

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型：UPDATE INSERT
    OperationType value();
}
```

然后定义切面来实现自动填补

```java
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    // 切入点
    @Pointcut("execution(* com.sky..mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    // 前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充");
    }
}

```

## 添加菜品

### 上传图片

这里可以用阿里云的oss存储功能对图片进行管理

```java
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
```



### 上传菜品

这里主要是相较前面之比，有一个当前端传入的时候，我们要获取传入数据库数据的主键

```xml
<mapper namespace="com.sky.mapper.DishMapper">
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        insert into dish (name, category_id, price, image, description, create_time, update_time, create_user, update_user)
            values
        (#{name},#{categoryId},#{price},#{image},#{description},#{createTime},#{updateTime},#{createUser},#{updateUser})
    </insert>
</mapper>
```



此外还有一个批量添加，应该用到一个叫foreach的东西

```java
<mapper namespace="com.sky.mapper.DishFlavorMapper">
    <insert id="insertBatch">
        insert into dish_flavor(dish_id, name, value)
        VALUES
        <foreach collection="flavors" item="item" separator=",">
            (#{item.dishId}, #{item.name} , #{item.value})
        </foreach>

    </insert>
</mapper>
```

# Day 4

实现的是套餐的相关接口，本质操作和菜品是一样的



# Day 5

用redis记录当前商铺的营业状态来和前端页面进行交互



# Day 6

微信小程序的相关功能
