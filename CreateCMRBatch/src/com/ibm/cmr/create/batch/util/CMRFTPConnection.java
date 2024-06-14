/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * Manages connections to the designated CreateCMR external SFTP server
 * 
 * @author JeffreyAZamora
 *
 */
public class CMRFTPConnection implements AutoCloseable {

  private static final Logger LOG = Logger.getLogger(CMRFTPConnection.class);

  private static final String REMOTE_HOST = System.getProperty("GCARS_FTP_HOST");
  private static final String USERNAME = System.getProperty("GCARS_FTP_USER");
  private static final String PASSWORD = System.getProperty("GCARS_FTP_PASS");
  private static final String KNOWN_HOSTS = System.getProperty("GCARS_FTP_KNOWN_HOSTS");
  private static final String PRIV_KEY = System.getProperty("GCARS_FTP_PRIV_KEY");

  private static int REMOTE_PORT = 22;
  private static final int SESSION_TIMEOUT = 10000;
  private static final int CHANNEL_TIMEOUT = 5000;

  private Session session = null;
  private ChannelSftp channel = null;

  /**
   * Creates a new instance of the {@link CMRFTPConnection} and internally opens
   * the session and channel
   * 
   * @throws JSchException
   */
  public CMRFTPConnection() throws JSchException {
    connect();
  }

  private void connect() throws JSchException {
    String sysPort = System.getProperty("GCARS_FTP_PORT");
    if (!StringUtils.isBlank(sysPort) && StringUtils.isNumeric(sysPort)) {
      REMOTE_PORT = Integer.parseInt(sysPort);
    }
    JSch jsch = new JSch();
    LOG.debug("Connecting to FTP Server " + REMOTE_HOST + ":" + REMOTE_PORT + " using " + USERNAME);
    LOG.debug(" - Known Hosts File: " + KNOWN_HOSTS);
    jsch.setKnownHosts(KNOWN_HOSTS);
    jsch.addIdentity(PRIV_KEY);
    this.session = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);

    Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    LOG.debug(" - Set Password...");
    this.session.setConfig(config);
    this.session.setPassword(PASSWORD);
    LOG.debug("Opening session...");
    this.session.connect(SESSION_TIMEOUT);

    LOG.debug("Opening channel...");
    this.channel = (ChannelSftp) this.session.openChannel("sftp");
    this.channel.connect(CHANNEL_TIMEOUT);
    LOG.info("Established SFTP connection to " + REMOTE_HOST + ":" + REMOTE_PORT + " using " + USERNAME);

  }

  /**
   * Copies a remote file from the SFTP server to the local file destination
   * 
   * @param remoteFile
   * @param localFile
   * @throws SftpException
   */
  public void getFile(String remoteFile, String localFile) throws SftpException {
    this.channel.get(remoteFile, localFile);
  }

  /**
   * Moves the remote file into the new remote location by internally renaming
   * the file via the channel
   * 
   * @param remoteFile
   * @param newRemoteLoc
   * @throws SftpException
   */
  public void moveFile(String remoteFile, String newRemoteLoc) throws SftpException {
    this.channel.rename(remoteFile, newRemoteLoc);
  }

  /**
   * Deletes a remote file
   * 
   * @param remoteFile
   * @throws SftpException
   */
  public void deleteFile(String remoteFile) throws SftpException {
    this.channel.rm(remoteFile);
  }

  /**
   * Uploads a local file to destination file on external sftp
   * 
   * @param localFile
   * @param destinationFile
   * @throws SftpException
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void putFile(String localFile, String destinationFile) throws SftpException, FileNotFoundException, IOException {
    try (FileInputStream fis = new FileInputStream(localFile)) {
      LOG.debug("Uploading file " + localFile + " to external SFTP file " + destinationFile);
      this.channel.put(fis, destinationFile);
      this.channel.chmod(755, destinationFile);
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> listFiles(String remoteDir) throws SftpException {
    Vector<ChannelSftp.LsEntry> entries = this.channel.ls(remoteDir);
    List<String> filenames = new ArrayList<>();
    for (ChannelSftp.LsEntry entry : entries) {
      SftpATTRS atts = entry.getAttrs();
      if (!atts.isDir()) {
        filenames.add(entry.getFilename());
      }
    }
    return filenames;
  }

  /**
   * Closes the SFTP channel and sessions
   */
  @Override
  public void close() throws Exception {
    if (this.channel != null && this.channel.isConnected()) {
      LOG.debug("Closing SFTP channel..");
      this.channel.disconnect();
    }
    if (this.session != null && this.session.isConnected()) {
      LOG.debug("Closing SFTP session..");
      this.session.disconnect();
      this.session = null;
    }
  }

}
