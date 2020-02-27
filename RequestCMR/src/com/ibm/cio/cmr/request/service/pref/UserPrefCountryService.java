/**
 * 
 */
package com.ibm.cio.cmr.request.service.pref;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.UserPrefCountries;
import com.ibm.cio.cmr.request.entity.UserPrefCountriesPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.pref.UserPrefCountryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class UserPrefCountryService extends BaseService<UserPrefCountryModel, UserPrefCountries> {

  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd-MM-yyyy HH:mm");

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(UserPrefCountryService.class);
  }

  @Override
  protected void performTransaction(UserPrefCountryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String action = model.getAction();

    AppUser user = AppUser.getUser(request);

    if ("ADD_CNTRY".equals(action)) {

      if (countryExists(entityManager, model.getRequesterId(), model.getIssuingCntry())) {
        throw new CmrException(MessageUtil.ERROR_PREF_COUNTRY_EXISTS);
      }

      UserPrefCountries cntry = new UserPrefCountries();
      UserPrefCountriesPK cntryPk = new UserPrefCountriesPK();
      cntryPk.setRequesterId(model.getRequesterId());
      cntryPk.setIssuingCntry(model.getIssuingCntry());
      cntry.setId(cntryPk);
      cntry.setCreateBy(user.getIntranetId());
      cntry.setCreateTs(SystemUtil.getCurrentTimestamp());

      createEntity(cntry, entityManager);

      user.setHasCountries(true);

    } else if ("REMOVE_CNTRY".equals(action)) {
      model.setIssuingCntry(model.getRemoveCntry());
      UserPrefCountries cntry = getCurrentRecord(model, entityManager, request);
      deleteEntity(cntry, entityManager);
    }
  }

  private boolean countryExists(EntityManager entityManager, String userId, String cntry) {
    String sql = ExternalizedQuery.getSql("USERPREF.GET_CNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER", userId);
    query.setParameter("CNTRY", cntry);
    return query.exists();
  }

  @Override
  protected List<UserPrefCountryModel> doSearch(UserPrefCountryModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    List<UserPrefCountryModel> results = new ArrayList<UserPrefCountryModel>();

    String sql = ExternalizedQuery.getSql("USERPREF.GET_COUNTRIES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER", model.getRequesterId());
    query.setForReadOnly(true);

    List<UserPrefCountries> rs = query.getResults(50, UserPrefCountries.class);
    UserPrefCountryModel cntryModel = null;
    for (UserPrefCountries cntry : rs) {
      cntryModel = new UserPrefCountryModel();
      copyValuesFromEntity(cntry, cntryModel);
      if (cntry.getCreateTs() != null) {
        cntryModel.setCreateTsString(FORMATTER.format(cntry.getCreateTs()));
      }
      cntryModel.setState(BaseModel.STATE_EXISTING);
      results.add(cntryModel);
    }

    return results;
  }

  @Override
  protected UserPrefCountries getCurrentRecord(UserPrefCountryModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    String sql = ExternalizedQuery.getSql("USERPREF.GET_CNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("USER", model.getRequesterId());
    query.setParameter("CNTRY", model.getIssuingCntry());
    List<UserPrefCountries> countries = query.getResults(1, UserPrefCountries.class);
    if (countries != null && countries.size() > 0) {
      return countries.get(0);
    }
    return null;
  }

  @Override
  protected UserPrefCountries createFromModel(UserPrefCountryModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    UserPrefCountries cntry = new UserPrefCountries();
    UserPrefCountriesPK pk = new UserPrefCountriesPK();
    cntry.setId(pk);
    copyValuesToEntity(model, cntry);
    return cntry;
  }

}
