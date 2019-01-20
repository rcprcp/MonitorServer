package com.cottagecoders.monitorserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

public class MonitorServer {

  public static final String DELIM = "\t";
  // env variable which points to config file.
  public static Configuration config = null;
  public static String hostName;

  public static void main(String... args) throws IOException {
    // get host name - if that fails, try for the ip address.
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      try {
        hostName = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        System.out.println("cannot get hostname or ipaddress.");
        System.exit(4);
      }
    }

    config = new Configuration();

    // TODO: test db connectivity here.

    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(config.getAsInt(Configuration.PORT));
    } catch (IOException e) {
      e.printStackTrace();
    }

    new HttpServer();

    while (true) {
      Socket socket = null;
      try {
        socket = serverSocket.accept();
      } catch (IOException ex) {
        System.out.println("I/O error: " + ex);
        ex.printStackTrace();
      }
      System.out.println("MonitorServer(): accepted connection");
      new Listener(socket).start();

    }
  }
}
