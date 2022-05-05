/**
 * 
 */
package com.ibm.cio.cmr.request.service.legacy;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchModel;
import com.ibm.cio.cmr.request.model.legacy.LegacySearchResultModel;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class LegacySearchService extends BaseSimpleService<List<LegacySearchResultModel>> {

  @Override
  protected List<LegacySearchResultModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    List<LegacySearchResultModel> results = new ArrayList<>();

    LegacySearchModel crit = (LegacySearchModel) params.getParam("crit");
    if (crit == null) {
      return results;
    }

    PreparedQuery query = buildQueryFromParams(entityManager, crit);
    List<CompoundEntity> qResults = query.getCompundResults(CmrtCust.class, "LegacySearchMapping");
    if (qResults != null && !qResults.isEmpty()) {
      for (CompoundEntity result : qResults) {
        CmrtCust cust = result.getEntity(CmrtCust.class);
        CmrtAddr addr = result.getEntity(CmrtAddr.class);
        LegacySearchResultModel item = new LegacySearchResultModel();
        PropertyUtils.copyProperties(item, cust.getId());
        PropertyUtils.copyProperties(item, cust);
        PropertyUtils.copyProperties(item, addr.getId());
        PropertyUtils.copyProperties(item, addr);

        if ("Y".equals(addr.getIsAddrUseMailing())) {
          item.getAddressUses().add("M");
        }
        if ("Y".equals(addr.getIsAddrUseBilling())) {
          item.getAddressUses().add("B");
        }
        if ("Y".equals(addr.getIsAddrUseInstalling())) {
          item.getAddressUses().add("I");
        }
        if ("Y".equals(addr.getIsAddrUseShipping())) {
          item.getAddressUses().add("S");
        }
        if ("Y".equals(addr.getIsAddrUseEPL())) {
          item.getAddressUses().add("E");
        }
        if ("Y".equals(addr.getIsAddrUseLitMailing())) {
          item.getAddressUses().add("L");
        }
        if ("Y".equals(addr.getIsAddressUseA())) {
          item.getAddressUses().add("UA");
        }
        if ("Y".equals(addr.getIsAddressUseB())) {
          item.getAddressUses().add("UB");
        }
        if ("Y".equals(addr.getIsAddressUseC())) {
          item.getAddressUses().add("UC");
        }
        if ("Y".equals(addr.getIsAddressUseD())) {
          item.getAddressUses().add("UD");
        }
        if ("Y".equals(addr.getIsAddressUseE())) {
          item.getAddressUses().add("UE");
        }
        if ("Y".equals(addr.getIsAddressUseF())) {
          item.getAddressUses().add("UF");
        }
        if ("Y".equals(addr.getIsAddressUseG())) {
          item.getAddressUses().add("UG");
        }
        if ("Y".equals(addr.getIsAddressUseH())) {
          item.getAddressUses().add("UH");
        }

        results.add(item);
      }
    }
    return results;
  }

  /**
   * Builds the SQL here. Not externalized because of complexity
   * 
   * @param crit
   * @return
   */
  private PreparedQuery buildQueryFromParams(EntityManager entityManager, LegacySearchModel crit) {

    String baseSql = "select * from CMRDB2D.CMRTCUST cust, CMRDB2D.CMRTADDR addr ";
    baseSql += "left outer join CMRDB2D.CMRTCEXT ext on cust.RCYAA = ext.RCYAA and cust.RCUXA = ext.RCUXA ";
    baseSql += "where cust.RCYAA = addr.RCYAA and cust.RCUXA = addr.RCUXA ";
    PreparedQuery query = new PreparedQuery(entityManager, baseSql);

    if (!StringUtils.isBlank(crit.getRealCtyCd())) {
      query.append("and cust.REALCTY = :REALCTY");
      query.setParameter("REALCTY", crit.getRealCtyCd());
    }
    if (!StringUtils.isBlank(crit.getCustomerNo())) {
      query.append("and cust.RCUXA = :RCUXA");
      query.setParameter("RCUXA", crit.getCustomerNo());
    }
    if (!StringUtils.isBlank(crit.getAbbrevNm())) {
      query.append("and upper(cust.NCUXB) like :ABBREV");
      query.setParameter("ABBREV", "%" + crit.getAbbrevNm() + "%");
    }
    if (!StringUtils.isBlank(crit.getName())) {
      query.append("and (upper(addr.NAME) like :NAME or upper(trim(addr.ADDRL1)||(trim(nvl(addr.ADDRL2,'')))) like :NAME)");
      query.setParameter("NAME", "%" + crit.getName().toUpperCase() + "%");
    }
    if (!StringUtils.isBlank(crit.getStreet())) {
      query.append(
          "and ( upper(trim(nvl(addr.STREETNO,''))||trim(nvl(addr.STREET,''))) like :STREET or upper(addr.ADDRL3) like :STREET or upper(addr.ADDRL4) like :STREET or upper(addr.ADDRL5) like :STREET)");
      query.setParameter("STREET", "%" + crit.getStreet().toUpperCase() + "%");
    }
    if (!StringUtils.isBlank(crit.getCity())) {
      query.append(
          "and ( upper(addr.CITY) like :CITY or upper(addr.ADDRL4) like :CITY or upper(addr.ADDRL5) like :CITY or upper(addr.ADDRL6) like :CITY)");
      query.setParameter("CITY", "%" + crit.getCity().toUpperCase() + "%");
    }
    if (!StringUtils.isBlank(crit.getZipCode())) {
      query.append(
          "and ( upper(addr.ZIPCODE) like :ZIP or upper(addr.ADDRL4) like :ZIP or upper(addr.ADDRL5) like :ZIP or upper(addr.ADDRL6) like :ZIP)");
      query.setParameter("ZIP", "%" + crit.getZipCode().toUpperCase() + "%");
    }

    if (!StringUtils.isBlank(crit.getVat())) {
      query.append("and ( upper(cust.VAT) = :VAT or upper(cust.RTPNO) like :VAT )");
      query.setParameter("VAT", crit.getVat().toUpperCase());
    }

    if ("A".equals(crit.getStatus())) {
      query.append("and cust.STATUS = 'A'");
    } else if ("C".equals(crit.getStatus())) {
      query.append("and cust.STATUS = 'C'");
    }

    if (!StringUtils.isBlank(crit.getCreateTsFrom())) {
      query.append("and cust.CREATE_TS  >= timestamp('" + crit.getCreateTsFrom() + "')");
      query.setParameter("CREATE_FROM", crit.getCreateTsFrom());
    }
    if (!StringUtils.isBlank(crit.getCreateTsTo())) {
      query.append("and cust.CREATE_TS  <= timestamp('" + crit.getCreateTsTo() + "')");
      query.setParameter("CREATE_TO", crit.getCreateTsTo());
    }

    if (!StringUtils.isBlank(crit.getUpdateTsFrom())) {
      query.append("and cust.UPDATE_TS  >= timestamp('" + crit.getUpdateTsFrom() + "')");
      query.setParameter("UPDATE_FROM", crit.getUpdateTsFrom());
    }
    if (!StringUtils.isBlank(crit.getUpdateTsTo())) {
      query.append("and cust.UPDATE_TS  <= timestamp('" + crit.getUpdateTsTo() + "')");
      query.setParameter("UPDATE_TO", crit.getUpdateTsTo());
    }

    if (!StringUtils.isBlank(crit.getiTaxCode())) {
      query.append("and ext.CODFIS = :CODFIS");
      query.setParameter("CODFIS", crit.getiTaxCode());
    }

    if (crit.getAddressUses() != null && !crit.getAddressUses().isEmpty()) {
      for (String use : crit.getAddressUses()) {
        switch (use) {
        case "M":
          query.append("and addr.ADDRMAIL = 'Y'");
          break;
        case "B":
          query.append("and addr.ADDRBILL = 'Y'");
          break;
        case "S":
          query.append("and addr.ADDRSHIP = 'Y'");
          break;
        case "I":
          query.append("and addr.ADDRINST = 'Y'");
          break;
        case "E":
          query.append("and addr.ADDREPL = 'Y'");
          break;
        case "L":
          query.append("and addr.ADDRLIT = 'Y'");
          break;
        case "F":
          query.append("and addr.ADDRUSEF = 'Y'");
          break;
        case "G":
          query.append("and addr.ADDRUSEG = 'Y'");
          break;
        case "H":
          query.append("and addr.ADDRUSEH = 'Y'");
          break;
        }
      }
    }

    if (crit.getRecCount() > 0) {
      query.append("fetch first " + crit.getRecCount() + " rows only");
    } else {
      query.append("fetch first 50 rows only");
    }

    return query;
  }

}
