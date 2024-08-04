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



# 登录

## Jwt

Jwt全称Json Web Token

由三部分组成，第一部分Header 记录令牌类型、签名算法等

第二部分携带一些自定义的信息，例如id和username

第三部分签名，防止token被篡改

（1） 登录成功后，生成令牌

（2） 后续每个请求，都要携带jwt令牌，系统在每次请求之前，都要校验token

首先是登录成功之后，生成jwt令牌

```java
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法，也就是header那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }
```

## 拦截器Interceptor

这个就是你打开网站时，点击某个页面如果没有登录会弹到登录页面中，拦截器就算这样的操作

使用拦截器之前要对拦截器进行定义

```java
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

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
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("输出当前的id" +  Thread.currentThread().getId());


        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前员工id：", empId);
            BaseContext.setCurrentId(empId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
```

prehandle是controller之前的处理，返回true才会放行到controller中，所以在token存在和相应没错误不报出异常才会放行。之后对拦截器进行配置，即可完成

```java
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login",
                        "/doc.html",
                        "/webjars/**");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login")
                .excludePathPatterns("/user/shop/status");
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket1() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    @Bean
    public Docket docket2() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将消息放进容器
        converters.add(0,converter);
    }
}

```

