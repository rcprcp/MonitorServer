package com.cottagecoders.monitorserver;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class DBReader {
  DBConnection connection;
  private PreparedStatement getConfigSQL;
  private PreparedStatement getInstanceSQL;

  DBReader() throws DBNotImplementedException, SQLException {
    connection = new DBConnection();

    getInstanceSQL = connection.getConnection().prepareStatement(
        "select distinct app_number, app_start_time from config order by app_start_time desc;");

    getConfigSQL = connection.getConnection().prepareStatement(
        "select * from config where app_number = ? and app_start_time = ? order by property_key");
  }

  List<ImmutablePair<Long, Long>> getInstances() throws SQLException{
    List<ImmutablePair<Long, Long>> ans = new ArrayList<>();
    ResultSet rs = getInstanceSQL.executeQuery();
    while(rs.next()) {
      ans.add(new ImmutablePair<Long, Long>(rs.getLong("app_number"), rs.getLong("app_start_time")));
    }
    rs.close();
    return ans;
  }

  List<Config> getConfigs(long appNumber, long appStartTime) throws SQLException {
    getConfigSQL.setLong(1, appNumber);
    getConfigSQL.setLong(2, appStartTime);
    ResultSet rs = getConfigSQL.executeQuery();
    Config conf;

    List<Config> theList = new ArrayList<>();
    while (rs.next()) {
      conf = new Config(
          rs.getString("app_number"),
          rs.getLong("app_start_time"),
          new ImmutablePair<String, String>(rs.getString("property_key"), rs.getString("property_value"))
      );
      theList.add(conf);
    }
    rs.close();
    return theList;
  }
}
