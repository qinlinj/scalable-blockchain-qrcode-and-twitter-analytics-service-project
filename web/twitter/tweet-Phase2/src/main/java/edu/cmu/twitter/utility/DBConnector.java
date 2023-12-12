
package edu.cmu.twitter.utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnector {
    private static final String DB_URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);


//        config.setMaximumPoolSize(10);
//        config.setMinimumIdle(5);
//        config.setConnectionTimeout(30000);
//        config.setIdleTimeout(600000);
//        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static void main(String[] args) {
        try (Connection connection = DBConnector.getConnection()) {
            System.out.println("Successfully connected to the database.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnector.closeDataSource();
        }
    }
}