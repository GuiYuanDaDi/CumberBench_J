<div align="center">

<!-- Logo（居中显示，带阴影） -->
<a href="#">
  <img src="./images/eyu1.png" 
       alt="Logo" 
       style="width: 400px; margin: 40px 0; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
</a>

<!-- 语言切换栏（带背景和圆角） -->
<div style="margin: 20px auto; padding: 12px; 
            background: #f8f9fa; border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
            width: fit-content;">
  <strong>
    <a href="./README.md" style="margin: 0 10px; color: #2c3e50; text-decoration: none;font-size: 18px;">🇺🇸 English</a>
    <span style="color: #ddd;">|</span>
    <a href="./README.zh-CN.md" style="margin: 0 10px; color: #2c3e50; text-decoration: none;font-size: 18px;">🇨🇳 中文</a>
  </strong>
</div>

</div>
# Database Consistency Test Tool - CumberBench

This project is a tool for testing database consistency under different isolation levels. It defines multiple test cases through a configuration file, uses Java JDBC to connect to the database, randomly generates insert, delete, and update operations, and checks data consistency.

## Project Structure

```
db-consistency-test-tool
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── App.java               # Application entry point
│   │   │   ├── config
│   │   │   │   └── ConfigParser.java  # Configuration file parser
│   │   │   ├── db
│   │   │   │   └── DatabaseConnection.java # Database connection management
│   │   │   ├── test
│   │   │   │   ├── TestCase.java      # Test case definition
│   │   │   │   ├── TestWorker.java    # Worker thread logic
│   │   │   │   └── SQLGenerator.java  # SQL statement generator
│   │   └── resources
│   │       └── config.ini             # Configuration file
├── pom.xml                             # Maven configuration file
└── README.md                           # Project documentation
```

## Features

- Parse configuration file to extract test case information.
- Connect to the database via JDBC and execute SQL operations.
- Randomly generate insert, delete, and update operations to simulate a concurrent environment.
- Check data consistency to ensure correctness under different isolation levels (Read Committed, Repeatable Read).

## Usage - Quick Start

1. **Configuration File**: Define database connection information and test cases in `./config.ini`.
2. **Run the Program**: Run the generated JAR file.
    ```sh
    bash run.sh
    ```
3. **Testing Process**: Test statements are stored in the `testsql` folder according to the client, run logs are in `test.log`, and errors are in `err.log`.

## Usage - Compile and Run

1. **Configuration File**: Define database connection information and test cases in `./config.ini`.
2. **Compile the Project**: Use Maven to compile the project.
    ```sh
    mvn clean install
    ```
3. **Run the Program**: Run the generated JAR file.
    ```sh
    bash run.sh
    ```
4. **Testing Process**: Test statements are stored in the `testsql` folder according to the client, run logs are in `test.log`, and errors are in `err.log`.

## Configuration File Example (`config.ini`)

```ini
[main]
jdbcurl=jdbc:postgresql://localhost:5432/testdb
username=testuser
password=testpass
; Test duration
test_duration=60
; Maximum random range used in tests
max_random=100
logging_sql = true

; RC test: The first field must be id,
; RR test: The first column is preferably not the primary key, as random values are likely to repeat

[test1]
create_sql=CREATE TABLE test_table_1 (id INT, name VARCHAR(100) NOT NULL, page CHAR(50), core DECIMAL(10, 2));
select_sql=SELECT * FROM test_table_1;
index_col=id, name
iso=READ_COMMITTED

[test2]
create_sql=CREATE TABLE test_table_2 (id INT, name VARCHAR(100));
select_sql=SELECT * FROM test_table_2;
index_col=id, name
iso=REPEATABLE_READ

[test3]
create_sql=CREATE TABLE test_table_3 (id INT, name VARCHAR(100));

[test4]
create_sql=CREATE TABLE users (user_id INT, username VARCHAR(100), email VARCHAR(100));
select_sql=SELECT * FROM users;
index_col=user_id, username, email
iso=REPEATABLE_READ

[test5]
create_sql=CREATE TABLE products (id INT, name VARCHAR(100), price DECIMAL(10, 2), stock INT, category_id INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
select_sql=SELECT * FROM products;
index_col=id, category_id
iso=READ_COMMITTED

[test...]
```

## Code Explanation

### `App.java`

The entry point of the application, responsible for initializing the configuration parser, testing the database connection, retrieving test cases, creating a thread pool, and submitting test tasks.

### `ConfigParser.java`

Parses the `config.ini` configuration file to extract database connection information and test case information.

### `DatabaseConnection.java`

Manages the database connection and provides methods to execute SQL statements.

### `TestCase.java`

Defines a test case, including the create table statement, select statement, index column, and isolation level.

### `TestWorker.java`

Worker thread logic, responsible for executing database operations and checking data consistency.

### `SQLGenerator.java`

Generates random SQL statements, including insert, delete, and update operations.

## Contribution

Feel free to submit issues and pull requests.

## License

This project is licensed under the MIT License.

## Contact Me

![小红书](./images/xiaohongshu.jpg)
![公众号](./images/gongzhonghao.jpg)
![wechat](./images/xiaogou.png)
