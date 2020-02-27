package com.ibm.cio.cmr.request.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.HkeyUtil;

@Component
public class UserManager {

  @Autowired
  private UserAuthenticationHelper userAuthenticationHelper;

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
  public boolean authenticateUser(String userId, String password) throws CmrException {
    return userAuthenticationHelper.authenticateUser(userId, password);
  }

  /**
   * Gets the cNUM by intranet addr.
   * 
   * @param intranetAddr
   *          the intranet addr
   * @return the cNUM by intranet addr
   */
  public String getUserCnum(String userIntranetAddress) {
    return BluePagesHelper.getCNUMByIntranetAddr(userIntranetAddress);
  }

  /**
   * This method retrieves Hkey by the user email address.
   * 
   * @param userEmail
   * @return
   * @throws InvalidHkeyException
   */
  public String getUserHkey(String userIntranetAddress) throws CmrException {
    return HkeyUtil.getHkeyByUserEmail(userIntranetAddress);
  }
}
