/**
 * 
 */
package com.ibm.cio.cmr.request.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.BdsTblInfo;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.DropdownModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

/**
 * @author Rangoli Saxena
 * 
 */
@Component
public class DropDownService extends BaseSimpleService<DropdownModel> {

  // private static final Logger LOG = Logger.getLogger(DropDownService.class);

  // private static final List<String> BDS_NO_MANDT = Arrays.asList(new String[]
  // { "T002T", "V_BP_MBR_LVL_TYPE", "V_BP_REL_TYPE_CD" });

  public DropdownModel getValuesStandAlone(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws CmrException {
    return doProcess(entityManager, request, params);
  }

  @Override
  public DropdownModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws CmrException {
    DropdownModel model = null;

    String queryId = (String) params.getParam("queryId");
    if ("BDS".equalsIgnoreCase(queryId)) {
      model = processBDSDropdownSql(entityManager, request, params);
    } else if ("LOV".equalsIgnoreCase(queryId)) {
      model = processLOVDropdownSql(entityManager, request, params);
    } else {
      model = processGenericDropdownSql(entityManager, request, params);
    }
    return model;
  }

  /**
   * For Generic SQL dropdowns
   * 
   * @param entityManager
   * @param request
   * @param params
   * @return
   */
  private DropdownModel processGenericDropdownSql(EntityManager entityManager, HttpServletRequest request, ParamContainer params) {
    String queryId = (String) params.getParam("queryId");

    DropdownModel model = null;
    List<DropdownItemModel> itemList = new ArrayList<DropdownItemModel>();

    String sql = ExternalizedQuery.getSql(queryId.toUpperCase());
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    if ("APPROVAL_TYP".equalsIgnoreCase(queryId) || "CMR_INTERNAL_TYPES".equalsIgnoreCase(queryId)) {
      String cmrCntry = (String) params.getParam("cmrIssuingCntry");
      if (cmrCntry == null) {
        cmrCntry = AppUser.getUser(request).getCmrIssuingCntry();
      }
      query.setParameter("CMR_CNTRY", cmrCntry);
    }
    query.setForReadOnly(true);

    for (String param : params.getParameterNames("queryId")) {
      query.setParameter(param, request.getParameter(param));
    }

    List<Object[]> results = query.getResults(-1);
    if (results != null && results.size() > 0) {
      Object[] row = null;
      model = new DropdownModel();
      model.setIdentifier("id");
      model.setLabel("name");
      DropdownItemModel blankOption = new DropdownItemModel();
      blankOption.setId("");
      blankOption.setName("");
      model.addItems(blankOption); // blank option
      DropdownItemModel item = null;
      for (Object obj : results) {
        row = (Object[]) obj;
        item = new DropdownItemModel();
        item.setName(row[1] != null ? row[1].toString() : "");
        item.setName(escapeText(item.getName()));
        item.setId(row[0] != null ? row[0].toString() : "");
        itemList.add(item);
        model.addItems(item);
      }
    }

    return model;
  }

  /**
   * For BDS Dropdown lists
   * 
   * @param entityManager
   * @param request
   * @param params
   * @return
   */
  private DropdownModel processBDSDropdownSql(EntityManager entityManager, HttpServletRequest request, ParamContainer params) {

    String fieldId = (String) params.getParam("fieldId");
    if (fieldId == null) {
      return new DropdownModel();
    }

    DropdownModel model = null;
    List<DropdownItemModel> itemList = new ArrayList<DropdownItemModel>();

    PreparedQuery query = getBDSSql(fieldId, entityManager, params, null);

    if (query != null) {
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults(-1);
      if (results != null && results.size() > 0) {
        Object[] row = null;
        model = new DropdownModel();
        model.setIdentifier("id");
        model.setLabel("name");
        DropdownItemModel blankOption = new DropdownItemModel();
        blankOption.setId("");
        blankOption.setName("");
        model.addItems(blankOption); // blank option
        DropdownItemModel item = null;
        List<String> codes = new ArrayList<String>();
        for (Object obj : results) {
          row = (Object[]) obj;
          if (!codes.contains(row[0].toString())) {
            item = new DropdownItemModel();
            item.setName(row[1] != null ? row[1].toString() : "");
            item.setName(escapeText(item.getName()));
            item.setId(row[0] != null ? row[0].toString() : "");
            itemList.add(item);
            model.addItems(item);
            codes.add(item.getId());
          }
        }
      }
    }

    if (model != null && model.getItems().size() == 2) {
      // automatically choose the first field if the only other option is blank
      if (StringUtils.isBlank(model.getItems().get(0).getId()) && StringUtils.isBlank(model.getItems().get(0).getName())) {
        model.setSelectedItem(model.getItems().get(1).getId());
      }
    }
    return model;
  }

  /**
   * Constructs the BDS table retrieval sql
   * 
   * @param fieldId
   * @param entityManager
   * @return
   */
  public PreparedQuery getBDSSql(String fieldId, EntityManager entityManager, ParamContainer params, String country) {
    String sql = ExternalizedQuery.getSql("BDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("FIELD_ID", "##" + fieldId);
    List<BdsTblInfo> info = query.getResults(1, BdsTblInfo.class);
    PreparedQuery bdsQuery = null;
    StringBuilder sb = new StringBuilder();
    if (info != null && info.size() > 0) {
      BdsTblInfo bds = info.get(0);
      if ("CMRIssuingCountry".equalsIgnoreCase(fieldId) && "Y".equals(params.getParam("newRequest"))) {
        // special handling
        sb.append("( ");
      }

      if (SystemLocation.UNITED_KINGDOM.equals(country) && ("SalesBusOffUK".equals(fieldId) || "SalRepNameNoUK".equals(fieldId))) {
        sb.append("select distinct trim(" + bds.getCd() + ") CD, ");
      } else {
        sb.append("select trim(" + bds.getCd() + ") CD, ");
      }

      if (CmrConstants.BDS_DISPLAYED_DATA.C.toString().equals(bds.getDispType())) {
        sb.append("trim(" + bds.getCd() + ")");
      } else if (CmrConstants.BDS_DISPLAYED_DATA.CD.toString().equals(bds.getDispType())) {
        sb.append("trim(" + bds.getCd() + ")" + "||' - '||trim(" + bds.getDesc() + ")");
      } else if (CmrConstants.BDS_DISPLAYED_DATA.D.toString().equals(bds.getDispType())) {
        sb.append("trim(" + bds.getDesc() + ")");
      } else if (CmrConstants.BDS_DISPLAYED_DATA.DC.toString().equals(bds.getDispType())) {
        sb.append("trim(" + bds.getDesc() + ")||' - '||trim(" + bds.getCd() + ")");
      }
      sb.append(" TXT from ").append(bds.getSchema() + "." + bds.getTbl());
      sb.append(" where length(" + bds.getCd() + ") > 0 ");
      // if ("897".equals((String) params.getParam("cmrIssuingCntry"))) {
      if ("County".equals(fieldId)) {
        sb.setLength(0);
      }
      // }
      // new REFT tables do not have MANDT. removing this for all.
      // if (!BDS_NO_MANDT.contains(bds.getTbl())) {
      // sb.append(" and MANDT = :MANDT");
      // }

      bdsQuery = new PreparedQuery(entityManager, sb.toString());

      appendSpecialFilters(entityManager, fieldId, bdsQuery, params, true, country);

      if ("CMRIssuingCountry".equalsIgnoreCase(fieldId) && "Y".equals(params.getParam("newRequest"))) {
        // special handling
        bdsQuery.append(" ) ");
      }

      if (!StringUtils.isEmpty(bds.getOrderByField())) {
        if (!"897".equals((String) params.getParam("cmrIssuingCntry"))) {
          if ("CMRIssuingCountry".equalsIgnoreCase(fieldId) && "Y".equals(params.getParam("newRequest"))) {
            // special handling
            bdsQuery.append(" order by CD asc");
          } else if ("Cluster".equals(fieldId) && PageManager.fromGeo("AP", (String) params.getParam("cmrIssuingCntry"))) {
            bdsQuery.append(
                " order by case when CLUSTER_DESC LIKE 'DEFAULT%' then 5 when CLUSTER_DESC LIKE 'Kyndryl%' then 4 when CLUSTER_DESC LIKE '%Ecosystem%' then 3 when CLUSTER_DESC LIKE 'ISA Select Core%' then 2 else 1 end");
          } else {
            if (!"County".equals(fieldId)) {
              bdsQuery.append(" order by " + bds.getOrderByField() + " asc");
            }
          }
        }
      }

      // bdsQuery.append(" ) a");
      bdsQuery.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    }

    return bdsQuery;
  }

  /**
   * Add extra where clauses for each Field ID
   * 
   * @param fieldId
   * @param sb
   */
  private void appendSpecialFilters(EntityManager entityManager, String fieldId, PreparedQuery query, ParamContainer params, boolean bds,
      String country) {
    String cntry = (String) params.getParam("cmrIssuingCntry");
    if ("CMRIssuingCountry".equalsIgnoreCase(fieldId)) {
      query.append("  and SYS_LOC_CD <> '100' ");
      // query.append(" and SPRAS = 'E' ");
      query.append("  and SYS_LOC_CD in (select CNTRY_CD from CREQCMR.SUPP_CNTRY) ");
      if ("Y".equals(params.getParam("newRequest"))) {
        query.append("  union  ");
        query.append("  select CD, TXT||' - '||CMR_ISSUING_CNTRY TXT ");
        query.append("  from CREQCMR.LOV ");
        query.append("  where FIELD_ID = '##SysLocExtension' ");
        query.append(" and CMR_ISSUING_CNTRY in (select CNTRY_CD from CREQCMR.SUPP_CNTRY) ");
      }
    }
    if ("StateProv".equalsIgnoreCase(fieldId)) {
      // support only US for now
      // Author: Dennis T Natad
      // Date: April 7, 2017
      // Project: createCMR LA Requirements
      // Defect 1180916: State/Province field should be optional for landed
      // country outside of LA
      // Description: Added LA Country support
      if ("897".equals(cntry)) {
        query.append("  and ((REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = :LAND1) and 'US' = :LAND1)");
        query.append("  or (REFT_COUNTRY_KEY = 0 and 'US' <> :LAND1))");
        query.setParameter("LAND1", params.getParam("landCntry"));
      } else if ("758".equalsIgnoreCase(cntry)) {
        // String dropDownCity = params.getParam("dropDownCity") != null ?
        // params.getParam("dropDownCity").toString() : "";
        String postalCode = params.getParam("postCd") != null ? params.getParam("postCd").toString() : "";
        if (!"".equals(postalCode)) {
          query.append(" and STATE_PROV_CD IN (select CD from creqcmr.lov where field_id='##StateProv' and cmr_issuing_cntry='758' and txt LIKE '%"
              + postalCode.substring(0, 2) + "%') and COMMENTS <> 'Italy CB'");
        } else {
          query.append(" and COMMENTS <> 'Italy CB'");
        }
        query.append(" and REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = :LAND1) ");
        query.setParameter("LAND1", params.getParam("landCntry"));
      } else if ("838".equalsIgnoreCase(cntry)) {
        // Story 1720159: State/Province should be automatically assigned based
        // on first two digits of Postal code
        String postalCode = params.getParam("postCd") != null ? params.getParam("postCd").toString() : "";
        if (!"".equals(postalCode) && "ES".equals(params.getParam("landCntry"))) {
          query.append(" and STATE_PROV_CD IN (select CD from creqcmr.lov where field_id='##StateProv' and cmr_issuing_cntry='838' and txt LIKE '%"
              + postalCode.substring(0, 2) + "%') and COMMENTS <> 'Spain CB'");
        } else {
          query.append(" and COMMENTS <> 'Spain CB'");
        }
        query.append("  and REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = :LAND1) ");
        query.setParameter("LAND1", params.getParam("landCntry"));
      } else {
        query.append("  and REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = :LAND1) ");
        query.setParameter("LAND1", params.getParam("landCntry"));
      }
    }

    if ("StateProvItaly".equalsIgnoreCase(fieldId)) {
      query.append(" and COMMENTS = 'Italy CB'");
      query.append(" and REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = 'IT') ");
      query.setParameter("LAND1", params.getParam("landCntry"));
    }

    if ("StateProvChina".equalsIgnoreCase(fieldId)) {
      query.append(" and LAND1 = :LAND1 ");
      query.append(" and MANDT = :MANDT ");
      String landCntryCN = params.getParam("landCntry").toString();
      if ("CN".equals(landCntryCN)) {
        query.append(" AND SUBSTRING((BLAND),1,1) > '9' ");
      }
      query.setParameter("LAND1", params.getParam("landCntry"));
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    }

    if ("TransportZone".equalsIgnoreCase(fieldId)) {
      // no dependency for now
      // query.append(" and SPRAS = 'E' ");
      // query.append(" and LAND1 = :LAND1 ");
      // query.setParameter("LAND1", params.getParam("landCntry"));
    }

    if ("County".equalsIgnoreCase(fieldId)) {
      // support only US for now
      // query.append(" and 'US' = :LAND1 ");
      // query.append(
      // " and REFT_STATE_PROV_KEY = (select REFT_STATE_PROV_KEY from
      // CMMA.REFT_STATE_PROV_W where STATE_PROV_CD = :REGIO and
      // REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W
      // where COUNTRY_CD = :LAND1)) ");
      // query.setParameter("REGIO", params.getParam("stateProv"));
      // query.setParameter("LAND1", params.getParam("landCntry"));

      query.append(
          "SELECT trim(LPAD(C_CNTY,3,0)) TXT, trim(N_CNTY) CD FROM CREQCMR.US_CMR_SCC WHERE LAND_CNTRY = :LAND1 AND N_ST = :N_ST GROUP BY N_CNTY, C_CNTY ORDER BY N_CNTY, C_CNTY ");
      query.setParameter("LAND1", params.getParam("landCntry"));
      query.setParameter("N_ST", params.getParam("stateProv"));
    }

    if ("Subindustry".equalsIgnoreCase(fieldId)) {
      String geo = "WW";
      if (SystemLocation.UNITED_STATES.equals(cntry)) {
        geo = "US";
      }
      if (!StringUtils.isEmpty((String) params.getParam("isicCd"))) {
        query.append("  and REFT_INDCL_KEY = (select REFT_INDCL_KEY from CMMA.REFT_UNSIC_W where REFT_UNSIC_CD = :ISIC and GEO_CD = '" + geo + "') ");
        query.setParameter("ISIC", params.getParam("isicCd"));
      } else {
        query.append("  and REFT_INDCL_KEY in (select REFT_INDCL_KEY from CMMA.REFT_UNSIC_W WHERE GEO_CD='" + geo + "') ");
      }
    }
    if ("ISIC".equalsIgnoreCase(fieldId)) {
      if (SystemLocation.UNITED_STATES.equals(cntry)) {
        query.append("  and GEO_CD = 'US' ");
      } else {
        query.append("  and GEO_CD = 'WW' ");
      }
      if (!StringUtils.isEmpty((String) params.getParam("subIndustryCd"))) {
        query.append("  and REFT_INDCL_KEY = (select REFT_INDCL_KEY from CMMA.REFT_INDCL_W where INDCL_CD = :INDCL_CD) ");
        query.setParameter("INDCL_CD", params.getParam("subIndustryCd"));
      } else if (!StringUtils.isEmpty((String) params.getParam("isicCd"))) {
        String isic = (String) params.getParam("isicCd");
        if (isic.toUpperCase().startsWith("LOV:")) {
          // format is LOV:##FieldName
          String lovFieldId = isic.split(":")[1].trim();
          String isicSql = ExternalizedQuery.getSql("LOV");
          PreparedQuery isicLovQuery = new PreparedQuery(entityManager, isicSql);
          isicLovQuery.setParameter("FIELD_ID", lovFieldId);
          isicLovQuery.setParameter("CMR_CNTRY", "*");
          List<Object[]> results = isicLovQuery.getResults();
          StringBuilder isicCodes = new StringBuilder();
          for (Object[] record : results) {
            isicCodes.append(isicCodes.length() > 0 ? "," : "");
            isicCodes.append("'").append(record[0].toString().trim()).append("'");
          }
          query.append("  and REFT_UNSIC_CD in (" + isicCodes.toString() + ")");
        } else {
          // handle likes, ranges, exclusions
          // like = 95%
          // range = 9501-9605
          // exclusion = ^900
          // can be a combination of like plus exclusion or range plus exclusion
          String[] isicValues = isic.split(",");
          StringBuilder orClause = new StringBuilder();
          int cnt = 0;
          for (String value : isicValues) {
            value = value.trim();
            if (value.contains("%")) {
              orClause.append(orClause.length() > 0 ? " or " : "");
              orClause.append(" REFT_UNSIC_CD like :ISIC" + cnt + " ");
              query.setParameter("ISIC" + cnt, value.trim());
              cnt++;
            } else if (value.contains("-")) {
              String[] range = value.split("-");
              orClause.append(orClause.length() > 0 ? " or " : "");
              orClause.append(" ( REFT_UNSIC_CD >= :ISIC" + cnt + " ");
              query.setParameter("ISIC" + cnt, range[0].trim());
              cnt++;
              orClause.append("  and REFT_UNSIC_CD <= :ISIC" + cnt + " ) ");
              query.setParameter("ISIC" + cnt, range[1].trim());
              cnt++;
            } else if (value.startsWith("^")) {
              query.append("  and REFT_UNSIC_CD <> :ISIC" + cnt + " ");
              query.setParameter("ISIC" + cnt, value.substring(1).trim());
              cnt++;
            }

          }
          if (orClause.length() > 0) {
            query.append(" and ( " + orClause.toString() + " )");
          }
        }
      }
    }
    if (fieldId.startsWith("CustomerSubGroup")) {
      query.append("  and REFT_USCMR_GRP_KEY = :GRP_KEY");
      query.setParameter("GRP_KEY", params.getParam("custGrp"));
    }

    if ("InternalDivDept".equals(fieldId)) {
      query.append(" and lov.TXT like :INTERNAL_DIV");
      query.setParameter("INTERNAL_DIV", params.getParam("div") + "%");
    }

    if ("CustomerType".equals(fieldId) && bds) {
      query.append("  and ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }

    // 1164466: Customer Type/Customer Sub Type scenario driven requirement for
    // LA
    /*
     * if ("CustomerSubType".equalsIgnoreCase(fieldId)) {
     * query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
     * query.append("  and CUST_TYP_VAL = :CUST_TYP_VAL");
     * query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
     * query.setParameter("CUST_TYP_VAL", params.getParam("custType")); }
     */
    if ("MrktChannelInd".equalsIgnoreCase(fieldId)) {
      query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
    }
    if ("SalesBusOff".equalsIgnoreCase(fieldId)) {
      if (!PageManager.fromGeo("EMEA", (String) params.getParam("cmrIssuingCntry"))
          && !PageManager.fromGeo("CEMEA", (String) params.getParam("cmrIssuingCntry"))
          && !PageManager.fromGeo("NORDX", (String) params.getParam("cmrIssuingCntry"))
          && !PageManager.fromGeo("BELUX", (String) params.getParam("cmrIssuingCntry"))
          && !PageManager.fromGeo("NL", (String) params.getParam("cmrIssuingCntry"))
          && !SystemLocation.PORTUGAL.equals(params.getParam("cmrIssuingCntry"))
          && !SystemLocation.CANADA.equals(params.getParam("cmrIssuingCntry"))) {
        query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
        query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
      }
      String cntry2 = (String) params.getParam("cmrIssuingCntry");

      if (cntry2 == null) {
        cntry2 = country;
      }

      if ("754".equals(cntry2) || "866".equals(cntry2)) {
        query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
        query.setParameter("ISSUING_CNTRY", cntry2);
      }
    }
    if ("SalesBusOffMU".equalsIgnoreCase(fieldId)) {
      query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
      query.setParameter("ISSUING_CNTRY", country);
    }
    if ("SalRepNameNo".equalsIgnoreCase(fieldId)) {
      String cntry2 = (String) params.getParam("cmrIssuingCntry");

      if (cntry2 == null) {
        cntry2 = country;
      }
      if (SystemLocation.PORTUGAL.equals(cntry) || "754".equals(cntry2)) {
        query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
        query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
      }
    }

    if ("SalesBusOffUK".equalsIgnoreCase(fieldId)) {
      String cntry2 = (String) params.getParam("cmrIssuingCntry");
      if (cntry2 == null) {
        cntry2 = country;
      }
      if ("866".equals(cntry2)) {
        query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
        query.setParameter("ISSUING_CNTRY", cntry2);
      }
    }

    if ("SalRepNameNoUK".equalsIgnoreCase(fieldId)) {
      String cntry2 = (String) params.getParam("cmrIssuingCntry");
      if (cntry2 == null) {
        cntry2 = country;
      }
      if ("866".equals(cntry2)) {
        query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
        query.setParameter("ISSUING_CNTRY", cntry2);
      }
    }

    if ("ISR".equalsIgnoreCase(fieldId)) {
      query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
    }

    if ("MrcCd".equalsIgnoreCase(fieldId)) {
      String issuingCntry = (String) params.getParam("cmrIssuingCntry");
      if (LAHandler.isSSAMXBRIssuingCountry(issuingCntry)) {
        query.append(" and (ISSUING_CNTRY = :ISSUING_CNTRY or ISSUING_CNTRY = '*') and SALES_BO_CD = :SALES_BO_CD");
        query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
        query.setParameter("SALES_BO_CD", params.getParam("salesBusOffCd"));
      } else if ("858".equals(issuingCntry) || "766".equals(issuingCntry)) {
        // do nothing
      } else {
        query.append(" and (ISSUING_CNTRY = :ISSUING_CNTRY or ISSUING_CNTRY = '*')");
        query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
      }

    }

    if ("MRCISU".equalsIgnoreCase(fieldId)) {
      query.append(" and (ISSUING_CNTRY = :ISSUING_CNTRY or ISSUING_CNTRY = '*') and MRC_CD = :MRC_CD");
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
      query.setParameter("MRC_CD", params.getParam("mrcCd"));
    }
    // SPECIAL FILTER FOR COLLECTOR DROPDOWN #1185812
    /*
     * check the fieldId if its CollectorNameNo then check the issuing country
     * from the request param and use this as a paramameter for query
     */
    if ("CollectorNameNo".equalsIgnoreCase(fieldId)) {
      query.append(" and (ISSUING_CNTRY = :CNTRY or ISSUING_CNTRY = '*')");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }

    if ("SBOMRC".equalsIgnoreCase(fieldId)) {
      query.append(" and (ISSUING_CNTRY = :ISSUING_CNTRY)");
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
    }

    if ("DropDownCity".equalsIgnoreCase(fieldId)) {
      if (CNHandler.isCNIssuingCountry(cntry)) {
        String param = params.getParam("stateProv") != null ? params.getParam("stateProv").toString() : "";
        query.append(" and CITY_ID like('" + param + "%')");
        query.append(" and (ISSUING_CNTRY = '" + cntry + "')");
        // query.setParameter("ISSUING_CNTRY", issuingCntry);
      } else {
        query.append(" and (ISSUING_CNTRY = '" + params.getParam("cmrIssuingCntry") + "')");
      }

    }

    if ("DropDownCityChina".equalsIgnoreCase(fieldId)) {
      CNHandler cnHandler = new CNHandler();
      String stateProv = params.getParam("stateProv") != null ? params.getParam("stateProv").toString() : "";
      String cityTxt = params.getParam("dropdowncity1") != null ? params.getParam("dropdowncity1").toString() : "";
      String param = cnHandler.getChinaCityID(entityManager, stateProv, cityTxt);
      query.append(" and CD = '" + param + "'");
      query.append(" and FIELD_ID = '##DropDownCityChina'");
      query.append(" and CMR_ISSUING_CNTRY = '" + SystemLocation.CHINA + "'");
    }

    if ("ChinaSearchTerm".equalsIgnoreCase(fieldId)) {
      // query.append(" and FIELD_ID = '##ChinaSearchTerm'");
      query.append(" and ISSUING_CNTRY = '" + SystemLocation.CHINA + "'");
    }

    // new impl of scenarios
    if ("CustomerScenarioType".equals(fieldId)) {
      query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
      String geoCd = (String) params.getParam("countryUse");
      if (!StringUtils.isEmpty(geoCd)) {
        if (!"706".equalsIgnoreCase(cntry)) {
          query.append("  and GEO_CD = :GEO_CD");
          query.setParameter("GEO_CD", geoCd);
        }
      }
    }
    if ("CustomerScenarioSubType".equals(fieldId)) {
      query.append("  and CUST_TYP_VAL = :CUST_TYP_VAL");
      query.append("  and ISSUING_CNTRY = :ISSUING_CNTRY");

      query.setParameter("CUST_TYP_VAL", params.getParam("custGrp"));
      query.setParameter("ISSUING_CNTRY", params.getParam("cmrIssuingCntry"));
    }
    if ("ProvinceName".equals(fieldId)) {
      query.append("  and ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }
    if ("ProvinceCode".equals(fieldId)) {
      query.append("  and ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }
    if ("Cluster".equals(fieldId)) {
      query.append("  and ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }

    if ("ClassCode".equalsIgnoreCase(fieldId)) {
      query.append(" and MANDT = :MANDT ");
      query.append(" and SPRAS = 'E' ");
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    }

    // if ("ISU".equalsIgnoreCase(fieldId)) {
    // if (SystemLocation.CHINA.equalsIgnoreCase(cntry)) {
    // String searchTerm = (String) params.getParam("searchTerm");
    // query.append(" and ISU_CD in (select ISU from creqcmr.sortl_isu where
    // search_term = :SORTL) ");
    // query.setParameter("SORTL", searchTerm);
    // }
    // }

    if ("SpainISU".equals(fieldId)) {
      query.append(" and ISU_CD in ('32','21','5B','04','3T','34','60')");
    }

    if ("StateProvConfig".equals(fieldId)) {
      // if (!"BR".equals(params.getParam("landCntry"))) {
      // query.append(" and STATE_PROV_CD = 'EX' and REFT_COUNTRY_KEY = (select
      // REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = 'BR')");
      // } else {
      // String LALanded = PageManager.getDefaultLandedCountry(cntry);
      // query.append(" and ((REFT_COUNTRY_KEY = (select REFT_COUNTRY_KEY from
      // CMMA.REFT_COUNTRY_W where COUNTRY_CD = :LAND1) and '"
      // + LALanded
      // + "' = :LAND1)");
      // query.append(" or (REFT_COUNTRY_KEY = 0 and '" + LALanded +
      // "' <> :LAND1))");
      // query.setParameter("LAND1", params.getParam("landCntry"));
      // }
      String LALanded = PageManager.getDefaultLandedCountry(cntry);
      query.append("and REFT_COUNTRY_KEY  = (select REFT_COUNTRY_KEY from CMMA.REFT_COUNTRY_W where COUNTRY_CD = '" + LALanded + "')");

    }

    if ("TipoCliente".equals(fieldId)) {
      query.append("  and CMR_ISSUING_CNTRY = :CNTRY");
      query.setParameter("CNTRY", params.getParam("cmrIssuingCntry"));
    }

    if ("USSicmen".equalsIgnoreCase(fieldId)) {
      if (SystemLocation.UNITED_STATES.equals(cntry)) {
        query.append("  and GEO_CD = 'US' ");
      } else {
        query.append("  and GEO_CD = 'WW' ");
      }
      if (!StringUtils.isEmpty((String) params.getParam("usSicmen"))) {
        String usSicmenVal = (String) params.getParam("usSicmen");
        query.append("  and REFT_UNSIC_CD in (" + usSicmenVal.toString() + ")");
      }

    }

  }

  /**
   * For LOV Dropdown lists
   * 
   * @param entityManager
   * @param request
   * @param params
   * @return
   */
  private DropdownModel processLOVDropdownSql(EntityManager entityManager, HttpServletRequest request, ParamContainer params) {

    String fieldId = (String) params.getParam("fieldId");
    if (fieldId == null) {
      return new DropdownModel();
    }
    String cmrCntry = (String) params.getParam("cmrIssuingCntry");
    if (cmrCntry == null) {
      cmrCntry = AppUser.getUser(request).getCmrIssuingCntry();
    }
    DropdownModel model = null;
    List<DropdownItemModel> itemList = new ArrayList<DropdownItemModel>();

    String sql = ExternalizedQuery.getSql("LOV");
    PreparedQuery query = new PreparedQuery(entityManager, sql);

    appendSpecialFilters(entityManager, fieldId, query, params, false, null);

    query.append(" order by lov.DISP_ORDER asc");
    query.setForReadOnly(true);
    query.setParameter("CMR_CNTRY", cmrCntry);
    query.setParameter("FIELD_ID", "##" + fieldId);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] row = null;
      model = new DropdownModel();
      model.setIdentifier("id");
      model.setLabel("name");
      boolean insertBlank = true;
      DropdownItemModel item = null;
      for (Object obj : results) {
        row = (Object[]) obj;
        item = new DropdownItemModel();
        item.setName(row[1] != null ? row[1].toString() : "");
        item.setName(escapeText(item.getName()));
        item.setId(row[0] != null ? row[0].toString() : "");
        if (StringUtils.isBlank(item.getId())) {
          insertBlank = false;
        }
        itemList.add(item);
        model.addItems(item);
        if (CmrConstants.YES_NO.Y.toString().equals(row[2])) {
          model.setSelectedItem(item.getId());
        }
      }
      if (insertBlank) {
        DropdownItemModel blankOption = new DropdownItemModel();
        blankOption.setId("");
        blankOption.setName("");
        model.getItems().add(0, blankOption);
      }
    }

    return model;
  }

  private String escapeText(String text) {
    String newText = new String(text);
    // newText = newText.replaceAll("'", "");
    // newText = newText.replaceAll(" & ", "and");
    // newText = newText.replaceAll("&", " and ");
    return newText;
  }

}
