package com.cottagecoders.monitor_server;

public class DBNotImplementedException extends Exception{
  public DBNotImplementedException() {

  }

  public DBNotImplementedException(String message)
  {
    super(message);
  }
}
