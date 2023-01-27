/*
 * Licensed Material - Property of IBM * � Copyright IBM Corporation 2010 - All Rights Reserved. 
 * US Government Users Restricted Rights - Use, duplication or disclosure 
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.ibm.cio.cmr.request.user;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.swat.password.ReturnCode;
import com.ibm.swat.password.cwa2;

/**
 * The Class UserAuthenticationHelper.
 */
@Component()
public class UserAuthenticationHelper {

  /** The Constant LOG. */
  private static final Logger LOG = Logger.getLogger(UserAuthenticationHelper.class);

  /**
   * Return Code Message Description 0 Authenticated The authentication was
   * successful. 1 No directory server specified The argument for specifying the
   * LDAP server is null or empty. 2 Missing search attribute ID The argument
   * for specifying the search attribute name is null or empty. 3 Missing search
   * attribute value The argument for specifying rhe search attribute value is
   * null or empty. 4 Missing password IBM Intranet Password specified is null
   * or empty. 5 Unable to fetch DN: FILTER There was an error extracting the DN
   * of the entry found in the directory for the specified e-mail or ID. 6
   * Unable to find matching entry: FILTER There was no entry found in the
   * directory for the specified e-mail or ID. 7 Multiple entries found There
   * were multiple entries found in the directory for the specified e-mail or
   * ID. 8 Unexpected LDAP error occurred There was an unexpected error when
   * authenticating the specified e-mail or ID. The exception caught for this
   * error can be obtained from the ReturnCode object returned. Please see
   * Javadocs for details. 9 Unable to authenticate: invalid credentials The
   * authentication was unsuccessful. The exception caught for this error can be
   * obtained from the ReturnCode object returned. Please see Javadocs for
   * details.
   * 
   * @param userId
   *          - User w3 Intranet Address
   * @param password
   * @return boolean - true if the user authentication passes.
   * @throws AuthenticationException
   *           the authentication exception
   */
  public boolean authenticateUser(String intranetId, String password) throws CmrException {
    // begin-user-code
    int cwaReturnCode = -1;
    try {

      cwa2 cwa = BluePagesHelper.getCWA2();

      // cwa2 cwa = new cwa2();
      final ReturnCode cwa2rc = cwa.authenticate(intranetId, password, ldapHost);

      LOG.info("Blue page authentication return code:" + cwa2rc.getCode() + " message: " + cwa2rc.getMessage());

      cwaReturnCode = cwa2rc.getCode();

    } catch (Exception e) {
      LOG.error("Unexpected error occured : " + e.getMessage());
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }

    if (cwaReturnCode != 0) {
      throw new CmrException(MessageUtil.ERROR_BLUEPAGES_AUTH);
    }

    return true;
  }

}
