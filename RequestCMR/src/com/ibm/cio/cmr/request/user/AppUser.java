package com.ibm.cio.cmr.request.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cmr.services.client.auth.Authorization;

/**
 * Class to represent a user that has successfully logged in the system
 * 
 * @author Jeffrey Zamora
 * 
 */
public class AppUser implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userCnum;
	private String empName;
	private String bluePagesName;
	private String intranetId;
	private String notesEmailId;
	private String countryCode;
	private String companyCode;
	private String processingCenter;
	private String cmrIssuingCntry;
	private boolean admin;
	private boolean preferencesSet;
	private boolean processor;
	private boolean cmde;
	private boolean approver;
	private String authCode;
	private boolean requestor;
	private boolean hasApprovals;
	private String defaultLineOfBusn;
	private String defaultRequestRsn;
	private String defaultReqType;
	private int defaultNoOfRecords;
	private boolean hasCountries;
	private boolean showPendingOnly;
	private boolean showLatestFirst;

	private Authorization auth = null;
	private List<String> roles = new ArrayList<>();
	private Map<String, List<String>> subRoles = new HashMap<>();

	/**
	 * Gets the current user from the session
	 * 
	 * @param request
	 * @return
	 */
	public static AppUser getUser(HttpServletRequest request) {
		AppUser user = (AppUser) request.getSession().getAttribute(CmrConstants.SESSION_APPUSER_KEY);
		return user;
	}

	public static boolean isLoggedOn(HttpServletRequest request) {
		return getUser(request) != null;
	}

	/**
	 * Removes the Appuser from the session
	 * 
	 * @param request
	 */
	public static void remove(HttpServletRequest request) {
		request.getSession().removeAttribute(CmrConstants.SESSION_APPUSER_KEY);
	}

	public void addRole(String roleId, String subRoleId) {
		this.roles.add(roleId);
		this.subRoles.put(roleId, new ArrayList<String>());
		if (!StringUtils.isEmpty(subRoleId)) {
			this.subRoles.get(roleId).add(subRoleId);
		}
	}

	public boolean hasRole(String roleId) {
		return this.roles.contains(roleId);
	}

	public List<String> getSubRoles(String roleId) {
		return this.subRoles.get(roleId);
	}

	public boolean hasRole(String roleId, String subRoleId) {
		return this.roles.contains(roleId) && this.subRoles.get(roleId).size() > 0;
	}

	/**
	 * Gets the IBM serial number of the user
	 * 
	 * @return
	 */
	public String getUserCnum() {
		return this.userCnum;
	}

	public void setUserCnum(String userCnum) {
		this.userCnum = userCnum;
	}

	/**
	 * Gets the Employee's name
	 * 
	 * @return
	 */
	public String getEmpName() {
		return this.empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	/**
	 * Gets the intranet ID of the user
	 * 
	 * @return
	 */
	public String getIntranetId() {
		return this.intranetId;
	}

	public void setIntranetId(String intranetId) {
		this.intranetId = intranetId;
	}

	/**
	 * Gets the notes email address of the user
	 * 
	 * @return
	 */
	public String getNotesEmailId() {
		return this.notesEmailId;
	}

	public void setNotesEmailId(String notesEmailId) {
		this.notesEmailId = notesEmailId;
	}

	/**
	 * Gets the country code of the user from Blue Pages
	 * 
	 * @return
	 */
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isPreferencesSet() {
		return preferencesSet;
	}

	public void setPreferencesSet(boolean preferencesSet) {
		this.preferencesSet = preferencesSet;
	}

	public String getBluePagesName() {
		return bluePagesName;
	}

	public void setBluePagesName(String bluePagesName) {
		this.bluePagesName = bluePagesName;
	}

	public String getProcessingCenter() {
		return processingCenter;
	}

	public void setProcessingCenter(String processingCenter) {
		this.processingCenter = processingCenter;
	}

	public String getCmrIssuingCntry() {
		return cmrIssuingCntry;
	}

	public void setCmrIssuingCntry(String cmrIssuingCntry) {
		this.cmrIssuingCntry = cmrIssuingCntry;
	}

	public boolean isProcessor() {
		return processor;
	}

	public void setProcessor(boolean processor) {
		this.processor = processor;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public boolean isRequestor() {
		return requestor;
	}

	public void setRequestor(boolean requestor) {
		this.requestor = requestor;
	}

	public String getDefaultLineOfBusn() {
		return defaultLineOfBusn;
	}

	public void setDefaultLineOfBusn(String defaultLineOfBusn) {
		this.defaultLineOfBusn = defaultLineOfBusn;
	}

	public String getDefaultRequestRsn() {
		return defaultRequestRsn;
	}

	public void setDefaultRequestRsn(String defaultRequestRsn) {
		this.defaultRequestRsn = defaultRequestRsn;
	}

	public boolean isCmde() {
		return cmde;
	}

	public void setCmde(boolean cmde) {
		this.cmde = cmde;
	}

	public String getDefaultReqType() {
		return defaultReqType;
	}

	public void setDefaultReqType(String defaultReqType) {
		this.defaultReqType = defaultReqType;
	}

	public int getDefaultNoOfRecords() {
		return defaultNoOfRecords;
	}

	public void setDefaultNoOfRecords(int defaultNoOfRecords) {
		this.defaultNoOfRecords = defaultNoOfRecords;
	}

	public boolean isHasCountries() {
		return hasCountries;
	}

	public void setHasCountries(boolean hasCountries) {
		this.hasCountries = hasCountries;
	}

	public boolean isShowPendingOnly() {
		return showPendingOnly;
	}

	public void setShowPendingOnly(boolean showPendingOnly) {
		this.showPendingOnly = showPendingOnly;
	}

	public Authorization getAuth() {
		return auth;
	}

	public void setAuth(Authorization auth) {
		this.auth = auth;
	}

	public boolean isShowLatestFirst() {
		return showLatestFirst;
	}

	public void setShowLatestFirst(boolean showLatestFirst) {
		this.showLatestFirst = showLatestFirst;
	}

	public boolean isApprover() {
		return approver;
	}

	public void setApprover(boolean approver) {
		this.approver = approver;
	}

	public boolean isHasApprovals() {
		return hasApprovals;
	}

	public void setHasApprovals(boolean hasApprovals) {
		this.hasApprovals = hasApprovals;
	}

}