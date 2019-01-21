package com.cottagecoders.monitorserver;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

class HttpServer extends NanoHTTPD {

  public HttpServer() throws IOException {
    super(MonitorServer.config.getAsInt(Configuration.HTTP_PORT));
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("\nRunning! Point your browser to http://" + MonitorServer.hostName + ":" + MonitorServer.config.getAsInt(
        Configuration.HTTP_PORT));
  }

  @Override
  public Response serve(IHTTPSession session) {
    String top = "<html><head><title>MonitorServer</title></head>" +
        "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/icon?family=Material+Icons\">" +
        "<link rel=\"stylesheet\" href=\"https://code.getmdl.io/1.3.0/material.indigo-blue.min.css\">" +
        "<script defer src=\"https://code.getmdl.io/1.3.0/material.min.js\"></script>";
    String queryParams = session.getQueryParameterString();
    String queryString = session.getUri();
    StringBuilder msg = new StringBuilder(top);
    if (queryString.startsWith("/config")) {
      msg.append(dumpConfig());
    } else if (queryString.startsWith("/apps")) {
      msg.append(dumpApps());
    } else {
      msg.append(dumpInstances());
    }
    return newFixedLengthResponse(msg + "</body></html>\n");
  }

  private String dumpInstances() {
    StringBuilder s = new StringBuilder(title("Instances"));
    try {
      DBReader dbr = new DBReader();
      List<ImmutablePair<Long, Long>> ins = dbr.getInstances();
      s.append(startTable("Instances"));

      for (ImmutablePair<Long, Long> elem : ins) {
        s.append(startRow());
        s.append(tableAdd(elem.left, new Date(elem.right).toString()));
        s.append(endRow());
      }
      s.append(endTable());
    } catch (DBNotImplementedException | SQLException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(2);
    }
    return s.toString();
  }

  private String dumpConfig() {
    StringBuilder s = new StringBuilder(title("Config"));
    try {
      DBReader dbr = new DBReader();
      List<ImmutablePair<Long, Long>> ins = dbr.getInstances();

      // TODO: hard-coded for right now.
      List<Config> conf = dbr.getConfigs(1, 1548022061674L);
      s.append(startTable("Config"));
      for (Config cc : conf) {
        System.out.println("conf " + conf);
        s.append(startRow());
        s.append(tableAdd(cc.appName));
        s.append(tableAdd(cc.startTime));
        s.append(tableAdd(cc.kv));
        s.append(endRow());
      }
      s.append(endTable());

    } catch (DBNotImplementedException | SQLException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(27);
    }
    return s.toString();
  }

  private String dumpApps() {
    String s = title("Apps");
    return s;
  }

  private String title(String name) {
    String s = "<body><br/><center><h1>";
    s += name.trim();
    s += "</h1></center><br/><br/>";
    return s;
  }

  private String startTable(String title) {
    return String.format("<table><th>%s</th>", title);
  }

  private String startRow() {
    return "<tr>";
  }

  private String endRow() {
    return "</tr>";
  }

  private String tableAdd(ImmutablePair<String, String> pair) {
    return String.format("<td>%s : %s</td>", pair.getLeft(), pair.getRight());
  }

  private String tableAdd(long l, String r){
    return String.format("<td>%d - %s</td>", l, r);
  }

  private String tableAdd(String s) {
    return String.format("<td>%s</td>", s);
  }

  private String tableAdd(long l) {
    return String.format("<td>%d</td>", l);
  }

  private String endTable() {
    return "</table>";
  }
}
