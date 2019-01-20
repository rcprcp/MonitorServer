package com.cottagecoders.monitorserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

public class Listener extends Thread {
  private Socket socket;
  private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

  private Listener() {
    // shouldn't call this.
  }

  Listener(Socket clientSocket) {
    this.socket = clientSocket;
  }

  public void run() {
    InputStream inp;
    BufferedReader br;
    try {
      inp = socket.getInputStream();
      br = new BufferedReader(new InputStreamReader(inp));
    } catch (IOException e) {
      return;
    }

    try {
      new DBLogger(queue);
    } catch(DBNotImplementedException | SQLException ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(27);
    }
    // read-and-process loop.
    String line;
    while (true) {
      try {
        line = br.readLine();
        queue.put(line);
        System.out.println("Listen(): threadId " + Thread.currentThread()
            .getName() + " line " + line + " queue size " + queue.size());
      } catch (InterruptedException ex) {
        ex.printStackTrace();

      } catch (IOException ex) {
        ex.printStackTrace();
        return;
      }
    }
  }
}
