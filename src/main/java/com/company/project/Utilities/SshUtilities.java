package com.company.project.Utilities;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.stream.Collectors;

/** Created by pavankovurru on 7/19/17. */
public class SshUtilities {

  // jumpserver ssh details
  private static String jumpServerSshUser;
  private static String jumpServerSshPassword;
  private static String jumpServerSshHostName;
  private static int jumpServerSshPort;
  private static Session jumpServerSession;

  // server ssh details
  private static String serverUser;
  private static String serverPassword;
  private static String serverHostName;
  private static int serverSshPort;
  private static Session serverSession;

  // Initiating Logger Object
  private static final Logger LOG = LogManager.getLogger();

  public static void initialize(String env) {

    if (env.matches(".*?dev.*?")) {

      // jump server
      jumpServerSshUser = "testuser";
      jumpServerSshPassword = "testpassword";
      jumpServerSshHostName = "sshHostName";
      jumpServerSshPort = 22;

      // Actual Server
      serverUser = "testuser";
      serverPassword = "testpassword";
      serverHostName = "sshHostName2";
      serverSshPort = 22;
    }

    if (env.matches(".*?stage.*?")) {
      // jump server
      jumpServerSshUser = "testuser";
      jumpServerSshPassword = "testpassword";
      jumpServerSshHostName = "sshHostName";
      jumpServerSshPort = 22;

      // Actual Server
      serverUser = "testuser";
      serverPassword = "testpassword";
      serverHostName = "sshHostName2";
      serverSshPort = 22;
    }
  }

  // Logs into  server based on the environment tests are run, executes a shell command and
  // returns response
  public static String logInToServerExecuteShellCommandAndReturnResponse(
      String serviceEndPoint, String shellCommand) {

    initialize(serviceEndPoint);
    String output = "";
    LOG.info("Executing Shell Command - " + shellCommand);

    try {
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");

      // Connecting to jump server
      JSch jsch = new JSch();
      jumpServerSession =
          jsch.getSession(jumpServerSshUser, jumpServerSshHostName, jumpServerSshPort);
      jumpServerSession.setPassword(jumpServerSshPassword);
      jumpServerSession.setConfig(config);
      jumpServerSession.connect();
      LOG.debug("SSH Connected - connected to jump server - " + jumpServerSshHostName);

      // Change the port number if the port is already used.
      int localPort = 15006;

      // Port Forwarding
      int assingedPort =
          jumpServerSession.setPortForwardingL(localPort, serverHostName, serverSshPort);

      Channel channel1 = jumpServerSession.openChannel("sftp");
      channel1.connect();
      ChannelSftp sftpChannel = (ChannelSftp) channel1;
      //getting contents of pen file from server
      InputStream pem = sftpChannel.get("permissions.pem");

      String result =
          new BufferedReader(new InputStreamReader(pem)).lines().collect(Collectors.joining("\n"));

      // writing data to pem file
      // Note: data will be flushed out as soon as this method done executing.
      writeToFile(result);

      // using a .pem file to get access to ssh
      String pemFileLocation = System.getProperty("user.dir") + "/src/main/resources/pemfile.pem";

      // using a .pem file to get access to ssh
      jsch.addIdentity(pemFileLocation, serverPassword);

      // Connecting to actual server -- syntax --> ssh -i filename.pem user@server
      serverSession = jsch.getSession(serverUser, "localhost", assingedPort);
      serverSession.setConfig(config);
      serverSession.connect(30000);
      LOG.debug("SSH Connected - connected to server - " + serverHostName);

      // Executing and printing shell commands
      Channel channel = serverSession.openChannel("exec");
      ((ChannelExec) channel).setCommand(shellCommand);
      channel.setInputStream(null);
      ((ChannelExec) channel).setErrStream(System.err);

      InputStream in = channel.getInputStream();
      channel.connect();
      byte[] temp = new byte[1024];
      while (true) {
        while (in.available() > 0) {
          int i = in.read(temp, 0, 1024);
          if (i < 0) break;
          // capturing shell output as a multi line string
          output = output + (new String(temp, 0, i));
        }
        if (channel.isClosed()) {
          LOG.debug("exit-status: " + channel.getExitStatus());
          break;
        }
      }

      LOG.debug("SSH Sessions Getting Disconnected - No Exceptions Observed");
    } catch (Exception e) {
      LOG.debug("SSH Sessions Getting Disconnected - Exception observed");
      e.printStackTrace();
    } finally {
      serverSession.disconnect();
      jumpServerSession.disconnect();
      writeToFile("");
    }

    return output;
  }

  // Update Log4J logging level to trace
  public static void updateLog4jLoggingLevel(String serviceEndPoint, String loggerLevel) {

    String log4j2Path = "path to log4j2.xml";

    // sed is not supported in stage env - This may be used in the future
    // LogInToServerExecuteShellCommandAndReturnResponse(serviceEndPoint, "sed -i
    // 's/\\(Logger.*name=\\\"co.*=\\\"\\).*\\(\\\"\\)/\\1" + loggerLevel + "\\2/' " + log4j2Path);

    logInToServerExecuteShellCommandAndReturnResponse(
        serviceEndPoint,
        "shell command");
    LOG.info("Updated Logger level to - " + loggerLevel);
  }

  // Writes contents of .pem file into local during test run, This is required for jSCH multi
  // tunneling
  public static void writeToFile(String data) {

    try {
      File myPemFile = new File(System.getProperty("user.dir") + "/src/main/resources/pemfile.pem");
      FileOutputStream pemFileStream = new FileOutputStream(myPemFile, false);
      pemFileStream.write(data.getBytes());
      pemFileStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
