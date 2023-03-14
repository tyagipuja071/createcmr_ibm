/**
 * 
 */
package com.ibm.cio.cmr.request.util.async;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.QuickSearchService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.mail.Email;
import com.ibm.cio.cmr.request.util.mail.MessageType;

/**
 * Creates requests asynchronously
 * 
 * @author JeffZAMORA
 *
 */
public class AsyncRequestCreator implements Runnable {

  private static final Logger LOG = Logger.getLogger(AsyncRequestCreator.class);

  private AppUser user;
  private String cmrNo;
  private String cmrIssuingCntry;
  private String subRegion;

  public AsyncRequestCreator(AppUser user, String cmrIssuingCntry, String subRegion, String cmrNo) {
    this.user = user;
    this.cmrIssuingCntry = cmrIssuingCntry;
    this.cmrNo = cmrNo;
    this.subRegion = subRegion;
  }

  @Override
  public void run() {
    LOG.debug("Starting request creation for CMR No. " + this.cmrNo + " under " + this.cmrIssuingCntry);
    DummyServletRequest dummy = new DummyServletRequest();
    dummy.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, this.user);
    CompanyRecordModel cmr = new CompanyRecordModel();
    cmr.setRecType(CompanyRecordModel.REC_TYPE_CMR);
    cmr.setCmrNo(this.cmrNo);
    cmr.setReqType(CmrConstants.REQ_TYPE_UPDATE);
    cmr.setIssuingCntry(this.cmrIssuingCntry);
    cmr.setSubRegion(this.subRegion);

    ParamContainer params = new ParamContainer();
    params.addParam("model", cmr);
    try {
      QuickSearchService service = new QuickSearchService();
      LOG.debug("Creating request from chosen CMR..");
      RequestEntryModel reqEntry = service.process(dummy, params);
      if (reqEntry != null && reqEntry.getReqId() > 0) {
        LOG.debug("Request " + reqEntry.getReqId() + " created.");
        sendSuccessMail(reqEntry);
      } else {
        LOG.debug("Request generation encountered an issue.");
        sendErrorMail();
      }
    } catch (Exception e) {
      LOG.error("Asyn request cannot be created.", e);
      sendErrorMail();
    }
  }

  private void sendSuccessMail(RequestEntryModel model) {
    StringBuilder email = new StringBuilder();
    email.append("<html>\n");
    email.append("<head>\n");
    email.append("<style>\n");
    email.append("  body,p,a { font-family: IBM Plex Sans, Calibri; font-size:12px }\n");
    email.append("</style>\n");
    email.append("</head>\n");
    email.append("<body>\n");
    email.append("Your update request for CMR No. " + this.cmrNo + " under CMR Issuing Country " + this.cmrIssuingCntry + " is now created.<br>\n");
    email.append("The Request ID is <strong>" + model.getReqId() + "</strong><br>\n");
    email.append("You can open the request by using the Request ID or by clicking this <a href=" + SystemConfiguration.getValue("APPLICATION_URL")
        + "/request/" + model.getReqId() + ">link</a> \n");
    email.append("</body>\n");
    email.append("</head>\n");
    email.append("</html>");

    Email mail = new Email();
    mail.setTo(this.user.getIntranetId());
    mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
    mail.setSubject("Update Request " + model.getReqId() + " created for CMR No. " + this.cmrNo + " under " + this.cmrIssuingCntry);
    mail.setType(MessageType.HTML);
    mail.setMessage(email.toString());
    LOG.debug("Sending request creation mail to " + this.user.getIntranetId());
    mail.send(SystemConfiguration.getValue("MAIL_HOST"));
  }

  private void sendErrorMail() {
    StringBuilder email = new StringBuilder();
    email.append("<html>\n");
    email.append("<head>\n");
    email.append("<style>\n");
    email.append("  body,p,a { font-family: IBM Plex Sans, Calibri; font-size:12px }\n");
    email.append("</style>\n");
    email.append("</head>\n");
    email.append("<body>\n");
    email.append(
        "Your update request for CMR No. " + this.cmrNo + " under CMR Issuing Country " + this.cmrIssuingCntry + " encountered an issue.<br>\n");
    email.append("Please try again after a few minutes or contact the CreateCMR helpdesk for support.");
    email.append("</body>\n");
    email.append("</head>\n");
    email.append("</html>");

    Email mail = new Email();
    mail.setTo(this.user.getIntranetId());
    mail.setFrom(SystemConfiguration.getValue("MAIL_FROM"));
    mail.setSubject("Error: Update Request for CMR No. " + this.cmrNo + " under " + this.cmrIssuingCntry + " not created");
    mail.setType(MessageType.HTML);
    mail.setMessage(email.toString());
    LOG.debug("Sending error in request creation mail to " + this.user.getIntranetId());
    mail.send(SystemConfiguration.getValue("MAIL_HOST"));
  }

}
