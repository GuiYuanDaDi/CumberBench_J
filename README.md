# Database Consistency Test Tool

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
- Check data consistency to ensure correctness under different isolation levels.

## Usage

1. **Configuration File**: Define database connection information and test cases in `src/main/resources/config.ini`.
2. **Compile the Project**: Use Maven to compile the project.
    ```sh
    mvn clean install
    ```
3. **Run the Program**: Run the generated JAR file.
    ```sh
    java -jar target/db-consistency-test-tool-1.0-SNAPSHOT.jar
    ```

## Configuration File Example (`config.ini`)

```ini
[main]
jdbcurl=jdbc:postgresql://localhost:5432/testdb
username=testuser
password=testpass
test_duration=60

[test1]
create_sql=CREATE TABLE test1 (id INT PRIMARY KEY, value VARCHAR(100));
select_sql=SELECT * FROM test1;
index_col=id
iso=READ_COMMITTED

[test2]
create_sql=CREATE TABLE test2 (id INT PRIMARY KEY, value VARCHAR(100));
select_sql=SELECT * FROM test2;
index_col=id
iso=REPEATABLE_READ
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