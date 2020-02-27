/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.io.File;
import java.net.InetAddress;
import java.security.KeyStoreException;

import javax.net.ssl.SSLHandshakeException;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.services.client.ServiceClient;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CheckCertificatesService extends BaseBatchService {

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {

    try {
      LOG.info("Testing connectivity to " + BATCH_SERVICE_URL);

      ServiceClient encryptClient = new ServiceClient(BATCH_SERVICE_URL) {

        @Override
        protected String getServiceId() {
          return "encrypt";
        }
      };

      encryptClient.execute();

      LOG.info("Connection succeeded. Certificates are up to date.");
    } catch (RuntimeException re) {
      boolean sendMail = false;
      Throwable t = re.getCause();
      Exception toSend = null;
      while (t != null) {
        if (t instanceof SSLHandshakeException || t instanceof KeyStoreException) {
          sendMail = true;
          toSend = (Exception) t;
          t = null;
        } else {
          t = t.getCause();
        }
      }
      addError(re);

      File marker = new File("checkcert.mrk");
      if (!marker.exists()) {
        if (sendMail) {
          sendAdminMail(toSend);
          marker.createNewFile();
        }
      } else {
        LOG.info("Check Cert marker exist at " + marker.getAbsolutePath() + ". not sending mail until cleared.");
      }
      return false;
    }
    return true;
  }

  private void sendAdminMail(Exception e) {
    String recipients = BatchUtil.getProperty("BATCH_ADMIN");
    if (!StringUtils.isEmpty(recipients)) {

      String env = SystemConfiguration.getValue("SERVER_ALIAS", null);
      if (StringUtils.isEmpty(env)) {
        env = BatchUtil.getProperty("BATCH_ENV");
        try {
          env = InetAddress.getLocalHost().getHostName();
        } catch (Exception e1) {

        }
      }
      LOG.debug("Sending Certificate Error mail to : " + recipients);
      Email mail = new Email();
      mail.setSubject("The batch keystore on " + env + " needs to be updated.");
      mail.setTo(recipients);
      mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));

      StringBuilder sb = new StringBuilder();
      sb.append("<html>");
      sb.append("<head><style> body { font-family:Calibri; font-size:14px}");
      sb.append("</style></head>");
      sb.append("<body>");
      sb.append("The keystore on " + env + " located at " + BatchUtil.getProperty("ssl.keystore.loc")
          + " has expired certificates and needs to be updated.<br> ");
      sb.append("Please contact the CMR Team to generate a new keystore and update [create cmr home]/batch/application/batch-props.properties property ");
      sb.append("<strong>ssl.keystore.loc</strong> with the new file.");
      sb.append("<br><br>");
      sb.append("Exception was: " + e.toString());
      sb.append("</body>");
      sb.append("</html>");

      mail.setType(MessageType.HTML);
      mail.setMessage(sb.toString());
      mail.setImportant(true);
      mail.send(SystemConfiguration.getValue("MAIL_HOST"));
    } else {
      LOG.warn("The certificates on the store has expired and the batch admin list is empty.");
    }
  }

  @Override
  protected boolean isTransactional() {
    return false;
  }

  @Override
  protected boolean useServicesConnections() {
    return true;
  }

}
