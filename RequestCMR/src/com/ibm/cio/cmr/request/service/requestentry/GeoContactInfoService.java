package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.GeoContactInfoPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.requestentry.GeoContactInfoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

/**
 * @author Neil Sherwin Espolong
 * */
@Component
public class GeoContactInfoService extends BaseService<GeoContactInfoModel, GeoContactInfo> {

  public static final String FIND_ALL_BY_REQ_ID = "CONTACTINFO.FINDALL";
  public static final String FIND_ONE_RECORD = "CONTACTINFO.FINDONE.DETAILS";
  public static final String GENEREATE_NEW_ID = "CONTACTINFO.GENERATE.ID";
  public static final String CHECK_IF_EXIST = "CONTACTINFO.CHECK.EXISTS";
  public static final String FINDALL_BY_SEQNUM = "CONTACTINFO.FINDALL.BY_SEQ";
  public static final String FINDALL_BY_TYPE = "CONTACTINFO.FINDALL.BY_TYPE";
  public static final String FIND_EM_RECORD = "CONTACTINFO.FIND.EM.RECORD";
  public static final String FIND_CF_LE_RECORDS = "CONTACTINFO.FIND.LE.CF.RECORDS";
  public static final String FIND_LE_RECORDS = "CONTACTINFO.FIND.LE.RECORDS";
  public static final String DELETE_EXISTING_LE_CF = "CONTACTINFO.DELETE.LE.CF.RECORDS";

  private List<GeoContactInfo> entityList;

  private List<GeoContactInfoModel> dtoList;

  private GeoContactInfoModel dto;

  private GeoContactInfo entity = null;

  private GeoContactInfoPK entityPK = null;

