package com.cottagecoders.monitorserver;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.omg.CORBA.SystemException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class HttpServer extends NanoHTTPD {

  public HttpServer() throws IOException {
    super(MonitorServer.config.getAsInt(Configuration.HTTP_PORT));
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("\nRunning! Point your browser to http://" + MonitorServer.hostName + ":" + MonitorServer.config.getAsInt(
        Configuration.HTTP_PORT));
  }

  @Override
  public Response serve(IHTTPSession session) {
    String queryParams = session.getQueryParameterString();
    String queryString = session.getUri();
    StringBuilder msg = new StringBuilder("<html><head><title>MonitorServer</title></head>");
    if (queryString.startsWith("/config")) {
      msg.append(dumpConfig());
    } else if (queryString.startsWith("/apps")) {
      msg.append(dumpApps());
    } else {
      msg.append("Invalid request:  queryParams '" + queryParams + "' queryString '" + queryString + "'");
    }

    return newFixedLengthResponse(msg + "</body></html>\n");
  }

  private String dumpConfig() {
    StringBuilder s = new StringBuilder(title("Config"));
    try {
      DBReader dbr = new DBReader();
      List<Config> conf = dbr.getConfigs();
      s.append(startTable("Config"));
      for (Config cc : conf) {
        s.append(startRow());
        s.append(tableAdd(cc.appName));
        s.append(tableAdd(cc.startTime));
        for (ImmutablePair<String, String> elem : cc.kv) {
          System.out.println("elem got here. " + elem.toString());
          s.append(tableAdd(elem));
        }
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
    s += "</h1></center><br/<br/>";
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
