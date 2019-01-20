package com.cottagecoders.monitor_server;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

public class DBLogger {
  static final String DELIM = "\t";
  private static final String DATABASE_NAME = "monitordb";
  private Connection conn;
  private PreparedStatement stmtInsertAppNameMapping;
  private PreparedStatement stmtSelectAppNameMapping;
  private PreparedStatement stmtInsertAppStart;
  private PreparedStatement stmtInsertElapsedInfo;
  private PreparedStatement stmtSelectMethodNameMapping;
  private PreparedStatement stmtInsertMethodNameMapping;
  private PreparedStatement stmtInsertConfigInfo;
  private HashMap<String, Long> appNameCache = new HashMap<>();
  private HashMap<String, Long> methodNameCache = new HashMap<>();


  DBLogger(LinkedBlockingQueue<String> queue) throws DBNotImplementedException, SQLException {

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

    System.out.println("DBLogger: Connected to database " + jdbcUrl);

    // create statement for app name mapping
    stmtInsertAppNameMapping = conn.prepareStatement("INSERT INTO app_name_mapping (app_name) VALUES(?)",
        Statement.RETURN_GENERATED_KEYS
    );

    // create statement for method name mapping
    stmtInsertMethodNameMapping = conn.prepareStatement("INSERT INTO method_name_mapping (method_name) VALUES(?)",
        Statement.RETURN_GENERATED_KEYS
    );

    // statement to get method number.
    stmtSelectMethodNameMapping = conn.prepareStatement(
        "SELECT method_number FROM method_name_mapping WHERE method_name = ?");

    // select the id from app_name table
    stmtSelectAppNameMapping = conn.prepareStatement("SELECT app_number FROM app_name_mapping WHERE app_name = ?");

    // insert the app name.
    stmtInsertAppStart = conn.prepareStatement(
        "INSERT INTO app_start_events (app_number, app_start_time, host) VALUES (?, ?, ?)");

    stmtInsertConfigInfo = conn.prepareStatement(
        "INSERT INTO config (app_number, app_start_time, property_key, property_value) VALUES ( ?, ?, ?, ?)");

    stmtInsertElapsedInfo = conn.prepareStatement(
        "INSERT INTO elapsed_events (app_number, app_start_time, elapsed_interval_ns, method_number, " +
            "method_start_sequence, " + "method_end_sequence) VALUES ( ?, ?, ?, ?, ?, ?)");

    // start a thread listening on the queue.
    Thread t = new Thread() {
      public void run() {
        // TODO: remove size thing - or make better.
        try {
          while (true) {
            String line = queue.take();
            if (StringUtils.isEmpty(line)) {
              continue;
            }
            System.out.println("" + queue.size() + " queue entry: " + line);
            process(line);
          }
        } catch (InterruptedException ex) {
          System.out.println("DBLogger:  exception taking from queue:  " + ex.getMessage());
          ex.printStackTrace();
        }
      }
    };
    t.start();

  }

  private long lookupNameMapping(
      String name, PreparedStatement lookup, PreparedStatement insert, HashMap<String, Long> cache
  ) throws SQLException {

    if (cache.containsKey(name)) {
      return cache.get(name);
    }

    lookup.setString(1, name);
    ResultSet rs = lookup.executeQuery();
    if (rs.next()) {
      long id = rs.getLong(1);
      cache.put(name, id);
      rs.close();
      return id;
    }

    long id = -1;
    insert.setString(1, name);
    insert.execute();
    rs = insert.getGeneratedKeys();
    rs.next();
    id = rs.getInt(1);
    rs.close();

    System.out.println("DBLogger: lookupNameMapping " + id + " name " + name);
    return id;
  }

  private void insertStartRecord(String data) {
    // record type: "start"
    // appname
    // epoch
    // nanos

    String[] parts = data.split(DELIM);
    try {

      // cross reference app name to app number.
      long appId = lookupNameMapping(parts[1], stmtSelectAppNameMapping, stmtInsertAppNameMapping, appNameCache);

      stmtInsertAppStart.setLong(1, appId);
      stmtInsertAppStart.setString(2, parts[2]);  //epoch
      stmtInsertAppStart.setString(3, parts[3]);  //host
      stmtInsertAppStart.execute();
    } catch (SQLException ex) {
      System.out.println("DBLogger: InsertStartRecord() " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void insertElapsedRecord(String data) {
    //record type = "elapsed"
    // appname
    // epoch
    // method name
    // nanos
    // starting sequence
    // ending sequence

    // split
    String[] parts = data.split(DELIM);

    try {
      // lookup app_number
      long appId = lookupNameMapping(parts[1], stmtSelectAppNameMapping, stmtInsertAppNameMapping, appNameCache);

      // lookup method_number
      long methodId = lookupNameMapping(
          parts[3],
          stmtSelectMethodNameMapping,
          stmtInsertMethodNameMapping,
          appNameCache
      );

      // insert:
      stmtInsertElapsedInfo.setLong(1, appId);
      stmtInsertElapsedInfo.setString(2, parts[2]);  // epoch
      stmtInsertElapsedInfo.setString(3, parts[4]);  // elapsed nanos
      stmtInsertElapsedInfo.setLong(4, methodId);  // method number
      stmtInsertElapsedInfo.setString(5, parts[5]);  // start sequence
      stmtInsertElapsedInfo.setString(6, parts[6]);  // end sequence
      stmtInsertElapsedInfo.execute();
    } catch (SQLException ex) {
      System.out.println("DBLogger: InsertStartRecord() " + ex.getMessage());
      ex.printStackTrace();
    }


  }

  private void insertConfigInfo(String data) {
    //record type
    // appname
    // epoch
    // property_key
    // property_value

    String[] parts = data.split(DELIM);

    try {
      long appId = lookupNameMapping(parts[1], stmtSelectAppNameMapping, stmtInsertAppNameMapping, appNameCache);

      stmtInsertConfigInfo.setLong(1, appId);
      stmtInsertConfigInfo.setString(2, parts[2]);  //epoch
      stmtInsertConfigInfo.setString(3, parts[3]);  // property_key
      stmtInsertConfigInfo.setString(4, parts[4]);  // property_value
      stmtInsertConfigInfo.execute();
    } catch (SQLException ex) {
      System.out.println("DBLogger: InsertStartRecord() " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void process(String data) {

    String[] parts = data.split(DELIM);
    if (StringUtils.isEmpty(parts[0])) {
      return;
    }

    switch (parts[0]) {
      case "start":
        insertStartRecord(data);
        break;

      case "end":
        break;

      case "elapsed":
        insertElapsedRecord(data);
        break;

      case "jvm":
        break;

      case "cpu":
        break;

      case "memory":
        break;

      case "gc":
        break;

      case "configitem":
      case "configfile":
        insertConfigInfo(data);
        break;

      default:
        break;
    }
  }
}