  @Autowired
  private CmrClientService clientSvc;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(GeoContactInfoService.class.getSimpleName());
  }

  @Override
  protected void performTransaction(GeoContactInfoModel model, EntityManager em, HttpServletRequest req) throws CmrException {
    initLogger().debug("performing transaction . . .");
    setEntity(new GeoContactInfo());
    String currentAction = model.getAction();
    String country = model.getCmrIssuingCntry();
    String reqType = model.getReqType();
    if ("ADD_CONTACTINFO".equalsIgnoreCase(currentAction)) {
      initLogger().info("adding new contact info entry . . .");
      model.setContactInfoId(generateNewContactId(model, em, req, String.valueOf(model.getReqId())));
      if (LAHandler.isSSAIssuingCountry(country)) {
        /* for manual creation and not from LAHandler.setDataDefaultsOnCreate */
        if (StringUtils.isEmpty(model.getContactType())) {
          // default to EM
          model.setContactType("EM");
        }
        if (StringUtils.isEmpty(model.getContactName())) {
          model.setContactName("N");
        }
        if (StringUtils.isEmpty(model.getContactPhone())) {
          if (!SystemLocation.PERU.equalsIgnoreCase(country)) {
            model.setContactPhone(".");
          }
        }
        if (SystemLocation.ECUADOR.equals(country) || SystemLocation.COLOMBIA.equals(country) || SystemLocation.BOLIVIA_PLURINA.equals(country)) {
          // 1375486 ECUADOR COLOMBIA BOLIVIA_PLURINA
          model.setContactTreatment("Sr.");
          model.setContactFunc(".");
        } else if (SystemLocation.PERU.equals(country) || SystemLocation.CHILE.equals(country)) {
          // 1375486 PERU CHILE
          model.setContactTreatment("Sr.");
          model.setContactFunc(".");
        } else if (SystemLocation.VENEZUELA_BOLIVARIAN.equals(country)) {
          // 1375486 VENEZUELA_BOLIVARIAN
          model.setContactTreatment("");
          model.setContactFunc(".");
        } else {
          model.setContactTreatment("");
          model.setContactFunc("");
        }
      }
      setEntity(createFromModel(model, em, req));
      createEntity(getEntity(), em);
    } else if ("UPDATE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
      initLogger().info("updating contact info entry . . .");
      GeoContactInfo entity = getCurrentRecord(model, em, req);
      Date dateCreated = entity.getCreateTs();
      copyValuesToEntity(model, entity);
      entity.setCreateTs(dateCreated);
      entity.setUpdtTs(SystemUtil.getCurrentTimestamp());
      entity.setUpdtById(AppUser.getUser(req).getIntranetId());
      if (StringUtils.isEmpty(entity.getCreateById())) {
        entity.setCreateById(AppUser.getUser(req).getIntranetId());
      }
      updateEntity(entity, em);
      if (SystemLocation.BRAZIL.equalsIgnoreCase(country) || SystemLocation.PERU.equalsIgnoreCase(country)) {
        /* update all CF 001 LE 001 */
        if (reqType.equalsIgnoreCase("C")) {
          if (model.getContactType().equalsIgnoreCase(CmrConstants.CONTACT_TYPE_BR.EM_CONS.getStrLiteral())
              && model.getContactSeqNum().equalsIgnoreCase("001")) {
            List<GeoContactInfo> contsToUpdate = getCurrentLeCfRecords(em, model.getReqId());
            if (contsToUpdate != null && contsToUpdate.size() > 0) {
              for (GeoContactInfo tempCont : contsToUpdate) {
                tempCont.setContactEmail(model.getContactEmail());
                tempCont.setContactName(model.getContactName());
                tempCont.setContactPhone(model.getContactPhone());
                if (SystemLocation.PERU.equalsIgnoreCase(country)) {
                  tempCont.setContactTreatment(model.getContactTreatment());
                  tempCont.setContactFunc(model.getContactFunc());
                }
                tempCont.setUpdtById(AppUser.getUser(req).getIntranetId());
                tempCont.setUpdtTs(SystemUtil.getCurrentTimestamp());
                updateEntity(tempCont, em);
              }
            }
          }
        } else {
          // update request type for PERU
          if (SystemLocation.PERU.equalsIgnoreCase(country)) {
            if (model.getContactType().equalsIgnoreCase(CmrConstants.CONTACT_TYPE_BR.EM_CONS.getStrLiteral())
                && model.getContactSeqNum().equalsIgnoreCase("001")) {
              List<GeoContactInfo> contsToUpdate = getCurrentLeCfRecords(em, model.getReqId());
              if (contsToUpdate != null && contsToUpdate.size() > 0) {
                for (GeoContactInfo tempCont : contsToUpdate) {
                  tempCont.setContactEmail(model.getContactEmail());
                  tempCont.setContactName(model.getContactName());
                  tempCont.setContactPhone(model.getContactPhone());
                  tempCont.setContactTreatment(model.getContactTreatment());
                  tempCont.setContactFunc(model.getContactFunc());
                  tempCont.setUpdtById(AppUser.getUser(req).getIntranetId());
                  tempCont.setUpdtTs(SystemUtil.getCurrentTimestamp());
                  updateEntity(tempCont, em);
                }
              }
            }
          }
        }
      }
    } else if ("REMOVE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
      initLogger().info("removing contact info entry . . .");
      if (model.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
        // create request type
        setEntity(getCurrentRecord(model, em, req));
        if (SystemLocation.BRAZIL.equalsIgnoreCase(country) || SystemLocation.PERU.equalsIgnoreCase(country)) {
          if (model.getContactSeqNum().equalsIgnoreCase("001")) {
            getEntity().setCurrentEmail1(model.getCurrentEmail1());
            getEntity().setReqType(model.getReqType());
            // delete all 001
            doMassDeleteOfContactInfo(getEntity(), em, false, model, req);
          } else {
            // just delete
            deleteEntity(getEntity(), em);
          }
        } else {
          // other SSAMX
          // delete then adjust sequence by contact type
          deleteEntity(getEntity(), em);
          if (!StringUtils.isEmpty(getEntity().getContactSeqNum())) {
            String tempSeq = getEntity().getContactSeqNum().trim();
            int deletedSeq = Integer.parseInt(tempSeq.substring(2));
            getEntity().setCurrentEmail1(model.getCurrentEmail1());
            if (!CmrConstants.CONTACT_TYPE_BR_LST.contains(model.getContactType())) {
              adjustSeqNumber(getEntity(), em, deletedSeq, true, false, req);
            } else {
              adjustSeqNumber(getEntity(), em, deletedSeq, true, true, req);
            }
          }
        }
      } else {
        // update request type
        setEntity(getCurrentRecord(model, em, req));
        deleteEntity(getEntity(), em);
        // if (!StringUtils.isEmpty(getEntity().getContactSeqNum()) &&
        // CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
        // String tempSeq = getEntity().getContactSeqNum().trim();
        // int deletedSeq = Integer.parseInt(tempSeq.substring(2));
        // getEntity().setCurrentEmail1(model.getCurrentEmail1());
        // if
        // (!CmrConstants.CONTACT_TYPE_BR_LST.contains(model.getContactType()))
        // {
        // adjustSeqNumber(getEntity(), em, deletedSeq, false, false, req);
        // } else {
        // adjustSeqNumber(getEntity(), em, deletedSeq, false, true, req);
        // }
        // }
      }
    } else if ("REMOVE_CONTACTINFOS".equalsIgnoreCase(currentAction)) {
      doMassDeleteOfContactInfo(getEntity(), em, true, model, req);
    } else {
      log.info("INVALID ACTION. Check your scripts.");
      throw new CmrException(MessageUtil.ERROR_INVALID_ACTION, model.getAction() + " is not a supported ACTION.");
    }
  }

  private void doMassDeleteOfContactInfo(GeoContactInfo e, EntityManager em, boolean isGridAction, GeoContactInfoModel model, HttpServletRequest req)
      throws CmrException {
    PreparedQuery query = null;
    String sql = "";
    int currentSeq = 0;
    List<GeoContactInfo> tempList = null;
    if (isGridAction) {
      List<KeyContainer> checkKeys = extractKeys(model);
      sql = ExternalizedQuery.getSql(FIND_ONE_RECORD);
      query = new PreparedQuery(em, sql);
      GeoContactInfo contact = null;
      long reqId = 0;
      int contactId = 0;
      for (KeyContainer key : checkKeys) {
        reqId = Long.parseLong(key.getKey("reqId"));
        contactId = Integer.parseInt(key.getKey("contactInfoId"));
        query.setParameter("REQ_ID", reqId);
        query.setParameter("ID", contactId);
        contact = query.getSingleResult(GeoContactInfo.class);
        if (contact != null) {
          deleteEntity(contact, em);
        }
      }
    } else {
      // just for 001 sequence (create request type)
      sql = ExternalizedQuery.getSql(FINDALL_BY_SEQNUM);
      query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", e.getId().getReqId());
      query.setParameter("SEQ", e.getContactSeqNum().trim());
      tempList = query.getResults(GeoContactInfo.class);
      if (tempList != null && tempList.size() != 0) {
        if (!StringUtils.isEmpty(tempList.get(0).getContactSeqNum())) {
          String tempSeq = tempList.get(0).getContactSeqNum().trim();
          if (!StringUtils.isEmpty(tempSeq)) {
            currentSeq = Integer.parseInt(tempSeq.substring(2));
          }
        }
        for (GeoContactInfo tempEnt : tempList) {
          deleteEntity(tempEnt, em); // delete all 001
        }
        if (currentSeq != 0) {
          // no adjustment
          /* adjustSeqNumber(e, em, currentSeq, false, req); */
        }
      }
    }
  }

  private void adjustSeqNumber(GeoContactInfo deletedEnt, EntityManager em, int seqDeleted, boolean isUpdateReq, boolean hastContactType,
      HttpServletRequest req) throws CmrException {
    PreparedQuery query;
    List<GeoContactInfo> tempList;
    if ((!isUpdateReq || isUpdateReq) && !hastContactType) {
      // implementation if NO contact type was chosen. defaulted to 'n'
      switch (seqDeleted) {
      case 1:
        findAndReplaceSeqNum(deletedEnt, em, seqDeleted, req, Arrays.asList("002", "003", "004", "005"));
        break;
      case 2:
        findAndReplaceSeqNum(deletedEnt, em, seqDeleted, req, Arrays.asList("003", "004", "005"));
        break;
      case 3:
        findAndReplaceSeqNum(deletedEnt, em, seqDeleted, req, Arrays.asList("004", "005"));
        break;
      case 4:
        findAndReplaceSeqNum(deletedEnt, em, seqDeleted, req, Arrays.asList("005"));
        break;
      default:
        break;
      }
    } else if ((isUpdateReq || !isUpdateReq) && hastContactType) {
      String sql = ExternalizedQuery.getSql(FINDALL_BY_TYPE);
      query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", deletedEnt.getId().getReqId());
      query.setParameter("TYPE", deletedEnt.getContactType().trim());
      tempList = query.getResults(GeoContactInfo.class);
      if (tempList != null && tempList.size() != 0) {
        int sequenceInt = 0;
        String sequenceStr = "";
        for (GeoContactInfo tempEnt : tempList) {
          if (tempEnt != null) {
            sequenceInt++;
            sequenceStr = "00" + sequenceInt;
            tempEnt.setContactSeqNum(sequenceStr);
            if (sequenceStr.equals("001")) { // if 001
              String email1FromDb = clientSvc.getDataEmail1(tempEnt.getId().getReqId(), true, em);
              if (!StringUtils.isEmpty(email1FromDb)) { // if not null
                if (!email1FromDb.toUpperCase().equals(tempEnt.getContactEmail().toUpperCase())) {
                  // if not equal
                  // do nothing let validation handle
                }
              }
            }
            updateEntity(tempEnt, em);
          }
        } // for
      }
    }
  }

  private void findAndReplaceSeqNum(GeoContactInfo deletedEnt, EntityManager em, int seqDeleted, HttpServletRequest req, List<String> seeksFind) {
    PreparedQuery query;
    String sql = ExternalizedQuery.getSql(FINDALL_BY_SEQNUM);
    List<GeoContactInfo> tempList;
    List<String> seeksRepList = null;
    switch (seqDeleted) {
    case 1:
      seeksRepList = Arrays.asList("001", "002", "003", "004");
      break;
    case 2:
      seeksRepList = Arrays.asList("002", "003", "004");
      break;
    case 3:
      seeksRepList = Arrays.asList("003", "004");
      break;
    case 4:
      seeksRepList = Arrays.asList("004");
      break;
    default:
      break;
    }
    for (String seq : seeksFind) {
      query = new PreparedQuery(em, sql);
      query.setParameter("REQ_ID", deletedEnt.getId().getReqId());
      query.setParameter("SEQ", seq);
      tempList = query.getResults(GeoContactInfo.class);
      int loopCount = 0;
      if (tempList != null && tempList.size() != 0) {
        for (GeoContactInfo tempEnt : tempList) {
          tempEnt.setContactSeqNum(seeksRepList.get(loopCount));
          tempEnt.setUpdtById(AppUser.getUser(req).getIntranetId());
          tempEnt.setUpdtTs(SystemUtil.getCurrentTimestamp());
          String email1FromDb = clientSvc.getDataEmail1(tempEnt.getId().getReqId(), true, em);
          if (!StringUtils.isEmpty(email1FromDb)) {
            if (!email1FromDb.toUpperCase().equals(tempEnt.getContactEmail().toUpperCase())) {
              // if not equal do nothing let JS validation handle
            }
          }
          if (tempEnt.getContactSeqNum().equalsIgnoreCase("001")) {
            tempEnt.setContactEmail(deletedEnt.getCurrentEmail1());
          }
          updateEntity(tempEnt, em);
          loopCount++;
        } // for
        tempList = null;
      }
    }
  }

  @Override
  protected List<GeoContactInfoModel> doSearch(GeoContactInfoModel model, EntityManager em, HttpServletRequest req) throws CmrException {
    initLogger().info("getting all contact info. . .");
    setDtoList(new ArrayList<GeoContactInfoModel>());

    String sql = ExternalizedQuery.getSql(FIND_ALL_BY_REQ_ID);
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.append("ORDER BY CONTACT_NUM");
    query.setForReadOnly(true);

    setEntityList(query.getResults(GeoContactInfo.class));

    if (getEntityList() != null && getEntityList().size() != 0) {
      for (GeoContactInfo resultEntity : getEntityList()) {
        setDto(new GeoContactInfoModel());
        copyValuesFromEntity(resultEntity, getDto());
        getDtoList().add(getDto());
      }
    }
    return getDtoList();
  }

  @Override
  protected GeoContactInfo getCurrentRecord(GeoContactInfoModel model, EntityManager em, HttpServletRequest req) throws CmrException {
    initLogger().info("getting current contact record by request id. . .");
    String sql = ExternalizedQuery.getSql(FIND_ONE_RECORD);
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("ID", model.getContactInfoId());
    List<GeoContactInfo> tempContInfoList = query.getResults(1, GeoContactInfo.class);
    if (tempContInfoList != null && tempContInfoList.size() != 0) {
      return tempContInfoList.get(0);
    }
    return null;
  }

  @Override
  protected GeoContactInfo createFromModel(GeoContactInfoModel model, EntityManager em, HttpServletRequest req) throws CmrException {
    setEntityPK(new GeoContactInfoPK());
    getEntity().setId(getEntityPK());
    copyValuesToEntity(model, getEntity());
    getEntity().setCreateTs(SystemUtil.getCurrentTimestamp());
    getEntity().setCreateById(AppUser.getUser(req).getIntranetId());
    return getEntity();
  }

  public int generateNewContactId(GeoContactInfoModel model, EntityManager em, HttpServletRequest req, String reqId) throws CmrException {
    String maxIdFromDB = "";
    int newID = 0;
    String sqlGenScript = ExternalizedQuery.getSql(GENEREATE_NEW_ID);
    PreparedQuery query = new PreparedQuery(em, sqlGenScript);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    try {
      List<Object[]> results = query.getResults();
      if (results != null && results.size() > 0) {
        Object[] result = results.get(0);
        maxIdFromDB = (String) (result != null && result.length > 0 ? result[0] : "0");
        newID = Integer.parseInt(maxIdFromDB);
        if (newID >= 0) {
          newID++;
        }
      }
    } catch (Exception ex) {
      // integer parsing exception during generation
      newID++;
    }
    return newID;
  }

  @SuppressWarnings("unused")
  private boolean isContactInfoExists(GeoContactInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sqlQ = ExternalizedQuery.getSql(CHECK_IF_EXIST);
    PreparedQuery query = new PreparedQuery(entityManager, sqlQ);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("ID", model.getContactInfoId());
    query.setParameter("TYPE", model.getContactInfoId());
    query.setForReadOnly(true);
    return query.exists();
  }

  public List<GeoContactInfo> getCurrentLeRecords(EntityManager entityManager, long reqId) {
    List<GeoContactInfo> contactResults = new ArrayList<>();
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(FIND_LE_RECORDS));
    query.setParameter("REQ_ID", reqId);
    contactResults = query.getResults(GeoContactInfo.class);
    return contactResults;
  }

  public List<GeoContactInfo> getCurrentLeCfRecords(EntityManager entityManager, long reqId) {
    List<GeoContactInfo> contactResults = new ArrayList<>();
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(FIND_CF_LE_RECORDS));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("CONT1", "LE");
    query.setParameter("CONT2", "CF");
    contactResults = query.getResults(GeoContactInfo.class);
    return contactResults;
  }

  public void deleteAllContactDetails(List<GeoContactInfo> geoContactInfo, EntityManager entityManager, long reqId) {
    // String sql = ExternalizedQuery.getSql("DELETE_CONTACT_INFO");
    // PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setParameter("REQ_ID", reqId);
    // query.exe
    // entityManager.merge(this);
    if (geoContactInfo != null && geoContactInfo.size() > 0) {
      for (int i = 0; i < geoContactInfo.size(); i++) {
        GeoContactInfo contactInfo = geoContactInfo.get(i);
        entityManager.remove(contactInfo);
      }
    }
  }

  public void deleteCurrentLeCfRecords(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(DELETE_EXISTING_LE_CF));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("CONT1", "LE");
    query.setParameter("CONT2", "CF");
    int numRec = query.executeSql();
    initLogger().debug("deleteCurrentLeEmRecords : deleted " + numRec + " contact records. . .");

  }

  public List<GeoContactInfo> getEntityList() {
    return entityList;
  }

  public void setEntityList(List<GeoContactInfo> entityList) {
    this.entityList = entityList;
  }

  public List<GeoContactInfoModel> getDtoList() {
    return dtoList;
  }

  public void setDtoList(List<GeoContactInfoModel> dtoList) {
    this.dtoList = dtoList;
  }

  public GeoContactInfoModel getDto() {
    return dto;
  }

  public void setDto(GeoContactInfoModel dto) {
    this.dto = dto;
  }

  public GeoContactInfo getEntity() {
    return entity;
  }

  public void setEntity(GeoContactInfo entity) {
    this.entity = entity;
  }

  public GeoContactInfoPK getEntityPK() {
    return entityPK;
  }

  public void setEntityPK(GeoContactInfoPK entityPK) {
    this.entityPK = entityPK;
  }

}
