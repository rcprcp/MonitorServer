package com.cottagecoders.monitorserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class DBConnection {
  private Connection conn;

  DBConnection() throws DBNotImplementedException, SQLException {
    String jdbcUrl = MonitorServer.config.getAsString(Configuration.JDBC_URL);
    Properties props = new Properties();
    props.setProperty("user", MonitorServer.config.getAsString(Configuration.DB_USER));
    props.setProperty("password", MonitorServer.config.getAsString(Configuration.DB_PASSWORD));

    if (jdbcUrl.toLowerCase().startsWith("jdbc:mysql")) {
      conn = DriverManager.getConnection(jdbcUrl, props);
      conn.setAutoCommit(true);

    } else if (jdbcUrl.toLowerCase().startsWith("jdbc:postgres")) {
      conn = DriverManager.getConnection(jdbcUrl, props);

    } else {
      System.out.println("DBLogger: Invalid database type: " + jdbcUrl);
      throw new DBNotImplementedException("Invalid database type " + jdbcUrl);
    }
    System.out.println("DBConnection: Connected to database " + jdbcUrl);
  }

  Connection getConnection() {
    return conn;
  }
}
