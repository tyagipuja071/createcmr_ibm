/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

//import java.sql.SQLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.ApprovalReq;
import com.ibm.cio.cmr.request.entity.ApprovalReqPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DefaultApprovalRecipients;
import com.ibm.cio.cmr.request.entity.DefaultApprovals;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CRISServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.cris.CRISAccount;
import com.ibm.cmr.services.client.cris.CRISAddress;
import com.ibm.cmr.services.client.cris.CRISCompany;
import com.ibm.cmr.services.client.cris.CRISEstablishment;
import com.ibm.cmr.services.client.cris.CRISFullAccountRequest;
import com.ibm.cmr.services.client.cris.CRISQueryRequest;
import com.ibm.cmr.services.client.cris.CRISQueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * 
 */
public class JPHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(JPHandler.class);

  private static final String[] JP_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "ISU", "ClientTier", "LocalTax1", "SensitiveFlag" };

  private CRISAccount currentAccount = null;

  /**
   * Mapping of Address Type from RDc to CRIS
   */
  private static final Map<String, String> RDC_TO_LEGACY_ADDR_TYPE_MAP = new HashMap<String, String>() {
    private static final long serialVersionUID = 5812910531337191190L;

    {
      put("ZS01", "3");
      put("ZI01", "7");
      put("ZP01", "2");
    }
  };

  /**
   * Mapping Address Type from CRIS to CreateCMR
   */
  private static final Map<String, String> LEGACY_TO_CREATECMR_TYPE_MAP = new HashMap<String, String>() {

    private static final long serialVersionUID = 8216050354906133776L;

    {
      put("3", "ZS01");
      put("7", "ZI01");
      put("2", "ZP01");
      put("1", "ZS02");
      put("6", "ZI02");
      put("E", "ZI03");
      put("A", "ZP02");
      put("B", "ZP03");
      put("C", "ZP04");
      put("D", "ZP05");
      put("F", "ZP06");
      put("G", "ZP07");
      put("4", "ZP09");
      put("H", "ZP08");
    }
  };

  /**
   * Handle import of Company/Estab only
   */
  @Override
  public ImportCMRModel handleImportAddress(EntityManager entityManager, HttpServletRequest request, ParamContainer params,
      ImportCMRModel searchModel) throws Exception {

    // this is called when Estab + Company or Company is imported
    String addrType = searchModel.getAddrType();

    RequestEntryModel reqentry = (RequestEntryModel) params.getParam("model");

    CRISCompany company = null;
    CRISEstablishment establishment = null;
    if ("ZC01".equals(addrType)) {
      company = findCompanyFromCRIS(searchModel.getCmrNum());
      if (company == null) {
        throw new CmrException(MessageUtil.ERROR_RETRIEVE_COMPANY_DATA);
      }

    } else if ("ZE01".equals(addrType)) {
      establishment = findEstablishmentFromCRIS(searchModel.getCmrNum());
      if (establishment != null) {
        company = findCompanyFromCRIS(establishment.getCompanyNo());
        if (company == null) {
          throw new CmrException(MessageUtil.ERROR_RETRIEVE_COMPANY_DATA);
        }
      }
    }

    String custType = reqentry.getCustType();
    // String reqType = reqentry.getReqType();

    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    try {

      // update first the cust type value
      AdminPK adminPk = new AdminPK();
      adminPk.setReqId(reqentry.getReqId());
      Admin admin = entityManager.find(Admin.class, adminPk);

      DataPK dataPK = new DataPK();
      dataPK.setReqId(reqentry.getReqId());
      Data data = entityManager.find(Data.class, dataPK);
      DataPK dataRdcPk = new DataPK();
      dataRdcPk.setReqId(reqentry.getReqId());
      DataRdc dataRdc = entityManager.find(DataRdc.class, dataRdcPk);
      if (dataRdc == null) {
        dataRdc = new DataRdc();
        dataRdc.setId(dataRdcPk);
      }
      ScorecardPK scorecardPk = new ScorecardPK();
      scorecardPk.setReqId(reqentry.getReqId());
      Scorecard scorecard = entityManager.find(Scorecard.class, scorecardPk);

      if (admin != null) {
        LOG.debug("Setting Customer Type for Request ID " + reqentry.getReqId() + " to " + custType);
        PropertyUtils.copyProperties(admin, reqentry);
        admin.setCustType(custType);
        entityManager.merge(admin);
      }
      if (data != null) {

        if (custType != null && custType.contains("A")) {
          LOG.debug("Saving page data for Request ID " + reqentry.getReqId());
          PropertyUtils.copyProperties(data, reqentry);

          // 1722830 - when processor import Company/Estab address, JSIC will
          // not change if there is Account address for Create Request in some
          // scenarios.
          if ("C".equalsIgnoreCase(admin.getReqType())) {
            String custSubGrp = data.getCustSubGrp() != null ? data.getCustSubGrp() : "";
            switch (custSubGrp) {
            case "BPWPQ":
            case "ISOCU":
            case "BCEXA":
            case "BFKSC":
              data.setJsicCd(establishment != null && establishment.getJsic() != null ? establishment.getJsic().trim()
                  : company != null && company.getJsic() != null ? company.getJsic().trim() : null);
              break;
            case "":
            default:
              // only for requester
              if ("DRA".equalsIgnoreCase(admin.getReqStatus())) {
                data.setJsicCd(establishment != null && establishment.getJsic() != null ? establishment.getJsic().trim()
                    : company != null && company.getJsic() != null ? company.getJsic().trim() : null);
              }
              break;
            }
          } else if ("U".equalsIgnoreCase(admin.getReqType())) {
            data.setJsicCd(establishment != null && establishment.getJsic() != null ? establishment.getJsic().trim()
                : company != null && company.getJsic() != null ? company.getJsic().trim() : null);
          }

          // data.setCsBo(company.getSBO()== null ? company.getSBO() :
          // company.getSBO().trim());
          if (admin != null && admin.getReqType() != null && admin.getReqType().equals("U")) {
            data.setDunsNo(company != null ? company.getDunsNo() : null);
          }
          PropertyUtils.copyProperties(dataRdc, data);
          entityManager.merge(data);
          dataRdc.setId(dataPK);
          entityManager.merge(dataRdc);
          entityManager.flush();
        } else {
          // TODO clear the data fields here
          LOG.debug("Saving page data for Request ID " + reqentry.getReqId());
          PropertyUtils.copyProperties(data, reqentry);
          data.setJsicCd(establishment != null && establishment.getJsic() != null ? establishment.getJsic().trim()
              : company != null && company.getJsic() != null ? company.getJsic().trim() : null);
          // data.setCsBo(company.getSBO()== null ? company.getSBO() :
          // company.getSBO().trim());
          if (reqentry != null && reqentry.getReqType() != null && reqentry.getReqType().equals("U")) {
            if (establishment != null && establishment.getCompanyCd() != null && establishment.getCompanyCd().equals("AA")) {
              data.setCustGrp("IBMTP");
            } else if ("ZC01".equals(addrType)) {
              data.setCustGrp("IBMTP");
            } else {
              data.setCustGrp("SUBSI");
            }
          }
          if (admin != null && admin.getReqType() != null && admin.getReqType().equals("U")) {
            data.setDunsNo(company != null ? company.getDunsNo() : null);
          }
          PropertyUtils.copyProperties(dataRdc, data);
          entityManager.merge(data);
          dataRdc.setId(dataPK);
          entityManager.merge(dataRdc);
          entityManager.flush();
        }
      }
      if (scorecard != null) {
        AppUser user = AppUser.getUser(request);

        LOG.debug("Saving scorecard for Request ID " + reqentry.getReqId());
        scorecard.setFindCmrUsrNm(user.getBluePagesName());
        scorecard.setFindCmrUsrId(user.getIntranetId());
        scorecard.setFindCmrTs(SystemUtil.getCurrentTimestamp());
        scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Required);
        if ("X".equals(addrType)) {
          scorecard.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
          // scorecard.setFindCmrRejReason("No results from the search.");
        } else {
          scorecard.setFindCmrResult(CmrConstants.RESULT_ACCEPTED);
        }
        entityManager.merge(scorecard);
      }

      Addr addr = null;
      AddrPK addrPk = null;

      AddrRdc rdc = null;
      AddrPK rdcpk = null;
      StringBuilder sbPhone = new StringBuilder();
      if (company != null) {
        removeCurrentAddr(entityManager, reqentry.getReqId(), "ZC01");
        LOG.debug("Adding Company Address to Request ID " + reqentry.getReqId());
        addrPk = new AddrPK();
        addrPk.setReqId(reqentry.getReqId());
        addrPk.setAddrType("ZC01");
        addrPk.setAddrSeq("C");
        addr = new Addr();
        addr.setId(addrPk);
        // if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType) ||
        // (!StringUtils.isEmpty(custType) && !custType.contains("C"))) {
        addr.setParCmrNo(company.getCompanyNo());
        // }
        // mapping to companyNo
        addr.setCity2(company.getCompanyNo() == null ? company.getCompanyNo() : company.getCompanyNo().trim());
        addr.setCustNm1(company.getNameKanji() == null ? company.getNameKanji()
            : company.getNameKanji().trim().length() > 23 ? company.getNameKanji().trim().substring(0, 23) : company.getNameKanji().trim());
        String custNm4 = null;
        if (company.getNameKana() != null && company.getNameKana().trim().length() > 23) {
          custNm4 = company.getNameKana().trim().substring(23);
        }
        addr.setCustNm4(company.getNameKana() == null ? company.getNameKana()
            : company.getNameKana().trim().length() > 23 ? company.getNameKana().trim().substring(0, 23) : company.getNameKana().trim());
        addr.setPoBoxCity(custNm4);
        addr.setCustNm3(company.getNameAbbr() == null ? company.getNameAbbr() : company.getNameAbbr().trim());
        addr.setBldg(company.getBldg() == null ? company.getBldg() : company.getBldg().trim());
        addr.setDplChkResult(CmrConstants.ADDRESS_Not_Required);
        addr.setImportInd(CmrConstants.YES_NO.Y.toString());
        String addrTxt = null;
        if (company.getAddress() != null && company.getAddress().trim().length() > 23) {
          addrTxt = company.getAddress().trim().substring(23);
        }
        addr.setAddrTxt(company.getAddress() == null ? company.getAddress()
            : company.getAddress().trim().length() > 23 ? company.getAddress().trim().substring(0, 23) : company.getAddress().trim());
        addr.setAddrTxt2(addrTxt);
        sbPhone = new StringBuilder();
        sbPhone.append(!StringUtils.isEmpty(company.getPhoneShi()) ? company.getPhoneShi() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(company.getPhoneKyo()) ? company.getPhoneKyo() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(company.getPhoneBango()) ? company.getPhoneBango() : "");
        addr.setCustPhone(sbPhone.toString());
        addr.setLandCntry("JP");
        addr.setLocationCode(company.getLocCode() == null ? company.getLocCode() : company.getLocCode().trim());
        addr.setPostCd(company.getPostCode() == null ? company.getPostCode() : company.getPostCode().trim());
        addr.setCompanySize(company.getEmployeeSize());
        entityManager.persist(addr);

        rdc = new AddrRdc();
        rdcpk = new AddrPK();
        PropertyUtils.copyProperties(rdc, addr);
        PropertyUtils.copyProperties(rdcpk, addr.getId());
        rdc.setId(rdcpk);

        entityManager.persist(rdc);

        entityManager.flush();
      }

      if ("C".equals(custType)) {
        // TODO remove all address and data fields here
        removeOtherAddresses(entityManager, reqentry.getReqId(), "ZC01");
      } else {
        // normal import
        if (establishment != null) {
          removeCurrentAddr(entityManager, reqentry.getReqId(), "ZE01");
          LOG.debug("Adding Establishment Address to Request ID " + reqentry.getReqId());
          addrPk = new AddrPK();
          addrPk.setReqId(reqentry.getReqId());
          addrPk.setAddrType("ZE01");
          addrPk.setAddrSeq("E");
          addr = new Addr();
          addr.setId(addrPk);
          // if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType) ||
          // (!StringUtils.isEmpty(custType) && !custType.contains("E"))) {
          addr.setParCmrNo(establishment.getEstablishmentNo());
          // }
          // mapping to companyNo
          addr.setCity2(company.getCompanyNo() == null ? establishment.getCompanyNo() : establishment.getCompanyNo().trim());
          // Mapping to establishmentNo
          addr.setDivn(establishment.getEstablishmentNo() == null ? establishment.getEstablishmentNo() : establishment.getEstablishmentNo().trim());
          addr.setCustNm1(establishment.getNameKanji() == null ? establishment.getNameKanji()
              : establishment.getNameKanji().trim().length() > 23 ? establishment.getNameKanji().trim().substring(0, 23)
                  : establishment.getNameKanji().trim());
          String estCustNm4 = null;
          if (establishment.getAddress() != null && establishment.getAddress().trim().length() > 23) {
            estCustNm4 = establishment.getAddress().trim().substring(23);
          }
          addr.setCustNm4(establishment.getNameKana() == null ? establishment.getNameKana()
              : establishment.getNameKana().trim().length() > 23 ? establishment.getNameKana().trim().substring(0, 23)
                  : establishment.getNameKana().trim());
          addr.setPoBoxCity(estCustNm4);
          addr.setCustNm3(establishment.getNameAbbr() == null ? establishment.getNameAbbr() : establishment.getNameAbbr().trim());
          addr.setBldg(establishment.getBldg() == null ? establishment.getBldg() : establishment.getBldg().trim());
          String estAddrTxt = null;
          if (establishment.getAddress() != null && establishment.getAddress().trim().length() > 23) {
            estAddrTxt = establishment.getAddress().trim().substring(23);
          }
          addr.setAddrTxt(establishment.getAddress() == null ? establishment.getAddress()
              : establishment.getAddress().trim().length() > 23 ? establishment.getAddress().trim().substring(0, 23)
                  : establishment.getAddress().trim());
          addr.setAddrTxt2(estAddrTxt);
          addr.setDplChkResult(CmrConstants.ADDRESS_Not_Required);
          addr.setImportInd(CmrConstants.YES_NO.Y.toString());
          sbPhone = new StringBuilder();
          sbPhone.append(!StringUtils.isEmpty(establishment.getPhoneShi()) ? establishment.getPhoneShi() : "");
          sbPhone.append(sbPhone.length() > 0 ? "-" : "")
              .append(!StringUtils.isEmpty(establishment.getPhoneKyo()) ? establishment.getPhoneKyo() : "");
          sbPhone.append(sbPhone.length() > 0 ? "-" : "")
              .append(!StringUtils.isEmpty(establishment.getPhoneBango()) ? establishment.getPhoneBango() : "");
          addr.setCustPhone(sbPhone.toString());
          addr.setLandCntry("JP");
          addr.setLocationCode(establishment.getLocCode() == null ? establishment.getLocCode() : establishment.getLocCode().trim());
          addr.setPostCd(establishment.getPostCode() == null ? establishment.getPostCode() : establishment.getPostCode().trim());
          addr.setCompanySize(company.getEmployeeSize());
          addr.setEstabFuncCd(establishment.getFuncCode() == null ? establishment.getFuncCode() : establishment.getFuncCode().trim());
          entityManager.persist(addr);

          rdc = new AddrRdc();
          rdcpk = new AddrPK();
          PropertyUtils.copyProperties(rdc, addr);
          PropertyUtils.copyProperties(rdcpk, addr.getId());
          rdc.setId(rdcpk);

          entityManager.persist(rdc);

          entityManager.flush();

        }
      }

      // added to remove the Establishment tab when it exists and selected
      // record includes E
      if (custType != null && custType.contains("E")) {
        addrPk = new AddrPK();
        addrPk.setReqId(reqentry.getReqId());
        addrPk.setAddrType("ZE01");
        addrPk.setAddrSeq("E");
        Addr estab = entityManager.find(Addr.class, addrPk);
        if (estab != null) {
          if (!StringUtils.isEmpty(estab.getParCmrNo())) {
            LOG.debug("Establishment with parent tagging found, clearing Estab No.");
            estab.setParCmrNo(null);
            estab.setImportInd("N");
            entityManager.merge(estab);
            entityManager.flush();
          }
        }
      }

      if ("X".equals(addrType)) {
        removeParentReferences(entityManager, reqentry.getReqId());
      }

      transaction.commit();
    } catch (Exception e) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    }

    return searchModel;
  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    FindCMRRecordModel mainRecord = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
    boolean onlyCrisAddrFlag = false;
    if (mainRecord == null) {
      mainRecord = new FindCMRRecordModel();
      mainRecord.setCmrAddrType(searchModel.getAddrType());
      mainRecord.setCmrIssuedBy(searchModel.getCmrIssuingCntry());
      mainRecord.setCmrNum(searchModel.getCmrNum());
      mainRecord.setCmrAddrTypeCode(StringUtils.isNotEmpty(searchModel.getAddrType()) ? searchModel.getAddrType() : "ZS01");
      onlyCrisAddrFlag = true;
    }

    Set<String> addedRecords = new HashSet<>();
    this.currentAccount = findAccountFromCRIS(searchModel.getCmrNum());
    if (this.currentAccount == null) {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }
    CRISCompany company = this.currentAccount.getParentCompany();
    if (company == null) {
      throw new CmrException(MessageUtil.ERROR_RETRIEVE_COMPANY_DATA);
    }
    CRISEstablishment establishment = this.currentAccount.getParentEstablishment();
    if (establishment == null) {
      throw new CmrException(MessageUtil.ERROR_RETRIEVE_ESTABLISHMENT_DATA);
    }
    if (reqEntry.getReqType() != null && reqEntry.getReqType().equals("U")) {
      mainRecord.setCmrDuns(company.getDunsNo());
    }
    mainRecord.setJsic(this.currentAccount.getJSIC());
    mainRecord.setSbo(this.currentAccount.getSBO());
    mainRecord.setCsbo(this.currentAccount.getCSBO());
    mainRecord.setEducAllowanceGrp(this.currentAccount.getEducAllowanceGrp());
    mainRecord.setCRSCode(this.currentAccount.getCRSCode());
    // mainRecord.setSR(this.currentAccount.getSR());
    mainRecord.setBillingProcessCode(this.currentAccount.getBillingProcessCode());
    mainRecord.setInvoiceSplitCode(this.currentAccount.getInvoiceSplitCode());
    mainRecord.setCreditToCustNo(this.currentAccount.getCreditToCustNo());
    mainRecord.setBillingCustNo(this.currentAccount.getBillingCustNo());
    mainRecord.setTier2(this.currentAccount.getTier2());
    mainRecord.setSiInd(this.currentAccount.getSIInd());
    mainRecord.setIinInd(this.currentAccount.getINNInd());
    mainRecord.setLeasingCompanyIndc(this.currentAccount.getLeasingCompanyInd());
    mainRecord.setChannelCd(this.currentAccount.getChannelCode());
    mainRecord.setCreditCd(this.currentAccount.getCARCode());
    mainRecord.setValueAddRem(this.currentAccount.getValueAddRem());
    // mainRecord.setIcmsInd(this.currentAccount.getSalesTeamCode());govOfficeDivCode
    mainRecord.setGovOfficeDivCode(this.currentAccount.getGovOfficeDivCode());
    mainRecord.setCsDiv(this.currentAccount.getCSDiv());
    mainRecord.setOemInd(this.currentAccount.getOEMInd());
    mainRecord.setRepTeamMemberNo(this.currentAccount.getSR() != null && this.currentAccount.getSR().length() > 5
        ? this.currentAccount.getSR().substring(0, 5) : this.currentAccount.getSR());
    mainRecord.setSalesTeamCd(this.currentAccount.getDealerNo() != null && this.currentAccount.getDealerNo().length() > 5
        ? this.currentAccount.getDealerNo().substring(0, 5) : this.currentAccount.getDealerNo());
    mainRecord.setNameKanji(this.currentAccount.getNameKanji());
    if (reqEntry.getReqType() != null && reqEntry.getReqType().equals("U")) {
      if (establishment != null && establishment.getCompanyCd() != null) {
        mainRecord.setCompanyCd(establishment.getCompanyCd());
      }
    }
    mainRecord.setSboSub(this.currentAccount.getSboSub());
    mainRecord.setAttach(this.currentAccount.getAttach());
    if (onlyCrisAddrFlag) {
      List<FindCMRRecordModel> mainRecordList = new ArrayList<FindCMRRecordModel>();
      mainRecordList.add(mainRecord);
      source.setItems(mainRecordList);
    }

    List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
    // first navigate through the results, only extract
    // contract/billing/installing

    CRISAddress crisAddr = null;
    StringBuilder sbPhone = new StringBuilder();
    for (FindCMRRecordModel sourceRecord : source.getItems()) {
      crisAddr = getAddress(sourceRecord.getCmrAddrTypeCode());

      if (crisAddr != null) {
        // there is a legacy equivalent

        sourceRecord.setCmrAddrSeq(crisAddr.getAddrSeq());
        sourceRecord.setParentCMRNo(this.currentAccount.getAccountNo());
        sourceRecord.setCmrStreetAddress(crisAddr.getAddress());
        sourceRecord.setCmrName1Plain(crisAddr.getCompanyNameKanji());// this.currentAccount.getNameKanji()
        sourceRecord.setCmrName2Plain(crisAddr.getCompanyNameKana());// this.currentAccount.getNameKana()
        sourceRecord.setCmrName3(this.currentAccount.getNameAbbr());

        sourceRecord.setCmrDept(crisAddr.getDept());
        sourceRecord.setCmrOffice(crisAddr.getEstablishmentNameKanji());

        sbPhone = new StringBuilder();
        sbPhone.append(!StringUtils.isEmpty(crisAddr.getPhoneShi()) ? crisAddr.getPhoneShi() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(crisAddr.getPhoneKyo()) ? crisAddr.getPhoneKyo() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(crisAddr.getPhoneBango()) ? crisAddr.getPhoneBango() : "");
        sourceRecord.setCmrCustPhone(sbPhone.toString());

        sbPhone = new StringBuilder();
        sbPhone.append(!StringUtils.isEmpty(crisAddr.getFaxShi()) ? crisAddr.getFaxShi() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(crisAddr.getFaxKyo()) ? crisAddr.getFaxKyo() : "");
        sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(crisAddr.getFaxBango()) ? crisAddr.getFaxBango() : "");
        sourceRecord.setCmrCustFax(sbPhone.toString());
        sourceRecord.setCmrPostalCode(
            crisAddr.getPostCode() != null && crisAddr.getPostCode().trim().length() > 0 ? crisAddr.getPostCode() : sourceRecord.getCmrPostalCode());
        sourceRecord.setCmrBldg(crisAddr.getBldg());
        sourceRecord.setCmrName4(crisAddr.getContact());
        sourceRecord.setCustGroup(crisAddr.getCustGrp());
        sourceRecord.setCustClass(crisAddr.getCustClass());
        sourceRecord.setCmrCountryLanded("JP");
        sourceRecord.setSbo(this.currentAccount.getSBO());
        sourceRecord.setLocationNo(this.currentAccount.getLocCode());
        for (String adu : crisAddr.getAddrType().split("")) {
          String cmrAddrType = LEGACY_TO_CREATECMR_TYPE_MAP.get(adu);
          if (!StringUtils.isEmpty(adu) && !StringUtils.isEmpty(cmrAddrType) && !addedRecords.contains(cmrAddrType + "/" + crisAddr.getAddrSeq())) {

            FindCMRRecordModel copy = new FindCMRRecordModel();
            PropertyUtils.copyProperties(copy, sourceRecord);

            copy.setCmrAddrTypeCode(cmrAddrType);
            if (!cmrAddrType.equals(sourceRecord.getCmrAddrTypeCode())) {
              // clear sap no here
              copy.setCmrSapNumber(null);
            }
            LOG.debug("Adding " + copy.getCmrAddrTypeCode() + "/" + copy.getCmrAddrSeq() + " to the request.");
            addedRecords.add(copy.getCmrAddrTypeCode() + "/" + copy.getCmrAddrSeq());
            converted.add(copy);
          }
        }
      }
      // for RDC addr, if do not mapping to cris, do not import this type
      // else {
      // // try to map to rdc
      // sourceRecord.setCmrAddrSeq("1");
      // sourceRecord.setParentCMRNo(this.currentAccount.getAccountNo());
      // sourceRecord.setCmrName1Plain(sourceRecord.getCmrIntlName12());
      // sourceRecord.setCmrName2Plain(sourceRecord.getCmrOtherIntlBusinessName());
      // sourceRecord.setCmrName3(this.currentAccount.getNameAbbr());
      // // record.setCmrBldg(?);
      // addedRecords.add(sourceRecord.getCmrAddrTypeCode() + "/" +
      // sourceRecord.getCmrAddrSeq());
      // sourceRecord.setSbo(this.currentAccount.getSBO());
      // sourceRecord.setLocationNo(this.currentAccount.getLocCode());
      // // if account none this addr type, will use the PostCode which belong
      // // to Parent Establishment.
      // // this mean CRIS none this addr type, so need add a new addr type.
      // no
      // // matter create/update.
      // sourceRecord.setCmrPostalCode(null);//
      // this.currentAccount.getParentEstablishment().getPostCode()
      //
      // converted.add(sourceRecord);
      // }
    }

    FindCMRRecordModel record = null;
    // add the legacy addresses not in Rdc
    if (this.currentAccount != null && this.currentAccount.getAddresses() != null) {
      Collection<String> legacyValues = RDC_TO_LEGACY_ADDR_TYPE_MAP.values();
      String cmrType = null;
      for (CRISAddress legacyAddr : this.currentAccount.getAddresses()) {
        for (String adu : legacyAddr.getAddrType().split("")) {
          if (onlyCrisAddrFlag && adu != "3" || !StringUtils.isEmpty(adu) && !legacyValues.contains(adu)) {
            // this is an address on CRIS only, add to the request
            LOG.debug("Adding ADU " + adu + " to the request.");
            cmrType = LEGACY_TO_CREATECMR_TYPE_MAP.get(adu);
            if (cmrType != null && !addedRecords.contains(cmrType + "/" + legacyAddr.getAddrSeq())) {
              record = new FindCMRRecordModel();
              record.setCmrAddrTypeCode(cmrType);
              record.setCmrAddrSeq(legacyAddr.getAddrSeq());
              record.setParentCMRNo(this.currentAccount.getAccountNo());
              record.setCmrStreetAddress(legacyAddr.getAddress());
              record.setCmrName1Plain(legacyAddr.getCompanyNameKanji()); // this.currentAccount.getNameKanji()
              record.setCmrName2Plain(legacyAddr.getCompanyNameKana()); // this.currentAccount.getNameKana()
              record.setCmrName3(this.currentAccount.getNameAbbr());
              record.setCmrBldg(legacyAddr.getBldg());
              record.setCmrCountryLanded("JP");
              sbPhone = new StringBuilder();
              sbPhone.append(!StringUtils.isEmpty(legacyAddr.getPhoneShi()) ? legacyAddr.getPhoneShi() : "");
              sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(legacyAddr.getPhoneKyo()) ? legacyAddr.getPhoneKyo() : "");
              sbPhone.append(sbPhone.length() > 0 ? "-" : "")
                  .append(!StringUtils.isEmpty(legacyAddr.getPhoneBango()) ? legacyAddr.getPhoneBango() : "");
              record.setCmrCustPhone(sbPhone.toString());

              sbPhone = new StringBuilder();
              sbPhone.append(!StringUtils.isEmpty(legacyAddr.getFaxShi()) ? legacyAddr.getFaxShi() : "");
              sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(legacyAddr.getFaxKyo()) ? legacyAddr.getFaxKyo() : "");
              sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(legacyAddr.getFaxBango()) ? legacyAddr.getFaxBango() : "");
              record.setCmrCustFax(sbPhone.toString());

              record.setCmrPostalCode(legacyAddr.getPostCode());
              record.setSbo(this.currentAccount.getSBO());
              record.setLocationNo(this.currentAccount.getLocCode());
              record.setCustGroup(legacyAddr.getCustGrp());
              record.setCustClass(legacyAddr.getCustClass());
              record.setCmrOffice(legacyAddr.getEstablishmentNameKanji());
              record.setCmrName4(legacyAddr.getContact());
              record.setCmrDept(legacyAddr.getDept());

              converted.add(record);
            } else {
              LOG.debug("Skipping adding " + cmrType + "/" + legacyAddr.getAddrSeq());
            }
          }
        }
      }
    }

    // now add company and establishment
    record = new FindCMRRecordModel();

    // add here company fields
    record.setCompanyNo(company.getCompanyNo());
    record.setSbo(company.getSBO());
    record.setLocationNo(company.getLocCode());
    record.setCompanySize(company.getEmployeeSize());
    record.setCmrAddrTypeCode("ZC01");
    record.setCmrAddrSeq("C");
    record.setParentCMRNo(company.getCompanyNo());
    record.setCmrStreetAddress(company.getAddress());
    record.setCmrName1Plain(company.getNameKanji());
    record.setCmrName2Plain(company.getNameKana());
    record.setCmrName3(company.getNameAbbr());
    record.setCmrBldg(company.getBldg());
    sbPhone = new StringBuilder();
    sbPhone.append(!StringUtils.isEmpty(company.getPhoneShi()) ? company.getPhoneShi() : "");
    sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(company.getPhoneKyo()) ? company.getPhoneKyo() : "");
    sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(company.getPhoneBango()) ? company.getPhoneBango() : "");
    record.setCmrCustPhone(sbPhone.toString());
    record.setCmrCountryLanded("JP");
    record.setCmrPostalCode(company.getPostCode());
    converted.add(record);

    // add here establishment fields
    record = new FindCMRRecordModel();
    record.setCmrAddrTypeCode("ZE01");
    record.setCmrAddrSeq("E");
    // s establishment.getEstablishmentNo();
    record.setEstabNo(establishment.getEstablishmentNo());
    record.setCompanyNo(establishment.getCompanyNo());
    // record.setSbo(establishment.getSBO());
    record.setEstabFuncCd(establishment.getFuncCode());
    record.setLocationNo(establishment.getLocCode());
    record.setParentCMRNo(establishment.getEstablishmentNo());
    record.setCmrStreetAddress(establishment.getAddress());
    record.setCmrName1Plain(establishment.getNameKanji());
    record.setCmrName2Plain(establishment.getNameKana());
    record.setCmrName3(establishment.getNameAbbr());
    record.setCmrBldg(establishment.getBldg());
    sbPhone = new StringBuilder();
    sbPhone.append(!StringUtils.isEmpty(establishment.getPhoneShi()) ? establishment.getPhoneShi() : "");
    sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(establishment.getPhoneKyo()) ? establishment.getPhoneKyo() : "");
    sbPhone.append(sbPhone.length() > 0 ? "-" : "").append(!StringUtils.isEmpty(establishment.getPhoneBango()) ? establishment.getPhoneBango() : "");
    record.setCmrCustPhone(sbPhone.toString());
    record.setCmrCountryLanded("JP");
    record.setCmrPostalCode(establishment.getPostCode());
    converted.add(record);

    source.setItems(converted);
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    setProdTypeCheckedOnImport(data);
    data.setDunsNo(mainRecord.getCmrDuns());
    data.setJsicCd(mainRecord.getJsic());
    data.setCsBo(mainRecord.getCsbo());

    data.setEducAllowCd(mainRecord.getEducAllowanceGrp());
    data.setCrsCd(mainRecord.getCRSCode());
    // data.setIcmsInd(mainRecord.getSR());
    data.setBillingProcCd(mainRecord.getBillingProcessCode());
    data.setInvoiceSplitCd(mainRecord.getInvoiceSplitCode());
    data.setCreditToCustNo(mainRecord.getCreditToCustNo());
    data.setBillToCustNo(mainRecord.getBillingCustNo());
    data.setTier2(mainRecord.getTier2());
    data.setAbbrevNm(mainRecord.getCmrName3() == null ? mainRecord.getCmrName3() : mainRecord.getCmrName3().trim());
    data.setSiInd(mainRecord.getSiInd());
    data.setIinInd(mainRecord.getIinInd());
    data.setLeasingCompanyIndc(mainRecord.getLeasingCompanyIndc());
    data.setChannelCd(mainRecord.getChannelCd());
    data.setCreditCd(mainRecord.getCreditCd());
    data.setValueAddRem(mainRecord.getValueAddRem());
    data.setRepTeamMemberNo(mainRecord.getRepTeamMemberNo());
    data.setSalesTeamCd(mainRecord.getSalesTeamCd());
    data.setGovType(mainRecord.getGovOfficeDivCode());
    data.setEmail2(mainRecord.getNameKanji());
    data.setProxiLocnNo(mainRecord.getAttach());
    data.setCustAcctType(mainRecord.getCustGroup());
    data.setCustClass(mainRecord.getCustClass());
    // data.setIcmsInd(mainRecord.getIcmsInd());
    data.setCsDiv(mainRecord.getCsDiv());
    data.setOemInd(mainRecord.getOemInd());
    if (mainRecord.getCompanyCd() != null) {
      if (mainRecord.getCompanyCd().equals("AA")) {
        data.setCustGrp("IBMTP");
      } else {
        data.setCustGrp("SUBSI");
      }
    }
    if ("IBMTP".equals(data.getCustGrp())) {
      data.setSalesBusOffCd(
          mainRecord.getSbo() != null && mainRecord.getSbo().length() == 3 ? mainRecord.getSbo().substring(1) : mainRecord.getSbo());
    } else if (StringUtils.isEmpty(data.getCustGrp()) || data.getCustGrp().equals("SUBSI")) {
      data.setSalesBusOffCd(
          mainRecord.getSboSub() != null && mainRecord.getSboSub().length() == 3 ? mainRecord.getSboSub().substring(1) : mainRecord.getSboSub());
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain() == null ? currentRecord.getCmrName1Plain() : currentRecord.getCmrName1Plain().trim());

    address.setBldg(currentRecord.getCmrBldg() == null ? currentRecord.getCmrBldg() : currentRecord.getCmrBldg().trim());
    address.setParCmrNo(currentRecord.getParentCMRNo() == null ? currentRecord.getParentCMRNo() : currentRecord.getParentCMRNo().trim());
    address.setEstabFuncCd(currentRecord.getEstabFuncCd() == null ? currentRecord.getEstabFuncCd() : currentRecord.getEstabFuncCd().trim());
    address.setPostCd(currentRecord.getCmrPostalCode());
    address.setLocationCode(currentRecord.getLocationNo());
    address.setCompanySize(currentRecord.getCompanySize());
    address.setCity2(currentRecord.getCompanyNo());
    address.setDivn(currentRecord.getEstabNo());

    address.setOffice(currentRecord.getCmrOffice());
    address.setCustPhone(currentRecord.getCmrCustPhone());
    address.setCustFax(currentRecord.getCmrCustFax());
    address.setDept(currentRecord.getCmrDept());

    String addrTxt = null;
    if (currentRecord.getCmrStreetAddress() != null && currentRecord.getCmrStreetAddress().trim().length() > 23) {
      addrTxt = currentRecord.getCmrStreetAddress().trim().substring(23);
    }
    address.setAddrTxt(currentRecord.getCmrStreetAddress() == null ? currentRecord.getCmrStreetAddress()
        : currentRecord.getCmrStreetAddress().trim().length() > 23 ? currentRecord.getCmrStreetAddress().trim().substring(0, 23)
            : currentRecord.getCmrStreetAddress().trim());
    address.setAddrTxt2(addrTxt);
    // 1652081
    convertKATAKANA(currentRecord.getCmrName2Plain(), address);
    String custNm4 = null;
    if (address.getCustNm4() != null && address.getCustNm4().trim().length() > 23) {
      custNm4 = address.getCustNm4().trim().substring(23);
    }
    address.setCustNm4(address.getCustNm4() == null ? address.getCustNm4()
        : address.getCustNm4().trim().length() > 23 ? address.getCustNm4().trim().substring(0, 23) : address.getCustNm4().trim());
    address.setPoBoxCity(custNm4);

    if ("ZC01".equals(address.getId().getAddrType()) || "ZE01".equals(address.getId().getAddrType())) {
      address.setContact(null);
    } else {
      address.setContact(currentRecord.getCmrName4() == null ? currentRecord.getCmrName4()
          : currentRecord.getCmrName4().trim().length() > 15 ? currentRecord.getCmrName4().trim().substring(0, 15)
              : currentRecord.getCmrName4().trim());
    }
    String[] namearray = dividingCustName1toName2(address.getCustNm1(), null);
    if (namearray != null && namearray.length == 2) {
      address.setCustNm2(namearray[1]);
      address.setCustNm1(namearray[0]);
    } else {
      address.setCustNm2("");
    }
    String custType = admin.getCustType();
    String reqType = admin.getReqType();
    String addrType = address.getId().getAddrType();

    // 1652110
    convertBuilding(address.getBldg(), address);
    if (!"ZC01".equals(addrType) && !"ZE01".equals(addrType)) {
      convertBranch(address.getOffice(), address);
      convertDept(address.getDept(), address);
    }

    // 1652096 - set abbNm upper case
    // // import Company and Estab abbNm, but not Account abbNm
    // if ("ZC01".equals(addrType) || "ZE01".equals(addrType)) {
    address.setCustNm3(currentRecord.getCmrName3() == null ? currentRecord.getCmrName3() : currentRecord.getCmrName3().trim());
    converAbbNm(address.getCustNm3(), address);
    // }

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      // handle 'by-models'
      if (!StringUtils.isEmpty(custType)) {
        switch (custType) {
        case "CEA":
          address.setParCmrNo(null); // all types will be by model
          break;
        case "EA":
          if (!"ZC01".equals(addrType)) {
            address.setParCmrNo(null); // EA types will be by model
          }
          break;
        case "A":
          if (!"ZC01".equals(addrType) && !"ZE01".equals(addrType)) {
            address.setParCmrNo(null); // A types will be by model
          }
          break;
        }
      }
    }
  }

  private void convertKATAKANA(String value, Addr address) {
    String convertedKATAKANA = "";
    if (value != null && !value.isEmpty() && value.length() > 0) {
      convertedKATAKANA = value.replaceAll("ィ", "イ");
      convertedKATAKANA = value.replaceAll("ョ", "ヨ");
      convertedKATAKANA = value.replaceAll("ュ", "ユ");
      convertedKATAKANA = value.replaceAll("ヵ", "カ");
      convertedKATAKANA = value.replaceAll("ャ", "ヤ");
      convertedKATAKANA = value.replaceAll("ッ", "ツ");
      convertedKATAKANA = value.replaceAll("ァ", "ア");
      address.setCustNm4(convertedKATAKANA);
    }
  }

  private void convertBuilding(String value, Addr address) {
    String convertedBuilding = "";
    if (value != null && !value.isEmpty() && value.length() > 0) {
      convertedBuilding = value.replaceAll("㈱", "（株）");
      address.setBldg(convertedBuilding);
    }
  }

  private void convertBranch(String value, Addr address) {
    String convertedBranch = "";
    if (value != null && !value.isEmpty() && value.length() > 0) {
      convertedBranch = value.replaceAll("㈱", "（株）");
      address.setOffice(convertedBranch);
    }
  }

  private void convertDept(String value, Addr address) {
    String convertedDept = "";
    if (value != null && !value.isEmpty() && value.length() > 0) {
      convertedDept = value.replaceAll("㈱", "（株）");
      address.setDept(convertedDept);
    }
  }

  private void converAbbNm(String value, Addr address) {

    String convertedAbbNm = "";
    if (value != null && !value.isEmpty() && value.length() > 0) {
      convertedAbbNm = value.toUpperCase();
      address.setCustNm3(convertedAbbNm);
    }
  }

  @Override
  public int getName1Length() {
    return 30;
  }

  @Override
  public int getName2Length() {
    return 30;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    String requesterId = model.getRequesterId();
    if (!StringUtils.isEmpty(requesterId)) {
      Person p = BluePagesHelper.getPerson(requesterId);
      if (p != null) {
        mv.addObject("requesterId_UID", p.getEmployeeId().substring(0, p.getEmployeeId().length() - 3));
      }
    }
    String originatorId = model.getOriginatorId();
    if (!StringUtils.isEmpty(originatorId)) {
      Person p = BluePagesHelper.getPerson(originatorId);
      if (p != null) {
        mv.addObject("originatorId_UID", p.getEmployeeId().substring(0, p.getEmployeeId().length() - 3));
      }
    }

  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getJsicCd(), newData.getJsicCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "JSICCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getJsicCd(), "JSIC", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getJsicCd(), "JSIC", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getOemInd(), newData.getOemInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OEMInd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOemInd(), "OEMInd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOemInd(), "OEMInd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getEmail2(), newData.getEmail2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail2(), "AbbrevLocation", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail2(), "AbbrevLocation", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getLeasingCompanyIndc(), newData.getLeasingCompanyIndc())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LeasingCompIndc", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getLeasingCompanyIndc(), "LeasingCompIndc", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getLeasingCompanyIndc(), "LeasingCompIndc", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getEducAllowCd(), newData.getEducAllowCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EducationAllowance", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getLeasingCompanyIndc(), "EducationAllowance", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getLeasingCompanyIndc(), "EducationAllowance", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getCustClass(), newData.getCustClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustClass(), "CustClass", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustClass(), "CustClass", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getIinInd(), newData.getIinInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "IinInd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getIinInd(), "IinInd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getIinInd(), "IinInd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getValueAddRem(), newData.getValueAddRem())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ValueAddRem", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getValueAddRem(), "ValueAddRem", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getValueAddRem(), "ValueAddRem", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getChannelCd(), newData.getChannelCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ChannelCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getChannelCd(), "ChannelCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getChannelCd(), "ChannelCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getSiInd(), newData.getSiInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SiInd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSiInd(), "SiInd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSiInd(), "SiInd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getCrsCd(), newData.getCrsCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CrsCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCrsCd(), "CrsCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCrsCd(), "CrsCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CreditCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCreditCd(), "CreditCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCreditCd(), "CreditCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getGovType(), newData.getGovType())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Government", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getGovType(), "Government", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getGovType(), "Government", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getOutsourcingService(), newData.getOutsourcingService())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OutsourcingServ", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOutsourcingService(), "OutsourcingServ", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOutsourcingService(), "OutsourcingServ", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getCreditBp(), newData.getCreditBp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "DirectBp", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCreditBp(), "DirectBp", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCreditBp(), "DirectBp", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getZseriesSw(), newData.getZseriesSw())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "zSeriesSw", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getZseriesSw(), "zSeriesSw", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getZseriesSw(), "zSeriesSw", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getSalesTeamCd(), newData.getSalesTeamCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesSR", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesTeamCd(), "SalesSR", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesTeamCd(), "SalesSR", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getOrgNo(), newData.getOrgNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OriginatorNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOrgNo(), "OriginatorNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrgNo(), "OriginatorNo", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getProdType(), newData.getProdType())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ProdType", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getProdType(), "ProdType", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getProdType(), "ProdType", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getChargeCd(), newData.getChargeCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ChargeCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getChargeCd(), "ChargeCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getChargeCd(), "ChargeCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getSoProjectCd(), newData.getSoProjectCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ProjectCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSoProjectCd(), "ProjectCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSoProjectCd(), "ProjectCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCsDiv(), newData.getCsDiv())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CSDiv", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCsDiv(), "CSDiv", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCsDiv(), "CSDiv", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getBillingProcCd(), newData.getBillingProcCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BillingProcCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getBillingProcCd(), "BillingProcCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getBillingProcCd(), "BillingProcCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getInvoiceSplitCd(), newData.getInvoiceSplitCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InvoiceSplitCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getInvoiceSplitCd(), "InvoiceSplitCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getInvoiceSplitCd(), "InvoiceSplitCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getCreditToCustNo(), newData.getCreditToCustNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CreditToCustNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCreditToCustNo(), "CreditToCustNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCreditToCustNo(), "CreditToCustNo", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCsBo(), newData.getCsBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CSBOCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCsBo(), "CSBOCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCsBo(), "CSBOCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getTier2(), newData.getTier2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Tier2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTier2(), "Tier2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTier2(), "Tier2", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getBillToCustNo(), newData.getBillToCustNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BillToCustNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getBillToCustNo(), "BillToCustNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getBillToCustNo(), "BillToCustNo", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getAdminDeptCd(), newData.getAdminDeptCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AdminDeptCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getAdminDeptCd(), "AdminDeptCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAdminDeptCd(), "AdminDeptCd", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.JAPAN.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getAdminDeptLine(), newData.getAdminDeptLine())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AdminDeptLine", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getAdminDeptLine(), "AdminDeptLine", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAdminDeptLine(), "AdminDeptLine", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    if (!equals(addr.getContact(), addr.getContactOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "Contact", "-"));
      update.setNewData(addr.getContact());
      update.setOldData(addr.getContactOld());
      results.add(update);
    }
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  protected boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    setProdTypeOnSave(data);
    updateBillToCustomerNoAfterImport(data);
    setSalesRepTmDateOfAssign(data, admin, entityManager);
    updateCSBOBeforeDataSave(entityManager, admin, data);
    setAccountAbbNmOnSaveForBP(admin, data);
  }

  private void setSalesRepTmDateOfAssign(Data data, Admin admin, EntityManager entityManager) {
    String reqStatus = admin.getReqStatus();
    String custSubGrp = data.getCustSubGrp() == null ? "" : data.getCustSubGrp();
    String nowSalesTeamCd = data.getSalesTeamCd();
    String DataRdcsalesTeamCd = null;
    DataRdcsalesTeamCd = getSales_Team_Cd(entityManager, admin, data);
    String reqType = admin.getReqType();
    // System.out.println(reqType);

    if ("C".equalsIgnoreCase(reqType)) {
      if ("STOSC".equalsIgnoreCase(custSubGrp) || "STOSB".equalsIgnoreCase(custSubGrp) || "STOSI".equalsIgnoreCase(custSubGrp)) {
        data.setSalesRepTeamDateOfAssignment(null);
      } else if ("DRA".equalsIgnoreCase(reqStatus) || "PVA".equalsIgnoreCase(reqStatus)) {
        data.setSalesRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
      }
    }
    if ("U".equalsIgnoreCase(reqType)) {
      if (equals(DataRdcsalesTeamCd, nowSalesTeamCd)) {
        data.setSalesRepTeamDateOfAssignment(null);
      } else if ("DRA".equalsIgnoreCase(reqStatus) || "PVA".equalsIgnoreCase(reqStatus)) {
        data.setSalesRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
      }
    }
  }

  private String getSales_Team_Cd(EntityManager entityManager, Admin admin, Data data) {
    String salesTeamCd = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.SALES_TEAM_CD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      salesTeamCd = results.get(0);
      // System.out.println("salesTeamCd = " + salesTeamCd);
    }
    return salesTeamCd;
  }

  private void updateCSBOBeforeDataSave(EntityManager entityManager, Admin admin, Data data) {
    String custSubGrp = data.getCustSubGrp() == null ? "" : data.getCustSubGrp();
    if ("C".equalsIgnoreCase(admin.getReqType())) {
      switch (custSubGrp) {
      case "NORML":
      case "EUCMR":
      case "WHCMR":
      case "OUTSC":
      case "STOSB":
      case "STOSC":
      case "STOSI":
      case "INTER":
        String csboValue = null;
        csboValue = getCsboBeforeDataSave(entityManager, admin, data);
        if (csboValue != null && csboValue.length() > 0 && (data.getCsBo() == null || StringUtils.isEmpty(data.getCsBo()))) {
          data.setCsBo(csboValue);
        }
        break;
      default:
        break;
      }
    } else if ("U".equalsIgnoreCase(admin.getReqType())) {
      switch (custSubGrp) {
      case "NORML":
      case "EUCMR":
      case "WHCMR":
      case "OUTSC":
      case "BPWPQ":
      case "ISOCU":
      case "STOSB":
      case "STOSC":
      case "STOSI":
      case "INTER":
        String csboValue = null;
        csboValue = getCsboBeforeDataSave(entityManager, admin, data);
        if (csboValue != null && csboValue.length() > 0 && "DRA".equalsIgnoreCase(admin.getReqStatus())) {
          // update CSBO for requester in Update request
          data.setCsBo(csboValue);
        }
        break;
      default:
        break;
      }
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private String getCsboBeforeDataSave(EntityManager entityManager, Admin admin, Data data) {
    String postalCdZS01 = null;
    String csboValue = null;
    postalCdZS01 = getZS01PostCd(entityManager, admin, data);
    postalCdZS01 = dbcs2ascii(postalCdZS01);
    if (postalCdZS01 != null && postalCdZS01.length() > 0) {
      csboValue = getCSBOByPostCd(entityManager, data, postalCdZS01);
    }
    return csboValue;
  }

  /**
   * defect 1727965 - BP does not have address. Account Abb Name comes from CRIS
   * search via Credit Customer No.
   */
  private void setAccountAbbNmOnSaveForBP(Admin admin, Data data) {
    if (!"C".equalsIgnoreCase(admin.getReqType())) {
      return;
    }
    if (data.getCustSubGrp() == null || data.getCreditToCustNo() == null) {
      return;
    }
    if (!"DRA".equalsIgnoreCase(admin.getReqStatus())) {
      // do nothing if not requester
      return;
    }
    if ("BPWPQ".equalsIgnoreCase(data.getCustSubGrp())) {
      String accountAbbNm = null;
      String accountAbbNmInCris = null;
      String tier2 = data.getTier2() == null ? "" : data.getTier2();
      String dealerNo = data.getSalesTeamCd() == null ? "" : data.getSalesTeamCd();
      String creditToCustNo = data.getCreditToCustNo() == null ? "" : data.getCreditToCustNo();

      if (tier2.length() > 1) {
        if ("D".equalsIgnoreCase(tier2.substring(0, 1))) {
          tier2 = "A" + tier2.substring(1, tier2.length());
        }
      }

      accountAbbNmInCris = getaccountAbbNmInCris(creditToCustNo);

      if (accountAbbNmInCris == null || accountAbbNmInCris.length() < 1) {
        return;
      }

      if (tier2.length() + accountAbbNmInCris.length() + dealerNo.length() + 2 > 22) {

        if (tier2.length() > 0) {
          int endInd = 22 - tier2.length() - dealerNo.length() - 2;
          accountAbbNmInCris = accountAbbNmInCris.substring(0, endInd);
          accountAbbNm = tier2 + " " + accountAbbNmInCris + " " + dealerNo;
        } else {
          int endInd = 22 - dealerNo.length() - 1;
          accountAbbNmInCris = accountAbbNmInCris.substring(0, endInd);
          accountAbbNm = accountAbbNmInCris + " " + dealerNo;
        }

      } else {
        if (tier2.length() > 0) {
          int blankSpaceLength = 22 - tier2.length() - accountAbbNmInCris.length() - dealerNo.length() - 2;
          String blankSpace = "";
          for (int i = 0; i < blankSpaceLength; i++) {
            blankSpace += " ";
          }
          accountAbbNm = tier2 + " " + accountAbbNmInCris + blankSpace + " " + dealerNo;
        } else {
          int blankSpaceLength = 22 - accountAbbNmInCris.length() - dealerNo.length() - 1;
          String blankSpace = "";
          for (int i = 0; i < blankSpaceLength; i++) {
            blankSpace += " ";
          }
          accountAbbNm = accountAbbNmInCris + blankSpace + " " + dealerNo;
        }
      }

      if (accountAbbNm != null) {
        accountAbbNm = accountAbbNm.toUpperCase();
      }

      data.setAbbrevNm(accountAbbNm);
    }
  }

  private String getaccountAbbNmInCris(String creditToCustNo) {
    if (creditToCustNo == null) {
      return null;
    }
    String accountAbbNmInCris = null;
    CRISAccount crisRecord = null;
    try {
      crisRecord = findAccountFromCRIS(creditToCustNo);
    } catch (Exception e) {
      LOG.error("for JP BP Account Abb Name, search CRIS failed with creditToCustNo " + creditToCustNo);
      // e.printStackTrace();
      return null;
    }
    accountAbbNmInCris = crisRecord.getNameAbbr();
    return accountAbbNmInCris;
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    if (addr != null && StringUtils.isEmpty(addr.getLandCntry()) && cmrIssuingCntry.equals("760")) {
      addr.setLandCntry("JP");
    }
    addSpaceBetweenCompAndLegalName(addr);
    replaceBanGao(addr);

    if ("ZE01".equalsIgnoreCase(addr.getId().getAddrType())) {
      addr.setParCmrNo(addr.getDivn());
    }
    String custNm1 = addr.getCustNm1() == null ? "" : addr.getCustNm1();
    String custNm2 = addr.getCustNm2() == null ? "" : addr.getCustNm2();
    String custNm12 = custNm1 + custNm2;
    addr.setCustNm1(custNm12.length() > 15 ? custNm12.substring(0, 15) : custNm12);
    addr.setCustNm2(
        custNm12.length() > 15 ? custNm12.substring(15).length() > 15 ? custNm12.substring(15).substring(0, 15) : custNm12.substring(15) : "");
    String nameTemp = null;
    if (addr.getCustNm4() != null && addr.getCustNm4().trim().length() > 23) {
      nameTemp = addr.getCustNm4().trim().substring(23);
      addr.setPoBoxCity(nameTemp);
    }
    addr.setCustNm4(addr.getCustNm4() == null ? addr.getCustNm4()
        : addr.getCustNm4().trim().length() > 23 ? addr.getCustNm4().trim().substring(0, 23) : addr.getCustNm4().trim());

    String addrTemp = null;
    if (addr.getAddrTxt() != null && addr.getAddrTxt().trim().length() > 23) {
      addrTemp = addr.getAddrTxt().trim().substring(23);
      addr.setAddrTxt2(addrTemp);
    }
    addr.setAddrTxt(addr.getAddrTxt() == null ? addr.getAddrTxt()
        : addr.getAddrTxt().trim().length() > 23 ? addr.getAddrTxt().trim().substring(0, 23) : addr.getAddrTxt().trim());

    setCSBOBeforeAddrSave(entityManager, addr);
    setCustNmDetailBeforeAddrSave(entityManager, addr);
    setAccountAbbNmBeforeAddrSave(entityManager, addr);
  }

  private void setCSBOBeforeAddrSave(EntityManager entityManager, Addr addr) {
    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    if ("ZS01".equals(addr.getId().getAddrType()) && !StringUtils.isEmpty(addr.getPostCd())) {
      DataPK pk = new DataPK();
      pk.setReqId(addr.getId().getReqId());
      Data data = entityManager.find(Data.class, pk);

      String custSubGrp = data.getCustSubGrp() == null ? "" : data.getCustSubGrp();
      if ("C".equalsIgnoreCase(admin.getReqType())) {
        switch (custSubGrp) {
        case "NORML":
        case "EUCMR":
        case "WHCMR":
        case "OUTSC":
        case "STOSB":
        case "STOSC":
        case "STOSI":
        case "INTER":
          addPostCdCSBOLogicOnAddrSave(entityManager, addr, data);
          break;
        case "BPWPQ":
        case "ISOCU":
        case "BCEXA":
        case "BFKSC":
          data.setCsBo("");
          break;
        case "ABIJS":
        case "AHIJE":
        case "AUITS":
        case "AWIGS":
        case "BDRBS":
        case "BVMDS":
        case "BGICS":
        case "BHISO":
        case "BIJSC":
        case "BKRBS":
        case "BLNIS":
        case "BMISI":
        case "BPIJB":
        case "BQICL":
        case "BRMSI":
          setCSBOSubsidiaryValue(data);
          break;
        case "":
        default:
          break;
        }
      } else if ("U".equalsIgnoreCase(admin.getReqType())) {
        switch (custSubGrp) {
        case "NORML":
        case "EUCMR":
        case "WHCMR":
        case "OUTSC":
        case "BPWPQ":
        case "ISOCU":
        case "STOSB":
        case "STOSC":
        case "STOSI":
        case "INTER":
          addPostCdCSBOLogicOnAddrSave(entityManager, addr, data);
          break;
        case "BCEXA":
        case "BFKSC":
          data.setCsBo("");
          break;
        case "ABIJS":
        case "AHIJE":
        case "AUITS":
        case "AWIGS":
        case "BDRBS":
        case "BVMDS":
        case "BGICS":
        case "BIJSC":
        case "BHISO":
        case "BKRBS":
        case "BLNIS":
        case "BMISI":
        case "BPIJB":
        case "BQICL":
        case "BRMSI":
          setCSBOSubsidiaryValue(data);
          break;
        case "":
        default:
          break;
        }
      }
      entityManager.merge(data);
      entityManager.flush();
    }
  }

  private void addPostCdCSBOLogicOnAddrSave(EntityManager entityManager, Addr addr, Data data) {
    String csboValue = null;
    String postCd = null;
    postCd = dbcs2ascii(addr.getPostCd());
    String sql = ExternalizedQuery.getSql("QUERY.GET.CSBO_BY_POSTLCD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("POST_CD", postCd);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      csboValue = results.get(0);
      if (csboValue != null && csboValue.length() > 0) {
        data.setCsBo(csboValue);
      }
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private void setCSBOSubsidiaryValue(Data data) {
    data.setCsBo("0000");
  }

  private void setCustNmDetailBeforeAddrSave(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    String custSubGrp = data.getCustSubGrp() != null ? data.getCustSubGrp() : "";
    String chargeCode = "アウトソーシング・サービス";
    String chargeCdValue = data.getChargeCd() != null ? data.getChargeCd() : "";
    chargeCdValue = convert2DBCS(chargeCdValue);
    String projectCode = data.getSoProjectCd() != null ? data.getSoProjectCd() : "";
    projectCode = convert2DBCS(projectCode);
    String custNmDetail = null;
    String accountCustNMKANJI = "";
    accountCustNMKANJI = addr.getCustNm1() + addr.getCustNm2();

    if (admin.getReqType().equalsIgnoreCase("C") && "ZS01".equals(addr.getId().getAddrType())) {
      if (custSubGrp == "") {
        return;
      }
      if ("STOSB".equalsIgnoreCase(custSubGrp) || "STOSC".equalsIgnoreCase(custSubGrp)) {
        if (projectCode != null && projectCode.length() >= 5) {
          custNmDetail = chargeCode + "（" + projectCode.substring(0, 5) + "）";
        } else {
          custNmDetail = chargeCode + "（" + projectCode + "）";
        }
      } else if ("STOSI".equalsIgnoreCase(custSubGrp)) {
        if (projectCode != null && projectCode.length() >= 5) {
          custNmDetail = accountCustNMKANJI + "（" + projectCode.substring(0, 5) + "）";
        } else {
          custNmDetail = accountCustNMKANJI + "（" + projectCode + "）";
        }
      } else if ("INTER".equalsIgnoreCase(custSubGrp) || "BIJSC".equalsIgnoreCase(custSubGrp)) {
        custNmDetail = chargeCdValue + accountCustNMKANJI;
      } else {
        custNmDetail = accountCustNMKANJI;
      }

      if (custNmDetail != null) {
        if (custNmDetail.length() > 30) {
          custNmDetail = custNmDetail.substring(0, 30);
        }
        data.setEmail2(custNmDetail);
      }
    }

    entityManager.merge(data);
    entityManager.flush();
  }

  private void setAccountAbbNmBeforeAddrSave(EntityManager entityManager, Addr addr) {
    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    if ("C".equalsIgnoreCase(admin.getReqType())) {
      setAccountAbbNmBeforeAddrSaveCreate(entityManager, addr);
    } else if ("U".equalsIgnoreCase(admin.getReqType())) {
      setAccountAbbNmBeforeAddrSaveUpdate(entityManager, addr);
    }
  }

  private void setAccountAbbNmBeforeAddrSaveCreate(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    String custSubGrp = data.getCustSubGrp() != null ? data.getCustSubGrp() : "";
    String fullEngNm = addr.getCustNm3().toUpperCase();
    String accountAbbNm = null;

    if ("ZS01".equals(addr.getId().getAddrType())) {
      if (custSubGrp == "") {
        return;
      }
      switch (custSubGrp) {
      case "OUTSC":
        if (fullEngNm != null && !"".equalsIgnoreCase(fullEngNm)) {
          if (fullEngNm.length() > 17) {
            accountAbbNm = fullEngNm.substring(0, 17) + "   SO";
          } else {
            int blankSpaceLength = 22 - fullEngNm.length() - 5;
            String blankSpace = "";
            for (int i = 0; i < blankSpaceLength; i++) {
              blankSpace += " ";
            }
            accountAbbNm = fullEngNm + blankSpace + "   SO";
          }
        }
        break;
      case "BPWPQ":
        // defect 1727965 - BP does not have address. Account Abb Name comes
        // from CRIS search via Credit Customer No.
        accountAbbNm = data.getAbbrevNm();
        break;
      case "STOSB":
      case "STOSC":
        String chargeCd = data.getChargeCd();
        accountAbbNm = chargeCd + " " + fullEngNm;
        break;
      case "STOSI":
        String chargeCdI = data.getChargeCd();
        if (chargeCdI != null && chargeCdI.length() >= 5) {
          chargeCdI = chargeCdI.substring(0, 5);
        }
        accountAbbNm = "I" + chargeCdI + " " + fullEngNm;
        break;
      case "BCEXA":
      case "BFKSC":
        accountAbbNm = "";
        break;
      case "":
      default:
        accountAbbNm = fullEngNm;
      }
      if (accountAbbNm != null) {
        if (accountAbbNm.length() > 22) {
          accountAbbNm = accountAbbNm.substring(0, 22);
        }
        data.setAbbrevNm(accountAbbNm.toUpperCase());
      }
    }

    entityManager.merge(data);
    entityManager.flush();
  }

  private void setAccountAbbNmBeforeAddrSaveUpdate(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    String custSubGrp = data.getCustSubGrp() != null ? data.getCustSubGrp() : "";
    String fullEngNm = addr.getCustNm3().toUpperCase();
    String accountAbbNm = null;

    if ("ZS01".equals(addr.getId().getAddrType())) {
      if (custSubGrp == "") {
        return;
      }
      switch (custSubGrp) {
      case "BCEXA":
      case "BFKSC":
        accountAbbNm = "";
        break;
      case "":
      default:
        accountAbbNm = fullEngNm;
      }
      if (accountAbbNm != null) {
        if (accountAbbNm.length() > 22) {
          accountAbbNm = accountAbbNm.substring(0, 22);
        }
        data.setAbbrevNm(accountAbbNm.toUpperCase());
      }
    }

    entityManager.merge(data);
    entityManager.flush();
  }

  private void addSpaceBetweenCompAndLegalName(Addr addr) {
    // Story 1652126: Add one space between company type and legal name for the
    // field of Account_Customer Name-KANJI. (For example, 株式会社本田 should be
    // converted into 株式会社 本田)
    String patternStr;
    // Defect 1676927 2 spaces between specific company type and legal name
    if (addr.getCustNm1().indexOf("医療法人財団") > -1 || addr.getCustNm1().indexOf("医療法人社団") > -1) {
      patternStr = "(医療法人社団|医療法人財団)([^　])";
    } else if (addr.getCustNm1().indexOf("医療法人") > -1) {
      patternStr = "(医療法人)([^　])";
    } else {
      patternStr = "(株式会社|有限会社|合同会社|合名会社|合資会社|社会医療法人|財団法人|一般社団法人|公益財団法人|社団法人|一般社団法人|公益社団法人|宗教法人|学校法人|社会福祉法人|更生保護法人|相互会社|特定非営利活動法人|独立行政法人|地方独立行政法人|弁護士法人|有限責任中間法人|無限責任中間法人|行政書士法人|司法書士法人|税理士法人|国立大学法人|公立大学法人|農事組合法人|管理組合法人|社会保険労務士法人)([^　])";
    }
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(addr.getCustNm1());
    if (matcher.find()) {
      // add space
      String output = matcher.replaceFirst("$1　$2");
      addr.setCustNm1(output);
    }
    if (addr.getCustNm1() != null && addr.getCustNm1().length() > 15) {
      addr.setCustNm2(addr.getCustNm1().substring(15) + addr.getCustNm2());
      addr.setCustNm1(addr.getCustNm1().substring(0, 15));
    }
  }

  private void replaceBanGao(Addr addr) {
    // Story 1652105: Convert "丁目"/"番地",”番”,"号" into "－" or remove if it is the
    // last letter
    // 1652105 - do not replace 番町, i.e, "東京都千代田区霞ヶ関３番町２－１－３" is a legal value.
    String patternStrDingMuFan = "(\\d|[０-９])(丁目|番地|番(?!町)|号)$";
    String patternStrDingMuFanNotLast = "(\\d|[０-９])(丁目|番地|番(?!町)|号)";
    Pattern pattern1 = Pattern.compile(patternStrDingMuFan);
    Matcher matcher1 = pattern1.matcher(addr.getAddrTxt());
    if (matcher1.find()) {
      // remove the last letter
      String output = matcher1.replaceFirst("$1");
      output = convert2DBCS(output);
      addr.setAddrTxt(output);
    }
    // Story 1652105: Convert "丁目"/"番地",”番”,"号" into "－" or remove if it is the
    // last letter
    pattern1 = Pattern.compile(patternStrDingMuFanNotLast);
    matcher1 = pattern1.matcher(addr.getAddrTxt());
    if (matcher1.find()) {
      // Convert "丁目"/"番地",”番”,"号" into "－"
      String output = matcher1.replaceAll("$1－");
      output = convert2DBCS(output);
      addr.setAddrTxt(output);
    }
  }

  private String dbcs2ascii(String value) {
    String modifiedVal = null;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replaceAll("１", "1");
      modifiedVal = modifiedVal.replaceAll("２", "2");
      modifiedVal = modifiedVal.replaceAll("３", "3");
      modifiedVal = modifiedVal.replaceAll("４", "4");
      modifiedVal = modifiedVal.replaceAll("５", "5");
      modifiedVal = modifiedVal.replaceAll("６", "6");
      modifiedVal = modifiedVal.replaceAll("７", "7");
      modifiedVal = modifiedVal.replaceAll("８", "8");
      modifiedVal = modifiedVal.replaceAll("９", "9");
      modifiedVal = modifiedVal.replaceAll("０", "0");
      modifiedVal = modifiedVal.replaceAll("-", "");
      modifiedVal = modifiedVal.replaceAll("―", "");
      modifiedVal = modifiedVal.replaceAll("−", "");
      modifiedVal = modifiedVal.replaceAll("－", "");
    }
    return modifiedVal;
  }

  private String convert2DBCS(String value) {
    String modifiedVal = null;
    if (value != null && value.length() > 0) {
      modifiedVal = value;
      modifiedVal = modifiedVal.replaceAll("1", "１");
      modifiedVal = modifiedVal.replaceAll("2", "２");
      modifiedVal = modifiedVal.replaceAll("3", "３");
      modifiedVal = modifiedVal.replaceAll("4", "４");
      modifiedVal = modifiedVal.replaceAll("5", "５");
      modifiedVal = modifiedVal.replaceAll("6", "６");
      modifiedVal = modifiedVal.replaceAll("7", "７");
      modifiedVal = modifiedVal.replaceAll("8", "８");
      modifiedVal = modifiedVal.replaceAll("9", "９");
      modifiedVal = modifiedVal.replaceAll("0", "０");
      modifiedVal = modifiedVal.replaceAll("A", "Ａ");
      modifiedVal = modifiedVal.replaceAll("B", "Ｂ");
      modifiedVal = modifiedVal.replaceAll("C", "Ｃ");
      modifiedVal = modifiedVal.replaceAll("D", "Ｄ");
      modifiedVal = modifiedVal.replaceAll("E", "Ｅ");
      modifiedVal = modifiedVal.replaceAll("F", "Ｆ");
      modifiedVal = modifiedVal.replaceAll("G", "Ｇ");
      modifiedVal = modifiedVal.replaceAll("H", "Ｈ");
      modifiedVal = modifiedVal.replaceAll("I", "Ｉ");
      modifiedVal = modifiedVal.replaceAll("J", "Ｊ");
      modifiedVal = modifiedVal.replaceAll("K", "Ｋ");
      modifiedVal = modifiedVal.replaceAll("L", "Ｌ");
      modifiedVal = modifiedVal.replaceAll("M", "Ｍ");
      modifiedVal = modifiedVal.replaceAll("N", "Ｎ");
      modifiedVal = modifiedVal.replaceAll("O", "Ｏ");
      modifiedVal = modifiedVal.replaceAll("P", "Ｐ");
      modifiedVal = modifiedVal.replaceAll("Q", "Ｑ");
      modifiedVal = modifiedVal.replaceAll("R", "Ｒ");
      modifiedVal = modifiedVal.replaceAll("S", "Ｓ");
      modifiedVal = modifiedVal.replaceAll("T", "Ｔ");
      modifiedVal = modifiedVal.replaceAll("U", "Ｕ");
      modifiedVal = modifiedVal.replaceAll("V", "Ｖ");
      modifiedVal = modifiedVal.replaceAll("W", "Ｗ");
      modifiedVal = modifiedVal.replaceAll("X", "Ｘ");
      modifiedVal = modifiedVal.replaceAll("Y", "Ｙ");
      modifiedVal = modifiedVal.replaceAll("Z", "Ｚ");
      modifiedVal = modifiedVal.replaceAll(" ", "　");
    }
    return modifiedVal;
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(JP_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    setCSBOAfterImport(entityManager, admin, data);
    setAccountAbbNmAfterImport(entityManager, admin, data);
    updateBillToCustomerNoAfterImport(data);
  }

  private void updateBillToCustomerNoAfterImport(Data data) {
    if ("IBMTP".equals(data.getCustGrp())) {
      if ("BPWPQ".equals(data.getCustSubGrp())) {
        String D0074 = "D0074";
        String dealerNo = data.getSalesTeamCd() != null ? data.getSalesTeamCd() : "";
        String billToCustNo = null;
        if (dealerNo.toUpperCase().startsWith("D")) {
          if (dealerNo.compareToIgnoreCase(D0074) > 0) {
            billToCustNo = "930" + (dealerNo.substring(dealerNo.length() - 3));
          } else if (dealerNo.compareToIgnoreCase(D0074) <= 0) {
            int intDealerNo = Integer.parseInt(dealerNo.substring(dealerNo.length() - 3)) - 1;
            if (intDealerNo >= 100) {
              billToCustNo = "930" + intDealerNo;
            } else if (intDealerNo >= 10) {
              billToCustNo = "9300" + intDealerNo;
            } else {
              billToCustNo = "93000" + intDealerNo;
            }
          }
        } else if (dealerNo.toUpperCase().startsWith("S")) {
          billToCustNo = "940S" + dealerNo.substring(dealerNo.length() - 2);
        }
        data.setBillToCustNo(billToCustNo);
      }
    }
  }

  private void setCSBOAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String custSubGrp = data.getCustSubGrp() == null ? "" : data.getCustSubGrp();
    if ("C".equalsIgnoreCase(admin.getReqType())) {
      switch (custSubGrp) {
      case "NORML":
      case "EUCMR":
      case "WHCMR":
      case "OUTSC":
      case "STOSB":
      case "STOSC":
      case "STOSI":
      case "INTER":
        addPostCdCSBOLogic(entityManager, admin, data);
        break;
      case "BPWPQ":
      case "ISOCU":
      case "BCEXA":
      case "BFKSC":
        data.setCsBo("");
        break;
      case "ABIJS":
      case "AHIJE":
      case "AUITS":
      case "AWIGS":
      case "BDRBS":
      case "BVMDS":
      case "BGICS":
      case "BHISO":
      case "BIJSC":
      case "BKRBS":
      case "BLNIS":
      case "BMISI":
      case "BPIJB":
      case "BQICL":
      case "BRMSI":
        setCSBOSubsidiaryValue(data);
        break;
      case "":
      default:
        break;
      }
    } else if ("U".equalsIgnoreCase(admin.getReqType())) {
      switch (custSubGrp) {
      case "NORML":
      case "EUCMR":
      case "WHCMR":
      case "OUTSC":
      case "BPWPQ":
      case "ISOCU":
      case "STOSB":
      case "STOSC":
      case "STOSI":
      case "INTER":
        addPostCdCSBOLogic(entityManager, admin, data);
        break;
      case "BCEXA":
      case "BFKSC":
        data.setCsBo("");
        break;
      case "ABIJS":
      case "AHIJE":
      case "AUITS":
      case "AWIGS":
      case "BDRBS":
      case "BVMDS":
      case "BGICS":
      case "BIJSC":
      case "BHISO":
      case "BKRBS":
      case "BLNIS":
      case "BMISI":
      case "BPIJB":
      case "BQICL":
      case "BRMSI":
        setCSBOSubsidiaryValue(data);
        break;
      case "":
      default:
        break;
      }
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private void addPostCdCSBOLogic(EntityManager entityManager, Admin admin, Data data) {
    String postalCdZS01 = null;
    postalCdZS01 = getZS01PostCd(entityManager, admin, data);
    postalCdZS01 = dbcs2ascii(postalCdZS01);
    if (postalCdZS01 != null && postalCdZS01.length() > 0) {
      String csboValue = null;
      csboValue = getCSBOByPostCd(entityManager, data, postalCdZS01);
      if (csboValue != null && csboValue.length() > 0) {
        data.setCsBo(csboValue);
      } else {
        return;
      }
    } else {
      return;
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private String getZS01PostCd(EntityManager entityManager, Admin admin, Data data) {
    String postCd = null;
    String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.POSTCD.BY_REQID_ADDRTYP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("ADDR_TYPE", "ZS01");
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      postCd = results.get(0);
    }
    return postCd;
  }

  private String getCSBOByPostCd(EntityManager entityManager, Data data, String postalCdZS01) {
    String csboValue = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.CSBO_BY_POSTLCD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("POST_CD", postalCdZS01);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      csboValue = results.get(0);
    }
    return csboValue;
  }

  private void setAccountAbbNmAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String accntAbbNmValue = null;
    String sql = ExternalizedQuery.getSql("QUERY.GET.ACCOUNT_CUST_NM3");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("ADDR_TYPE", "ZS01");
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      accntAbbNmValue = results.get(0);
      data.setAbbrevNm(accntAbbNmValue);
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "ADDR_TXT", "DEPT", "OFFICE", "POST_CD", "BLDG", "CUST_PHONE",
        "LOCN_CD", "CUST_FAX", "ESTAB_FUNC_CD", "COMPANY_SIZE", "CONTACT", "ROL", "PO_BOX_CITY"));
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  protected String[] splitName(String name1, String name2, int length1, int length2) {
    String name = name1 + " " + (name2 != null ? name2 : "");
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
    String namePart3 = "";

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
    if (namePart2.length() > 35) {

      String[] tmpAr = namePart2.split(" ");
      int idxStart = 0;
      String ret = "";
      for (int i = 0; i < tmpAr.length; i++) {
        ret = ret + " " + tmpAr[i];
        idxStart = i + 1;
        String base = ret + " " + tmpAr[i + 1];
        if (base.length() > 35) {
          break;
        }
      }

      String temp1 = ret;
      namePart2 = temp1.trim();

      for (int i = idxStart; i < tmpAr.length; i++) {
        namePart3 = namePart3 + " " + tmpAr[i];
      } // namePart3 = temp2;

      namePart3 = namePart3.trim();

      LOG.debug("namePart2 >>> " + namePart2);
      System.out.println("idxStart >>> " + idxStart);
      System.out.println("namePart3 >>>" + namePart3);

    }

    return new String[] { namePart1, namePart2, namePart3 };

  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  /**
   * Queries CRIS and retrieves the account with the given No via full account
   * search
   * 
   * @param accountNo
   * @return
   * @throws Exception
   */
  private CRISAccount findAccountFromCRIS(String accountNo) throws Exception {
    LOG.debug("Querying CRIS for Account No. " + accountNo);
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    CRISServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, CRISServiceClient.class);
    CRISFullAccountRequest accountRequest = new CRISFullAccountRequest(accountNo);
    CRISQueryResponse response = client.executeAndWrap(CRISServiceClient.QUERY_APP_ID, accountRequest, CRISQueryResponse.class);
    if (response.isSuccess() && response.getData() != null && "F".equals(response.getData().getResultType())) {
      return response.getData().getAccount();
    } else {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }

  }

  /**
   * Queries CRIS and retrieves the company with the given No
   * 
   * @param companyNo
   * @return
   * @throws Exception
   */
  private CRISCompany findCompanyFromCRIS(String companyNo) throws Exception {
    LOG.debug("Querying CRIS for Company No. " + companyNo);
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    CRISServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, CRISServiceClient.class);
    CRISQueryRequest queryRequest = new CRISQueryRequest();
    queryRequest.setCompanyNo(companyNo);
    CRISQueryResponse response = client.executeAndWrap(CRISServiceClient.QUERY_APP_ID, queryRequest, CRISQueryResponse.class);
    if (response.isSuccess() && response.getData() != null && "C".equals(response.getData().getResultType())) {
      return response.getData().getCompanies() != null && response.getData().getCompanies().size() > 0 ? response.getData().getCompanies().get(0)
          : null;
    } else {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }
  }

  /**
   * Queries CRIS and retrieves the establishment with the given No
   * 
   * @param establishmentNo
   * @return
   * @throws Exception
   */
  private CRISEstablishment findEstablishmentFromCRIS(String establishmentNo) throws Exception {
    LOG.debug("Querying CRIS for Establishment No. " + establishmentNo);
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    CRISServiceClient client = CmrServicesFactory.getInstance().createClient(baseUrl, CRISServiceClient.class);
    CRISQueryRequest queryRequest = new CRISQueryRequest();
    queryRequest.setEstablishmentNo(establishmentNo);
    CRISQueryResponse response = client.executeAndWrap(CRISServiceClient.QUERY_APP_ID, queryRequest, CRISQueryResponse.class);
    if (response.isSuccess() && response.getData() != null && "E".equals(response.getData().getResultType())) {
      return response.getData().getEstablishments() != null && response.getData().getEstablishments().size() > 0
          ? response.getData().getEstablishments().get(0) : null;
    } else {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }
  }

  /**
   * Deletes an existing address without change log
   * 
   * @param entityManager
   * @param reqId
   * @param type
   * @param seq
   */
  private void removeCurrentAddr(EntityManager entityManager, long reqId, String type) {
    String sql = ExternalizedQuery.getSql("ADDRESS.REMOVE_CURRENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    query.setParameter("TYPE", type);
    // query.setParameter("SEQ", seq);
    // LOG.debug("Removing address " + type + "/" + seq + " from Request ID " +
    // reqId);
    LOG.debug("Removing address " + type + " from Request ID " + reqId);
    query.executeSql();

    sql = ExternalizedQuery.getSql("ADDRESS.REMOVE_CURRENT.RDC");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    query.setParameter("TYPE", type);
    // query.setParameter("SEQ", seq);
    // LOG.debug("Removing RDC address " + type + "/" + seq +
    // " from Request ID " + reqId);
    LOG.debug("Removing RDC address " + type + " from Request ID " + reqId);
    query.executeSql();

  }

  private void removeParentReferences(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.REMOVE_PARENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    LOG.debug("Removing parent references for Request ID " + reqId);
    query.executeSql();

  }

  /**
   * Deletes an existing address without change log
   * 
   * @param entityManager
   * @param reqId
   * @param type
   * @param seq
   */
  private void removeOtherAddresses(EntityManager entityManager, long reqId, String type) {
    String sql = ExternalizedQuery.getSql("ADDRESS.REMOVE_EXCEPT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    query.setParameter("TYPE", type);
    LOG.debug("Removing all address types for Request ID " + reqId + " which are not " + type);
    query.executeSql();
  }

  /**
   * Locates the equivalent {@link CRISAddress} record on the current
   * {@link CRISAccount} on this handler for the given rdcType
   * 
   * @param rdcType
   * @return
   */
  private CRISAddress getAddress(String rdcType) {
    if (this.currentAccount == null || this.currentAccount.getAddresses() == null) {
      return null;
    }
    String legacyType = RDC_TO_LEGACY_ADDR_TYPE_MAP.get(rdcType);
    if (legacyType == null) {
      return null;
    }
    for (CRISAddress address : this.currentAccount.getAddresses()) {
      if (legacyType.equals(address.getAddrType()) || address.getAddrType().contains(legacyType)) {
        return address;
      }
    }
    return null;
  }

  private void setProdTypeCheckedOnImport(Data data) {
    String prodType = data.getProdType();
    if (prodType != null && prodType.length() >= 8) {
      data.setCodCondition(prodType.substring(0, 1));
      data.setRemoteCustInd(prodType.substring(1, 2));
      data.setDecentralizedOptIndc(prodType.substring(2, 3));
      data.setImportActIndc(prodType.substring(3, 4));
      data.setMailingCondition(prodType.substring(4, 5));
      data.setSizeCd(prodType.substring(5, 6));
      data.setFootnoteNo(prodType.substring(6, 7));
      data.setFomeZero(prodType.substring(7, 8));
    }
  }

  private void setProdTypeOnSave(Data data) {
    String prodType = "";
    String prodType1 = "1".equals(data.getCodCondition()) ? "1" : "0";
    String prodType2 = "1".equals(data.getRemoteCustInd()) ? "1" : "0";
    String prodType3 = "1".equals(data.getDecentralizedOptIndc()) ? "1" : "0";
    String prodType4 = "1".equals(data.getImportActIndc()) ? "1" : "0";
    String prodType5 = "1".equals(data.getMailingCondition()) ? "1" : "0";
    String prodType6 = "1".equals(data.getSizeCd()) ? "1" : "0";
    String prodType7 = "1".equals(data.getFootnoteNo()) ? "1" : "0";
    String prodType8 = "1".equals(data.getFomeZero()) ? "1" : "0";
    prodType = prodType1 + prodType2 + prodType3 + prodType4 + prodType5 + prodType6 + prodType7 + prodType8;
    data.setProdType(prodType);
  }

  private DefaultApprovals getDefaultApprovals(EntityManager entityManager, String reqType) {
    String sql1 = ExternalizedQuery.getSql("APPROVAL.GETSOADMINDEFAPPROVALS");
    PreparedQuery query1 = new PreparedQuery(entityManager, sql1);
    query1.setParameter("REQUEST_TYP", reqType);
    return query1.getSingleResult(DefaultApprovals.class);

  }

  private DefaultApprovalRecipients getDefaultApprovalRecipients(EntityManager entityManager, long defaultApprovalId) {
    String sql = ExternalizedQuery.getSql("APPROVAL.GETSOADMINRECIPIENTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("DEFAULT_APPROVAL_ID", defaultApprovalId);
    return query.getSingleResult(DefaultApprovalRecipients.class);

  }

  @Override
  public ApprovalReq handleBPMANAGERApproval(EntityManager entityManager, long reqId, ApprovalReq approver, DefaultApprovals defaultApprovals,
      DefaultApprovalRecipients recipients, AppUser user, RequestEntryModel model) throws CmrException, SQLException {
    ApprovalReq theApprovalReq = saveAproval(entityManager, reqId, approver, defaultApprovals, recipients, user);
    // String theSOUserId = null;
    // boolean flag = false;
    // if (model.getReqType().equals("C")
    // && (model.getCustSubGrp() != null &&
    // (model.getCustSubGrp().equals("STOSC") ||
    // model.getCustSubGrp().equals("STOSI") || model.getCustSubGrp()
    // .equals("STOSB")))) {
    // DefaultApprovals defaultApprovalsSOADMIND =
    // getDefaultApprovals(entityManager, model.getReqType());
    // if (defaultApprovalsSOADMIND != null) {
    // DefaultApprovalRecipients recipients4SODMIN =
    // getDefaultApprovalRecipients(entityManager,
    // defaultApprovalsSOADMIND.getId()
    // .getDefaultApprovalId());
    // if (recipients4SODMIN != null)
    // theSOUserId = recipients4SODMIN.getId().getIntranetId();
    // }
    // flag = true;
    // } else if ((model.getReqType().equals("U")
    // && model.getCustClass() != null
    // && model.getCustClass().equals("81")
    // && (model.getSalesBusOffCd() != null &&
    // (model.getSalesBusOffCd().equals("53") ||
    // model.getSalesBusOffCd().equals("52")
    // || model.getSalesBusOffCd().equals("54") ||
    // model.getSalesBusOffCd().equals("56") ||
    // model.getSalesBusOffCd().equals("58") || model
    // .getSalesBusOffCd().equals("96"))) && (model.getCustAcctType() != null &&
    // model.getCustAcctType().equals("OU")
    // || model.getCustAcctType() == null ||
    // model.getCustAcctType().equals("")))) {
    //
    // DefaultApprovals defaultApprovalsSOADMIND =
    // getDefaultApprovals(entityManager, model.getReqType());
    //
    // if (defaultApprovalsSOADMIND != null) {
    // DefaultApprovalRecipients recipients4SODMIN =
    // getDefaultApprovalRecipients(entityManager,
    // defaultApprovalsSOADMIND.getId()
    // .getDefaultApprovalId());
    // if (recipients4SODMIN != null) {
    // ApprovalReq approver4SOADMIN = new ApprovalReq();
    // saveAproval(entityManager, reqId, approver4SOADMIN,
    // defaultApprovalsSOADMIND, recipients4SODMIN, user);
    // theSOUserId = recipients4SODMIN.getId().getIntranetId();
    // }
    // }
    // flag = true;
    // }
    //
    // if (flag && theSOUserId != null || !flag) {
    if (theApprovalReq != null) {
      Person ibmer = null;
      // to do get first line manager when deliver to prod, switch to this
      // BluePagesHelper.getManagerEmail([user.getUserCnum()]/[theSOUserId ] )
      // if (theSOUserId != null) {
      // ibmer =
      // BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(user.getUserCnum()));
      // } else {
      String originatorIdInAdmin = getOriginatorIdInAdmin(entityManager, reqId);

      if (originatorIdInAdmin != null && !originatorIdInAdmin.isEmpty()) {
        Person ibmerManager = null;
        ibmerManager = BluePagesHelper.getPerson(originatorIdInAdmin);
        ibmer = BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(ibmerManager == null ? "" : ibmerManager.getEmployeeId()));
      } else {
        ibmer = BluePagesHelper.getPerson(BluePagesHelper.getManagerEmail(user.getUserCnum()));
      }

      // }

      if (ibmer != null && theApprovalReq != null) {

        theApprovalReq.setIntranetId(ibmer.getEmail());
        theApprovalReq.setNotesId(ibmer.getNotesEmail());
        theApprovalReq.setDisplayName(ibmer.getName());

        entityManager.merge(theApprovalReq);
        entityManager.flush();
      }

    }
    return theApprovalReq;
  }

  private ApprovalReq saveAproval(EntityManager entityManager, long reqId, ApprovalReq approver, DefaultApprovals defaultApprovals,
      DefaultApprovalRecipients recipients, AppUser user) throws CmrException, SQLException {

    long approverId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "APPROVAL_ID", "CREQCMR");
    ApprovalReqPK approverPk = new ApprovalReqPK();
    approverPk.setApprovalId(approverId);
    approver.setId(approverPk);
    approver.setReqId(reqId);
    approver.setTypId(defaultApprovals.getTypId());
    approver.setGeoCd(defaultApprovals.getGeoCd());
    approver.setIntranetId(recipients.getId().getIntranetId());
    approver.setNotesId(recipients.getNotesId());
    approver.setDisplayName(recipients.getDisplayName());
    approver.setStatus(CmrConstants.APPROVAL_PENDING_MAIL);
    approver.setCreateBy(user.getIntranetId());
    approver.setLastUpdtBy(user.getIntranetId());
    approver.setCreateTs(SystemUtil.getCurrentTimestamp());
    approver.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    approver.setRequiredIndc(CmrConstants.APPROVAL_DEFLT_REQUIRED_INDC);
    entityManager.persist(approver);
    entityManager.flush();

    return approver;
  }

  private String getOriginatorIdInAdmin(EntityManager entityManager, long reqId) {

    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPk);

    String originatorIdInAdmin = null;
    originatorIdInAdmin = admin.getOriginatorId();
    return originatorIdInAdmin;
  };

  public static boolean isJPIssuingCountry(String issuingCntry) {
    if (SystemLocation.JAPAN.equals(issuingCntry))
      return true;
    else
      return false;
  }

  public static boolean isClearDPL(AddressModel model, Addr addr, EntityManager entityManager) {
    String aCustEnName = addr.getCustNm3() != null ? addr.getCustNm3().trim().toLowerCase() : "";
    String mCustEnName = model.getCustNm3() != null ? model.getCustNm3().trim().toLowerCase() : "";
    if (!StringUtils.equals(aCustEnName, mCustEnName))
      return true;
    return false;

  }

  @Override
  public String[] dividingCustName1toName2(String name1, String name2) {
    String[] nameArray = new String[2];
    if (name1 != null && name1.length() > 15 || name1 != null && name2 != null && (name1.length() + name2.length()) > 30
        || name2 != null && name2.length() > 15) {
      String nameTotal = name1 + (name2 == null ? "" : name2);
      nameArray[0] = nameTotal.substring(0, 15);
      nameArray[1] = nameTotal.substring(15).length() > 15 ? nameTotal.substring(15, 30) : nameTotal.substring(15);
    } else {
      nameArray[0] = name1;
      nameArray[1] = name2;
    }
    return nameArray;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
}
