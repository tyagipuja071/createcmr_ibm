package com.ibm.cio.cmr.request.util.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Email {

  private static final Logger LOG = Logger.getLogger(Email.class);

  private String subject;
  private String to;
  private String from;
  private String cc;
  private String bcc;
  private String message;
  private boolean important;
  private List<File> attachments;
  private Map<String, ByteArrayOutputStream> attachmentData;
  private MessageType type = MessageType.TEXT;

  public Email() {
    this.attachments = new ArrayList<File>();
    this.attachmentData = new HashMap<>();
  }

  public static void main(String[] args) throws Exception {
    Email mail = new Email();
    mail.setFrom("jefftest<j@u.com>");
    mail.setTo("zamoraja@ph.ibm.com");

    mail.setMessage("Test message");
    mail.addAttachment("c:/affected.html");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    FileInputStream fis = new FileInputStream("c:/test.pdf");
    try {
      IOUtils.copy(fis, bos);
      mail.addAttachment("test.pdf", bos);
    } finally {
      fis.close();
    }
    mail.setSubject("test email with attachment.");

    mail.send("smtp.icds.ibm.com");
  }

  /**
   * Sends the mail. Returns false if there was any error in sending the mail.
   * 
   * @param emailHost
   * @return
   */
  public boolean send(String emailHost) {

    // Recipient's email ID
    String to = getTo();

    // Sender's email ID
    String from = getFrom();

    // Host of the email
    String host = emailHost;

    // Get system properties
    Properties properties = System.getProperties();

    // Setup mail server
    properties.setProperty("mail.smtp.host", host);

    // Get the default Session object.
    Session session = Session.getDefaultInstance(properties);

    try {
      // Create a default MimeMessage object.
      MimeMessage message = new MimeMessage(session);

      // Set From: header field of the header.
      message.setFrom(new InternetAddress(from));

      // Set To: header field of the header.
      for (String email : to.split(",")) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
      }

      if (getCc() != null && !"".equals(getCc().trim())) {
        message.addRecipient(Message.RecipientType.CC, new InternetAddress(getCc()));
      }
      if (getBcc() != null && !"".equals(getBcc().trim())) {
        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(getBcc()));
      }

      // Set Subject: header field
      message.setSubject(getSubject(), "UTF-8");

      // Now set the actual message
      if (this.attachments.size() == 0 && this.attachmentData.size() == 0) {
        if (this.type == MessageType.HTML) {
          message.setContent(getMessage(), "text/html; charset=UTF-8");
        } else if (this.type == MessageType.APPLICATION) {
          message.setContent(getMessage(), "multipart/mixed");
        } else {
          message.setText(getMessage(), "UTF-8");
        }
      } else {

        Multipart multiPartMail = new MimeMultipart();

        MimeBodyPart mainPart = new MimeBodyPart();
        if (this.type == MessageType.HTML) {
          mainPart.setContent(getMessage(), "text/html; charset=UTF-8");
        } else if (this.type == MessageType.APPLICATION) {
          mainPart.setContent(getMessage(), "multipart/mixed");
        } else {
          mainPart.setText(getMessage());
        }
        multiPartMail.addBodyPart(mainPart);

        MimeBodyPart attachmentPart = null;
        DataSource ds = null;
        for (File attachment : this.attachments) {
          attachmentPart = new MimeBodyPart();
          ds = new FileDataSource(attachment);
          attachmentPart.setDataHandler(new DataHandler(ds));
          attachmentPart.setFileName(attachment.getName());
          LOG.debug("Attaching file: " + attachment.getName());
          multiPartMail.addBodyPart(attachmentPart);
        }
        for (String filename : this.attachmentData.keySet()) {
          ByteArrayOutputStream attachment = this.attachmentData.get(filename);
          attachmentPart = new MimeBodyPart();
          ds = new ByteArrayDataSource(attachment.toByteArray(), "application/octet-stream");
          attachmentPart.setDataHandler(new DataHandler(ds));
          attachmentPart.setFileName(filename);
          LOG.debug("Attaching file: " + filename);
          multiPartMail.addBodyPart(attachmentPart);
          try {
            attachment.close();
          } catch (IOException e) {
          }
        }

        message.setContent(multiPartMail, "text/html; charset=UTF-8");
      }

      if (isImportant()) {
        message.setHeader("X-Priority", "1");
        message.setHeader("Importance", "High");
      }

      MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
      mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
      mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
      mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
      mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
      mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
      CommandMap.setDefaultCommandMap(mc);

      LOG.info("Sending email [" + emailHost + "]: " + getSubject() + " to " + getTo());

      // Send message
      Transport.send(message);

      return true;
    } catch (MessagingException mex) {
      LOG.error("There was an error in sending the email. [" + mex.getMessage() + "]", mex);
      return false;
    }
  }

  public void addAttachment(String fileName) throws Exception {
    File attachment = new File(fileName);
    if (!attachment.exists() || attachment.isDirectory()) {
      LOG.warn("File must exist and must not be a directory. Attachment not added.");
      LOG.warn("Supplied file: " + fileName);
      return;
    }
    this.attachments.add(attachment);

  }

  public void addAttachment(String name, ByteArrayOutputStream bos) throws Exception {
    this.attachmentData.put(name, bos);

  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getCc() {
    return cc;
  }

  public void setCc(String cc) {
    this.cc = cc;
  }

  public String getBcc() {
    return bcc;
  }

  public void setBcc(String bcc) {
    this.bcc = bcc;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

}
