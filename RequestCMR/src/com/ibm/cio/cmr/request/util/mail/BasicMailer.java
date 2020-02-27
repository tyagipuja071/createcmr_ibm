package com.ibm.cio.cmr.request.util.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.PropertyConfigurator;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.listener.CmrContextListener;

public class BasicMailer {

  /*
   * Valid parameters: -s Subject -f From -t to -c cc -b bcc -m message -r
   * message file (will be read)
   */

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out
          .println("Usage mail [-s] [subject] [-from] [from] [-to] [to] [-msg] [msg] [-cc] [cc] [-bcc] [bcc] [-file] [messagefile] [-type] [TEXT/HTML]");
      return;
    }
    if (args.length < 8) {
      throw new Exception("At least subject, to, from, and message/message file should be supplied.");
    }
    String to = null;
    String from = null;
    String subject = null;
    String cc = null;
    String bcc = null;
    String msg = null;
    MessageType type = MessageType.TEXT;

    String param = null;
    for (int i = 0; i < args.length; i++) {
      param = args[i];
      if ("-s".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        subject = args[i + 1];
      } else if ("-to".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        to = args[i + 1];
      } else if ("-from".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        from = args[i + 1];
      } else if ("-cc".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        cc = args[i + 1];
      } else if ("-bcc".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        bcc = args[i + 1];
      } else if ("-file".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        msg = getMessage(args[i + 1]);
      } else if ("-msg".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        msg = args[i + 1];
      } else if ("-type".equals(param) && args.length > i + 1 && !args[i + 1].startsWith("-")) {
        String mtype = args[i + 1];
        type = "HTML".equalsIgnoreCase(mtype) ? MessageType.HTML : MessageType.TEXT;
      }
    }

    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource("cmr-log4j.properties"));

    Email mail = new Email();
    mail.setSubject(subject);
    mail.setTo(to);
    mail.setFrom(from);
    mail.setCc(cc);
    mail.setBcc(bcc);
    mail.setMessage(msg);
    mail.setType(type);

    mail.send(SystemConfiguration.getValue("MAIL_HOST"));
  }

  private static String getMessage(String fileLoc) throws IOException {
    File mail = new File(fileLoc);
    if (!mail.exists()) {
      return "";
    }
    FileInputStream fis = new FileInputStream(mail);
    try {
      InputStreamReader isr = new InputStreamReader(fis);
      try {
        BufferedReader br = new BufferedReader(isr);
        try {
          StringBuilder sb = new StringBuilder();
          String line = null;
          while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
          }
          return sb.toString();
        } finally {
          br.close();
        }
      } finally {
        isr.close();
      }
    } finally {
      fis.close();
    }
  }

}
