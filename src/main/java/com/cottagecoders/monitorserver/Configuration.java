package com.cottagecoders.monitorserver;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

class Configuration {

  private static final String DBWRITER_PROPERTIES = "DBWRITER_PROPERTIES";
  public static final String JDBC_URL = "jdbcUrl";
  public static final String DB_USER = "dbUser";
  public static final String DB_PASSWORD = "dbPassword";
  public static final String PORT = "port";
  public static final String HTTP_PORT = "httpPort";

  // configuration elements -
  private static String jdbcUrl = null;
  private static String dbUser = null;
  private static String dbPassword = null;
  private static Properties properties = null;
  private static int port = 0;

  Configuration() throws IOException {
    if (StringUtils.isEmpty(System.getenv(DBWRITER_PROPERTIES))) {
      System.out.println("Cannot access env variable " + DBWRITER_PROPERTIES);
      System.exit(27);
    }

    try (FileInputStream is = new FileInputStream(System.getenv(DBWRITER_PROPERTIES))) {
      properties = new Properties();
      if (is != null) {
        properties.load(is);
      } else {
        throw new FileNotFoundException("failed to load config file " + System.getenv(DBWRITER_PROPERTIES));
      }
    }

  }

  public String getAsString(String propName) {
    if (StringUtils.isEmpty(properties.getProperty(propName))) {
      return "";
    }
    return properties.getProperty(propName);
  }

  public int getAsInt(String propName) {
    int num = 0;
    if (!StringUtils.isEmpty(properties.getProperty(propName))) {
      try {
        num = Integer.parseInt(properties.getProperty(propName));
      } catch (NumberFormatException ex) {
        System.out.print("Config: invalid value for property " + propName + " value " + properties.getProperty(propName));
      }
    }
    return num;
  }

  public String[] getAsArray(String propName) {
    if (StringUtils.isEmpty(properties.getProperty(propName))) {
      return new String[0];
    }
    String[] parts = properties.getProperty(propName).split(",");
    return parts;
  }

  public boolean getAsBoolean(String propName) {
    if (getAsString(propName).equalsIgnoreCase("true")) {
      return true;
    }
    return false;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String k : properties.stringPropertyNames()) {
      sb.append("Config() key: ");
      sb.append(k);
      sb.append("  value: ");
      sb.append(properties.getProperty(k));
      sb.append("\n");
    }
    return sb.toString();
  }


}
