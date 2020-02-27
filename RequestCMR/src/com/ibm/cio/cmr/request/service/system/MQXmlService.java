/**
 * 
 */
package com.ibm.cio.cmr.request.service.system;

import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import com.ibm.cio.cmr.request.entity.MqIntfReqData;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.MQXmlModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.mq.MQXml;
import com.ibm.cio.cmr.request.util.mq.MQXmlHandler;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class MQXmlService extends BaseSimpleService<MQXmlModel> {

  private static final Logger LOG = Logger.getLogger(MQXmlService.class);

  @Override
  protected MQXmlModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    String sql = ExternalizedQuery.getSql("MQXML.GETXMLS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("ID", Long.parseLong((String) params.getParam("ID")));

    List<MqIntfReqData> mqXmlList = query.getResults(MqIntfReqData.class);
    MQXmlModel model = new MQXmlModel();
    if (mqXmlList != null) {

      if (mqXmlList.size() == 0) {
        model.setExists(false);
        model.setError(true);
        model.setErrorMsg("Data not found for this request.");
      } else {
        MQXmlHandler handler = null;
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        MQXml mqXml = null;
        String xmlString = null;

        for (MqIntfReqData xmlRecord : mqXmlList) {
          try {
            xmlString = xmlRecord.getContents();
            handler = MQXmlHandler.getHandler(xmlString);
            handler.newXml(xmlRecord.getId().getFileName());

            StringReader sr = new StringReader(xmlString);
            try {
              InputSource source = new InputSource(sr);
              parser.parse(source, handler);
              mqXml = handler.getXml();
              if (mqXml != null) {
                model.add(mqXml);
              }
            } finally {
              sr.close();
            }
          } catch (Exception e) {
            LOG.warn("Cannot parse xml for " + xmlRecord.getId().getQueryReqId() + "/" + xmlRecord.getId().getFileName());
          }
        }
      }
    } else {
      model.setExists(false);
      model.setError(true);
      model.setErrorMsg("Data not found for this request.");
    }
    return model;
  }

}
