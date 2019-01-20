package com.cottagecoders.monitorserver;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HttpServer extends NanoHTTPD {

  public HttpServer() throws IOException {
    super(MonitorServer.config.getAsInt(Configuration.HTTP_PORT));
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("\nRunning! Point your browsers to http://" +
        MonitorServer.hostName + ":" +
        MonitorServer.config.getAsInt(Configuration.HTTP_PORT));
  }

  @Override
  public Response serve(IHTTPSession session) {
    String msg = "<h1><center>epic fail.<center></h1>";
    try (InputStream is = HttpServer.class.getResourceAsStream("/index.html");) {
      msg = IOUtils.toString(is, "UTF-8");

    } catch (IOException ex) {
      System.out.println("exception: " + ex.getMessage());
      ex.printStackTrace();
    }
    return newFixedLengthResponse(msg + "</body></html>\n");
  }
}
