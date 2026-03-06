# English Backend Project

## 项目简介
这是一个基于 Spring Boot + MyBatis-Plus 的英语学习平台后端项目。

## 技术栈
- **Spring Boot** 3.2.0
- **MyBatis-Plus** 3.5.4
- **MySQL** 8.0+
- **Lombok**
- **JDK** 17

## 项目结构
```
src/main/java/com/suncan/english/
├── Main.java                    # 主启动类
├── common/                      # 通用类
│   └── Result.java             # 统一响应结果
├── config/                      # 配置类
│   ├── WebConfig.java          # Web 配置（跨域）
│   └── MybatisPlusConfig.java  # MyBatis-Plus 配置（分页）
├── controller/                  # 控制器层
│   └── UserController.java     # 用户控制器
├── entity/                      # 实体类
│   └── User.java               # 用户实体
├── exception/                   # 异常处理
│   ├── BusinessException.java  # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── mapper/                      # Mapper 接口
│   └── UserMapper.java         # 用户 Mapper
└── service/                     # 服务层
    ├── UserService.java        # 用户服务接口
    └── UserServiceImpl.java    # 用户服务实现
```

## 快速开始

### 1. 数据库初始化
执行 `src/main/resources/schema.sql` 创建数据库和表

### 2. 配置数据库连接
修改 `src/main/resources/application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/english_learning
    username: your_username
    password: your_password
```

### 3. 启动项目
运行 `Main.java` 即可启动项目

### 4. 访问接口
- 默认端口：8080
- API 示例：`http://localhost:8080/api/user/list`

## API 接口

### 用户管理
- `GET /api/user/list` - 获取所有用户
- `GET /api/user/{id}` - 查询指定用户
- `POST /api/user` - 创建用户
- `PUT /api/user` - 更新用户
- `DELETE /api/user/{id}` - 删除用户

## 开发规范
- 统一使用 `Result` 封装返回结果
- 业务异常使用 `BusinessException`
- Controller -> Service -> Mapper 三层架构
- 实体类使用 Lombok 简化代码

## 后续扩展建议
- 添加 JWT 认证
- 集成 Swagger/Knife4j 文档
- 添加 Redis 缓存
- 完善日志记录
- 添加单元测试
