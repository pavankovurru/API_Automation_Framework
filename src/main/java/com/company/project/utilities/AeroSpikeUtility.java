package com.company.project.utilities;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/** Created by pavankovurru on 7/19/17. */
public class AeroSpikeUtility {

  // jumpserver ssh details
  private static String jumpServerSshUser;
  private static String jumpServerSshPassword;
  private static String jumpServerSshHostName;
  private static int jumpServerSshPort;
  private static Session jumpServerSession;

  // Aerospike server ssh details
  private static String aerospikeserverUser;
  private static String aerospikeserverPassword;
  private static String aerospikeserverHostName;
  private static int aerospikeserverSshPort;
  private static Session aerospikeserverSession;

  // Initiating Logger Object
  private static final Logger LOG = LogManager.getLogger();

  public static void initialize(String env) {

    if (env.matches(".*?dev.*?")) {

      // jump server
      jumpServerSshUser = "user";
      jumpServerSshPassword = "password";
      jumpServerSshHostName = "server1";
      jumpServerSshPort = 22;

      // Actual Server
      aerospikeserverUser = "aerospikeserver";
      aerospikeserverPassword = "password";
      aerospikeserverHostName = "areospike hostname";
      aerospikeserverSshPort = 22;
    }

    if (env.matches(".*?stage.*?")) {
      // jump server
      jumpServerSshUser = "user";
      jumpServerSshPassword = "password";
      jumpServerSshHostName = "server1";
      jumpServerSshPort = 22;

      // Actual Server
      aerospikeserverUser = "aerospikeserver";
      aerospikeserverPassword = "password";
      aerospikeserverHostName = "areospike hostname";
      aerospikeserverSshPort = 22;
    }
  }

  // Logs into Aerospike server based on the environment tests are run, executes aql command and
  // returns response
  public static String LogInToAerospikeExecuteAqlQueryAndReturnResponse(
      String env, String nameSpace, String setName, String aerospikeRecordkey) {

    initialize(env);
    String aerospikeResult = "";

    try {
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");

      // Connecting to jump server
      JSch jsch = new JSch();
      jumpServerSession =
          jsch.getSession(jumpServerSshUser, jumpServerSshHostName, jumpServerSshPort);
      jumpServerSession.setPassword(jumpServerSshPassword);
      jumpServerSession.setConfig(config);
      jumpServerSession.connect(15000);
      LOG.info("SSH Connected - connected to jump server - " + jumpServerSshHostName);

      // Change the port number if the port is already used.
      int localPort = 17242;

      // Port Forwarding
      int assingedPort =
          jumpServerSession.setPortForwardingL(
              localPort, aerospikeserverHostName, aerospikeserverSshPort);

      // capturing contents of pem file from remote server
      Channel channelSftp = jumpServerSession.openChannel("sftp");
      channelSftp.connect();
      ChannelSftp sftpChannel = (ChannelSftp) channelSftp;
      InputStream pem =
          sftpChannel.get(
              "phunware-developer.pem"); // capturing data from a pem file located in jumpserver
      String result =
          new BufferedReader(new InputStreamReader(pem)).lines().collect(Collectors.joining("\n"));

      // writing data to pem file
      // Note: data will be flushed out as soon as this method done executing.
      SshUtilities.writeToFile(result);

      // using a .pem file to get access to ssh
      String pemFileLocation = System.getProperty("user.dir") + "/src/main/resources/pemfile.pem";
      jsch.addIdentity(pemFileLocation, aerospikeserverPassword);

      // Connecting to actual server -- syntax --> ssh -i filename.pem user@server
      aerospikeserverSession = jsch.getSession(aerospikeserverUser, "localhost", assingedPort);
      aerospikeserverSession.setConfig(config);
      aerospikeserverSession.connect(15000);
      LOG.info("SSH Connected - connected to server - " + aerospikeserverHostName);

      // Change the port number if the port is already used.
      int localPortAerospike = 17243;

      // Port Forwarding for connecting to aerospike
      int assingedPortAerospike =
          aerospikeserverSession.setPortForwardingL(localPortAerospike, "localhost", 3000);

      // Connecting to Areospike
      AerospikeClient client = new AerospikeClient("localhost", assingedPortAerospike);
      Key key = new Key(nameSpace, setName, aerospikeRecordkey);

      // Read Areospike record using unique Aerospike key
      Record record = client.get(null, key);
      aerospikeResult = record.toString();
      client.close();

      LOG.info("SSH Sessions Getting Disconnected - No Exceptions Observed");
    } catch (Exception ee) {
      LOG.info("SSH Sessions Getting Disconnected - Exception observed");
      ee.printStackTrace();
    } finally {
      aerospikeserverSession.disconnect();
      jumpServerSession.disconnect();
      // flushing contents from pem file
      SshUtilities.writeToFile("");
    }

    return aerospikeResult;
  }
}
