# 数据库一致性测试工具

该项目是一个用于测试数据库在不同隔离级别下数据一致性的工具。它通过配置文件定义多个测试用例，使用 Java JDBC 连接数据库，随机生成增删改操作，并校验数据的一致性。

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

jdbcurl=jdbc:postgresql://localhost:5432/testdb
username=testuser
password=testpass
;测试时间
test_duration=60
;测试使用的最大随机范围
max_random = 100

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

![小红书](images/xiaohongshu.png)
![公众号](images/公众号.jpg)