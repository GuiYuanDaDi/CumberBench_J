![logo](./images/eyu1.png)
# 数据库一致性测试工具 - CumberBench

该项目是一个用于测试数据库在不同隔离级别下数据一致性的工具。它通过配置文件定义多个测试用例，使用 Java JDBC 连接数据库，支持主备测试，随机生成增删改操作，并校验数据的一致性。

## 项目结构

```
db-consistency-test-tool
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── App.java               # 应用程序入口点
│   │   │   ├── config
│   │   │   │   └── ConfigParser.java  # 配置文件解析器
│   │   │   ├── db
│   │   │   │   └── DatabaseConnection.java # 数据库连接管理
│   │   │   ├── test
│   │   │   │   ├── TestCase.java      # 测试用例定义
│   │   │   │   ├── TestWorker.java    # 工作线程逻辑
│   │   │   │   └── SQLGenerator.java  # SQL 语句生成器
│   │   └── resources
│   │       └── config.ini             # 配置文件
├── pom.xml                             # Maven 配置文件
└── README.md                           # 项目文档
```

## 功能

- 解析配置文件，提取测试用例信息。
- 通过 JDBC 连接数据库，执行 SQL 操作。
- 随机生成增删改操作，模拟并发环境。
- 校验数据一致性，确保在不同隔离级（读已提交，可重复读）别下数据的正确性。

## 使用方法--快速使用

1. **配置文件**: 在 ./config.ini` 中定义数据库连接信息和测试用例，自行创建
2. **运行程序**: 运行生成的 JAR 文件。
    ```sh
    bash run.sh
    ```
3. **测试过程**: 测试语句根据客户端存放在testsql文件夹下，运行日志在test.log ,错误在err.log
## 使用方法--编译使用
1. **配置文件**: 在 ./config.ini` 中定义数据库连接信息和测试用例。
2. **编译项目**: 使用 Maven 编译项目。
    ```sh
    mvn clean install
    ```
3. **运行程序**: 运行生成的 JAR 文件。
    ```sh
    bash run.sh
    ```
4. **测试过程**: 测试语句根据客户端存放在testsql文件夹下，运行日志在test.log ,错误在err.log

## 配置文件示例 (`config.ini`)

```ini
[main]
;测试时间
test_duration=60
;测试使用的最大随机范围
max_random = 100
logging_sql = true

;默认database1 为主库连接 ，备上以以只读测试为主。
[database1]
jdbcurl=jdbc:postgresql://localhost:5432/testdb
username=testuser
password=testpass

[database2]
jdbcurl=jdbc:postgresql://localhost:5432/testdb
username=testuser
password=testpass
;容忍延迟
;delay=1

;RC 测试 第一个字段必须为id,
;RR 测试第一列最好不是主健，因为随机值容易重复

[test1]
create_sql=CREATE TABLE test_table_1 (id INT , name VARCHAR(100) NOT NULL , page char(50), core  DECIMAL(10, 2));
select_sql=select * from test_table_1;
index_col=id, name
iso=READ_COMMITTED


[test2]
create_sql=CREATE TABLE test_table_2 (id INT , name VARCHAR(100));
select_sql=select * from test_table_2;
index_col=id, name
iso=REPEATABLE_READ

[test3]
create_sql=CREATE TABLE test_table_3 (id INT , name VARCHAR(100));
select_sql=select * from test_table_3;
index_col=id, name
iso=READ_COMMITTED

[test4]
create_sql=CREATE TABLE users (user_id INT ,username VARCHAR(50) ,email VARCHAR(100) ,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
select_sql=SELECT * FROM users;
index_col=user_id, username, email
iso=REPEATABLE_READ

[test5]
create_sql=CREATE TABLE products (id INT ,name VARCHAR(100),price DECIMAL(10, 2),stock INT,category_id INT,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
select_sql=SELECT * FROM products;
index_col=id, category_id
iso=READ_COMMITTED

[test...]


```
## 功能原理
### 一、REPEATABLE_READ 测试原理

#### 数据管理

    根据每个用例，预填充随机数据，建立初始数据集。

    设置事务隔离级别为 REPEATABLE_READ，关闭自动提交。

#### 多线程场景模拟

    只读线程：事务内多次执行只读操作，确保多次读取结果一致。

    混合线程：在事务中随机执行增删改操作，同事务内穿插多次一致性检查，验证事务内数据稳定性。

    只写线程：执行随机写操作，模拟业务场景。

    70%概率提交事务，30%概率回滚，模拟真实场景。

#### 数据一致性验证

    读线程内执行配置项中定义的 SELECT 语句。

    多次获取结果集，逐行对比字符串字段，确保完全一致。

    检查 COUNT(*) 结果，防止幻读现象。

    发现不一致立即记录错误并终止测试。





### 二、READ_COMMITTED 测试原理

#### 数据管理

    将测试数据按线程 ID 分区（如 thread1 处理 1-500，thread2 处理 501-1000）。

    使用双队列（在数据库中/不在数据库中）跟踪数据状态。

#### 事务操作模拟

    多线程随机执行插入（从不在数据库中队列取数据）、删除（从数据库中队列取数据）、更新（组合删除+插入）。

    30%概率主动回滚，测试数据回退正确性。

    自动处理死锁回滚，重新加入数据池。

#### 数据一致性验证可见性验证

    提交前：检查本事务修改的数据对本事务可见，对其他事务不可见。

    提交后：验证修改数据对所有事务可见。

    通过新建独立连接模拟并发事务读取，验证读已提交特性。



### 三、共性设计

#### 压力测试

    使用连接池连接数据库。

    每个事务包含 0-20 个随机增删改查操作，模拟高并发场景。

    使用原子计数器统计事务/语句。

#### 日志系统

    运行日志与sql分开记录。

    sql记录：记录完整 SQL 语句及执行时间戳。通过 ControlledFileWriter 控制日志开关。

    运行日志 ：随机记录验证正确结果，准确记录错误结果。

#### 验证维度

    字段级一致性（字符串比对）

    行数一致性（COUNT(*) 校验）

    跨事务可见性（多连接交叉验证）

#### 整体评估

    程序通过精准的状态管理、多维度的数据校验和真实的事务行为模拟，全面验证数据库在 RR 和 RC 隔离级别下的 ACID 特性，特别关注重复读、幻读、脏读等典型问题的检测。

## 代码说明

### `App.java`

应用程序的入口点，负责初始化配置解析器、测试数据库连接、获取测试用例、创建线程池并提交测试任务。

### `ConfigParser.java`

解析配置文件 `config.ini`，提取数据库连接信息和测试用例信息。

### `DatabaseConnection.java`

管理数据库连接，提供执行 SQL 语句的方法。

### `TestCase.java`

定义测试用例，包括创建表语句、选择语句、索引列和隔离级别。

### `TestWorker.java`

工作线程逻辑，负责执行数据库操作并校验数据一致性。

### `SQLGenerator.java`

生成随机的 SQL 语句，包括插入、删除和更新操作。



## 贡献

欢迎提交问题和拉取请求。

## 许可证

本项目使用 MIT 许可证。

## 联系我

![小红书](./images/xiaohongshu.jpg)
![公众号](./images/gongzhonghao.jpg)
![wechat](./images/xiaogou.png)
