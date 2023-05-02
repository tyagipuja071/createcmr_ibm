/**
 *
 */
package com.ibm.cio.cmr.request.service.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.DeleteReactivate;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.MassCreate;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.GeoContactInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoTaxInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.LicenseModel;
import com.ibm.cio.cmr.request.model.window.MassDataSummaryModel;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.AdminService;
import com.ibm.cio.cmr.request.service.requestentry.LicenseService;
import com.ibm.cio.cmr.request.service.requestentry.TaxInfoService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;

/**
 * @author Jeffrey Zamora
 *
 */
@Component
public class RequestSummaryService extends BaseSimpleService<RequestSummaryModel> {
  private static Logger LOG = Logger.getLogger(RequestSummaryService.class);
  public static final String TYPE_CUSTOMER = "C";
  public static final String TYPE_IBM = "IBM";

  @Override
  public RequestSummaryModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String sql = ExternalizedQuery.getSql("REQUEST.SUMMARY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", params.getParam("reqId"));
    query.setForReadOnly(true);
    List<CompoundEntity> results = query.getCompundResults(Data.class, Data.REQUEST_SUMMARY_MAPPING);
    if (results != null && results.size() > 0) {
      CompoundEntity entity = results.get(0);
      Admin admin = entity.getEntity(Admin.class);
      Data data = entity.getEntity(Data.class);
      RequestSummaryModel summary = new RequestSummaryModel();
      summary.setAdmin(admin);
      summary.setData(data);
      summary.setCountry((String) entity.getValue("COUNTRY_DESC"));
      summary.setProcessingDesc((String) entity.getValue("PROCESSING_DESC"));
      summary.setIsicDesc((String) entity.getValue("ISIC_DESC"));

      if (LAHandler.isBRIssuingCountry(data.getCmrIssuingCntry())) {
        summary.setProxyLocnDesc(LAHandler.getProxiLocnDesc(entityManager, data.getCmrIssuingCntry(), data.getProxiLocnNo()));
      }

      return summary;
    }
    return null;
  }

  /**
   * gets the list of Updated fields for the request's name/address data
   *
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<UpdatedNameAddrModel> getUpdatedNameAddr(EntityManager entityManager, long reqId) throws CmrException {
    try {
      List<UpdatedNameAddrModel> results = new ArrayList<UpdatedNameAddrModel>();

      String sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setForReadOnly(true);

      List<UpdatedAddr> updatedRecords = query.getResults(UpdatedAddr.class);

      // add the removed addresses
      sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR.REMOVED");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setForReadOnly(true);

      List<UpdatedAddr> removedRecords = query.getResults(UpdatedAddr.class);

      // Israel mismatch legacy - RDC address
      sql = ExternalizedQuery.getSql("GET_REQ_ISSUING_CNTRY");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      String cmrIssuingCntry = query.getSingleResult(String.class);

      List<UpdatedAddr> mismatchRecords = null;
      if (StringUtils.isNotEmpty(cmrIssuingCntry) && SystemLocation.ISRAEL.equals(cmrIssuingCntry)) {
        sql = ExternalizedQuery.getSql("IL.SUMMARY.NAMEADDR.MISMATCH");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", reqId);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setForReadOnly(true);

        mismatchRecords = query.getResults(UpdatedAddr.class);
      }

      List<UpdatedAddr> records = new ArrayList<UpdatedAddr>();
      if (updatedRecords != null) {
        if (mismatchRecords != null && mismatchRecords.size() > 0) {
          // mark mismatch address with L as importInd
          for (UpdatedAddr updatedAddr : updatedRecords) {
            for (UpdatedAddr mismatchAddr : mismatchRecords) {
              String updatedAddrSeq = updatedAddr.getId().getAddrSeq();
              if (StringUtils.isNotEmpty(updatedAddrSeq) && updatedAddrSeq.equals(mismatchAddr.getId().getAddrSeq())
                  && !"N".equals(updatedAddr.getImportInd())) {
                updatedAddr.setImportInd("L");
              }
            }
          }
        }
        records.addAll(updatedRecords);
      }
      if (removedRecords != null) {
        records.addAll(removedRecords);
      }

      if (records != null && records.size() > 0) {
        Map<String, String> addressTypes = new HashMap<>();
        if (records.size() > 0) {
          addressTypes = getAddressTypes(records.get(0).getCmrCountry(), entityManager);
        }
        for (UpdatedAddr addr : records) {
          parseNameAddrDiff(addressTypes, addr, results, entityManager);
        }
      }

      // add removed addresses
      sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR.REMOVED");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setForReadOnly(true);

      return results;

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        LOG.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    }
  }

  /**
   * gets the list of Updated fields for the request's name/address data
   *
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<UpdatedNameAddrModel> getUpdatedNameAddr(long reqId) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      return getUpdatedNameAddr(entityManager, reqId);
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  /**
   * Gets the list of Updated fields for the request's data
   *
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<UpdatedDataModel> getUpdatedData(EntityManager entityManager, Data newData, long reqId, String type) throws CmrException {
    try {
      List<UpdatedDataModel> results = new ArrayList<UpdatedDataModel>();

      String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setForReadOnly(true);

      String cmrCountry = newData.getCmrIssuingCntry();
      GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrCountry);
      UpdatedDataModel update = null;
      List<DataRdc> records = query.getResults(DataRdc.class);

      String reqType = getReqType(entityManager, reqId);

      transformCheckbox(newData);
      if (records != null && records.size() > 0) {
        for (DataRdc oldData : records) {
          if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevNm(), newData.getAbbrevNm())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "AbbrevName"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevName", "-"));
            update.setNewData(newData.getAbbrevNm());
            update.setOldData(oldData.getAbbrevNm());
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getAffiliate(), newData.getAffiliate())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Affiliate"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "Affiliate", "-"));
            update.setNewData(newData.getAffiliate());
            update.setOldData(oldData.getAffiliate());
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getBpRelType(), newData.getBpRelType())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "BPRelationType"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "BPRelationType", "-"));
            update.setNewData(getCodeAndDescription(newData.getBpRelType(), "BPRelationType", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getBpRelType(), "BPRelationType", cmrCountry));
            results.add(update);
          }
          if (TYPE_CUSTOMER.equals(type) && (StringUtils.isNotBlank(newData.getCapInd()) && !equals(oldData.getCapInd(), newData.getCapInd()))
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CAP"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "CAP", "-"));
            update.setNewData(newData.getCapInd());
            update.setOldData(oldData.getCapInd());
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getCmrIssuingCntry(), newData.getCmrIssuingCntry())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CMRIssuingCountry"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "CMRIssuingCountry", "-"));
            update.setNewData(getCodeAndDescription(newData.getCmrIssuingCntry(), "CMRIssuingCountry", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getCmrIssuingCntry(), "CMRIssuingCountry", cmrCountry));
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getCmrNo(), newData.getCmrNo())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CMRNumber"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "CMRNumber", "-"));
            update.setNewData(newData.getCmrNo());
            update.setOldData(oldData.getCmrNo());
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getCmrOwner(), newData.getCmrOwner())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CMROwner"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "CMROwner", "-"));
            update.setNewData(getCodeAndDescription(newData.getCmrOwner(), "CMROwner", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getCmrOwner(), "CMROwner", cmrCountry));
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getCompany(), newData.getCompany())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Company"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "Company", "-"));
            update.setNewData(newData.getCompany());
            update.setOldData(oldData.getCompany());
            results.add(update);
          }
          if (!"848".equals(oldData.getCmrIssuingCntry()) && !SystemLocation.GERMANY.equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_IBM.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustClassCode"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "CustClassCode", "-"));
              update.setNewData(getCodeAndDescription(newData.getCustClass(), "CustClassCode", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getCustClass(), "CustClassCode", cmrCountry));
              results.add(update);
            }
          }
          if (!"848".equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustPrefLang(), newData.getCustPrefLang())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustLang"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "CustLang", "-"));
              update.setNewData(getCodeAndDescription(newData.getCustPrefLang(), "CustLang", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getCustPrefLang(), "CustLang", cmrCountry));
              if ("624".equalsIgnoreCase(cmrCountry)) {
                if ("Swedish".equalsIgnoreCase(update.getNewData()))
                  update.setNewData("Dutch");
                if ("Swedish".equalsIgnoreCase(update.getOldData()))
                  update.setOldData("Dutch");
                LOG.info("For 624, Swedish is replaced with Dutch.");
              }
              results.add(update);
            }
          }
          // CMR-2093:Turkey - Requirement for CoF (Comercial Financed) field
          if ("862".equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CommercialFinanced"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
              update.setNewData(getCodeAndDescription(newData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
              results.add(update);
            }
          }
          if ("897".equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_IBM.equals(type) && !equals(oldData.getEducAllowCd(), newData.getEducAllowCd())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "EducAllowCd"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "EducAllowCd", "-"));
              update.setNewData(newData.getEducAllowCd());
              update.setOldData(oldData.getEducAllowCd());
              results.add(update);
            }

            if (TYPE_IBM.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "OrdBlk"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "OrdBlk", "-"));
              update.setNewData(newData.getOrdBlk());
              update.setOldData(oldData.getOrdBlk());
              results.add(update);
            }

            if (TYPE_IBM.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "SpecialTaxCd"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "SpecialTaxCd", "-"));
              update.setNewData(newData.getSpecialTaxCd());
              update.setOldData(oldData.getSpecialTaxCd());
              results.add(update);
            }

            if (TYPE_IBM.equals(type) && !equals(oldData.getTaxExempt2(), newData.getTaxExemptStatus2())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "TaxExempt2"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "TaxExempt2", "-"));
              update.setNewData(newData.getTaxExemptStatus2());
              update.setOldData(oldData.getTaxExempt2());
              results.add(update);
            }
            if (TYPE_IBM.equals(type) && !equals(oldData.getTaxExempt3(), newData.getTaxExemptStatus3())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "TaxExempt3"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "TaxExempt3", "-"));
              update.setNewData(newData.getTaxExemptStatus3());
              update.setOldData(oldData.getTaxExempt3());
              results.add(update);
            }

          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getInacCd(), newData.getInacCd()) && !LAHandler.isBRIssuingCountry(cmrCountry)
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "INACCode"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "INACCode", "-"));
            update.setNewData(newData.getInacCd());
            update.setOldData(oldData.getInacCd());
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getInacType(), newData.getInacType()) && !LAHandler.isBRIssuingCountry(cmrCountry)
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "INACType"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "INACType", "-"));
            update.setNewData(getCodeAndDescription(newData.getInacType(), "INACType", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getInacType(), "INACType", cmrCountry));
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getIsuCd(), newData.getIsuCd())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "ISU"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "ISU", "-"));
            update.setNewData(getCodeAndDescription(newData.getIsuCd(), "ISU", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getIsuCd(), "ISU", cmrCountry));
            results.add(update);
          }

          if (!LAHandler.isSSAMXBRIssuingCountry(oldData.getCmrIssuingCntry())) {
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(), newData.getTaxCd1())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "LocalTax1"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
              update.setNewData(newData.getTaxCd1());
              update.setOldData(oldData.getTaxCd1());
              results.add(update);
            }
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "LocalTax2"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
              update.setNewData(newData.getTaxCd2());
              update.setOldData(oldData.getTaxCd2());
              results.add(update);
            }
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getVat(), newData.getVat())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "VAT"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "VAT", "-"));
              update.setNewData(newData.getVat());
              update.setOldData(oldData.getVat());
              results.add(update);
            }
            /*
             * if (TYPE_CUSTOMER.equals(type) &&
             * (StringUtils.isNoneBlank(oldData.getVatInd()) &&
             * !equals(oldData.getVatInd(), newData.getVatInd())) && (geoHandler
             * == null || !geoHandler.skipOnSummaryUpdate(cmrCountry,
             * "VATInd"))) { update = new UpdatedDataModel();
             * update.setDataField(PageManager.getLabel(cmrCountry, "VATInd",
             * "-")); update.setNewData(newData.getVatInd());
             * update.setOldData(oldData.getVatInd()); results.add(update); }
             */
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxPayerCustCd(), newData.getTaxPayerCustCd())
                && !CmrConstants.REQ_TYPE_UPDATE.equals(reqType)
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "PSTExemptLicNum"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "PSTExemptLicNum", "-"));
              update.setNewData(newData.getTaxPayerCustCd());
              update.setOldData(oldData.getTaxPayerCustCd());
              results.add(update);
            }
            if (TYPE_IBM.equals(type) && !equals(oldData.getEnterprise(), newData.getEnterprise())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Enterprise"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "Enterprise", "-"));
              update.setNewData(newData.getEnterprise());
              update.setOldData(oldData.getEnterprise());
              results.add(update);
            }
            if (TYPE_IBM.equals(type) && !equals(oldData.getSearchTerm(), newData.getSearchTerm())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "SearchTerm"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "SearchTerm", "-"));
              update.setNewData(newData.getSearchTerm());
              update.setOldData(oldData.getSearchTerm());
              results.add(update);
            }
            if (TYPE_IBM.equals(type) && !equals(oldData.getClientTier(), newData.getClientTier())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "ClientTier"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "ClientTier", "-"));
              update.setNewData(getCodeAndDescription(newData.getClientTier(), "ClientTier", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getClientTier(), "ClientTier", cmrCountry));
              results.add(update);
            }
          }

          /*
           * if ("848".equals(oldData.getCmrIssuingCntry())) { if
           * (TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(),
           * newData.getTaxCd1()) && (geoHandler == null ||
           * !geoHandler.skipOnSummaryUpdate(cmrCountry, "CollectionCd"))) {
           * update = new UpdatedDataModel();
           * update.setDataField(PageManager.getLabel(cmrCountry,
           * "CollectionCd", "-"));
           * update.setNewData(getCodeAndDescription(newData.getCollectionCd(),
           * "CollectionCd", cmrCountry));
           * update.setOldData(getCodeAndDescription(oldData.getCollectionCd(),
           * "CollectionCd", cmrCountry)); results.add(update); }
           */

          if (LAHandler.isLACountry(oldData.getCmrIssuingCntry()) && !LAHandler.isARIssuingCountry(oldData.getCmrIssuingCntry())) {
            // if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getVat(),
            // newData.getVat())
            // && (geoHandler == null ||
            // !geoHandler.skipOnSummaryUpdate(cmrCountry, "VAT"))) {
            // update = new UpdatedDataModel();
            // update.setDataField(PageManager.getLabel(cmrCountry, "VAT",
            // "-"));
            // update.setNewData(newData.getVat());
            // update.setOldData(oldData.getVat());
            // results.add(update);
            // }
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(), newData.getTaxCd1())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "LocalTax1"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
              update.setNewData(newData.getTaxCd1());
              update.setOldData(oldData.getTaxCd1());
              results.add(update);
            }
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getMemLvl(), newData.getMemLvl())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "MembLevel"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "MembLevel", "-"));
            update.setNewData(getCodeAndDescription(newData.getMemLvl(), "MembLevel", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getMemLvl(), "MembLevel", cmrCountry));
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getPpsceid(), newData.getPpsceid())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "PPSCEID"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "PPSCEID", "-"));
            update.setNewData(newData.getPpsceid());
            update.setOldData(oldData.getPpsceid());
            results.add(update);
          }
          if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getSensitiveFlag(), newData.getSensitiveFlag())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "SensitiveFlag"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "SensitiveFlag", "-"));
            update.setNewData(getCodeAndDescription(newData.getSensitiveFlag(), "SensitiveFlag", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getSensitiveFlag(), "SensitiveFlag", cmrCountry));
            results.add(update);
          }
          if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getIsicCd(), newData.getIsicCd())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "ISIC"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "ISIC", "-"));
            update.setNewData(getCodeAndDescription(newData.getIsicCd(), "ISIC", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getIsicCd(), "ISIC", cmrCountry));
            results.add(update);
          }
          if (TYPE_IBM.equals(type) && !equals(oldData.getSitePartyId(), newData.getSitePartyId()) && !CmrConstants.REQ_TYPE_UPDATE.equals(reqType)
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "SitePartyID"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "SitePartyID", "-"));
            update.setNewData(newData.getSitePartyId());
            update.setOldData(oldData.getSitePartyId());
            results.add(update);
          }
          if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getSubIndustryCd(), newData.getSubIndustryCd())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Subindustry"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "Subindustry", "-"));
            update.setNewData(getCodeAndDescription(newData.getSubIndustryCd(), "Subindustry", cmrCountry));
            update.setOldData(getCodeAndDescription(oldData.getSubIndustryCd(), "Subindustry", cmrCountry));
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getBgId(), newData.getBgId())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "BuyingGroupID"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "BuyingGroupID", "-"));
            update.setNewData(newData.getBgId());
            update.setOldData(oldData.getBgId());
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getGbgId(), newData.getGbgId())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "GlobalBuyingGroupID"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "GlobalBuyingGroupID", "-"));
            update.setNewData(newData.getGbgId());
            update.setOldData(oldData.getGbgId());
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getBgRuleId(), newData.getBgRuleId())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "BGLDERule"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "BGLDERule", "-"));
            update.setNewData(newData.getBgRuleId());
            update.setOldData(oldData.getBgRuleId());
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getCovId(), newData.getCovId())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CoverageID"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "CoverageID", "-"));
            update.setNewData(newData.getCovId());
            update.setOldData(oldData.getCovId());
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getGeoLocCd(), newData.getGeoLocationCd()) && !CmrConstants.REQ_TYPE_UPDATE.equals(reqType)
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "GeoLocationCode"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "GeoLocationCode", "-"));
            update.setNewData(newData.getGeoLocationCd());
            update.setOldData(oldData.getGeoLocCd());
            results.add(update);
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getDunsNo(), newData.getDunsNo())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "DUNS"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "DUNS", "-"));
            update.setNewData(newData.getDunsNo());
            update.setOldData(oldData.getDunsNo());
            results.add(update);
          }

          if ("838".equals(oldData.getCmrIssuingCntry()) || "822".equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CollectionCd"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
              update.setNewData(getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
              results.add(update);
            }

            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getMailingCondition(), newData.getMailingCondition())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "MailingCond"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "MailingCond", "-"));
              update.setNewData(newData.getMailingCondition());
              update.setOldData(oldData.getMailingCondition());
              results.add(update);
            }
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getLegacyCurrencyCd(), newData.getLegacyCurrencyCd())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CurrencyCd"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "CurrencyCd", "-"));
              update.setNewData(newData.getLegacyCurrencyCd());
              update.setOldData(oldData.getLegacyCurrencyCd());
              results.add(update);
            }
          }

          if (!("760".equals(oldData.getCmrIssuingCntry()) || "864".equals(oldData.getCmrIssuingCntry())
              || "649".equals(oldData.getCmrIssuingCntry()))) {
            if (TYPE_IBM.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CodFlag"))) {
              String dataField = PageManager.getLabel(cmrCountry, "CodFlag", "-");
              if (!StringUtils.isEmpty(dataField) && !"-".equals(dataField)) {
                update = new UpdatedDataModel();
                update.setDataField(dataField);
                update.setNewData(newData.getCreditCd());
                update.setOldData(oldData.getCreditCd());
                results.add(update);
              }
            }
          }

          if ("726".equals(oldData.getCmrIssuingCntry())) {
            if (TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())
                && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "ModeOfPayment"))) {
              update = new UpdatedDataModel();
              update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
              update.setNewData(getCodeAndDescription(newData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
              update.setOldData(getCodeAndDescription(oldData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
              results.add(update);
            }
          }

          if (TYPE_IBM.equals(type) && !equals(oldData.getMilitary(), newData.getMilitary())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Military"))) {
            update = new UpdatedDataModel();
            update.setDataField(PageManager.getLabel(cmrCountry, "Military", "-"));
            update.setNewData("Y".equals(newData.getMilitary()) ? "Yes" : "");
            update.setOldData("Y".equals(oldData.getMilitary()) ? "Yes" : "");
            results.add(update);
          }

          if (geoHandler != null) {
            geoHandler.addSummaryUpdatedFields(this, type, cmrCountry, newData, oldData, results);
          }

        }
      }

      return results;

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        e.printStackTrace();
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    }
  }

  /**
   * Gets the list of Updated fields for the request's data
   *
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<UpdatedDataModel> getUpdatedData(Data newData, long reqId, String type) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      return getUpdatedData(entityManager, newData, reqId, type);
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  /**
   * Parses the difference between name/addresses
   *
   * @param addr
   * @param results
   */
  private void parseNameAddrDiff(Map<String, String> addressTypes, UpdatedAddr addr, List<UpdatedNameAddrModel> results,
      EntityManager entityManager) {
    UpdatedNameAddrModel update = null;
    String sapNumber = addr.getSapNo();
    String addrType = addr.getId().getAddrType();
    String cmrCountry = addr.getCmrCountry();
    String seqNo = addr.getId().getAddrSeq();

    String addrTypeDesc = addressTypes.get(addrType);
    if (StringUtils.isEmpty(addrTypeDesc)) {
      addrTypeDesc = DropdownListController.getDescription("AddressType", addrType, cmrCountry);
    }

    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrCountry);

    if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
      addrTypeDesc += " (" + addr.getId().getAddrSeq() + ")";
    }

    if ("X".equals(addr.getImportInd())) {
      update = new UpdatedNameAddrModel();
      update.setAddrTypeCode(addrType);
      update.setAddrType(addrTypeDesc);
      update.setAddrSeq(seqNo);
      update.setSapNumber("[removed]");
      update.setDataField("- Address Removed -");
      results.add(update);
    } else if ("L".equals(addr.getImportInd())) {
      update = new UpdatedNameAddrModel();
      update.setAddrTypeCode(addrType);
      update.setAddrType(addrTypeDesc);
      update.setAddrSeq(seqNo);
      update.setSapNumber("Existing DB2 Address not in RDC");
      update.setDataField("All fields");
      results.add(update);
    } else if (StringUtils.isEmpty(addr.getSapNo()) && !"Y".equals(addr.getImportInd()) && !"L".equals(addr.getImportInd())) {
      update = new UpdatedNameAddrModel();
      update.setAddrTypeCode(addrType);
      update.setAddrType(addrTypeDesc);
      update.setAddrSeq(seqNo);
      update.setSapNumber("[new]");
      update.setDataField("All fields");
      results.add(update);
    } else {
      if (!equals(addr.getSapNo(), addr.getSapNoOld())) {
        update = new UpdatedNameAddrModel();
        update.setAddrTypeCode(addrType);
        update.setAddrType(addrTypeDesc);
        update.setAddrSeq(seqNo);
        update.setSapNumber(addr.getSapNo());
        update.setDataField(PageManager.getLabel(cmrCountry, "SAPNumber", "-"));
        update.setNewData(addr.getSapNo());
        update.setOldData(addr.getSapNoOld());
        results.add(update);
      } else {
        if (geoHandler != null && geoHandler.customerNamesOnAddress()
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName1"))) {
          if (!equals(addr.getCustNm1(), addr.getCustNm1Old())) {
            update = new UpdatedNameAddrModel();
            update.setAddrTypeCode(addrType);
            update.setAddrType(addrTypeDesc);
            update.setAddrSeq(seqNo);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName1", "-"));
            update.setNewData(addr.getCustNm1());
            update.setOldData(addr.getCustNm1Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm2(), addr.getCustNm2Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName2"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrTypeCode(addrType);
            update.setAddrType(addrTypeDesc);
            update.setAddrSeq(seqNo);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName2", "-"));
            update.setNewData(addr.getCustNm2());
            update.setOldData(addr.getCustNm2Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm3(), addr.getCustNm3Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName3"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrTypeCode(addrType);
            update.setAddrType(addrTypeDesc);
            update.setAddrSeq(seqNo);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName3", "-"));
            update.setNewData(addr.getCustNm3());
            update.setOldData(addr.getCustNm3Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm4(), addr.getCustNm4Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName4"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrTypeCode(addrType);
            update.setAddrType(addrTypeDesc);
            update.setAddrSeq(seqNo);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName4", "-"));
            update.setNewData(addr.getCustNm4());
            update.setOldData(addr.getCustNm4Old());
            results.add(update);
          }
          // if (!equals(addr.getCustNm3(), addr.getCustNm3Old())) {
          // update = new UpdatedNameAddrModel();
          // update.setAddrType(DropdownListController.getDescription("AddressType",
          // addrType, cmrCountry));
          // update.setSapNumber(sapNumber);
          // update.setDataField(PageManager.getLabel(cmrCountry,
          // "CustomerName1", "-"));
          // update.setDataField(update.getDataField().replace("1", "3"));
          // update.setNewData(addr.getCustNm3());
          // update.setOldData(addr.getCustNm3Old());
          // results.add(update);
          // }
          // if (!equals(addr.getCustNm4(), addr.getCustNm4Old())) {
          // update = new UpdatedNameAddrModel();
          // update.setAddrType(DropdownListController.getDescription("AddressType",
          // addrType, cmrCountry));
          // update.setSapNumber(sapNumber);
          // update.setDataField(PageManager.getLabel(cmrCountry,
          // "CustomerName1", "-"));
          // update.setDataField(update.getDataField().replace("1", "4"));
          // update.setNewData(addr.getCustNm4());
          // update.setOldData(addr.getCustNm4Old());
          // results.add(update);
          // }
        }
        if (!equals(addr.getAddrTxt(), addr.getAddrTxtOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StreetAddress1"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress1", "-"));
          update.setNewData(addr.getAddrTxt());
          update.setOldData(addr.getAddrTxtOld());
          results.add(update);
        }
        if (!equals(addr.getCity1(), addr.getCity1Old()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "City1"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "City1", "-"));
          update.setNewData(addr.getCity1());
          update.setOldData(addr.getCity1Old());
          results.add(update);
        }
        if (!equals(addr.getCity2(), addr.getCity2Old()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "City2"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "City2", "-"));
          update.setNewData(addr.getCity2());
          update.setOldData(addr.getCity2Old());
          results.add(update);
        }
        if (!equals(addr.getStateProv(), addr.getStateProvOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StateProv"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StateProv", "-"));
          update.setNewData(addr.getStateProv());
          update.setOldData(addr.getStateProvOld());
          results.add(update);
        }
        if (!equals(addr.getPostCd(), addr.getPostCdOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "PostalCode"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "PostalCode", "-"));
          update.setNewData(addr.getPostCd());
          update.setOldData(addr.getPostCdOld());
          results.add(update);
        }
        if (!equals(addr.getLandCntry(), addr.getLandCntryOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "LandedCountry"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LandedCountry", "-"));
          update.setNewData(addr.getLandCntry());
          update.setOldData(addr.getLandCntryOld());
          results.add(update);
        }
        if (!equals(addr.getCounty(), addr.getCountyOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "County"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "County", "-"));
          update.setNewData(addr.getCounty());
          update.setOldData(addr.getCountyOld());
          results.add(update);
        }
        if (!equals(addr.getBldg(), addr.getBldgOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Building"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Building", "-"));
          update.setNewData(addr.getBldg());
          update.setOldData(addr.getBldgOld());
          results.add(update);
        }
        if (!equals(addr.getFloor(), addr.getFloorOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Floor"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Floor", "-"));
          update.setNewData(addr.getFloor());
          update.setOldData(addr.getFloorOld());
          results.add(update);
        }
        if (!equals(addr.getOffice(), addr.getOfficeOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Office"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Office", "-"));
          update.setNewData(addr.getOffice());
          update.setOldData(addr.getOfficeOld());
          results.add(update);
        }
        if (!equals(addr.getDept(), addr.getDeptOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Department"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Department", "-"));
          update.setNewData(addr.getDept());
          update.setOldData(addr.getDeptOld());
          results.add(update);
        }
        if (!equals(addr.getPoBox(), addr.getPoBoxOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBox"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBox", "-"));
          update.setNewData(addr.getPoBox());
          update.setOldData(addr.getPoBoxOld());
          results.add(update);
        }
        // No POBox City and POBox Postal Code Summary for PT, CY, GR, SP & UKI
        if (!(SystemLocation.CYPRUS.equals(cmrCountry) || SystemLocation.SPAIN.equals(cmrCountry) || SystemLocation.PORTUGAL.equals(cmrCountry)
            || SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.UNITED_KINGDOM.equals(cmrCountry)
            || SystemLocation.IRELAND.equals(cmrCountry)) && !equals(addr.getPoBoxCity(), addr.getPoBoxCityOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBoxCity"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBoxCity", "-"));
          update.setNewData(addr.getPoBoxCity());
          update.setOldData(addr.getPoBoxCityOld());
          results.add(update);
        }
        if (!(SystemLocation.CYPRUS.equals(cmrCountry) || SystemLocation.SPAIN.equals(cmrCountry) || SystemLocation.PORTUGAL.equals(cmrCountry)
            || SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.UNITED_KINGDOM.equals(cmrCountry)
            || SystemLocation.IRELAND.equals(cmrCountry)) && !equals(addr.getPoBoxPostCd(), addr.getPoBoxPostCdOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBoxPostalCode"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBoxPostalCode", "-"));
          update.setNewData(addr.getPoBoxPostCd());
          update.setOldData(addr.getPoBoxPostCdOld());
          results.add(update);
        }
        if (!equals(addr.getCustFax(), addr.getCustFaxOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustFAX"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "CustFAX", "-"));
          update.setNewData(addr.getCustFax());
          update.setOldData(addr.getCustFaxOld());
          results.add(update);
        }
        if (!equals(addr.getCustLangCd(), addr.getCustLangCdOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustLangCd"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "CustLangCd", "-"));
          update.setNewData(addr.getCustLangCd());
          update.setOldData(addr.getCustLangCdOld());
          results.add(update);
        }
        if (!equals(addr.getCustPhone(), addr.getCustPhoneOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustPhone"))) {
          if (!"ZS01".equals(addr.getId().getAddrType()) && SystemLocation.TURKEY.equals(cmrCountry)) {
            // if Turkey and non sold-to address, do nothing
          } else {
            update = new UpdatedNameAddrModel();
            update.setAddrTypeCode(addrType);
            update.setAddrType(addrTypeDesc);
            update.setAddrSeq(seqNo);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustPhone", "-"));
            update.setNewData(addr.getCustPhone());
            update.setOldData(addr.getCustPhoneOld());
            results.add(update);
          }
        }
        if (!equals(addr.getTransportZone(), addr.getTransportZoneOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "TransportZone"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "TransportZone", "-"));
          update.setNewData(addr.getTransportZone());
          update.setOldData(addr.getTransportZoneOld());
          results.add(update);
        }
        if (!equals(addr.getDivn(), addr.getDivnOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Division"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Division", "-"));
          update.setNewData(addr.getDivn());
          update.setOldData(addr.getDivnOld());
          results.add(update);
        }
        if (!equals(addr.getAddrTxt2(), addr.getAddrTxt2Old())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StreetAddress2"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(addrTypeDesc);
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress2", "-"));
          update.setNewData(addr.getAddrTxt2());
          update.setOldData(addr.getAddrTxt2Old());
          results.add(update);
        }
        /*
         * LA Defect 1299028: Request Summary Commented but will be uncommented
         * after 8/26 release
         */
        // addr tax code 1
        if (!equals(addr.getTaxCd1(), addr.getTaxCd1Old())) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(DropdownListController.getDescription("AddressType", addrType, cmrCountry));
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
          update.setNewData(addr.getTaxCd1());
          update.setOldData(addr.getTaxCd1Old());
          results.add(update);
        }
        // addr tax code 2
        if (!equals(addr.getTaxCd2(), addr.getTaxCd2Old())) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrType(DropdownListController.getDescription("AddressType", addrType, cmrCountry));
          update.setAddrSeq(seqNo);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
          update.setNewData(addr.getTaxCd2());
          update.setOldData(addr.getTaxCd2Old());
          results.add(update);
        }
        // vat
        if (!equals(addr.getVat(), addr.getVatOld())) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrSeq(seqNo);
          update.setAddrType(DropdownListController.getDescription("AddressType", addrType, cmrCountry));
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "VAT", "-"));
          update.setNewData(addr.getVat());
          update.setOldData(addr.getVatOld());
          results.add(update);
        }

        // ExtWalletId
        if (!equals(addr.getExtWalletId(), addr.getExtWalletIdOld())) {
          update = new UpdatedNameAddrModel();
          update.setAddrTypeCode(addrType);
          update.setAddrSeq(seqNo);
          update.setAddrType(DropdownListController.getDescription("AddressType", addrType, cmrCountry));
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "ExtWalletId", "-"));
          update.setNewData(addr.getExtWalletId());
          update.setOldData(addr.getExtWalletIdOld());
          results.add(update);
        }

        if (geoHandler != null) {
          geoHandler.addSummaryUpdatedFieldsForAddress(this, cmrCountry, addrTypeDesc, sapNumber, addr, results, entityManager);
        }
      }
    }
  }

  /**
   * Checks absolute equality between the strings
   *
   * @param val1
   * @param val2
   * @return
   */
  public boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isBlank(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isBlank(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  /**
   * Gets the code and description value
   *
   * @param code
   * @param fieldId
   * @return
   */
  public String getCodeAndDescription(String code, String fieldId, String cntry) {
    String desc = DropdownListController.getDescription(fieldId, code, cntry);
    if (!StringUtils.isEmpty(desc)) {
      return desc;
    }
    return code;
  }

  /**
   * Get the sold to details
   *
   * @param summary
   * @param summary
   */
  public RequestSummaryModel getSoldToDetails(RequestSummaryModel summary) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      String sql = ExternalizedQuery.getSql("REQUEST.SUMMARY_SOLDTO");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", summary.getAdmin().getId().getReqId());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<CompoundEntity> results = query.getCompundResults(1, Addr.class, Addr.SOLD_TO_MAPPING);
      if (results != null && results.size() > 0) {
        CompoundEntity entity = results.get(0);
        Addr addr = entity.getEntity(Addr.class);
        summary.setAddr(addr);
        summary.setAddrtypetxt((String) entity.getValue("ADDR_TYPE_TXT"));
        summary.setLandedcountry((String) entity.getValue("Landed_COUNTRY"));
        summary.setStateprovdesc((String) entity.getValue("STATE_PROV_DESC"));
        summary.setCountyDesc((String) entity.getValue("COUNTY_CD_DESC"));

        if (summary != null && summary.getData() != null && CNHandler.isCNIssuingCountry(summary.getData().getCmrIssuingCntry())) {
          AddressService aService = new AddressService();
          IntlAddr iAddr = new IntlAddr();
          GeoContactInfo cInfo = new GeoContactInfo();

          iAddr = aService.getIntlAddrById(addr, entityManager);
          summary.setCnCustName1(iAddr != null ? iAddr.getIntlCustNm1() : "");
          summary.setCnCustName2(iAddr != null ? iAddr.getIntlCustNm2() : "");
          summary.setCnAddrTxt(iAddr != null ? iAddr.getAddrTxt() : "");
          summary.setCnAddrTxt2(iAddr != null ? iAddr.getIntlCustNm4() : "");
          summary.setCnDistrict(iAddr != null ? iAddr.getCity2() : "");
          summary.setCnCity(iAddr != null ? iAddr.getCity1() : "");

          cInfo = aService.getGeoContactInfoById(addr, entityManager);
          summary.setCnCustContNm(cInfo != null ? cInfo.getContactName() : "");
          summary.setCnCustContJobTitle(cInfo != null ? cInfo.getContactFunc() : "");
          summary.setCnCustContPhone2(cInfo != null ? cInfo.getContactPhone() : "");
        }

      }

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return summary;
  }

  /**
   * Check if other address exist for request
   *
   * @param reqId
   * @param boolean
   */
  public boolean checkForOtherAddr(long reqId) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    boolean otheraddrexist = false;
    try {

      String sql = ExternalizedQuery.getSql("REQUEST.SUMMARY_Other_addr");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      otheraddrexist = query.exists();

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return otheraddrexist;
  }

  /**
   * gets the list of Mass Update records
   *
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<MassDataSummaryModel> getMassData(HttpServletRequest request, long reqId, boolean massCreate, boolean isReactDel) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      List<MassDataSummaryModel> results = new ArrayList<MassDataSummaryModel>();

      if (massCreate) {
        String sql = ExternalizedQuery.getSql("GET.MASS.CREATE");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("PAR_REQ_ID", reqId);

        List<MassCreate> records = query.getResults(MassCreate.class);
        setMassCreateDataList(records, results);
      } else if (isReactDel) {
        String sql = ExternalizedQuery.getSql("GET.DELETE_REACTIVATE");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("PAR_REQ_ID", reqId);

        List<DeleteReactivate> records = query.getResults(DeleteReactivate.class);
        setMassReactDelDataList(records, results);
      } else {
        String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("PAR_REQ_ID", reqId);

        List<MassUpdt> records = query.getResults(MassUpdt.class);
        setMassUpdateDataList(records, results);
      }

      return results;

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        LOG.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  private void setMassUpdateDataList(List<MassUpdt> records, List<MassDataSummaryModel> results) {
    if (records != null && records.size() > 0) {
      MassDataSummaryModel massData = null;
      for (MassUpdt massUpdt : records) {
        massData = new MassDataSummaryModel();
        massData.setCmrNo(massUpdt.getCmrNo());
        massData.setIterationId(massUpdt.getId().getIterationId());
        massData.setSeqNo(massUpdt.getId().getSeqNo());
        if (massUpdt.getRowStatusCd() != null && "PASS".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("Automatic Processing Completed");
        } else if (massUpdt.getRowStatusCd() != null && "FAIL".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("Error (Automatic Processing)");
        } else if (massUpdt.getRowStatusCd() != null && "READY".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("Ready for Processing");
        } else if (massUpdt.getRowStatusCd() != null && "RDCER".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("Error (RDc Processing)");
        } else if (massUpdt.getRowStatusCd() != null && "DONE".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("All Processing Completed");
        } else if (massUpdt.getRowStatusCd() != null && "LDONE".equals(massUpdt.getRowStatusCd().trim())) {
          massData.setStatus("Legacy Direct Processing Completed");
        } else {
          massData.setStatus("");
          LOG.debug("Invalid mass update request status : " + massUpdt.getRowStatusCd());
        }
        massData.setErrorTxt(massUpdt.getErrorTxt());
        results.add(massData);
      }
    }
  }

  private void setMassReactDelDataList(List<DeleteReactivate> records, List<MassDataSummaryModel> results) {
    if (records != null && records.size() > 0) {
      MassDataSummaryModel massData = null;

      for (DeleteReactivate delReact : records) {
        massData = new MassDataSummaryModel();
        massData.setCmrNo(delReact.getCmrNo());
        massData.setIterationId(delReact.getId().getIterationId());
        massData.setSeqNo(delReact.getId().getSeqNo());
        massData.setName(delReact.getName());
        massData.setOrderBlock(delReact.getOrderBlock());
        massData.setDeleted("X".equals(delReact.getDeleted()) ? "Yes" : "");
        if (delReact.getRowStatusCd() != null && "PASS".equals(delReact.getRowStatusCd().trim())) {
          massData.setStatus("Automatic Processing Completed");
        } else if (delReact.getRowStatusCd() != null && "FAIL".equals(delReact.getRowStatusCd().trim())) {
          massData.setStatus("Error (Automatic Processing)");
        } else if (delReact.getRowStatusCd() != null && "READY".equals(delReact.getRowStatusCd().trim())) {
          massData.setStatus("Ready for Processing");
        } else if (delReact.getRowStatusCd() != null && "RDCER".equals(delReact.getRowStatusCd().trim())) {
          massData.setStatus("Error (RDc Processing)");
        } else if (delReact.getRowStatusCd() != null && "DONE".equals(delReact.getRowStatusCd().trim())) {
          massData.setStatus("All Processing Completed");
        } else {
          massData.setStatus("");
          LOG.debug("Invalid mass update request status : " + delReact.getRowStatusCd());
        }
        massData.setErrorTxt(delReact.getErrorTxt());
        results.add(massData);
      }
    }
  }

  private void setMassCreateDataList(List<MassCreate> records, List<MassDataSummaryModel> results) {
    if (records != null && records.size() > 0) {
      MassDataSummaryModel massData = null;
      for (MassCreate massCreate : records) {
        massData = new MassDataSummaryModel();
        massData.setCmrNo(massCreate.getCmrNo());
        massData.setIterationId(massCreate.getId().getIterationId());
        massData.setSeqNo(massCreate.getId().getSeqNo());
        if (massCreate.getRowStatusCd() != null && CmrConstants.MASS_CREATE_ROW_STATUS_PASS.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("Automatic Processing Completed");
        } else if (massCreate.getRowStatusCd() != null && CmrConstants.MASS_CREATE_ROW_STATUS_FAIL.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("Error (Automatic Processing)");
        } else if (massCreate.getRowStatusCd() != null
            && CmrConstants.MASS_CREATE_ROW_STATUS_UPDATE_FAILE.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("Error (Automatic Update)");
        } else if (massCreate.getRowStatusCd() != null && CmrConstants.MASS_CREATE_ROW_STATUS_READY.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("Ready for Processing");
        } else if (massCreate.getRowStatusCd() != null && CmrConstants.MASS_CREATE_ROW_STATUS_RDC_ERROR.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("Error (RDc Processing)");
        } else if (massCreate.getRowStatusCd() != null && CmrConstants.MASS_CREATE_ROW_STATUS_DONE.equals(massCreate.getRowStatusCd().trim())) {
          massData.setStatus("All Processing Completed");
        } else {
          massData.setStatus("");
          LOG.debug("Invalid mass update request status : " + massCreate.getRowStatusCd());
        }
        massData.setErrorTxt(massCreate.getErrorTxt());
        results.add(massData);
      }
    }
  }

  private Map<String, String> getAddressTypes(String cntry, EntityManager entityManager) {
    HashMap<String, String> map = new HashMap<>();
    String sql = ExternalizedQuery.getSql("SUMMARY.GET_ADDRESS_TYPES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    List<Object[]> results = query.getResults();
    if (results != null) {
      for (Object[] result : results) {
        map.put((String) result[0], (String) result[1]);
      }
    }
    return map;
  }

  public List<GeoTaxInfoModel> getCurrentTaxInfoDetails(long reqId, String issuingCntry) {
    EntityManager em = JpaManager.getEntityManager();
    List<GeoTaxInfoModel> taxInfo = new ArrayList<GeoTaxInfoModel>();
    GeoTaxInfoModel inputParam = new GeoTaxInfoModel();
    TaxInfoService taxService = new TaxInfoService();
    inputParam.setReqId(reqId);

    try {
      taxInfo = taxService.getCurrTaxInfo(inputParam, em, issuingCntry);
    } catch (Exception e) {
      LOG.error("An error has occured in retrieving tax info values coming from the CROS query service.");
      e.printStackTrace();
    }

    return taxInfo;
  }

  public List<GeoContactInfoModel> getAddlContactDetails(HttpServletRequest request, long reqId, String issuingCntry) throws CmrException {
    EntityManager em = JpaManager.getEntityManager();
    List<GeoContactInfoModel> contactLstDto = new ArrayList<GeoContactInfoModel>();
    String sql = ExternalizedQuery.getSql("CONTACTINFO.FINDALL");
    GeoContactInfoModel contDto = null;
    try {
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", reqId);
      query.append("ORDER BY CONTACT_TYPE");
      query.setForReadOnly(true);
      List<GeoContactInfo> ent = query.getResults(GeoContactInfo.class);
      if (ent != null && ent.size() != 0) {
        for (GeoContactInfo resultEntity : ent) {
          contDto = new GeoContactInfoModel();
          copyContactInfoDetails(resultEntity, contDto);
          contactLstDto.add(contDto);
        }
      }
    } catch (Exception ex) {
      LOG.error(ex.getMessage(), ex);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    } finally {
      em.clear();
      em.close();
    }
    return contactLstDto;
  }

  public List<GeoContactInfoModel> getCurrentContactInfoDetails(String reqId, String issuingCntry, String cmr) throws CmrException {
    EntityManager em = JpaManager.getEntityManager();
    DataRdc origContacts = LegacyCommonUtil.getOldData(em, reqId);
    List<GeoContactInfoModel> contactLstDto = new ArrayList<GeoContactInfoModel>();

    String email1 = "";
    String email2 = "";
    String email3 = "";

    try {
      if (origContacts != null) {
        email1 = StringUtils.isNotBlank(origContacts.getEmail1()) ? origContacts.getEmail1() : "";
        email2 = StringUtils.isNotBlank(origContacts.getEmail2()) ? origContacts.getEmail2() : "";
        email3 = StringUtils.isNotBlank(origContacts.getEmail3()) ? origContacts.getEmail3() : "";
      }

      String sql = ExternalizedQuery.getSql("CONTACTINFO.FINDALL");
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", reqId);
      query.append("ORDER BY CONTACT_TYPE");
      query.setForReadOnly(true);

      // Get contact info detail values from the DB
      List<GeoContactInfo> currentContacts = query.getResults(GeoContactInfo.class);
      List<String> currentSeqNum = new ArrayList<>();

      if (currentContacts != null & currentContacts.size() > 0) {
        for (GeoContactInfo c : currentContacts) {
          GeoContactInfoModel newModel = new GeoContactInfoModel();

          newModel.setContactEmail(c.getContactEmail());
          newModel.setContactName(c.getContactName());
          newModel.setContactType(c.getContactType());
          newModel.setContactPhone(c.getContactPhone());
          newModel.setContactSeqNum(c.getContactSeqNum());

          if ("EM".equals(c.getContactType())) {
            currentSeqNum.add(c.getContactSeqNum());
            if ("001".equals(c.getContactSeqNum()) && !email1.equals(c.getContactEmail())) {
              newModel.setRemoved("ADDED");
              contactLstDto.add(getRemovedContact(email1, "001"));
            } else if ("002".equals(c.getContactSeqNum()) && !email2.equals(c.getContactEmail())) {
              newModel.setRemoved("ADDED");
              contactLstDto.add(getRemovedContact(email2, "002"));
            } else if ("003".equals(c.getContactSeqNum()) && !email3.equals(c.getContactEmail())) {
              newModel.setRemoved("ADDED");
              contactLstDto.add(getRemovedContact(email3, "003"));
            }
          }
          contactLstDto.add(newModel);
        }
      }

      // Get removed contact info detail values only
      if (StringUtils.isNotBlank(email1) && !currentSeqNum.contains("001")) {
        contactLstDto.add(getRemovedContact(email1, "001"));
      } else if (StringUtils.isNotBlank(email2) && !currentSeqNum.contains("002")) {
        contactLstDto.add(getRemovedContact(email2, "002"));
      } else if (StringUtils.isNotBlank(email3) && !currentSeqNum.contains("003")) {
        contactLstDto.add(getRemovedContact(email3, "003"));
      }

    } catch (Exception e) {
      LOG.error("An error has occured in retrieving contact info detail values coming from the query service.");
      e.printStackTrace();
    }

    return contactLstDto;
  }

  private GeoContactInfoModel getRemovedContact(String email, String seqNum) {
    GeoContactInfoModel removed = new GeoContactInfoModel();
    removed.setContactEmail(email);
    removed.setContactName("N");
    removed.setContactType("EM");
    removed.setContactPhone(".");
    removed.setRemoved("REMOVED");
    removed.setContactSeqNum(seqNum);
    return removed;
  }

  private void copyContactInfoDetails(GeoContactInfo from, GeoContactInfoModel to) {
    to.setContactType(from.getContactType());
    to.setContactName(from.getContactName());
    to.setContactEmail(from.getContactEmail());
    to.setContactPhone(from.getContactPhone());
    to.setContactSeqNum(from.getContactSeqNum());
    to.setReqId(from.getId().getReqId());
    to.setContactInfoId(from.getId().getContactInfoId());
    to.setCreateById(from.getCreateById());
    to.setCreateTs(from.getCreateTs());
    to.setUpdtById(from.getUpdtById());
    to.setUpdtTs(from.getUpdtTs());
  }

  public List<MassDataSummaryModel> getDplSummarryData(HttpServletRequest request, long reqId) throws CmrException {
    List<MassDataSummaryModel> results = new ArrayList<MassDataSummaryModel>();
    EntityManager entityManager = JpaManager.getEntityManager();
    AdminService adminSvc = new AdminService();

    try {
      Admin admin = adminSvc.getCurrentRecordById(reqId, entityManager);
      String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ADDR.BY.ID.EMEA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("PAR_REQ_ID", reqId);
      query.setParameter("ITERATION_ID", admin.getIterationId());
      List<MassUpdtAddr> records = query.getResults(MassUpdtAddr.class);
      setMassUpdateDplDataList(records, results);
    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        LOG.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return results;
  }

  private void setMassUpdateDplDataList(List<MassUpdtAddr> records, List<MassDataSummaryModel> results) {
    if (records != null && records.size() > 0) {
      MassDataSummaryModel massData = null;
      for (MassUpdtAddr massUpdtAddr : records) {
        massData = new MassDataSummaryModel();
        massData.setCmrNo(massUpdtAddr.getCmrNo());
        massData.setIterationId(massUpdtAddr.getId().getIterationId());
        massData.setSeqNo(massUpdtAddr.getId().getSeqNo());

        if ("P".equals(massUpdtAddr.getDplChkResult())) {
          massData.setDplChkStatus("Pass");
        } else if ("F".equals(massUpdtAddr.getDplChkResult())) {
          massData.setDplChkStatus("Fail");
        }

        massData.setDplChkTS(String.valueOf(massUpdtAddr.getDplChkTimestamp()));
        results.add(massData);
      }
    }
  }

  private void transformCheckbox(Data data) throws Exception {

    String cmrCountry = data.getCmrIssuingCntry();

    if (SystemLocation.UNITED_STATES.equals(cmrCountry)) {
      if (!StringUtils.isEmpty(data.getRestrictTo())) {
        data.setRestrictInd(CmrConstants.YES_NO.Y.toString());
      } else {
        data.setRestrictInd(CmrConstants.YES_NO.N.toString());
      }
      if (!CmrConstants.YES_NO.Y.toString().equals(data.getFedSiteInd())) {
        data.setFedSiteInd(CmrConstants.YES_NO.N.toString());
      }
      if (!CmrConstants.YES_NO.Y.toString().equals(data.getOemInd())) {
        data.setOemInd(CmrConstants.YES_NO.N.toString());
      }
      if (!CmrConstants.YES_NO.Y.toString().equals(data.getOutCityLimit())) {
        data.setOutCityLimit(CmrConstants.YES_NO.N.toString());
      }
    }
  }

  public String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.CA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
  }

  public List<LicenseModel> getNewLicenses(HttpServletRequest request, long reqId) {
    LicenseService service = new LicenseService();
    EntityManager entityManager = JpaManager.getEntityManager();
    return service.getNewLicenses(reqId, entityManager);
  }

  public List<LicenseModel> getNewLicenses(EntityManager entityManager, long reqId) throws CmrException {
    LicenseService service = new LicenseService();
    return service.getNewLicenses(reqId, entityManager);
  }

}
