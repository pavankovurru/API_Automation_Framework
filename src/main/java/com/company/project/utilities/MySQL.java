package com.company.project.utilities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/** Created by pavankovurru on 4/26/17. */
public class MySQL {

  // ssh details
  private static String SSH_USER;
  private static String SSH_PASSWORD;
  private static String SSH_HOSTNAME;
  private static int SSH_PORT;

  // db details
  private static String MY_SQL_HOSTNAME;
  private static String DATABASE;
  private static int LOCALPORT;
  private static int REMOTEPORT;
  private static String DB_USERNAME;
  private static String DB_PASSWORD;

  private static Connection connection;
  private static Session session;
  public static ArrayList<String> dbResult = new ArrayList<>();

  // Initiating Logger Object
  private static final Logger log = LogManager.getLogger();

  // Use this to connect to mySQL db via jump server , send sql query and retrieve each row as an
  // arraylist element
  public static ArrayList query_Post_connection_To_MySQL_Via_JumpServer(
      String dbQuery, String serviceEndPoint) {

    if (serviceEndPoint.matches(".*?stage.*?")) {

      // TODO UPDATE THESE DETAILS

      // ssh details
      SSH_USER = "";
      SSH_PASSWORD = "";
      SSH_HOSTNAME = "";
      SSH_PORT = 22;
      // db details
      MY_SQL_HOSTNAME = "";
      DATABASE = "";
      LOCALPORT = 3366;
      REMOTEPORT = 3306;
      DB_USERNAME = "";
      DB_PASSWORD = "";
    }

    try {

      Properties config = new Properties();
      config.put(
          "StrictHostKeyChecking",
          "no"); // Set StrictHostKeyChecking property to avoid UnknownHostKey issue

      JSch jsch = new JSch();
      session = jsch.getSession(SSH_USER, SSH_HOSTNAME, SSH_PORT);
      session.setPassword(SSH_PASSWORD);
      session.setConfig(config);

      session.connect();
      log.info("SSH Connected");

      int assinged_port = session.setPortForwardingL(LOCALPORT, MY_SQL_HOSTNAME, REMOTEPORT);
      log.info("localhost:" + assinged_port + " -> " + MY_SQL_HOSTNAME + ":" + REMOTEPORT);
      log.info("Port Forwarded");
    } catch (JSchException e) {
      log.info("JSch Exception Caught....");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // MY SQL DATA BASE CONNECTIVITY
    try {

      // Register  driver class - This is not mandatory with the most recent JDBC drivers
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Create Connection
      log.info("Getting Connection -" + "jdbc:mysql://localhost:" + LOCALPORT);
      connection =
          DriverManager.getConnection(
              "jdbc:mysql://localhost:" + LOCALPORT + "/" + DATABASE, DB_USERNAME, DB_PASSWORD);

      log.info("IS DATA BASE CONNECTION ClOSED -" + connection.isClosed());

      // Create Statement Object
      Statement stmt = connection.createStatement();
      log.info("Established DB Connection....");

      // Execute query and capture result set
      log.info("Executing Query....");
      log.info(dbQuery);
      ResultSet rs = stmt.executeQuery(dbQuery);

      // Processing result - storing each row as an element in arraylist.
      while (rs.next()) {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        for (int i = 1; i <= numberOfColumns; i++) {
          sb.append(rs.getString(i));
          if (i < numberOfColumns) {
            sb.append(", ");
          }
        }
        String rowdata = sb.toString();
        dbResult.add(rowdata);
      }

      // closing connection object ResultSet will be closed automatically
      connection.close();
      System.out.println("Closing Database Connection");

    } catch (SQLException e) {
      log.info("SQL Exception Caught....");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Terminate db connection
      try {
        if (connection != null && !connection.isClosed()) {
          System.out.println("Closed Database Connection");
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (session != null && session.isConnected()) {
        System.out.println("Closing SSH Connection");
        session.disconnect();
      }
    }

    return dbResult;
  }
}
