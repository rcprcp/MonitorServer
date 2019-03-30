package com.cottagecoders.monitorserver;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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

  /**
   * Initial processing of the HTTP request - boilerplate for the HMTL we
   * will return to the client
   *
   * @param session
   * @return response which also contains the HTML
   */
  @Override
  public Response serve(IHTTPSession session) {
    String queryParams = session.getQueryParameterString();
    String queryString = session.getUri();
    // TODO: Delete debugging code.
//    System.out.println("queryParms: " + queryParams);
    System.out.println("queryString: " + queryString);

    if(queryString.startsWith("/css/") || queryString.startsWith("/js/")) {
      return sendFile(queryString);
    }

    String boilerPlate = boilerPlate();
    String msg = "";
    if (queryString.startsWith("/config")) {
      msg = boilerPlate.replaceAll("@@TITLE@@", "Config");
      msg = msg.replaceAll("@@CONTENT@@", dumpConfig());

    } else if (queryString.startsWith("/apps")) {
      msg = boilerPlate.replaceAll("@@TITLE@@", "Apps");
      msg = msg.replaceAll("@@CONTENT@@", dumpApps());

    } else if (queryString.startsWith("/instances")) {
      msg = boilerPlate.replaceAll("@@TITLE@@", "Instances");
      msg = msg.replaceAll("@@CONTENT@@", dumpInstances());

    } else {
      msg = boilerPlate.replaceAll("@@TITLE@@", "Help");
      String content = "<p/><a href='apps'>apps</a> - display all the applications";
      content += "<p/><a href='config'>config</a> - display all the configuration data";
      content += "<p/><a href='instances'>instances</a> - display all the executions of an app";
      msg = msg.replaceAll("@@CONTENT@@", content);

    }
    return newFixedLengthResponse(msg);
  }

  private Response sendFile(String fileName){

    String mimeType = "text/html";
    if(fileName.startsWith("/css")) {
      mimeType = "text/css";
    } else if(fileName.startsWith("/js")) {
      mimeType = "application/javascript";
    }

    String html = "";
    try( InputStream is = HttpServer.class.getResourceAsStream(fileName)) {
      html = IOUtils.toString(is, Charset.defaultCharset());
    } catch(IOException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(27);
    }
    Response resp = newFixedLengthResponse(html);
    resp.setMimeType(mimeType);
    return resp;
  }

  private String boilerPlate() {
    String html = "";
    try (InputStream is = ClassLoader.getSystemResourceAsStream("index.html")) {
      html = IOUtils.toString(is, Charset.defaultCharset());

    } catch(IOException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(27);
    }
    return html;
  }

  /**
   * create the HTML to display listing all the instances
   * of a process running.
   * @return HTML
   */
  private String dumpInstances() {
    StringBuilder s = new StringBuilder();
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
    StringBuilder s = new StringBuilder();
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
    return "Apps";
  }

  /**
   * create beginning of html table
   *
   * @param title Table title.
   * @return
   */
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
