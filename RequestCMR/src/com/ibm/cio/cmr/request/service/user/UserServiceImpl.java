package com.ibm.cio.cmr.request.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.user.UserManager;

/**
 * Service class for the user related operations.
 * 
 * @author pjurak
 * 
 */
@Service(value = "userService")
public class UserServiceImpl implements UserService {

  @Autowired
  private UserManager userManager;

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.cio.wwquota.service.UserService#authenticateUser(java.lang.String,
   * java.lang.String)
   */
  @Override
  @Transactional(readOnly = true)
  public boolean authenticateUser(String userId, String password) throws CmrException {
    return userManager.authenticateUser(userId, password);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.cio.wwquota.service.UserService#getUserCnum(java.lang.String)
   */
  @Override
  @Transactional(readOnly = true)
  public String getUserCnum(String userIntranetAddress) {
    return userManager.getUserCnum(userIntranetAddress);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.cio.wwquota.service.UserService#getUserHkey(java.lang.String)
   */
  @Override
  @Transactional(readOnly = true)
  public String getUserHkey(String userIntranetAddress) throws CmrException {
    return userManager.getUserHkey(userIntranetAddress);
  }
}
