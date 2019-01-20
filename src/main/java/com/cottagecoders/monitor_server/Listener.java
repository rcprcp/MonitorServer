package com.cottagecoders.monitor_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Listener extends Thread {
  private Socket socket;
  private LinkedBlockingQueue queue;

  private Listener() {
    // shouldn't call this.
  }

  public Listener(Socket clientSocket, LinkedBlockingQueue queue) {
    this.socket = clientSocket;
    this.queue = queue;
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

    // read-and-process loop.
    String line;
    while (true) {
      try {
        line = br.readLine();
        queue.put(line);
      } catch (InterruptedException ex) {
        ex.printStackTrace();

      } catch(IOException ex) {
        ex.printStackTrace();
        return;
      }
    }
  }
}
