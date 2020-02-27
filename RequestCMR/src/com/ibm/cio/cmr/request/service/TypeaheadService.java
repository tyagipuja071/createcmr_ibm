/**
 * 
 */
package com.ibm.cio.cmr.request.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.TypeaheadModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class TypeaheadService {

  public List<TypeaheadModel> getSuggestions(String id, String term) {
    List<TypeaheadModel> suggestions = new ArrayList<TypeaheadModel>();

    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("TYPEAHEAD." + id.toUpperCase());
      if (!StringUtils.isEmpty(sql)) {
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("INPUT", "%" + term.trim() + "%");
        List<Object[]> results = query.getResults();
        TypeaheadModel model = null;
        for (Object[] item : results) {
          model = new TypeaheadModel();
          model.setValue((String) item[0]);
          model.setLabel((String) item[1]);
          suggestions.add(model);
        }
      }
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return suggestions;
  }

}
