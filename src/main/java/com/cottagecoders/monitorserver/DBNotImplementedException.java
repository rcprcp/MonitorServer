package com.cottagecoders.monitorserver;

public class DBNotImplementedException extends Exception{
  public DBNotImplementedException() {

  }

  public DBNotImplementedException(String message)
  {
    super(message);
  }
}
