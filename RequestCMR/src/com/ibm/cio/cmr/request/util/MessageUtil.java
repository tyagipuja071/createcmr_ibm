package com.ibm.cio.cmr.request.util;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.Message;

/**
 * Handles the system messages for both exceptions and information
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MessageUtil {

	private static Properties BUNDLE = new Properties();
	private static final DecimalFormat CODE_FORMATTER = new DecimalFormat("0000");
	public static final String ERROR_MSG_KEY = "errorMessage";
	public static final String INFO_MSG_KEY = "infoMessage";

	/**
	 * Loads the sql with the given key
	 * 
	 * @param sqlKey
	 */
	private static void load() throws Exception {
		InputStream is = ConfigUtil.getResourceStream("cmr-messages.properties");
		try {
			BUNDLE.clear();
			BUNDLE.load(is);
		} finally {
			is.close();
		}
	}

	public static String getMessage(int messageCode) {
		try {
			String msg = BUNDLE.getProperty(CODE_FORMATTER.format(messageCode));
			return msg;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Returns a list of client-side messages. All client-side messages have
	 * codes starting with '5'
	 * 
	 * @return
	 */
	public static List<Message> getClientMessages() {
		List<Message> messages = new ArrayList<Message>();
		Message msg = null;
		for (Object key : BUNDLE.keySet()) {
			if (key.toString().startsWith("5")) {
				msg = new Message();
				msg.setCode(Integer.parseInt((String) key));
				msg.setMessage(getMessage(msg.getCode()));
				messages.add(msg);
			}
		}
		return messages;
	}

	/**
	 * Corrects the state of the messages on the request (for redirected
	 * requests)
	 * 
	 * @param request
	 */
	public static void checkMessages(HttpServletRequest request) {
		if (request.getParameter(ERROR_MSG_KEY) != null && request.getAttribute(ERROR_MSG_KEY) == null) {
			request.setAttribute(ERROR_MSG_KEY, request.getParameter(ERROR_MSG_KEY));
		}
		if (request.getParameter(INFO_MSG_KEY) != null && request.getAttribute(INFO_MSG_KEY) == null) {
			request.setAttribute(INFO_MSG_KEY, request.getParameter(INFO_MSG_KEY));
		}
	}

	/**
	 * Sets the error message on the model
	 * 
	 * @param model
	 * @param messageCode
	 */
	public static void setErrorMessage(ModelMap model, int messageCode) {
		model.addAttribute(ERROR_MSG_KEY, getMessage(messageCode));
	}

	/**
	 * Sets the error message on the mv
	 * 
	 * @param mv
	 * @param messageCode
	 */
	public static void setErrorMessage(ModelAndView mv, int messageCode, Object... params) {
		String message = getMessage(messageCode);
		if (params != null) {
			message = MessageFormat.format(message, params);
		}
		mv.addObject(ERROR_MSG_KEY, message);
	}

	public static void setErrorMessage(ModelAndView mv, int messageCode) {
		mv.addObject(ERROR_MSG_KEY, getMessage(messageCode));
	}

	/**
	 * Sets the info message on the model with the parameters
	 * 
	 * @param model
	 * @param messageCode
	 * @param params
	 */
	public static void setInfoMessage(ModelMap model, int messageCode, Object... params) {
		String message = getMessage(messageCode);
		if (params != null) {
			message = MessageFormat.format(message, params);
		}
		model.addAttribute(INFO_MSG_KEY, message);
	}

	/**
	 * Sets the info message on the model
	 * 
	 * @param model
	 * @param messageCode
	 */
	public static void setInfoMessage(ModelMap model, int messageCode) {
		setInfoMessage(model, messageCode, (Object[]) null);
	}

	/**
	 * Sets the info message on the mv with the given parameters
	 * 
	 * @param mv
	 * @param messageCode
	 * @param params
	 */
	public static void setInfoMessage(ModelAndView mv, int messageCode, Object... params) {
		String message = getMessage(messageCode);
		if (params != null) {
			message = MessageFormat.format(message, params);
		}
		mv.addObject(INFO_MSG_KEY, message);
	}

	/**
	 * Sets the info message on the mv
	 * 
	 * @param mv
	 * @param messageCode
	 */
	public static void setInfoMessage(ModelAndView mv, int messageCode) {
		setInfoMessage(mv, messageCode, (Object[]) null);
	}

	public static void setInfoMessage(ModelAndView mv, String message) {
		mv.addObject(INFO_MSG_KEY, message);
	}

	/**
	 * Refreshes the Messages
	 * 
	 * @throws Exception
	 */
	public static void refresh() throws Exception {
		load();
	}

	/* List here the public static final int identifiers for your code */

	/* Error Messages, prefixed by ERROR_ */
	/* Error messages are from 0-999 */
	public static final int ERROR_GENERAL = 0;
	public static final int ERROR_BLUEPAGES_AUTH = 1;
	public static final int ERROR_BLUEGROUPS_AUTH = 2;
	public static final int ERROR_HKEY_INVALID = 3;
	public static final int ERROR_TIMEOUT = 4;
	public static final int ERROR_CANNOT_GET_CURRENT = 5;
	public static final int ERROR_ALREADY_DELEGATE = 6;
	public static final int ERROR_FACES_CANNOT_CONNECT = 7;
	public static final int ERROR_MGR_CANNOT_BE_RETRIEVED = 8;
	public static final int ERROR_CANNOT_ADD_YOURSELF_AS_DELEGATE = 9;
	public static final int ERROR_CANNOT_FORCE_CHANGE_STATUS = 10;
	public static final int ERROR_CANNOT_SAVE_REQUEST = 11;
	public static final int ERROR_ALREADY_NOTIFY = 12;
	public static final int ERROR_CANNOT_ADD_YOURSELF_AS_NOTIFY = 13;
	public static final int ERROR_ALREADY_ADDRESS = 14;
	public static final int ERROR_NO_FIND_CMR_DEFINED = 15;
	public static final int ERROR_FIND_CMR_ERROR = 16;
	public static final int ERROR_NO_CMRS = 17;
	public static final int ERROR_FILE_ATTACH_NAME = 18;
	public static final int ERROR_FILE_ATTACH_SIZE = 19;
	public static final int ERROR_FILE_DL_ERROR = 20;
	public static final int ERROR_DPL_ERROR = 21;
	public static final int ERROR_DPL_RESET = 22;
	public static final int ERROR_INVALID_REQ_ID = 23;
	public static final int ERROR_DPL_EVS_ERROR = 25;
	public static final int ERROR_ALREADY_TAXINFO = 26;
	public static final int ERROR_INVALID_ACTION = 27;
	public static final int ERROR_LEGACY_RETRIEVE = 28;
	public static final int ERROR_PREF_COUNTRY_EXISTS = 29;
	public static final int ERROR_CANNOT_FIND_ADDRESS = 30;
	public static final int ERROR_ADDRESS_ALREADY_EXISTS = 31;
	public static final int ERROR_MACHINE_ALREADY_EXISTS = 32;
	public static final int ERROR_CANNOT_AUTHENTICATE = 33;
	public static final int ERROR_DPL_NOT_DONE = 34;
	public static final int ERROR_IMPORT_DATA = 35;
	public static final int ERROR_RETRIEVE_COMPANY_DATA = 36;
	public static final int ERROR_RETRIEVE_ESTABLISHMENT_DATA = 37;
	public static final int ERROR_FILE_ATTACH_NAME_FORMAT = 38;

	// Code Maint 7000-7999
	public static final int ERROR_APPROVAL_TYPE_CANNOT_BE_DELETED = 7001;

	/* Info Messages, prefixed by INFO_ */
	/* General Info messages are from 8001-8999 */
	public static final int INFO_LOGOUT = 8001;
	public static final int INFO_RECORD_SAVED = 8002;
	public static final int INFO_RECORD_DELETED = 8003;
	public static final int INFO_MASS_RECORD_DELETED = 8004;
	public static final int INFO_FORCE_CHANGE_STATUS_OK = 8005;
	/* Request Entry */
	public static final int INFO_REQUEST_SAVED = 8006;
	public static final int INFO_REQUEST_PROCESSED = 8007;
	public static final int INFO_REQUEST_CANCEL_PROCESSING = 8008;
	public static final int INFO_REQUEST_SAVED_WITH_ERROR = 8009;
	public static final int INFO_REQUEST_SAVED_REJECT = 8010;
	public static final int INFO_ATTACHMENT_ADDED = 8011;
	public static final int INFO_ATTACHMENT_DOWNLOADED = 8012;
	public static final int INFO_DPL_SUCCESS = 8013;
	public static final int INFO_REQUEST_CREATED = 8014;
	public static final int INFO_MASS_FILE_DOWNLOADED = 8015;
	public static final int INFO_MASS_TEMPLATE_DOWNLOADED = 8016;
	public static final int INFO_MASS_FILE_UPLOADED = 8017;
	public static final int ERROR_MASS_FILE = 8018;
	public static final int ERROR_MASS_FILE_ROWS = 8019;
	public static final int INFO_ERROR_LOG_DOWNLOADED = 8020;
	public static final int ERROR_ERROR_LOG_DOWNLOAD = 8021;
	public static final int INFO_ERROR_LOG_EMPTY = 8022;
	public static final int INFO_MASS_REQUEST_COMPLETED = 8023;
	public static final int ERROR_MASS_FILE_CMR_ROW = 8024;
	public static final int ERROR_MASS_FILE_CMR_NO_DATA = 9053;
	public static final int ERROR_MASS_FILE_INVALID_CMRNO = 8051;
	public static final int ERROR_MASS_FILE_ROW = 8025;
	public static final int ERROR_MASS_FILE_EMPTY = 8026;
	public static final int ERROR_MASS_FILE_CONFIG = 8027;
	public static final int ERROR_CLAIMED_ALREADY = 8028;
	public static final int ERROR_MASS_FILE_VERSION = 8029;
	public static final int ERROR_MASS_FILE_VALIDATED = 8030;
	public static final int ERROR_MASS_FILE_ISU_CD = 8031;
	public static final int ERROR_INVALID_SCREENSHOT = 8032;

	// uki messages for mass update
	public static final int ERROR_MASS_FILE_SBO = 8033;
	public static final int ERROR_MASS_FILE_IBO = 8034;
	public static final int ERROR_MASS_FILE_CEBO = 8035;
	public static final int ERROR_MASS_FILE_SALESMAN_NO = 8036;
	public static final int ERROR_MASS_FILE_EMBARGO = 8037;
	public static final int ERROR_MASS_FILE_MODE_OF_PAYMNT = 8038;
	public static final int ERROR_MASS_FILE_VAT = 8039;
	public static final int ERROR_MASS_FILE_POSTAL_CD = 8040;
	public static final int ERROR_MASS_FILE_ISU = 8041;
	public static final int ERROR_MASS_FILE_TAX_CD = 8042;
	public static final int ERROR_MASS_FILE_INAC_CD = 8043;
	public static final int ERROR_MASS_FILE_COMPANY = 8044;
	public static final int ERROR_MASS_FILE_SITE_ID_ZP01 = 8045;
	public static final int ERROR_MASS_FILE_SITE_ID_ZI01 = 8046;
	public static final int ERROR_MASS_FILE_SITE_ID_ZD01 = 8047;
	public static final int ERROR_MASS_FILE_CMR_SITE_ID_ROW = 8048;
	public static final int ERROR_MASS_FILE_CMR_ALPHANUMERIC_ROW = 8049;
	public static final int INFO_PDF_GENERATED = 8050;

	// CND region messages for mass updates
	public static final int ERROR_MASS_FILE_INVALID_ISU_CTC = 8052;
	public static final int INFO_RECREATE = 8053;

	/* Module specific codes */
	/* test 9001-9099 */
	public static final int INFO_SADR_NAME_UPDATE = 9001;

	public static final int INFO_PREF_ADD_DELEGATE = 9002;
	public static final int INFO_PREF_REMOVE_DELEGATE = 9003;
	public static final int INFO_PREF_SAVED = 9004;
	public static final int INFO_CLAIM_SUCCESSFUL = 9005;
	public static final int INFO_OPEN_ATTACHMENT = 9006;
	public static final int INFO_REMOVE_ATTACHMENT = 9007;

	public static final int INFO_NOTIFY_ADD_LIST = 9008;
	public static final int INFO_NOTIFY_REMOVE_LIST = 9009;
	public static final int INFO_ADDRESS_ADD_LIST = 9010;
	public static final int INFO_ADDRESS_REMOVE_LIST = 9011;
	public static final int INFO_ADDRESS_UPDATE_LIST = 9012;
	public static final int INFO_CMR_IMPORTED = 9013;
	public static final int INFO_NO_CMR_IMPORTED = 9014;
	public static final int INFO_DNB_IMPORTED = 9015;

	public static final int INFO_CMR_ADD_LIST = 9016;
	public static final int INFO_CMR_REMOVE_LIST = 9017;
	public static final int ERROR_CMR_LENGTH = 9018;
	public static final int ERROR_INVALID_CMR_COUNT = 9019;

	public static final int INFO_TAXINFO_ADD_LIST = 9020;
	public static final int INFO_TAXINFO_REMOVE_LIST = 9021;
	public static final int INFO_TAXINFO_UPDATE_LIST = 9022;

	public static final int INFO_APPROVAL_ADD_LIST = 9023;
	public static final int ERROR_CANNOT_ADD_YOURSELF_AS_APPROVAL = 9024;
	public static final int INFO_APPROVALSAVED_SUCCESSFULLY = 9025;
	public static final int ERROR_APPROVAL_STATUS_CHANGED = 9026;
	public static final int ERROR_APPROVAL_OVERRIDE_SAME_APPROVER = 9027;

	public static final int ERROR_APPROVAL_DEFAULT_DRA = 9028;

	public static final int INFO_ADDRESS_COPIED = 9029;
	public static final int INFO_ADDRESSES_REMOVED = 9030;

	public static final int INFO_CONTACT_ADD_SUCCESS = 9031;

	public static final int INFO_CONTACT_UPDATE_SUCCESS = 9032;

	public static final int INFO_CONTACT_REMOVE_SUCCESS = 9033;

	public static final int INFO_CONTACT_ADD_FAILED = 9034;

	public static final int INFO_CONTACT_UPDATE_FAILED = 9035;

	public static final int INFO_CONTACT_REMOVE_FAILED = 9036;

	public static final int INFO_CONTACTS_REMOVE_SUCCESS = 9037;

	public static final int INFO_CONTACTS_REMOVE_FAILED = 9038;

	public static final int INFO_MASSDPLCHECK_LOGFILE_DOWNLOADED = 9039;

	public static final int INFO_MASSDPLCHECK_SUCCESS = 9040;

	public static final int INFO_PREF_CNTRY_ADDED = 9041;
	public static final int INFO_PREF_CNTRY_REMOVED = 9042;
	public static final int INFO_MACHINE_ADD_LIST = 9043;
	public static final int INFO_MACHINE_REMOVE_LIST = 9044;
	public static final int INFO_GEN_IMPORT_SUCCESS = 9045;
	public static final int INFO_NO_CMR_ONLY_CRIS_IMPORTED = 9046;
	// revived cmrs
	public static final int INFO_REV_CMR_FILE_PROCESSED = 9055;
	// CRIS Errors 100-199
	public static final int CRIS_ERROR_QUERY = 101;
	public static final int CRIS_ERROR_NO_COMPANY = 102;
	public static final int CRIS_ERROR_NO_ESTABLISHMENT = 103;
	public static final int CRIS_ERROR_NO_ACCOUNT = 104;

	public static final int ERROR_CMR_REACTIVATE_RDC = 9047;

	public static final int INFO_MASSLDDPLCHECK_SUCCESS = 9048;
	// automation specifc
	public static final int VERIFY_COMPANY_SUCCESS = 9049;
	public static final int VERIFY_SCENARIO_SUCCESS = 9050;
	public static final int OVERRIDE_DNB_SUCCESS = 9051;

	// Single Reactivate query Error
	public static final int SINGLE_REACT_ERROR_QUERY = 551;

	public static final int OEM_IMPORT_US_CREATE = 9052;

	public static final int ERROR_MASS_FILE_TAX_TEAM = 9054;
	public static final int ERROR_MASS_FILE_TAX_TEAM_STATUS = 9056;
	public static final int ERROR_MASS_FILE_CMR_IS_NOT_IBM = 9057;

	// SSO Error
	public static final int ERROR_GET_PUBLIC_KEY = 9058;
}
