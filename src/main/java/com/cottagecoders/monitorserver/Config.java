package com.cottagecoders.monitorserver;

import org.apache.commons.lang3.tuple.ImmutablePair;

class Config {
  String appName;
  long startTime;
  ImmutablePair kv;

  private Config() {
    // don't call this.
  }

  Config(String appName, long startTime, ImmutablePair<String, String> kv) {
    this.appName = appName;
    this.startTime = startTime;
    this.kv = kv;
  }
}
