/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.ImportCMRElement;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipients;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Interface to handle GEO Specific handling on different parts of the backend
 * code like CMR Import, entity saving, etc.
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class GEOHandler {

  private static final Logger LOG = Logger.getLogger(GEOHandler.class);

  public static final List<String> ADDRESS_FIELDS_SKIP_CHECK = Arrays.asList("REQ_ID", "ADDR_TYPE", "ADDR_SEQ", "ADDR_STD_RESULT",
      "ADDR_STD_ACCEPT_IND", "ADDR_STD_REJ_REASON", "ADDR_STD_REJ_CMT", "ADDR_STD_TS", "IMPORT_IND", "DPL_CHK_RESULT", "DPL_CHK_INFO", "DPL_CHK_TS",
      "DPL_CHK_BY_ID", "DPL_CHK_BY_NM", "DPL_CHK_ERR_LIST", "RDC_CREATE_DT", "RDC_LAST_UPDT_DT", "COUNTY_NAME", "STD_CITY_NM", "PAIRED_ADDR_SEQ");

  /**
   * From the result model which is from CMR search, do the necessary
   * manipulations to make it conform with the country's business logic
   * 
   * @param source
   * @throws Exception
   */

  /**
   * Converts the FindCMR results into a correct list that will be handled by
   * the GEO. The list can be trimmed or expanded depending on the logic.
   * Per-record values can also be assigned.
   * 
   * @param source
   * @throws Exception
   */
  public abstract void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception;

  /**
   * Sets Data values. This is called during actual Data creation and update
   * when a CMR is imported
   * 
   * 
   * @param data
   * @param results
   * @param mainRecord
   * @throws Exception
   */
  public abstract void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception;

  /**
   * Sets Admin values. This is called during actual Admin creation and update
   * when a CMR is imported
   * 
   * @param admin
   * @param currentRecord
   * @throws Exception
   */
  public abstract void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception;

  /**
   * Create other Address. This is called during ZS01 imported, then copy to
   * other address do it when a CMR/DNB is imported
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @throws Exception
   */
  public abstract void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception;

  /**
   * Sets Address values. This is called during actual Address creation and
   * update when a CMR is imported
   * 
   * @param address
   * @param currentRecord
   * @param cmrNo
   * @throws Exception
   */
  public abstract void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception;

  /**
   * Gets the length for the Name1 field. Used for breaking down a name field
   * into 2 fields. This length will be used for the first field
   * 
   * @return
   */
  public abstract int getName1Length();

  /**
   * Gets the length for the Name2 field. Used for breaking down a name field
   * into 2 fields. This length will be used for the second field
   * 
   * @return
   */
  public abstract int getName2Length();

  /**
   * Sets any default values for the Admin record during creation. Called during
   * CMR import for new records or when a new request is being created
   * 
   * @param admin
   */
  public abstract void setAdminDefaultsOnCreate(Admin admin);

  /**
   * Sets any default values for the Data record during creation. Called during
   * CMR import for new records or when a new request is being created
   * 
   * @param data
   */
  public abstract void setDataDefaultsOnCreate(Data data, EntityManager entityManager);

  /**
   * When loading a request on the details page, this will be called to retrieve
   * any country specific page attributes.<br>
   * Note that the EntityManager passed to this method is
   * <strong>non-transactional</strong> and should be used only for queries
   * 
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  public abstract void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception;

  /**
   * Called during import and other functions. Sets {@link Admin} and
   * {@link Data} values based on request type during import
   * 
   * @param requestType
   * @param admin
   * @param data
   * @param importing
   */
  public abstract void handleImportByType(String requestType, Admin admin, Data data, boolean importing);

  /**
   * Adds the extra fields for request summary (update type)
   * 
   * @param service
   * @param type
   * @param cmrCountry
   * @param newData
   * @param oldData
   * @param results
   */
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
  };

  /**
   * Adds the extra fields for request summary for address (update type)
   * 
   * @param service
   * @param type
   * @param cmrCountry
   * @param newData
   * @param oldData
   * @param results
   */
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    // noop here
  }

  /**
   * For retrieval of Coverage information, converts the request to the correct
   * values based on the {@link Admin} and {@link RequestEntryModel} values
   * 
   * @param entityManager
   * @param request
   * @param mainAddr
   * @param data
   */
  public abstract void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data);

  /**
   * Splits the name field into two values. Returns a String array of length 2
   * 
   * @param name1
   * @param name2
   * @param length1
   * @param length2
   * @return
   */
  protected String[] splitName(String name1, String name2, int length1, int length2) {
    String name = (name1 != null ? name1 : "") + " " + (name2 != null ? name2 : "");
    if (StringUtils.isBlank(name)) {
      return new String[] { "", null };
    }
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
    boolean part1Ok = false;
    for (String part : parts) {
      if ((namePart1 + " " + part).trim().length() > length1 || part1Ok) {
        part1Ok = true;
        namePart2 += " " + part;
      } else {
        namePart1 += " " + part;
      }
    }
    namePart1 = namePart1.trim();

    if (namePart1.length() == 0) {
      namePart1 = name.substring(0, length1);
      namePart2 = name.substring(length1);
    }
    if (namePart1.length() > length1) {
      namePart1 = namePart1.substring(0, length1);
    }
    namePart2 = namePart2.trim();
    if (namePart2.length() > length2) {
      namePart2 = namePart2.substring(0, length2);
    }

    return new String[] { namePart1, namePart2 };

  }

  public String[] doSplitName(String name1, String name2, int length1, int length2) {
    return splitName(name1, name2, length1, length2);
  }

  /**
   * Splits the street/address field into two values. Returns a String array of
   * length 2
   * 
   * @param addr
   * @param addr1
   * @param addr2
   * @param length1
   * @param length2
   */
  protected void splitAddress(Addr addr, String addr1, String addr2, int length1, int length2) {
    String address = addr1 + " " + (addr2 != null ? addr2 : "");
    String[] parts = address.split("[ ]");

    String adPart1 = "";
    String adPart2 = "";
    boolean part1Ok = false;
    for (String part : parts) {
      if ((adPart1 + " " + part).trim().length() > length1 || part1Ok) {
        part1Ok = true;
        adPart2 += " " + part;
      } else {
        adPart1 += " " + part;
      }
    }
    adPart1 = adPart1.trim();

    if (adPart1.length() == 0 && address.length() > length1) {
      adPart1 = address.substring(0, length1);
      adPart2 = address.substring(length1);
    }
    if (adPart1.length() > length1) {
      adPart1 = adPart1.substring(0, length1);
    }
    adPart2 = adPart2.trim();
    if (adPart2.length() > length1) {
      adPart2 = adPart2.substring(0, length1);
    }

    addr.setAddrTxt(adPart1);
    addr.setAddrTxt2(adPart2);

  }

  /**
   * Returns true if the CMR Search interface should retrieve also customers
   * with OB 93.
   * 
   * @return
   */
  public abstract boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry);

  /**
   * Called before saving of {@link Data} record.
   * 
   * @param entityManager
   * @param admin
   * @param cmrIssuingCntry
   */
  public abstract void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception;

  /**
   * Called before saving of {@link Data} record.
   * 
   * @param entityManager
   * @param data
   * @param cmrIssuingCntry
   */
  public abstract void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception;

  /**
   * Called before saving of {@link Addr} record.
   * 
   * @param entityManager
   * @param addr
   * @param cmrIssuingCntry
   */
  public abstract void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception;

  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    // noop. override on converter if needed
  }

  /**
   * Returns true if the customer names are implemented on the Address level,
   * false if on the main general tab
   * 
   * @return
   */
  public abstract boolean customerNamesOnAddress();

  /**
   * Returns true if the sequence no from the import will be used for the
   * address record or not
   * 
   * @return
   */
  public abstract boolean useSeqNoFromImport();

  /**
   * Returns true if the field specified should be skipped from the list of
   * Updates on the summary screen for Update requests
   * 
   * @param field
   * @return
   */
  public abstract boolean skipOnSummaryUpdate(String cntry, String field);

  /**
   * Called after performing all processes of Import CMR. Can be used to perform
   * last process changes
   * 
   * @param entityManager
   * @param admin
   * @param data
   */
  public abstract void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception;

  /**
   * Called when displaying the address record for Update requests. Returns the
   * address update mode indicator.<br>
   * <ul>
   * <li>(blank) - no changed</li>
   * <li>U - updated</li>
   * <li>N - new address</li>
   * </ul>
   * 
   * @param entityManager
   * @param addr
   * @return
   * @throws Exception
   */
  public abstract List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry);

  /**
   * Returns true if the handler determines that checklists are needed for this
   * country
   * 
   * @param cmrIssiungCntry
   * @return
   */
  public abstract boolean hasChecklist(String cmrIssiungCntry);

  /**
   * Filters the current list of addresses from the query. Used to remove
   * internal addresses from the UI
   * 
   * @param results
   */
  public void doFilterAddresses(List<AddressModel> results) {
    // noop
  }

  /**
   * The default method to handle import address. Override in the handler if
   * necessary.
   * 
   * @throws Exception
   */
  public ImportCMRModel handleImportAddress(EntityManager entityManager, HttpServletRequest request, ParamContainer params,
      ImportCMRModel searchModel) throws Exception {

    Long reqId = (Long) params.getParam("reqId");

    RequestEntryService reqEntryService = new RequestEntryService();
    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPk);

    if (admin == null) {
      LOG.debug("Cannot locate Admin record for Request ID " + reqId);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }

    FindCMRResultModel result = (FindCMRResultModel) params.getParam("results");
    if (result != null && result.getItems() != null) {
      String targetType = searchModel.getAddrType();
      String targetSeq = searchModel.getAddrSeq();
      FindCMRRecordModel cmr = null;
      for (FindCMRRecordModel record : result.getItems()) {
        if (targetType.equals(record.getCmrAddrTypeCode()) && targetSeq.equals(record.getCmrAddrSeq())) {
          cmr = record;
          break;
        }
      }
      if (cmr != null) {
        // check if this exists on the request
        String sql = ExternalizedQuery.getSql("IMPORT.CHECK_ADDR");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", reqId);
        query.setParameter("SAP_NO", cmr.getCmrSapNumber());
        if (query.exists()) {
          throw new CmrException(MessageUtil.ERROR_ADDRESS_ALREADY_EXISTS);
        }

        AddrPK pk = new AddrPK();
        pk.setReqId(reqId);
        pk.setAddrType(cmr.getCmrAddrTypeCode());
        pk.setAddrSeq(cmr.getCmrAddrSeq());

        Addr existingAddr = entityManager.find(Addr.class, pk);
        if (existingAddr != null) {
          throw new CmrException(MessageUtil.ERROR_ADDRESS_ALREADY_EXISTS);
        }

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
          LOG.debug("Importing " + cmr.getCmrAddrTypeCode() + " address from CMR " + cmr.getCmrNum() + " Issued By: " + cmr.getCmrIssuedBy());
          Addr addr = new Addr();
          AddrPK addrPk = new AddrPK();
          addrPk.setReqId(reqId);
          String type = cmr.getCmrAddrTypeCode();
          addrPk.setAddrType(type);
          addrPk.setAddrSeq(cmr.getCmrAddrSeq());
          addr.setId(addrPk);

          addr.setCity1(cmr.getCmrCity());
          addr.setCity2(cmr.getCmrCity2());
          addr.setStateProv(cmr.getCmrState());
          addr.setPostCd(cmr.getCmrPostalCode());
          addr.setLandCntry(cmr.getCmrCountryLanded());
          addr.setSapNo(cmr.getCmrSapNumber());
          addr.setAddrStdResult("X");
          addr.setRdcCreateDt(cmr.getCmrRdcCreateDate());
          addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());
          addr.setCounty(cmr.getCmrCountyCode());
          addr.setCountyName(cmr.getCmrCounty());
          addr.setAddrTxt(cmr.getCmrStreetAddress());
          addr.setImportInd(CmrConstants.YES_NO.Y.toString());

          addr.setCustPhone(cmr.getCmrCustPhone());
          addr.setCustFax(cmr.getCmrCustFax());
          addr.setCustLangCd(cmr.getCmrBusNmLangCd());
          addr.setTransportZone(cmr.getCmrTransportZone());
          addr.setPoBox(cmr.getCmrPOBox());
          addr.setPoBoxCity(cmr.getCmrPOBoxCity());
          addr.setPoBoxPostCd(cmr.getCmrPOBoxPostCode());
          addr.setBldg(cmr.getCmrBldg());
          addr.setFloor(cmr.getCmrFloor());
          addr.setOffice(cmr.getCmrOffice());
          addr.setDept(cmr.getCmrDept());
          addr.setParCmrNo(cmr.getCmrNum());
          setAddressValuesOnImport(addr, admin, cmr, cmr.getCmrNum());

          reqEntryService.createEntity(addr, entityManager);

          transaction.commit();
        } catch (Exception e) {
          if (transaction.isActive()) {
            transaction.rollback();
          }
          throw e;
        }

      } else {
        throw new CmrException(MessageUtil.ERROR_CANNOT_FIND_ADDRESS, targetType, targetSeq);
      }
    }
    ImportCMRModel retModel = new ImportCMRModel();
    retModel.setReqId(reqId);
    retModel.setProspect(false);

    return retModel;

  }

  /**
   * Returns the map of FieldID + Field Name that is used by this handler
   * 
   * @return
   */
  public Map<String, String> getUIFieldIdMap() {
    return null;
  }

  /**
   * Returns a clone of the given address, assigning the address type
   * 
   * @param source
   * @return
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public FindCMRRecordModel cloneAddress(FindCMRRecordModel source, String addrType) throws Exception {
    FindCMRRecordModel target = new FindCMRRecordModel();
    PropertyUtils.copyProperties(target, source);
    target.setCmrAddrTypeCode(addrType);
    return target;
  }

  /**
   * Secondary check on handler level whether an address changed or not.
   * 
   * @param entityManager
   * @param addr
   * @param cmrIssuingCntry
   * @param computedChangeInd
   * @return
   */
  public boolean isAddressChanged(EntityManager entityManager, Addr addr, String cmrIssuingCntry, boolean computedChangeInd) {
    // by default, return the original computed changed ind
    return computedChangeInd;
  }

  /**
   * gets the mapped 2-digit ISO country code given the country description
   * 
   * @param entityManager
   * @param desc
   * @return
   */
  protected String getCountryCode(EntityManager entityManager, String desc) {
    LOG.debug("Retrieving country code for " + desc);
    if (entityManager == null) {
      return null;
    }
    String sql = ExternalizedQuery.getSql("GEN.GET_COUNTRY_CD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DESC", desc.toUpperCase());
    query.setParameter("DESC2", "%" + desc.toUpperCase() + "%");
    if (desc.length() > 6) {
      query.setParameter("DESC3", "%" + desc.toUpperCase().substring(0, 7) + "%");
    } else {
      query.setParameter("DESC3", "%" + desc.toUpperCase() + "%");
    }
    if (desc.length() > 5) {
      query.setParameter("DESC4", "%" + desc.toUpperCase().substring(0, 6) + "%");
    } else {
      query.setParameter("DESC4", "%" + desc.toUpperCase() + "%");
    }
    query.setParameter("DESC5", desc != null ? desc.toUpperCase().trim() : "");
    List<String> codes = query.getResults(String.class);
    if (codes != null && !codes.isEmpty()) {
      return codes.get(0);
    }
    return null;
  }

  /**
   * Gets the mapped 2 or 3-digit state code given the country code and state
   * description
   * 
   * @param entityManager
   * @param cntryCd
   * @param desc
   * @return
   */
  protected String getStateCode(EntityManager entityManager, String cntryCd, String desc) {
    LOG.debug("Retrieving state code for " + desc + " under " + cntryCd);
    if (entityManager == null || (cntryCd != null && cntryCd.length() > 3)) {
      return null;
    }
    String sql = ExternalizedQuery.getSql("GEN.GET_STATE_CD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cntryCd);
    query.setParameter("DESC", desc.toUpperCase());
    query.setParameter("DESC2", "%" + desc.toUpperCase() + "%");
    if (desc.length() > 4) {
      query.setParameter("DESC3", "%" + desc.toUpperCase().substring(0, 5) + "%");
    } else {
      query.setParameter("DESC3", "%" + desc.toUpperCase() + "%");
    }
    if (desc.length() > 3) {
      query.setParameter("DESC4", "%" + desc.toUpperCase().substring(0, 4) + "%");
    } else {
      query.setParameter("DESC4", "%" + desc.toUpperCase() + "%");
    }
    query.setParameter("DESC5", desc != null ? desc.toUpperCase().trim() : "");
    List<String> codes = query.getResults(String.class);
    if (codes != null && !codes.isEmpty()) {
      return codes.get(0);
    }
    return null;
  }

  public ApprovalReq handleBPMANAGERApproval(EntityManager entityManager, long reqId, ApprovalReq approver, DefaultApprovals defaultApprovals,
      DefaultApprovalRecipients recipients, AppUser user, RequestEntryModel model) throws CmrException, SQLException {
    return null;
  }

  public String[] dividingCustName1toName2(String name1, String name2) {
    return null;
  }

  public void doAddMassUpdtValidation(TemplateValidation validation, String country) {
    // noop
  }

  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    return null;
  }

  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  /**
   * Generate the sequence number when a new address is created on the request.
   * 
   * @param entityManager
   * @param addrType
   * @param reqId
   * @return seqno
   */
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    return null;
  }

  /**
   * Generate the sequence number when a new address is created on the request
   * using the copy.
   * 
   * @param entityManager
   * @param addrType
   * @param reqId
   * @return seqno
   */
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    return null;
  }

  // CreateCMR 2.0 methods

  /**
   * Called during execution of global buying group matching. Other dependent
   * values on LDE should be set here
   * 
   * @param entityManager
   * @param requestData
   * @param ldeField
   * @param ldeValue
   */
  public void setGBGValues(EntityManager entityManager, RequestData requestData, String ldeField, String ldeValue) {
    // noop
  }

  /**
   * Returns the model to use for the request entry page for the given country
   * 
   * @return
   */
  public BaseV2RequestModel getAutomationRequestModel(String country, String reqType) {
    return null;
  }

  /**
   * Do some altering on the {@link RequestEntryModel} object before saving the
   * data for v2
   * 
   * @param model
   */
  public void alterModelBeforeSave(RequestEntryModel model) {
    // noop
  }

  /**
   * After the initial save of the base {@link RequestEntryModel}, perform
   * several transactions on the {@link Admin}, {@link Data}, or ZS01
   * {@link Addr} records created
   * 
   * @param entityManager
   * @param model
   * @param request
   */
  public void saveV2Entries(EntityManager entityManager, RequestEntryModel model, HttpServletRequest request, Admin admin, Data data, Addr soldTo)
      throws Exception {
    // noop
  }

  /**
   * Called before the import CMR function. Recreates the original v2 model for
   * the country for final overrides
   * 
   * @param entityManager
   * @param model
   * @param requestData
   * @return
   */
  public BaseV2RequestModel recreateModelFromRequest(EntityManager entityManager, RequestData requestData) {
    return null;
  }

  /**
   * Called after the global {@link ImportCMRElement} functionality to cater for
   * processes to be done after the initial import of CMR data to the request
   * 
   * @param entityManager
   * @param requestData
   */
  public void doAfterImportCMRFromAutomation(EntityManager entityManager, BaseV2RequestModel model, RequestData requestData) {
    // noop
  }

  public abstract boolean isNewMassUpdtTemplateSupported(String issuingCountry);

  public void convertCoverageRulesInput(EntityManager entityManager, com.ibm.cio.cmr.utils.coverage.objects.CoverageInput rulesCovInput,
      Addr mainAddr, RequestEntryModel data) {
    CoverageInput input = new CoverageInput();
    try {
      PropertyUtils.copyProperties(input, rulesCovInput);
      input.setSORTL(rulesCovInput.getSORTL());
      convertCoverageInput(entityManager, input, mainAddr, data);
      PropertyUtils.copyProperties(rulesCovInput, input);
      rulesCovInput.setSORTL(input.getSORTL());
    } catch (Exception e) {
      // noop
    }

  }

  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    // NO OP
  }

  protected static String validateColValFromCell(XSSFCell cell) {
    String colVal = "";
    if (cell != null) {
      switch (cell.getCellTypeEnum()) {
      case STRING:
        colVal = cell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = cell.getNumericCellValue();
        if (nvalue > 0) {
          colVal = "" + nvalue;
        }
        break;
      default:
        break;
      }
    }
    return colVal;
  }

}
