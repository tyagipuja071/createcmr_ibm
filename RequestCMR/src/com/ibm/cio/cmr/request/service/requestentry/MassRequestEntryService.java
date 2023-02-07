/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.AutomationConst;
import com.ibm.cio.cmr.request.automation.util.geo.FranceUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.BdsTblInfo;
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtAddrPK;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MassUpdtDataPK;
import com.ibm.cio.cmr.request.entity.MassUpdtPK;
import com.ibm.cio.cmr.request.entity.ProcCenter;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.StatusTrans;
import com.ibm.cio.cmr.request.entity.StatusTransPK;
import com.ibm.cio.cmr.request.masschange.MassChangeTemplateManager;
import com.ibm.cio.cmr.request.masschange.obj.MassChangeTemplate;
import com.ibm.cio.cmr.request.masschange.obj.TemplateColumn;
import com.ibm.cio.cmr.request.masschange.obj.TemplateTab;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation.ValidationRow;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.DataModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateAddressModel;
import com.ibm.cio.cmr.request.model.requestentry.MassUpdateModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.approval.ApprovalService;
import com.ibm.cio.cmr.request.service.dpl.DPLSearchService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.IERPRequestUtils;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.at.ATUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNDHandler;
import com.ibm.cio.cmr.request.util.geo.impl.DEHandler;
import com.ibm.cio.cmr.request.util.geo.impl.JPHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFile.ValidationResult;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileParser;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateUtil;
import com.ibm.cio.cmr.request.util.swiss.SwissUtil;
import com.ibm.cmr.services.client.dpl.DPLCheckResult;
import com.ibm.comexp.at.exportchecks.ews.EWSProperties;

/**
 * Main Service for request entry
 * 
 * @author Eduard Bernardo
 * 
 */
@Component
public class MassRequestEntryService extends BaseService<RequestEntryModel, CompoundEntity> {
  private static final Logger LOG = Logger.getLogger(MassRequestEntryService.class);
  private final AdminService adminService = new AdminService();
  private final DataService dataService = new DataService();
  private final MassUpdtService massService = new MassUpdtService();
  private final MassUpdtDataService massDataService = new MassUpdtDataService();
  private final MassUpdtAddrService massAddrService = new MassUpdtAddrService();
  private final ApprovalService approvalService = new ApprovalService();
  private static final String STATUS_CHG_CMT_PRE_PREFIX = "ACTION \"";
  private static final String STATUS_CHG_CMT_MID_PREFIX = "\" changed the REQUEST STATUS to \"";
  private static final String STATUS_CHG_CMT_POST_PREFIX = "\n";
  private static final String CMR_SHEET_NAME = "Mass Change";
  private static final String MASS_DATA = "Mass Data";
  private static final String CONFIG_SHEET_NAME = "Config";
  private static final int CMR_ROW_NO = 2;
  private static final int CONFIG_ROW_NO = 2;
  private static final int TITLE_ROW_NO = 1;

  private static HashMap<String, Integer> DATA_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZS01_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZI01_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZP01_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZD01_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZS02_FLD = new HashMap<>();
  private static HashMap<String, Integer> ZP02_FLD = new HashMap<>();
  private static final List<String> IL_AUTO_DPL_SEACRH = Arrays.asList("CTYA", "CTYB", "CTYC", "ZI01", "ZS02");

  private String massUpdtRdcOnly;

  @Autowired
  private ScorecardService scoreService;

  @Autowired
  private AddressService addrService;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MassRequestEntryService.class.getSimpleName());
  }

  @Override
  protected void performTransaction(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String action = model.getAction();
    if (CmrConstants.Save().equalsIgnoreCase(action) || "REJECT_SEARCH".equalsIgnoreCase(action) || "NO_DATA_SEARCH".equalsIgnoreCase(action)
        || "CREATE_NEW".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, false);
    } else if ("DPL_REFRESH".equalsIgnoreCase(action)) {
      performSave(model, entityManager, request, true);
    } else if ("SUBMIT_MASS_FILE".equalsIgnoreCase(action)) {
      long reqId = model.getReqId();
      Data data = getCurrentDataRecordById(entityManager, reqId);
      String cmrIssuingCntry = "";

      if ((data.getCmrIssuingCntry() != null && !"".equalsIgnoreCase(data.getCmrIssuingCntry()))) {
        cmrIssuingCntry = data.getCmrIssuingCntry();
      } else {
        cmrIssuingCntry = "";
      }

      model.setCmrIssuingCntry(data.getCmrIssuingCntry());

      if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, cmrIssuingCntry)) {
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else if (SwissUtil.isCountrySwissEnabled(entityManager, cmrIssuingCntry)) {
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else if (ATUtil.isCountryATEnabled(entityManager, cmrIssuingCntry)) {// CMR-803
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else if (IERPRequestUtils.isCountryDREnabled(entityManager, cmrIssuingCntry)) {
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else if (FranceUtil.isCountryFREnabled(entityManager, cmrIssuingCntry)) {
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else if (LAHandler.isLACountry(cmrIssuingCntry)) {
        performLegacyDirectMassUpdate(model, entityManager, request);
      } else {
        performMassUpdate(model, entityManager, request);
      }

    } else if ("PROCESS_FILE".equalsIgnoreCase(action)) {
      processMassFile(entityManager, request);
    } else if ("MASS_DPL".equalsIgnoreCase(model.getAction())) {
      performMassDplChecking(model, entityManager, request);
    } else if (CmrConstants.Mark_as_Completed().equalsIgnoreCase(action)) {
      performMassCreateComplete(model, entityManager, request, true);
    } else if ("DPL_CHECK".equalsIgnoreCase(model.getAction())) {
      long reqId = model.getReqId();
      Admin admin = getCurrentRecordById(reqId);
      performLDMassDplChecking(model, entityManager, request, admin);
    } else {
      // Claim conditionally approved request (Edit Request)
      if (CmrConstants.Claim().equalsIgnoreCase(action) && CmrConstants.APPROVAL_RESULT_COND_APPROVED.equals(model.getApprovalResult())) {
        ApprovalService service = new ApprovalService();
        service.updateApprovals(entityManager, "EDIT_REQUEST", model.getReqId(), AppUser.getUser(request));
      }
      StatusTrans trans = getStatusTransition(entityManager, model);
      if (trans == null) {
        return; // no transition, no processing needed
      }
      if (CmrConstants.Claim().equalsIgnoreCase(action)) {
        // 1150209: throw an error if request has been claimed already, JZ Mar
        // 2, 2017
        if (canClaim(entityManager, model)) {
          throw new CmrException(MessageUtil.ERROR_CLAIMED_ALREADY);
        }
        performGenericAction(trans, model, entityManager, request, null);
        RequestUtils.addToNotifyList(this, entityManager, AppUser.getUser(request), model.getReqId());
      } else if (CmrConstants.Unlock().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Send_for_Processing().equalsIgnoreCase(action)) {
        performSendForProcessing(trans, model, entityManager, request);
      } else if (CmrConstants.Processing_Validation_Complete().equalsIgnoreCase(action)
          || CmrConstants.Processing_Validation_Complete2().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Processing_Create_Up_Complete().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.All_Processing_Complete().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null, null, true);
      } else if (CmrConstants.Reject().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null, null, false);
        processObsoleteApprovals(entityManager, model.getReqId(), AppUser.getUser(request));
      } else if (CmrConstants.Cancel_Processing().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Create_Update_CMR().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Create_Update_Approved().equalsIgnoreCase(action)) {
        performGenericAction(trans, model, entityManager, request, null);
      } else if (CmrConstants.Cancel_Request().equalsIgnoreCase(action)) {
        performCancelRequest(trans, model, entityManager, request);
      }
    }

  }

  private void performLDMassDplChecking(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request, Admin admin)
      throws CmrException {
    // query all addresses where name1 or name2 is not empty.
    List<MassUpdtAddr> addrs = LegacyDirectUtil.getMassUpdtAddrsForDPLCheck(entityManager, String.valueOf(model.getReqId()),
        String.valueOf(admin.getIterationId()));
    GEOHandler handler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());
    if (addrs != null && addrs.size() > 0) {
      for (MassUpdtAddr addr : addrs) {
        String cmrNoVal = addr.getCmrNo();
        String custName1Val = addr.getCustNm1();
        String custName2Val = addr.getCustNm2();
        String custToUse = "";
        Map<String, String> dplStatRowMap = null;

        if (!StringUtils.isEmpty(custName1Val) || !StringUtils.isEmpty(custName2Val)) {
          dplStatRowMap = new HashMap<>();
          if (!StringUtils.isEmpty(custName1Val)) {
            custToUse = custName1Val;
          } else {
            custToUse = custName2Val;
          }
          AppUser user = AppUser.getUser(request);
          dplStatRowMap = doCheckLDMassUpdtAddr(user, model, admin, entityManager, custToUse, cmrNoVal, addr);// doCheckCmrAddressForCurrRow

          if (dplStatRowMap != null) {
            addr.setDplChkResult(dplStatRowMap.get("ROW_DPL_STAT"));
            LOG.debug(">>> Setting the DPL Result of > " + dplStatRowMap.get("ROW_DPL_STAT"));
            addr.setDplChkTimestamp(SystemUtil.getCurrentTimestamp());
            LOG.debug(">>> Setting the DPL Result Timestamp of > " + dplStatRowMap.get("ROW_DPL_RUN_DATE"));
            entityManager.merge(addr);

            if (handler != null && handler.shouldAutoDplSearchMassUpdate()) {
              if (addr.getDplChkResult().equals("F")) {
                LOG.debug("Start performing auto search DPL...");
                if (model.getCmrIssuingCntry().equals((SystemLocation.ISRAEL))) {
                  if (!IL_AUTO_DPL_SEACRH.contains(addr.getId().getAddrType())) {
                    continue;
                  }
                }
                ParamContainer params = new ParamContainer();
                params.addParam("processType", "ATTACHMASSUPDT");
                params.addParam("reqId", admin.getId().getReqId());
                params.addParam("user", user);
                params.addParam("filePrefix", "AutoDPLSearch_");
                params.addParam("custname1", custName1Val);
                params.addParam("custname2", custName2Val);
                params.addParam("addrType", addr.getId().getAddrType());
                params.addParam("dplChkResult", addr.getDplChkResult());
                try {
                  DPLSearchService dplService = new DPLSearchService();
                  dplService.process(null, params);
                } catch (Exception e) {
                  this.log.warn("DPL results not attached to the request", e);
                }
              }
              LOG.debug("End performing auto search DPL...");
            }
          }
        }
      }
    }
  }

  private void performMassDplChecking(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    if (model.getCmrIssuingCntry().equalsIgnoreCase("631") && model.getReqType().equalsIgnoreCase("M")) {
      long reqId = model.getReqId();
      Admin admin = adminService.getCurrentRecord(model, entityManager, request);
      String filePath = admin.getFileName();
      String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
      String zipFilePath = filePath + ".zip";
      Workbook workBookFromZip = null;
      Sheet dataSheetFromZip = null;
      DataFormatter df = new DataFormatter();
      Map<String, String> dplStatRowMap = null;
      File uploadDir = prepareUploadDir(reqId);

      File file = new File(zipFilePath);
      if (!file.exists()) {
        initLogger().info("Mass file: " + filePath + " does not exist. Cannot proceed Mass Dpl Checking");
        throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
      }

      ZipEntry zipEnt = null;

      // get file inside the zip
      try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        Enumeration<?> entry = zipFile.entries();
        if (entry.hasMoreElements()) {
          zipEnt = zipFile.getEntry(fileName);
        }
        workBookFromZip = new XSSFWorkbook(zipFile.getInputStream(zipEnt));
        dataSheetFromZip = workBookFromZip.getSheet(CMR_SHEET_NAME);
      } catch (Exception ex) {
        initLogger().error(ex.getMessage());
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }

      // add new cell title for DPL
      try {
        for (Row dataRow : dataSheetFromZip) {
          CreationHelper createHelper = workBookFromZip.getCreationHelper();
          if (dataRow.getRowNum() == TITLE_ROW_NO) { // title row
            Cell dplStatTitle = dataRow.getCell(45 - 1);
            if (dplStatTitle == null) {
              dataRow.createCell(45 - 1).setCellValue(createHelper.createRichTextString("DPL STATUS"));
            }
            Cell dplDateTitle = dataRow.getCell(46 - 1);
            if (dplDateTitle == null) {
              dataRow.createCell(46 - 1).setCellValue(createHelper.createRichTextString("DPL RUN DATE"));
            }
            Cell dplErrorTitle = dataRow.getCell(47 - 1);
            if (dplErrorTitle == null) {
              dataRow.createCell(47 - 1).setCellValue(createHelper.createRichTextString("DPL ERROR"));
            }
          } else if (dataRow.getRowNum() >= CMR_ROW_NO) {
            // data rows
            dataRow.createCell(45 - 1);
            dataRow.createCell(46 - 1);
            dataRow.createCell(47 - 1);
          }
        }
      } catch (Exception ex) {
        initLogger().error(ex.getMessage(), ex);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }

      try {
        for (Row dataRow : dataSheetFromZip) {
          if (dataRow.getRowNum() >= CMR_ROW_NO) {
            String cmrNoVal = df.formatCellValue(dataRow.getCell(1 - 1));
            String custName1Val = df.formatCellValue(dataRow.getCell(15 - 1));
            String custName2Val = df.formatCellValue(dataRow.getCell(16 - 1));
            String custToUse = "";
            CreationHelper createHelper = workBookFromZip.getCreationHelper();
            if (!StringUtils.isEmpty(custName1Val) || !StringUtils.isEmpty(custName2Val)) {
              dplStatRowMap = new HashMap<>();
              if (!StringUtils.isEmpty(custName1Val)) {
                custToUse = custName1Val;
              } else {
                custToUse = custName2Val;
              }
              dplStatRowMap = doCheckCmrAddressForCurrRow(model, entityManager, custToUse, "0" + cmrNoVal);
              dataRow.getCell(45 - 1).setCellValue(createHelper.createRichTextString(dplStatRowMap.get("ROW_DPL_STAT")));
              dataRow.getCell(46 - 1).setCellValue(createHelper.createRichTextString(dplStatRowMap.get("ROW_DPL_RUN_DATE")));
              if (dplStatRowMap.get("ROW_DPL_STAT").equalsIgnoreCase("All Failed")
                  || dplStatRowMap.get("ROW_DPL_STAT").equalsIgnoreCase("Some Failed/Not Done")) {
                dataRow.getCell(47 - 1).setCellValue(createHelper.createRichTextString(dplStatRowMap.get("ERROR_LOG")));
              }
            } else {
              if (!StringUtils.isEmpty(cmrNoVal)) {
                dataRow.getCell(47 - 1).setCellValue(createHelper.createRichTextString("No Customer Name 1/Customer Name 2"));
              }
            }
          }
        }
      } catch (Exception ex) {
        initLogger().error("error while parsing workbook " + ex.getMessage(), ex);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }

      boolean computeSuccess = computeFileOverAllDplStat(workBookFromZip, reqId, request, entityManager);

      if (computeSuccess) {
        try {
          int iterId = admin.getIterationId();
          String newFileName = "MassUpdate_" + reqId + "_Iter" + iterId + "_MassDpl_LOG.xlsx";

          if (iterId >= 2) {
            // double checking
            iterId = iterId - 1;
            String oldFileName = "MassUpdate_" + reqId + "_Iter" + iterId + "_MassDpl_LOG.xlsx";
            String oldFilePath = uploadDir.getAbsolutePath() + "/" + oldFileName;
            oldFilePath = oldFilePath.replaceAll("[\\\\]", "/");
            File dplWorkBookOldFile = new File(oldFilePath);
            if (dplWorkBookOldFile.exists()) {
              initLogger().info("Deleteting old dpl file : " + oldFileName);
              dplWorkBookOldFile.delete();
            }
          }
          String newFilePath = uploadDir.getAbsolutePath() + "/" + newFileName;
          newFilePath = newFilePath.replaceAll("[\\\\]", "/");
          File dplWorkBookNewFile = new File(newFilePath);
          if (dplWorkBookNewFile.exists()) {
            dplWorkBookNewFile.delete();
            initLogger().error("file deleted " + newFileName + " will create new file");
          }
          try (FileOutputStream newOutStream = new FileOutputStream(dplWorkBookNewFile)) {
            workBookFromZip.write(newOutStream);
          } finally {
            workBookFromZip.close();
          }
        } catch (Exception ex) {
          initLogger().error(ex.getMessage(), ex);
          throw new CmrException(MessageUtil.ERROR_GENERAL);
        } finally {
          // finally done
        }
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } else {
      initLogger().error(model.getCmrIssuingCntry() + " not yet supported for mass DPL Check");
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
  }

  private boolean computeFileOverAllDplStat(Workbook workBookToParse, long reqId, HttpServletRequest request, EntityManager entityManager) {
    int all = 0, passed = 0, failed = 0, notdone = 0, notrequired = 0;
    try {
      Scorecard score = addrService.getScorecardRecord(entityManager, reqId);
      AppUser currentUser = AppUser.getUser(request);
      DataFormatter df = new DataFormatter();
      Sheet dataSheet = workBookToParse.getSheet(CMR_SHEET_NAME);
      for (Row dataRow : dataSheet) {
        if (dataRow.getRowNum() >= CONFIG_ROW_NO) {
          String custName1Val = df.formatCellValue(dataRow.getCell(15 - 1));
          String custName2Val = df.formatCellValue(dataRow.getCell(16 - 1));
          String rowStat = df.formatCellValue(dataRow.getCell(45 - 1));
          if (!StringUtils.isEmpty(custName1Val) || !StringUtils.isEmpty(custName2Val)) {
            all++;
            if ("All Passed".equals(rowStat)) {
              passed++;
            } else if ("All Failed".equals(rowStat)) {
              failed++;
            } else if ("Not Done".equals(rowStat)) {
              notdone++;
            } else if ("Not Required".equals(rowStat)) {
              notrequired++;
            }
          }
        }
      }
      if (all == notrequired) {
        score.setDplChkResult("NR");
      } else if (all == passed + notrequired) {
        score.setDplChkResult("AP");
      } else if (all == failed + notrequired) {
        score.setDplChkResult("AF");
      } else if (passed > 0 && all != passed) {
        score.setDplChkResult("SF");
      }
      if (notdone != all) {
        score.setDplChkTs(SystemUtil.getCurrentTimestamp());
        score.setDplChkUsrId(currentUser.getIntranetId());
        score.setDplChkUsrNm(currentUser.getBluePagesName());
        updateEntity(score, entityManager);
      }
    } catch (Exception ex) {
      initLogger().error("error while running computeFileOverAllDplStat", ex);
      return false;
    }
    return true;
  }

  // public void performDPLCheckPerMassUpdtAddress(EntityManager eManager,
  // AppUser user, String cmr, List<MassUpdtAddr> muAddrs) throws CmrException {
  // log.debug("Entering performDPLCheckPerMassUpdtAddress()");
  // FindCMRResultModel results = null;
  //
  // try {
  // if (eManager == null) {
  // eManager = JpaManager.getEntityManager();
  // }
  //
  // if (muAddrs == null && muAddrs.size() > 0) {
  // throw new CmrException(new
  // Exception(">> There are no mass update addresses to perform DPL check
  // on."));
  // return;
  // }
  //
  // // Now we build the Addr list, but we query first findCMR
  // for (MassUpdtAddr muAddr : muAddrs) {
  // // start query
  // results = LegacyDirectUtil.findCmrByAddrSeq(muAddr.getCmrNo(), cmr,
  // muAddr.getAddrSeqNo(), "");
  // }
  //
  // for (Addr addr : addresses) {
  // // initialize all
  // addr.setDplChkResult(null);
  // }
  //
  // if (geoHandler != null) {
  // geoHandler.doBeforeDPLCheck(entityManager, data, addresses);
  // }
  //
  // DPLCheckResult dplResult = null;
  // String errorInfo = null;
  //
  // for (Addr addr : addresses) {
  // errorInfo = null;
  // if (addr.getDplChkResult() == null) {
  // Boolean errorStatus = false;
  // try {
  // dplResult = dplCheckAddress(admin, addr, geoHandler != null ?
  // !geoHandler.customerNamesOnAddress() : false);
  // } catch (Exception e) {
  // log.error("Error in performing DPL Check when call EVS on Request ID " +
  // reqId + " Addr " + addr.getId().getAddrType() + "/"
  // + addr.getId().getAddrSeq(), e);
  // if (dplResult == null) {
  // dplResult = new DPLCheckResult();
  // }
  // errorStatus = true;
  // }
  // if (dplResult.isPassed()) {
  // addr.setDplChkResult("P");
  // addr.setDplChkById(user.getIntranetId());
  // addr.setDplChkByNm(user.getBluePagesName());
  // addr.setDplChkErrList(null);
  // addr.setDplChkInfo(null);
  // addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
  // entityManager.merge(addr);
  // } else {
  // errorInfo = "";
  // if (dplResult.isUnderReview()) {
  // errorInfo += " Export under review";
  // }
  // if (errorStatus) {
  // errorInfo = MessageUtil.getMessage(MessageUtil.ERROR_DPL_EVS_ERROR);
  // } else if (!StringUtils.isEmpty(dplResult.getFailureDesc())) {
  // errorInfo += ", " + dplResult.getFailureDesc();
  // }
  // List<String> available = EWSProperties.listAllDplExportLocation();
  // List<String> passed = dplResult.getLocationsPassed();
  // StringBuilder failedList = new StringBuilder();
  // for (String loc : available) {
  // if (passed != null && !passed.contains(loc) && !"ALL".equals(loc)) {
  // failedList.append(failedList.length() > 0 ? ", " : "");
  // failedList.append(loc);
  // }
  // }
  //
  // addr.setDplChkResult("F");
  // addr.setDplChkById(user.getIntranetId());
  // addr.setDplChkByNm(user.getBluePagesName());
  // addr.setDplChkErrList(failedList.toString());
  // addr.setDplChkInfo(errorInfo);
  // addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
  // entityManager.merge(addr);
  // }
  //
  // } else {
  // addr.setDplChkById(user.getIntranetId());
  // addr.setDplChkByNm(user.getBluePagesName());
  // addr.setDplChkErrList(null);
  // addr.setDplChkInfo(null);
  // addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
  // entityManager.merge(addr);
  // }
  // }
  //
  // entityManager.flush();
  // // compute the overall score
  // recomputeDPLResult(user, entityManager, reqId);
  //
  // // commit only once
  // transaction.commit();
  //
  // } catch (Exception e) {
  //
  // // rollback transaction when exception occurs
  // if (transaction != null && transaction.isActive()) {
  // transaction.rollback();
  // }
  //
  // // only wrap non CmrException errors
  // if (e instanceof CmrException) {
  // log.error("Error in performing DPL Check on Request ID" + ((CmrException)
  // e).getMessage());
  // throw (CmrException) e;
  // } else {
  // log.error("Error in performing DPL Check on Request ID" + reqId, e);
  // throw new CmrException(MessageUtil.ERROR_DPL_ERROR);
  // }
  // } finally {
  // // try to rollback, for safekeeping
  // if (transaction != null && transaction.isActive()) {
  // transaction.rollback();
  // }
  //
  // ChangeLogListener.clearManager();
  // // empty the manager
  // entityManager.clear();
  // entityManager.close();
  // log.debug("Exiting performDPLCheck()");
  // }
  // }

  private Map<String, String> doCheckLDMassUpdtAddr(AppUser user, RequestEntryModel model, Admin admin, EntityManager entityManager, String custToUse,
      String cmrNo, MassUpdtAddr muAddr) throws CmrException {
    List<AddressModel> extractedAddr = new ArrayList<>();
    List<Addr> tempExtractAddr = new ArrayList<Addr>();
    FindCMRResultModel results = null;
    List<FindCMRRecordModel> cmrs = new ArrayList<>();
    long reqId = model.getReqId();
    String cmrCntry = model.getCmrIssuingCntry();
    DPLCheckResult dplResult = null;
    String rowDplOverAllStat = "";
    Map<String, List<String>> lndCntMap = new HashMap<String, List<String>>();
    Map<String, String> mapTrans = new HashMap<>();
    Map<String, Addr> mapExtracted = new HashMap<String, Addr>();
    try {
      results = SystemUtil.findCMRs(cmrNo, cmrCntry, 2000, "");
      if (results.getItems().size() > 0) {
        Collections.sort(results.getItems()); // have main record on top
      }
      if (results != null) {
        cmrs = results.getItems();
      }
    } catch (Exception ex) {
      initLogger().error("there was an error while performing cmr search : " + ex.getMessage());
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }

    // CMR-7562 - append legacy only addresses here
    cmrs.addAll(getAddressesNotInRdc(cmrs, entityManager, cmrNo, cmrCntry));

    if (cmrs.size() > 0) {
      String type = null;
      Map<String, Integer> seqMap = new HashMap<String, Integer>();
      Integer seq = null;
      for (FindCMRRecordModel cmrsMods : cmrs) {
        Addr addr = new Addr();
        AddrPK addrPK = new AddrPK();
        String muAddrSeqNo = "";
        String cmrsModsSeqNo = "";

        if (muAddr.getAddrSeqNo().length() != cmrsMods.getCmrAddrSeq().length()) {
          muAddrSeqNo = LegacyDirectUtil.handleLDSeqNoScenario(muAddr.getAddrSeqNo(), true);
          cmrsModsSeqNo = LegacyDirectUtil.handleLDSeqNoScenario(cmrsMods.getCmrAddrSeq(), true);
        }

        if (muAddrSeqNo.equals(cmrsModsSeqNo)) {
          addrPK.setAddrSeq(cmrsMods.getCmrAddrSeq());
          addrPK.setReqId(reqId);
          addrPK.setAddrType(cmrsMods.getCmrAddrTypeCode());
          addr.setId(addrPK);
          addr.setLandCntry(cmrsMods.getCmrCountryLanded());
          addr.setDplChkResult("ND");
          addr.setCustNm1(cmrsMods.getCmrName1Plain());
          addr.setCustNm2(cmrsMods.getCmrName2Plain());
          addr.setAddrTxt(cmrsMods.getCmrStreetAddress());
          addr.setAddrTxt2(cmrsMods.getCmrStreetAddressCont());
          addr.setStateProv(cmrsMods.getCmrState());
          addr.setCity1(cmrsMods.getCmrCity());
          addr.setParCmrNo(muAddr.getCmrNo());

          if (!StringUtils.isEmpty(muAddr.getCustNm1())) {
            addr.setCustNm1(muAddr.getCustNm1());
          }

          if (!StringUtils.isEmpty(muAddr.getCustNm2())) {
            addr.setCustNm2(muAddr.getCustNm2());
          }

          tempExtractAddr.add(addr);
          // mapExtracted.put(muAddr.getCmrNo() + muAddr.getAddrSeqNo(),
          // addr);
        }
      }

      // get distinct landed country for dpl check
      for (AddressModel mods : extractedAddr) {
        if (!lndCntMap.containsKey(mods.getLandCntry())) {
          lndCntMap.put(mods.getLandCntry(), new ArrayList<String>());
          lndCntMap.get(mods.getLandCntry()).add(custToUse);
        }
      }
    }

    for (Addr addrM : tempExtractAddr) {
      Boolean errorStatus = false;
      Boolean isPrivate = false;
      try {
        dplResult = addrService.dplCheckAddress(admin, addrM, null, model.getCmrIssuingCntry(), false, isPrivate);
      } catch (Exception ex) {
        initLogger().error("Error in performing DPL Check when call EVS on Request ID " + reqId, ex);
        if (dplResult == null) {
          dplResult = new DPLCheckResult();
        }
        errorStatus = true;
      }
      if (dplResult.isPassed()) {
        // addrM.setDplChkResult("P");
        // muAddr.setDplChkResult("P");
        mapTrans.put("ROW_DPL_STAT", "P");
      } else if (!dplResult.isPassed()) {
        // addrM.setDplChkResult("F");
        // muAddr.setDplChkResult("F");
        mapTrans.put("ROW_DPL_STAT", "F");
      }

      if (model.getCmrIssuingCntry().equals(SystemLocation.ISRAEL)) {
        if (muAddr.getId().getAddrType().equals("ZS01") || muAddr.getId().getAddrType().equals("ZP01")
            || muAddr.getId().getAddrType().equals("ZD01")) {
          mapTrans.put("ROW_DPL_STAT", "N");
        }
      }

      mapTrans.put("ROW_DPL_RUN_DATE", SystemUtil.getCurrentTimestamp().toString());
      // muAddr.setDplChkTimestamp(SystemUtil.getCurrentTimestamp());
      // entityManager.merge(muAddr);
    }
    return mapTrans;
  }

  /**
   * Retrieves the address records which are not found in FindCMR (legacy only)
   * 
   * @param entityManager
   * @param cmrNo
   * @param cmrCntry
   * @return
   */
  private List<FindCMRRecordModel> getAddressesNotInRdc(List<FindCMRRecordModel> cmrs, EntityManager entityManager, String cmrNo, String cmrCntry) {
    // get the address sequences in rdc, prefilter
    StringBuilder sequences = new StringBuilder();
    for (FindCMRRecordModel cmr : cmrs) {
      String seqNo = cmr.getCmrAddrSeq();
      if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
        sequences.append(sequences.length() > 0 ? "," : "");
        sequences.append("'" + LegacyDirectUtil.handleLDSeqNoScenario(seqNo, true) + "'");
      }
    }
    LOG.debug("Retreiving addresses not in RDC for RCYAA " + cmrCntry + " and RCUXA " + cmrNo);
    String extAddrSql = ExternalizedQuery.getSql("GET.LD_MASS_UPDT_EXT_ADDR");
    PreparedQuery extAddrQuery = new PreparedQuery(entityManager, extAddrSql);
    if (sequences.length() > 0) {
      // those are already in rdc, skip them
      extAddrQuery.append("and ADDRNO not in (" + sequences.toString() + ")");
    }
    extAddrQuery.setParameter("CNTRY", cmrCntry);
    extAddrQuery.setParameter("CMR_NO", cmrNo);
    extAddrQuery.setForReadOnly(true);
    List<CmrtAddr> addresses = extAddrQuery.getResults(CmrtAddr.class);
    List<FindCMRRecordModel> extAddresses = new ArrayList<FindCMRRecordModel>();
    if (addresses != null && !addresses.isEmpty()) {
      for (CmrtAddr addr : addresses) {
        FindCMRRecordModel rec = new FindCMRRecordModel();

        rec.setCmrAddrSeq(addr.getId().getAddrNo());
        rec.setCmrAddrTypeCode("EXT1");
        rec.setCmrCountryLanded(PageManager.getDefaultLandedCountry(cmrCntry));
        rec.setCmrName1Plain(addr.getAddrLine1());
        rec.setCmrName2(addr.getAddrLine2());
        rec.setCmrStreetAddress(addr.getAddrLine3());
        rec.setCmrStreetAddressCont(addr.getAddrLine4());
        rec.setCmrCity(addr.getAddrLine5()); // does not matter, DPL check only
                                             // does name + country
        extAddresses.add(rec);
      }
    }
    LOG.debug("Extracted " + extAddresses.size() + " from legacy DB2..");
    return extAddresses;
  }

  private Map<String, String> doCheckCmrAddressForCurrRow(RequestEntryModel model, EntityManager entityManager, String custToUse, String cmrNo)
      throws CmrException {
    List<AddressModel> extractedAddr = new ArrayList<>();
    FindCMRResultModel results = null;
    List<FindCMRRecordModel> cmrs = new ArrayList<>();
    long reqId = model.getReqId();
    String cmrCntry = model.getCmrIssuingCntry();
    DPLCheckResult dplResult = null;
    String rowDplOverAllStat = "";
    Map<String, List<String>> lndCntMap = new HashMap<String, List<String>>();
    Map<String, String> mapTrans = new HashMap<>();
    try {
      results = SystemUtil.findCMRs(cmrNo, cmrCntry, 2000, "");
      if (results.getItems().size() > 0) {
        Collections.sort(results.getItems()); // have main record on top
      }
      if (results != null) {
        cmrs = results.getItems();
      }
    } catch (Exception ex) {
      initLogger().error("there was an error while performing cmr search : " + ex.getMessage());
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
    if (cmrs.size() > 0) {
      String type = null;
      Map<String, Integer> seqMap = new HashMap<String, Integer>();
      Integer seq = null;
      for (FindCMRRecordModel cmrsMods : cmrs) {
        AddressModel addr = new AddressModel();
        addr.setReqId(reqId);
        if (seqMap.get(type) == null) {
          seqMap.put(type, new Integer(0));
        }
        seq = seqMap.get(type);
        addr.setAddrSeq((seq + 1) + "");
        seqMap.put(type, new Integer(seq + 1));
        addr.setLandCntry(cmrsMods.getCmrCountryLanded());
        addr.setCmrNo(cmrNo);
        addr.setDplChkResult("ND");
        extractedAddr.add(addr);

      }

      // get distinct landed country for dpl check
      for (AddressModel mods : extractedAddr) {
        if (!lndCntMap.containsKey(mods.getLandCntry())) {
          lndCntMap.put(mods.getLandCntry(), new ArrayList<String>());
          lndCntMap.get(mods.getLandCntry()).add(custToUse);
        }
      }
    }

    List<String> custNames = null;
    String errorInfo = null;
    for (String lndCnt : lndCntMap.keySet()) {
      custNames = lndCntMap.get(lndCnt);
      for (String custName : custNames) {
        Boolean errorStatus = false;
        try {
          dplResult = addrService.dplCheckViaServices(reqId, lndCnt, custName);
        } catch (Exception ex) {
          initLogger().error("Error in performing DPL Check when call EVS on Request ID " + reqId, ex);
          if (dplResult == null) {
            dplResult = new DPLCheckResult();
          }
          errorStatus = true;
        }
        if (dplResult.isPassed()) {
          for (AddressModel mod : extractedAddr) {
            if (mod.getLandCntry().equalsIgnoreCase(lndCnt)) {
              mod.setDplChkResult("P");
            }
          }
        } else {
          errorInfo = "";
          if (dplResult.isUnderReview()) {
            errorInfo += " Export under review";
          }
          if (errorStatus) {
            errorInfo = MessageUtil.getMessage(MessageUtil.ERROR_DPL_EVS_ERROR);
          } else if (!StringUtils.isEmpty(dplResult.getFailureDesc())) {
            errorInfo += ", " + dplResult.getFailureDesc();
          }
          for (AddressModel mods : extractedAddr) {
            if (mods.getLandCntry().equalsIgnoreCase(lndCnt)) {
              mods.setDplChkResult("F");
              List<String> passed = dplResult.getLocationsPassed();
              StringBuilder failedList = new StringBuilder();
              try {
                List<String> available = EWSProperties.listAllDplExportLocation();
                for (String loc : available) {
                  if (passed != null && !passed.contains(loc) && !"ALL".equals(loc)) {
                    failedList.append(failedList.length() > 0 ? ", " : "");
                    failedList.append(loc);
                  }
                }
              } catch (Exception e) {
                // TODO: handle exception
              }
              mods.setDplChkInfo(errorInfo);
              mods.setDplChkErrList(failedList.toString());
            }
          }
        }
      }
    }

    rowDplOverAllStat = computeDplStatForCurrentRow(extractedAddr);
    mapTrans.put("ROW_DPL_STAT", rowDplOverAllStat);
    mapTrans.put("ROW_DPL_RUN_DATE", SystemUtil.getCurrentTimestamp().toString());

    if (rowDplOverAllStat.equalsIgnoreCase("All Failed") || rowDplOverAllStat.equalsIgnoreCase("Some Faild/Not Done")) {
      String compiledErr = "";
      for (AddressModel addr : extractedAddr) {
        if (addr.getDplChkResult().equalsIgnoreCase("F")) {
          compiledErr += "Type:" + addr.getAddrType() + " ERROR:" + addr.getDplChkInfo() + ", " + addr.getDplChkErrList();
        }
      }
      mapTrans.put("ERROR_LOG", compiledErr);
    }
    return mapTrans;
  }

  private String computeDplStatForCurrentRow(List<AddressModel> extractedAddr) {
    String rowOverAllStat = "";
    int all = 0, passed = 0, failed = 0, notdone = 0, notrequired = 0;
    for (AddressModel mods : extractedAddr) {
      all++;
      if ("P".equals(mods.getDplChkResult())) {
        passed++;
      } else if ("F".equals(mods.getDplChkResult())) {
        failed++;
      } else if ("ND".equals(equals(mods.getDplChkResult()))) {
        notdone++;
      } else if ("NR".equals(equals(mods.getDplChkResult()))) {
        notrequired++;
      }
    }

    if (all == notrequired) {
      rowOverAllStat = "Not Required";
    } else if (all == passed + notrequired) {
      rowOverAllStat = "All Passed";
    } else if (all == failed + notrequired) {
      rowOverAllStat = "All Failed";
    } else if (passed > 0 && all != passed) {
      rowOverAllStat = "Some Faild/Not Done";
    }
    /*
     * if (notdone != all) { }
     */
    return rowOverAllStat;
  }

  /**
   * Saves the request. This method handles both Create and Update
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performSave(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request, boolean noScorecard) throws Exception {
    AppUser user = AppUser.getUser(request);

    Admin adminToUse = null;
    if (BaseModel.STATE_NEW == model.getState()) {
      CompoundEntity entity = createFromModel(model, entityManager, request);
      long reqId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "REQ_ID");

      // create the Admin record
      Admin admin = entity.getEntity(Admin.class);
      admin.getId().setReqId(reqId);
      admin.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
      admin.setRequesterId(user.getIntranetId());
      admin.setRequesterNm(user.getBluePagesName());
      admin.setCreateTs(SystemUtil.getCurrentTimestamp());
      admin.setLastUpdtBy(user.getIntranetId());
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setCovBgRetrievedInd(CmrConstants.YES_NO.N.toString());
      String sysType = SystemConfiguration.getValue("SYSTEM_TYPE");
      admin.setWaitInfoInd(!StringUtils.isBlank(sysType) ? sysType.substring(0, 1) : null);

      // modified to use the new way of checking automatic processing on request
      // type level
      // setDisableProc(model, admin); // FOR LA and EMEA

      if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
      }

      // always set ITERATION_ID =1 for reactivate and delete requests
      if ("R".equalsIgnoreCase(model.getReqType()) || "D".equalsIgnoreCase(model.getReqType())) {
        admin.setIterationId(1);
      } else if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(model.getReqType().trim())) {
        // create the MassUpdt record
        MassUpdt massUpdt = new MassUpdt();
        MassUpdtPK pk = new MassUpdtPK();
        // massUpdt.setRowStatusCd(rowStatusCd);
        pk.setIterationId(0);
        pk.setSeqNo(0);
        pk.setParReqId(reqId);

        massUpdt.setId(pk);
        massUpdt.setRowStatusCd("PASS");
        massUpdt.setCmrNo("");
        createEntity(massUpdt, entityManager);

        // create the MassUpdtData record
        MassUpdtData massUpdtData = new MassUpdtData();
        MassUpdtDataPK pk1 = new MassUpdtDataPK();
        pk1.setIterationId(0);
        pk1.setSeqNo(0);
        pk1.setParReqId(reqId);
        massUpdtData.setId(pk1);
        createEntity(massUpdtData, entityManager);

        admin.setIterationId(0);
      } else if ("M".equalsIgnoreCase(model.getReqType())) {
        if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
          Scorecard score = entity.getEntity(Scorecard.class);
          score.getId().setReqId(reqId);
          createEntity(score, entityManager);
        } else if (model.getCmrIssuingCntry().equalsIgnoreCase("760")) {
          // create the MassUpdt record
          MassUpdt massUpdt = new MassUpdt();
          MassUpdtPK pk = new MassUpdtPK();
          // massUpdt.setRowStatusCd(rowStatusCd);
          pk.setIterationId(0);
          pk.setSeqNo(0);
          pk.setParReqId(reqId);

          massUpdt.setId(pk);
          massUpdt.setRowStatusCd("");
          massUpdt.setCmrNo("");
          createEntity(massUpdt, entityManager);

          // create the MassUpdtData record
          MassUpdtData massUpdtData = new MassUpdtData();
          MassUpdtDataPK pk1 = new MassUpdtDataPK();
          pk1.setIterationId(0);
          pk1.setSeqNo(0);
          pk1.setParReqId(reqId);
          massUpdtData.setId(pk1);
          createEntity(massUpdtData, entityManager);

          admin.setIterationId(0);
        }
      }

      RequestUtils.setClaimDetails(admin, request);
      // clear cmt value as it is saved in new table .
      createEntity(admin, entityManager);
      adminToUse = admin;

      // create the Data record
      Data data = entity.getEntity(Data.class);
      data.getId().setReqId(reqId);
      createEntity(data, entityManager);

      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, "AUTO: Request created.", CmrConstants.Save());
      // save comment in req_cmt_log table .
      // save only if it is not null or not blank
      if (null != model.getCmt() && !model.getCmt().isEmpty()) {
        RequestUtils.createCommentLog(this, entityManager, user, reqId, model.getCmt());
      }

      RequestUtils.addToNotifyList(this, entityManager, user, reqId);

      model.setReqId(reqId);
    } else {
      CompoundEntity entity = getCurrentRecord(model, entityManager, request);
      boolean clearCmrNoAndSap = false;
      if (entity != null) {
        Admin admin = entity.getEntity(Admin.class);
        if (admin != null && "U".equals(admin.getReqType()) && "C".equals(model.getReqType())) {
          clearCmrNoAndSap = true;
        }
      }
      copyValuesToEntity(model, entity);

      if (noScorecard) {
        // mass dpl
        if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
          Scorecard score = entity.getEntity(Scorecard.class);
          entityManager.detach(score);
        }
      }

      // detachScoreCard(entityManager, entity);

      // create the Admin record
      Admin admin = entity.getEntity(Admin.class);
      admin.setLastUpdtBy(user.getIntranetId());
      admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
      admin.setWarnMsgSentDt(null);

      // POOJA TYAGI
      // setDisableProc(model, admin); // FOR LA and EMEA
      if (StringUtils.isEmpty(admin.getLockInd())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getProcessedFlag())) {
        admin.setLockInd(CmrConstants.YES_NO.N.toString());
      }
      if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
        admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
      }

      // clear cmt value as it is saved in new table .
      // always clear the values when status is changed to PCP
      if ("PCP".equals(admin.getReqStatus())) {
        admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
        admin.setProcessedTs(null);
      }

      setLockByName(admin);
      updateEntity(admin, entityManager);

      adminToUse = admin;
      // create the Data record
      Data data = entity.getEntity(Data.class);
      if (clearCmrNoAndSap) {
        data.setCmrNo(null);
      }
      updateEntity(data, entityManager);
      if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(model.getReqType().trim())) {
        performUpdateByEnt(model, entityManager);
      }

      if (clearCmrNoAndSap) {
        clearCMRNoAndSap(entityManager, model.getReqId());
      }
      // save comment in req_cmt_log table while updating a request .
      // save only if it is not null or not blank
      if (null != model.getCmt() && !model.getCmt().isEmpty()) {
        RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), model.getCmt());
      }

      if (!noScorecard) {
        if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
          // no op
        }
      }
    }

    // compute the internal type, ensure value is saved to db first before
    // executing
    if (adminToUse != null) {
      String reqType = model.getReqType();
      String cmrIssuingCountry = model.getCmrIssuingCntry();
      CmrInternalTypes type = RequestUtils.computeInternalType(entityManager, reqType, cmrIssuingCountry, adminToUse.getId().getReqId());
      if (type != null) {
        adminToUse.setInternalTyp(type.getId().getInternalTyp());
        adminToUse.setSepValInd(type.getSepValInd());
      }
      updateEntity(adminToUse, entityManager);
    }

  }

  private void clearCMRNoAndSap(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARSAPNO"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARADDRRESULT"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

    query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.CLEARSAPNO_RDC"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

  }

  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String processingCenter) throws Exception {
    performGenericAction(trans, model, entityManager, request, null, null, false);
  }

  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String sendToId, String sendToNm, boolean complete) throws Exception {
    performGenericAction(trans, model, entityManager, request, sendToId, sendToNm, complete, true);
  }

  /**
   * Generic Action
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performGenericAction(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request,
      String sendToId, String sendToNm, boolean complete, boolean transitionToNext) throws Exception {
    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);

    copyValuesToEntity(model, entity);

    // update the Admin record
    Admin admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setWarnMsgSentDt(null);

    // detach trans from em
    entityManager.detach(trans);

    if (transitionToNext) {
      // set request status SVA/SV2 for MassCreate system validation
      if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(admin.getReqType())
          && CmrConstants.Send_for_Processing().equalsIgnoreCase(model.getAction())
          && CmrConstants.REQUEST_STATUS.DRA.toString().equals(trans.getId().getCurrReqStatus())) {
        if (StringUtils.isNotEmpty(model.getCmrIssuingCntry())) {
          trans.setNewReqStatus(CmrConstants.REQUEST_STATUS.SVA.toString());
          // trans.setNewReqStatus(CmrConstants.REQUEST_STATUS.SMA.toString());
          trans.setNewLockedInd(CmrConstants.YES_NO.N.toString());
        } /*
           * else {
           * trans.setNewReqStatus(CmrConstants.REQUEST_STATUS.SVA.toString());
           * trans.setNewLockedInd(CmrConstants.YES_NO.N.toString()); }
           */
      } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equalsIgnoreCase(admin.getReqType())
          && CmrConstants.Create_Update_CMR().equalsIgnoreCase(model.getAction())
          && CmrConstants.REQUEST_STATUS.PVA.toString().equals(trans.getId().getCurrReqStatus())) {
        trans.setNewReqStatus(CmrConstants.REQUEST_STATUS.SV2.toString());
        // trans.setNewReqStatus(CmrConstants.REQUEST_STATUS.SM2.toString());
        trans.setNewLockedInd(CmrConstants.YES_NO.N.toString());
      }
      admin.setReqStatus(trans.getNewReqStatus());
    }

    // always clear the values when status is changed to PCP
    if (transitionToNext && "PCP".equals(trans.getNewReqStatus())) {
      admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
      admin.setProcessedTs(null);
    }

    // always set ITERATION_ID =1 for reactivate and delete requests
    if ("R".equalsIgnoreCase(admin.getReqType()) || "D".equalsIgnoreCase(admin.getReqType())) {
      admin.setIterationId(1);
    }

    if (transitionToNext) {
      if (CmrConstants.YES_NO.Y.toString().equals(trans.getNewLockedInd())
          && CmrConstants.YES_NO.N.toString().equals(trans.getId().getCurrLockedInd())) {
        // the request is to be locked
        RequestUtils.setClaimDetails(admin, request);
      } else if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
          && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
        // request to be unlocked
        RequestUtils.clearClaimDetails(admin);
      }
      if (sendToId != null) {
        admin.setLastProcCenterNm(sendToId);
      }
    }

    if (StringUtils.isEmpty(admin.getLockInd())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getProcessedFlag())) {
      admin.setLockInd(CmrConstants.YES_NO.N.toString());
    }
    if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
    }

    if (!PageManager.autoProcEnabled(model.getCmrIssuingCntry(), model.getReqType())) {
      admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
    }

    // set cmt as blank
    updateEntity(admin, entityManager);

    // MASS_UPDATE | set ROW_STATUS_CD = READY if 'CRU'
    if ((CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(admin.getReqType()) || CmrConstants.REQ_TYPE_DELETE.equalsIgnoreCase(admin.getReqType())
        || CmrConstants.REQ_TYPE_REACTIVATE.equalsIgnoreCase(admin.getReqType()))
        && CmrConstants.Create_Update_CMR().equals(trans.getId().getAction())) {
      massDataService.setToReady(entityManager, model.getReqId(), admin.getIterationId());
    }

    if (!CmrConstants.Send_for_Processing().toString().equals(model.getAction())
        && CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(model.getReqType().trim())) {
      performUpdateByEnt(model, entityManager);
    }

    // save only if it is not null or not blank
    if (null != model.getCmt() && !model.getCmt().isEmpty()) {
      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), model.getCmt());
    }
    // update the Data record
    Data data = entity.getEntity(Data.class);
    updateEntity(data, entityManager);

    // check if there's a status change
    if (!trans.getId().getCurrReqStatus().equals(trans.getNewReqStatus())) {
      String rejectReason = model.getRejectReason();
      String rejReasonCd = model.getRejReasonCd();
      if (StringUtils.isEmpty(rejectReason)) {
        rejectReason = null;
      } else {
        rejectReason = getRejectReason(entityManager, rejectReason);
      }
      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, model.getStatusChgCmt(), model.getAction(), sendToId, sendToNm,
          complete, rejectReason, rejReasonCd, model.getRejSupplInfo1(), model.getRejSupplInfo2());

      // save comment in req_cmt_log table .
      // save only if it is not null or not blank
      if (null != model.getStatusChgCmt() && !model.getStatusChgCmt().isEmpty()) {
        String action = model.getAction();
        String actionDesc = getActionDescription(action, entityManager);
        String statusDesc = getstatusDescription(trans.getNewReqStatus(), entityManager);
        String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + STATUS_CHG_CMT_MID_PREFIX + statusDesc + STATUS_CHG_CMT_POST_PREFIX
            + model.getStatusChgCmt();
        RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
      }
    }

    if (CmrConstants.Send_for_Processing().equals(model.getAction()) && !transitionToNext) {
      // request has been sent for processing, but will not move to next, still
      // save the comments and workflow history
      String wfComment = "Approvals generated/sent. Requester comments: " + model.getStatusChgCmt();
      if (wfComment.length() > 250) {

        wfComment = wfComment.substring(0, 237) + " (truncated)";
      }
      RequestUtils.createWorkflowHistory(this, entityManager, request, admin, model.getStatusChgCmt(), model.getAction(), null, null, false, null,
          null, null, null);
      String action = model.getAction();
      String actionDesc = getActionDescription(action, entityManager);
      String statusDesc = getstatusDescription(admin.getReqStatus(), entityManager);
      String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + " generated and/or sent approvals. Request still in " + statusDesc
          + " status until all approval are received.";
      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
    }

    log.debug(model.getStatusChgCmt());

  }

  protected void performMassCreateComplete(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request, boolean complete)
      throws Exception {
    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);

    copyValuesToEntity(model, entity);

    // update the Admin record
    Admin admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setReqStatus(CmrConstants.REQUEST_STATUS.COM.toString());

    // request to be unlocked
    RequestUtils.clearClaimDetails(admin);

    // update Admin table
    updateEntity(admin, entityManager);

    // update Mass tables
    if (CmrConstants.REQ_TYPE_MASS_UPDATE.equalsIgnoreCase(admin.getReqType())) {
      massDataService.completeMassRequest(entityManager, admin.getId().getReqId());
    }

    // there's a status change
    RequestUtils.createWorkflowHistory(this, entityManager, request, admin, CmrConstants.Mark_as_Completed(), model.getAction(), null, null, complete,
        null, null, null, null);

    // save comment in req_cmt_log table .
    String comment = STATUS_CHG_CMT_PRE_PREFIX + CmrConstants.Mark_as_Completed() + "\"";
    RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);

    log.debug(model.getStatusChgCmt());

  }

  /**
   * Send for processing
   * 
   * @param trans
   * @param model
   * @param entityManager
   * @param request
   * @param processingCenter
   * @throws Exception
   */
  protected void performSendForProcessing(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    String procCenterName = "Bratislava"; // TODO change
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETPROCESSINGCENTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cmrIssuingCntry);
    List<ProcCenter> procCenters = query.getResults(1, ProcCenter.class);
    if (procCenters != null && procCenters.size() > 0) {
      procCenterName = procCenters.get(0).getProcCenterNm();
    }

    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);
    copyValuesToEntity(model, entity);
    if (CmrConstants.REQ_TYPE_UPDT_BY_ENT.equalsIgnoreCase(model.getReqType().trim())) {
      performUpdateByEnt(model, entityManager);
    }
    if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(model.getReqType()) && PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
      performMassUpdateValidationsEMEA(model, entityManager, request);
    }
    if (CmrConstants.REQ_TYPE_MASS_UPDATE.equals(model.getReqType()) && CNDHandler.isCNDCountry(cmrIssuingCntry)) {
      performMassUpdateValidationsDECND(model, entityManager, request);
    } else if (JPHandler.isJPIssuingCountry(cmrIssuingCntry)) {
      performMassUpdateJP(model, entityManager, request);
    }
    String result = null;
    String autoConfig = RequestUtils.getAutomationConfig(entityManager, cmrIssuingCntry);

    if (AutomationConst.AUTOMATE_PROCESSOR.equals(autoConfig) || AutomationConst.AUTOMATE_BOTH.equals(autoConfig)) {
      if (!isMassRequestAutomationEnabled(entityManager, model.getCmrIssuingCntry(), model.getReqType())) {
        result = approvalService.processDefaultApproval(entityManager, model.getReqId(), model.getReqType(), user, model);
      } else {
        this.log.info("Processor automation enabled, skipping default approvals.");
      }
    } else {
      result = approvalService.processDefaultApproval(entityManager, model.getReqId(), model.getReqType(), user, model);
    }
    performGenericAction(trans, model, entityManager, request, procCenterName, null, false, StringUtils.isBlank(result));
  }

  private void performMassUpdateJP(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    int iterationId = 0;
    String sql = null;
    PreparedQuery query = null;
    model.setEmeaSeqNo(0);
    try {
      // get latest iteration id
      sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ITERID.DECND");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", model.getReqId());
      List<Integer> iterId = query.getResults(Integer.class);
      if (iterId != null && iterId.size() > 0) {
        iterationId = iterId.get(0);
      }
      String sqlUpdate = ExternalizedQuery.getSql("UPDATE.MASS.UPDATE");
      PreparedQuery updateQuery = new PreparedQuery(entityManager, sqlUpdate);
      updateQuery.setParameter("PAR_REQ_ID", model.getReqId());
      updateQuery.setParameter("SEQ_NO", 0);
      updateQuery.setParameter("ITERATION_ID", iterationId);

      log.debug("Updating MassUpdate records for mass reqId = " + model.getReqId());
      updateQuery.executeSql();

      String sqlMassUpdate = ExternalizedQuery.getSql("UPDATE.MASS.UPDATE.DATA");
      PreparedQuery updateMassQuery = new PreparedQuery(entityManager, sqlMassUpdate);
      updateMassQuery.setParameter("PAR_REQ_ID", model.getReqId());
      updateMassQuery.setParameter("SEQ_NO", 0);
      updateMassQuery.setParameter("ITERATION_ID", iterationId);

      log.debug("Updating MassUpdateData records for mass reqId = " + model.getReqId());
      updateMassQuery.executeSql();

    } catch (Exception e) {
      this.log.error("Error in processing file for mass change.", e);
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        throw cmre;
      } else {
        throw e;
      }
    }
  }

  /**
   * Special unlock only for Named originator or delegate of locking user
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performSpecialUnlock(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    StatusTrans trans = new StatusTrans();
    StatusTransPK pk = new StatusTransPK();
    pk.setAction(model.getAction());
    pk.setCurrLockedInd(CmrConstants.YES_NO.Y.toString());
    pk.setCurrReqStatus(model.getReqStatus());
    pk.setReqType("*");
    trans.setId(pk);
    trans.setNewLockedInd(CmrConstants.YES_NO.N.toString());
    trans.setNewReqStatus(model.getReqStatus());
    performGenericAction(trans, model, entityManager, request, null);
  }

  /**
   * Cancel Request
   * 
   * @param trans
   * @param model
   * @param entityManager
   * @param request
   * @throws Exception
   */
  protected void performCancelRequest(StatusTrans trans, RequestEntryModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    AppUser user = AppUser.getUser(request);
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);

    Admin admin = entity.getEntity(Admin.class);
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setReqStatus(trans.getNewReqStatus());
    if (CmrConstants.YES_NO.Y.toString().equals(trans.getNewLockedInd())
        && CmrConstants.YES_NO.N.toString().equals(trans.getId().getCurrLockedInd())) {
      // the request is to be locked
      RequestUtils.setClaimDetails(admin, request);
    } else if (CmrConstants.YES_NO.N.toString().equals(trans.getNewLockedInd())
        && CmrConstants.YES_NO.Y.toString().equals(trans.getId().getCurrLockedInd())) {
      // request to be unlocked
      RequestUtils.clearClaimDetails(admin);
    }
    updateEntity(admin, entityManager);

    RequestUtils.createWorkflowHistory(this, entityManager, request, admin, model.getStatusChgCmt(), model.getAction(), null, null, false, null, null,
        null, null);

    // save comment in req_cmt_log table .
    // save only if it is not null or not blank
    if (null != model.getStatusChgCmt() && !model.getStatusChgCmt().isEmpty()) {

      String action = model.getAction();
      String actionDesc = getActionDescription(action, entityManager);
      String statusDesc = getstatusDescription(trans.getNewReqStatus(), entityManager);
      String comment = STATUS_CHG_CMT_PRE_PREFIX + actionDesc + STATUS_CHG_CMT_MID_PREFIX + statusDesc + STATUS_CHG_CMT_POST_PREFIX
          + model.getStatusChgCmt();

      RequestUtils.createCommentLog(this, entityManager, user, model.getReqId(), comment);
    }
  }

  /**
   * Gets the {@link StatusTrans} record associated with the request status,
   * lock ind, and the action to be taken
   * 
   * @param entityManager
   * @param model
   * @return
   */
  private StatusTrans getStatusTransition(EntityManager entityManager, RequestEntryModel model) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETNEXTSTATUS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CURR_REQ_STATUS", model.getReqStatus());
    query.setParameter("CURR_LOCKED_IND", model.getLockInd() != null ? model.getLockInd() : CmrConstants.YES_NO.N.toString());
    query.setParameter("ACTION", model.getAction());
    query.setForReadOnly(true);
    List<StatusTrans> trans = query.getResults(2, StatusTrans.class);
    if (trans != null && trans.size() > 0) {
      for (StatusTrans transrec : trans) {
        if ("PPN".equals(transrec.getNewReqStatus())) {
          String processingIndc = SystemUtil.getAutomationIndicator(entityManager, model.getCmrIssuingCntry());
          if ("P".equals(processingIndc) || "B".equals(processingIndc)) {
            if (isMassRequestAutomationEnabled(entityManager, model.getCmrIssuingCntry(), model.getReqType())) {
              this.log.debug("Processor automation enabled for " + model.getCmrIssuingCntry() + ". Setting " + model.getReqId() + " to AUT");
              transrec.setNewReqStatus("AUT"); // set to automated processing
            }
          }
        }
        if ("*".equals(transrec.getId().getReqType())) {
          return transrec;
        } else if (transrec.getId().getReqType().equals(model.getReqType())) {
          return transrec;
        }
      }
    }
    return null;
  }

  private String getRejectReason(EntityManager entityManager, String code) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.GETREJECTREASON");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", code);
    List<Object[]> codes = query.getResults(1);
    if (codes != null && codes.size() > 0) {
      return (String) codes.get(0)[0];
    }
    return code;
  }

  @Override
  protected List<RequestEntryModel> doSearch(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CompoundEntity entity = getCurrentRecord(model, entityManager, request);
    RequestEntryModel newModel = new RequestEntryModel();
    copyValuesFromEntity(entity, newModel);
    Admin admin = entity.getEntity(Admin.class);
    if (admin != null) {
      newModel.setProcCenter(admin.getLastProcCenterNm());
    }
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected CompoundEntity getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    long reqId = model.getReqId();
    if (reqId > 0) {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.MAIN");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      AppUser user = AppUser.getUser(request);
      query.setParameter("REQUESTER_ID", user.getIntranetId());
      query.setParameter("PROC_CENTER", user.getProcessingCenter());
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<CompoundEntity> records = query.getCompundResults(1, Admin.class, Admin.REQUEST_ENTRY_SERVICE_MAPPING);
      if (records != null && records.size() > 0) {
        return records.get(0);
      }
    }
    return null;
  }

  @Override
  protected CompoundEntity createFromModel(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    CompoundEntity entity = new CompoundEntity();
    Admin admin = adminService.createFromModel(model, entityManager, request);
    Data data = dataService.createFromModel(model, entityManager, request);
    if (model.getCmrIssuingCntry().equalsIgnoreCase("631")) {
      Scorecard score = scoreService.createFromModel(model, entityManager, request);
      entity.addEntity(score);
    }
    entity.addEntity(admin);
    entity.addEntity(data);
    return entity;
  }

  @Override
  public void copyValuesFromEntity(CompoundEntity from, RequestEntryModel to) {
    Admin admin = from.getEntity(Admin.class);
    if (admin != null) {
      adminService.copyValuesFromEntity(admin, to);

    }
    Data data = from.getEntity(Data.class);
    if (data != null) {
      dataService.copyValuesFromEntity(data, to);
    }

    if (null != data.getCmrIssuingCntry() && data.getCmrIssuingCntry().equalsIgnoreCase("631")) {
      Scorecard score = from.getEntity(Scorecard.class);
      scoreService.copyValuesFromEntity(score, to);
    }

    to.setClaimRole((String) from.getValue("CLAIM_ROLE"));
    to.setOverallStatus((String) from.getValue("OVERALL_STATUS"));
    to.setSubIndustryDesc((String) from.getValue("SUB_INDUSTRY_DESC"));
    to.setIsicDesc((String) from.getValue("ISIC_DESC"));
    to.setProcessingStatus((String) from.getValue("PROCESSING_STATUS"));
    to.setCanClaim((String) from.getValue("CAN_CLAIM"));
    to.setCanClaimAll((String) from.getValue("CAN_CLAIM_ALL"));
    to.setAutoProcessing((String) from.getValue("AUTO_PROCESSING"));
  }

  @Override
  public void copyValuesToEntity(RequestEntryModel from, CompoundEntity to) {
    Admin admin = to.getEntity(Admin.class);
    if (admin == null) {

    }
    adminService.copyValuesToEntity(from, admin);
    Data data = to.getEntity(Data.class);
    if (data != null) {
      dataService.copyValuesToEntity(from, data);
    }

    to.setValue("CLAIM_ROLE", from.getClaimRole());
    to.setValue("OVERALL_STATUS", from.getOverallStatus());
    to.setValue("SUB_INDUSTRY_DESC", from.getSubIndustryDesc());
    to.setValue("ISIC_DESC", from.getIsicDesc());
    to.setValue("PROCESSING_STATUS", from.getProcessingStatus());
    to.setValue("CAN_CLAIM", from.getCanClaim());
    to.setValue("CAN_CLAIM_ALL", from.getCanClaimAll());
    to.setValue("AUTO_PROCESSING", from.getAutoProcessing());
  }

  protected String getstatusDescription(String status, EntityManager entityManager) throws CmrException {
    String desc = null;
    String sql = ExternalizedQuery.getSql("status_desc");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", status);

    List<Object[]> results = query.getResults(1);
    for (Object[] result : results) {
      desc = (String) result[5];
    }

    return desc;
  }

  protected String getActionDescription(String action, EntityManager entityManager) throws CmrException {
    String desc = null;
    String sql = ExternalizedQuery.getSql("action_desc");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ACTION", action);

    List<Object[]> results = query.getResults(1);
    for (Object[] result : results) {
      desc = (String) result[2];
    }

    return desc;
  }

  public DataModel getRdcDataModel(long reqId) throws CmrException {
    DataModel model = new DataModel();
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("REQUESTENTRY.GET_RDC_DATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      List<Data> results = query.getResults(1, Data.class);
      if (results != null && results.size() > 0) {
        Data data = results.get(0);
        PropertyUtils.copyProperties(model, data);
      }

    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in processing transaction " + model, e);
      }

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

    return model;
  }

  public void processMassFile(EntityManager entityManager, HttpServletRequest request) throws Exception {
    DiskFileItemFactory factory = new DiskFileItemFactory();

    this.log.debug("Uploading mass change file..");
    String massUpdtDir = SystemConfiguration.getValue("MASS_UPDATE_FILES_DIR");
    String tmpDir = massUpdtDir + "/" + "tmp";
    String cmrIssuingCntry = "";
    File tmp = new File(tmpDir);
    if (!tmp.exists()) {
      tmp.mkdirs();
    }
    // Set factory constraints
    factory.setSizeThreshold(5000);
    factory.setRepository(tmp);

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    long reqId = 0;
    String token = null;
    int newIterId = 0;
    String extName = ".xlsx"; // default file extension

    boolean massCreate = false;
    for (FileItem item : items) {
      if (item.isFormField()) {
        if ("reqId".equals(item.getFieldName())) {
          reqId = Long.parseLong(item.getString());
          this.log.debug(" - Request ID " + reqId);
        }
        if ("massTokenId".equals(item.getFieldName())) {
          token = item.getString();
        }
      }
      if ("massFile".equals(item.getFieldName())) {
        String massFileName = item.getName();
        if (!StringUtils.isBlank(massFileName)) {
          extName = "." + FilenameUtils.getExtension(massFileName);
        }
      }
    }
    LOG.debug("Mass File: Req ID = " + reqId);

    try {
      Admin admin = getCurrentRecordById(entityManager, reqId);
      Data data = getCurrentDataRecordById(entityManager, reqId);
      Scorecard scoring = null;

      if (admin != null) {
        newIterId = admin.getIterationId() + 1;

      }
      if (data != null) {
        cmrIssuingCntry = (data.getCmrIssuingCntry() != null && !"".equalsIgnoreCase(data.getCmrIssuingCntry())) ? data.getCmrIssuingCntry() : "";
      }

      if (cmrIssuingCntry != null && LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, cmrIssuingCntry)) {
        processLegacyDirectMassFile(entityManager, request, reqId, token, items);
        return;
      }

      if (cmrIssuingCntry != null && SwissUtil.isCountrySwissEnabled(entityManager, cmrIssuingCntry)) {
        processLegacyDirectMassFile(entityManager, request, reqId, token, items);
        return;
      }

      if (cmrIssuingCntry != null && IERPRequestUtils.isCountryDREnabled(entityManager, cmrIssuingCntry)
          && ((!cmrIssuingCntry.equals(SystemLocation.CANADA) && !CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType()))
              || (cmrIssuingCntry.equals(SystemLocation.CANADA) && CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType())))
          || (LAHandler.isLACountry(cmrIssuingCntry) && CmrConstants.REQ_TYPE_MASS_UPDATE.equals(admin.getReqType()))) {
        processLegacyDirectMassFile(entityManager, request, reqId, token, items);
        return;
      }

      // CMR-803
      if (cmrIssuingCntry != null && ATUtil.isCountryATEnabled(entityManager, cmrIssuingCntry)) {
        processLegacyDirectMassFile(entityManager, request, reqId, token, items);
        return;
      }
      if (cmrIssuingCntry != null && FranceUtil.isCountryFREnabled(entityManager, cmrIssuingCntry)) {
        processLegacyDirectMassFile(entityManager, request, reqId, token, items);
        return;
      }
      if (cmrIssuingCntry.equalsIgnoreCase("631")) {
        scoring = addrService.getScorecardRecord(entityManager, reqId);
      }

      if (reqId > 0 && newIterId > 0) {
        File uploadDir = prepareUploadDir(reqId);

        // MASS FILE | set filename
        String fileName = null;
        if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
          fileName = "MassCreate_" + reqId + "_Iter" + (newIterId) + extName;
          massCreate = true;
        } else {
          fileName = "MassUpdate_" + reqId + "_Iter" + (newIterId) + ".xlsx";
        }

        String filePath = uploadDir.getAbsolutePath() + "/" + fileName;
        filePath = filePath.replaceAll("[\\\\]", "/");

        for (FileItem item : items) {
          if (!item.isFormField()) {
            if ("massFile".equals(item.getFieldName())) {

              // MASS FILE | validate the mass file
              if (massCreate) {
                if (!validateMassCreateFile(item.getInputStream(), reqId, newIterId)) {
                  throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                }
                log.info("mass create file validated");
              } else {
                if (PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileEMEA(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                  log.info("mass update file validated");
                } else if (CNDHandler.isCNDCountry(cmrIssuingCntry)) {
                  if (!validateMassUpdateFileDECND(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                  log.info("mass update file validated");
                } else if (PageManager.fromGeo("MCO1", cmrIssuingCntry) || PageManager.fromGeo("MCO2", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileMCO(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("CEMEA", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileCEMEA(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("NORDX", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileNORDX(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("JP", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileJP(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (IERPRequestUtils.isCountryDREnabled(entityManager, cmrIssuingCntry) || LAHandler.isLACountry(cmrIssuingCntry)) {
                  if (!validateDRMassUpdateFile(filePath, data, admin, cmrIssuingCntry)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("CA", cmrIssuingCntry)) {
                  if (!validateMassUpdateCA(item.getInputStream())) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("US", cmrIssuingCntry)) {
                  // CREATCMR-4872
                  boolean requesterFromTaxTeam = false;
                  String strRequesterId = admin.getRequesterId().toLowerCase();
                  requesterFromTaxTeam = BluePagesHelper.isUserInUSTAXBlueGroup(strRequesterId);
                  if (requesterFromTaxTeam) {
                    if (!validateMassUpdateUSTaxTeam(item.getInputStream())) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  } else {
                    if (!validateMassUpdateUS(item.getInputStream())) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  }
                } else {
                  if (!validateMassUpdateFile(item.getInputStream())) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                  log.info("mass update file validated");
                }

              }

              // String filePath = uploadDir.getAbsolutePath() + "/" + fileName;
              // filePath = filePath.replaceAll("[\\\\]", "/");

              // MASS FILE | write the file
              File file = new File(filePath);
              if (massCreate) {
                if (file.exists()) {
                  file.delete();
                  log.info("Existing mass file will be replaced.");
                }
                FileOutputStream fos = new FileOutputStream(file);
                try {
                  IOUtils.copy(item.getInputStream(), fos);
                } finally {
                  fos.close();
                }
              } else {
                // create the zip filename
                String zipFileName = filePath + ".zip";
                file = new File(zipFileName);
                if (file.exists()) {
                  file.delete();
                  log.info("Existing mass file will be replaced.");
                }

                FileOutputStream fos = new FileOutputStream(new File(zipFileName));
                try {
                  ZipOutputStream zos = new ZipOutputStream(fos);

                  try {
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);

                    // put the file as the single entry
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    InputStream is = item.getInputStream();
                    try {
                      while ((length = is.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                      }
                    } finally {
                      is.close();
                    }
                    zos.closeEntry();

                  } finally {
                    zos.close();
                  }
                } finally {
                  fos.close();
                }
                file = new File(zipFileName);
              }

              // if file has been created,
              if (file.exists()) {
                // MASS FILE | update DB records
                if (massCreate) {
                  log.info("mass create file saved");
                  updateAdminMassFields(request, entityManager, reqId, newIterId, filePath);
                  request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_UPLOADED));

                } else {
                  log.info("mass update file saved");
                  List<MassUpdateModel> modelList = new ArrayList<>();
                  if (PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
                    setMassUpdateListEMEA(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (CNDHandler.isCNDCountry(cmrIssuingCntry)) {
                    setMassUpdateListDECND(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (LAHandler.isLACountry(cmrIssuingCntry)) {
                    setMassUpdateListLA(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("MCO", cmrIssuingCntry) || PageManager.fromGeo("MCO1", cmrIssuingCntry)
                      || PageManager.fromGeo("MCO2", cmrIssuingCntry)) {
                    setMassUpdateListMCO(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("CEMEA", cmrIssuingCntry)) {
                    setMassUpdateListCEMEA(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("NORDX", cmrIssuingCntry)) {
                    setMassUpdateListNORDX(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("FR", cmrIssuingCntry)) {
                    setMassUpdateListFR(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("JP", cmrIssuingCntry)) {
                    setMassUpdateListJP(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else if (PageManager.fromGeo("CA", cmrIssuingCntry)) {
                    setMassUpdateListCA(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  } else {
                    setMassUpdateList(modelList, item.getInputStream(), reqId, newIterId, filePath);
                  }

                  RequestEntryModel model = new RequestEntryModel();
                  model.setReqId(reqId);
                  model.setAction("SUBMIT_MASS_FILE");
                  model.setMassUpdateList(modelList);
                  processTransaction(model, request);
                  updateAdminMassFields(request, entityManager, reqId, newIterId, filePath);
                  if (data.getCmrIssuingCntry().equalsIgnoreCase("631") && newIterId > 1) {
                    // new file uploaded. reset
                    if (scoring != null && !scoring.getDplChkResult().equalsIgnoreCase("Not Done")) {
                      clearDplResults(model.getReqId(), entityManager, true);
                    }
                  }
                  request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_UPLOADED));
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      this.log.error("Error in processing file for mass change.", e);
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        request.getSession().setAttribute(token, "N," + cmre.getMessage());
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

  private boolean validateMassCreateFile(InputStream inputStream, long reqId, int iterationId) throws Exception {
    MassCreateFileParser parser = new MassCreateFileParser();
    MassCreateFile data = parser.parse(inputStream, reqId, iterationId, true);

    if (ValidationResult.InvalidFormat == data.getValidationResult()) {
      log.error("Mass file has invalid format.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    } else if (ValidationResult.IncorrectVersion == data.getValidationResult()) {
      log.error("Mass file version mismatch, please download the latest template.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_VERSION);
    } else if (ValidationResult.NotValidated == data.getValidationResult()) {
      log.error("Mass file has not yet been validated, please validate the file before uploading.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_VALIDATED);
    } else if (ValidationResult.Passed == data.getValidationResult() && data.getRows() != null) {
      String maxRows = SystemConfiguration.getValue("MASS_CREATE_MAX_ROWS", "100");
      if (data.getRows() != null && (data.getRows().size() >= Integer.parseInt(maxRows))) {
        log.error("Total cmrRecords exceed the maximum limit of " + maxRows);
        throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
      }
    }

    return true;
  }

  private File prepareUploadDir(long reqId) {
    String massUpdtDir = SystemConfiguration.getValue("MASS_UPDATE_FILES_DIR");
    String uploadDirPath = massUpdtDir + "/" + reqId;
    File uploadDir = new File(uploadDirPath);
    if (!uploadDir.exists()) {
      log.debug("Preparing directory " + uploadDirPath);
      uploadDir.mkdir();
    }
    return uploadDir;
  }

  public boolean validateMassUpdateUS(InputStream mfStream) throws Exception {

    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,

    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    String errTxtCmrNo = "";
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          } else {
            String strCmrNo = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim();
            if (isSkipCMRno(strCmrNo)) {
              if (!StringUtils.isEmpty(errTxtCmrNo)) {
                errTxtCmrNo = errTxtCmrNo + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
              } else {
                errTxtCmrNo = Integer.toString(cmrRow.getRowNum() + 1);
              }
            }
          }

          isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim();
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    if (!StringUtils.isEmpty(errTxtCmrNo)) {
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_IS_NOT_IBM, errTxtCmrNo);
    }

    log.debug("Total cmrRecords = " + cmrRecords);

    // Add validations here...

    return true;

  }

  // CREATCMR-4872
  public boolean validateMassUpdateUSTaxTeam(InputStream mfStream) throws Exception {

    Workbook mfWb = new XSSFWorkbook(mfStream);
    String errTxtVal = "";
    String errTxtCmrNo = "";
    String errTaxStatus = "";
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);
    // valid TaxTeam
    List<String> validTaxfields = Arrays.asList("TAX_EXEMPT_STATUS_1", "TAX_EXEMPT_STATUS_2", "TAX_EXEMPT_STATUS_3", "TAX_CD1", "TAX_CD2", "TAX_CD3",
        "ICC_TAX_CLASS", "ICC_TAX_EXEMPT_STATUS", "CMR_NO", "OUT_CITY_LIMIT", "CITY1", "COUNTY", "STATE_PROV", "LAND_CNTRY");
    // CREATCMR-5447
    List<String> validTaxExemptStatus = Arrays.asList("A", "B", "M", "N", "O", "P", "Q", "R", "S", "T", "V", "Z", "X", "@", " ");
    // validate Sheets
    List<String> errRowNo = new ArrayList<String>();
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,

    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;
          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          } else {
            String strCmrNo = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim();
            if (isSkipCMRno(strCmrNo)) {
              if (!StringUtils.isEmpty(errTxtCmrNo)) {
                errTxtCmrNo = errTxtCmrNo + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
              } else {
                errTxtCmrNo = Integer.toString(cmrRow.getRowNum() + 1);
              }
            }
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (!StringUtils.isEmpty(val)) {
              if (validTaxfields.contains(key)) {
                // CREATCMR-5447
                if ((key.equals("ICC_TAX_EXEMPT_STATUS") || key.equals("TAX_EXEMPT_STATUS_1") || key.equals("TAX_EXEMPT_STATUS_2")
                    || key.equals("TAX_EXEMPT_STATUS_3")) && !validTaxExemptStatus.contains(val)) {
                  log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " cannot be update");
                  if (!StringUtils.isEmpty(errTaxStatus)) {
                    errTaxStatus = errTaxStatus + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
                  } else {
                    errTaxStatus = Integer.toString(cmrRow.getRowNum() + 1);
                  }
                  break;
                } else {
                  if (val.length() > dataLmt.get(key)) {
                    log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
                  }
                }

              } else {
                log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " cannot be update");
                errRowNo.add(Integer.toString(cmrRow.getRowNum() + 1));
                if (!StringUtils.isEmpty(errTxtVal)) {
                  errTxtVal = errTxtVal + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
                } else {
                  errTxtVal = Integer.toString(cmrRow.getRowNum() + 1);
                }
                break;
              }
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (!StringUtils.isEmpty(val)) {
              if (validTaxfields.contains(key)) {
                // CREATCMR-5447
                if (val.length() > addrLmt.get(key)) {
                  log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
                  throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
                }
              } else {
                log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + "  cannot be update");
                if (!errRowNo.contains(Integer.toString(cmrRow.getRowNum() + 1))) {
                  errRowNo.add(Integer.toString(cmrRow.getRowNum() + 1));
                  if (!StringUtils.isEmpty(errTxtVal)) {
                    errTxtVal = errTxtVal + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
                  } else {
                    errTxtVal = Integer.toString(cmrRow.getRowNum() + 1);
                  }
                }
                break;
              }
            }

            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (!StringUtils.isEmpty(val)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + "  cannot be update");
              if (!errRowNo.contains(Integer.toString(cmrRow.getRowNum() + 1))) {
                errRowNo.add(Integer.toString(cmrRow.getRowNum() + 1));
                if (!StringUtils.isEmpty(errTxtVal)) {
                  errTxtVal = errTxtVal + ", row " + Integer.toString(cmrRow.getRowNum() + 1);
                } else {
                  errTxtVal = Integer.toString(cmrRow.getRowNum() + 1);
                }
              }
              break;
            }
          }
          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    if (!StringUtils.isEmpty(errTxtVal)) {
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_TAX_TEAM, errTxtVal);
    }

    if (!StringUtils.isEmpty(errTaxStatus)) {
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_TAX_TEAM_STATUS, errTaxStatus);
    }

    if (!StringUtils.isEmpty(errTxtCmrNo)) {
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_IS_NOT_IBM, errTxtCmrNo);
    }

    log.debug("Total cmrRecords = " + cmrRecords);

    return true;

  }

  public boolean validateMassUpdateFile(InputStream mfStream) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,

    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    String clientTier = null;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim();
          clientTier = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CLIENT_TIER") - 1)).trim();
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          if (!StringUtils.isBlank(isuCd)) {
            if ("5K".equals(isuCd)) {
              if (!"@".equals(clientTier)) {
                log.error("Client Tier should be '@' for the selected ISU Code: row " + (cmrRow.getRowNum() + 1));
                throw new CmrException(MessageUtil.ERROR_MASS_FILE_INVALID_ISU_CTC, Integer.toString(cmrRow.getRowNum() + 1));
              }
            }
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);

    // Add validations here...

    return true;
  }

  public boolean validateMassUpdateFileEMEA(InputStream mfStream, Data data, Admin admin) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);
    String reqReason = (admin.getReqReason() != null && !"".equalsIgnoreCase(admin.getReqReason())) ? admin.getReqReason() : "";
    String cntry = data.getCmrIssuingCntry() != null ? data.getCmrIssuingCntry() : "";

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,
                                                        // ZD01, ZP01 & ZS02
    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          // emea mass update type requests validations
          if (reqReason.equalsIgnoreCase("PB")) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SALES_BO_CD") - 1)).trim())) {
              log.error("Selling Branch Office field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SBO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INSTALL_BRANCH_OFF") - 1)).trim())) {
              log.error("Installing Branch Office field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_IBO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ENGINEERING_BO") - 1)).trim())) {
              log.error("Engineering Branch Office field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_CEBO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("REP_TEAM_MEMBER_NO") - 1)).trim())) {
              log.error("Salesman Number field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SALESMAN_NO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ORD_BLK") - 1)).trim())) {
              log.error("Embargo Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMBARGO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MODE_OF_PAYMENT") - 1)).trim())) {
              log.error("Mode of Payment field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_MODE_OF_PAYMNT, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("VAT") - 1)).trim())) {
              log.error("Vat Number field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_VAT, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("POST_CD") - 1)).trim())) {
              log.error("ZS01 Postal Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("POST_CD") - 1)).trim())) {
              log.error("ZP01 Postal Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("POST_CD") - 1)).trim())) {
              log.error("ZD01 Postal Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("POST_CD") - 1)).trim())) {
              log.error("ZI01 Postal Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZS02_FLD.get("POST_CD") - 1)).trim())) {
              log.error("ZS02 Postal Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())) {
              log.error("Industry Solution Unit (ISU) field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SPECIAL_TAX_CD") - 1)).trim())) {
              log.error("Tax Code field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_TAX_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
          } else if (reqReason.equalsIgnoreCase("MOVE")) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SALES_BO_CD") - 1)).trim())) {
              log.error("Selling Branch Office field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SBO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("REP_TEAM_MEMBER_NO") - 1)).trim())) {
              log.error("Salesman Number field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SALESMAN_NO, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())) {
              log.error("Industry Solution Unit (ISU) field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INAC_CD") - 1)).trim())) {
              log.error("International account number (INAC)field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_INAC_CD, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COMPANY") - 1)).trim())) {
              log.error("Company field is required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_COMPANY, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(0, 2);
          }
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZD01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZS02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  public boolean validateMassUpdateFileMCO(InputStream mfStream, Data data, Admin admin) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,
                                                        // ZD01, ZP01 & ZS02
    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(0, 2);
          }
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZD01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZS02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  public boolean validateMassUpdateFileCEMEA(InputStream mfStream, Data data, Admin admin) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,
                                                        // ZD01, ZP01, ZS02, &
                                                        // ZP02
    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP02.toString().equals(addrTyp.trim())) {
            ZP02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(0, 2);
          }
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZD01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZS02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  public boolean validateMassUpdateFileNORDX(InputStream mfStream, Data data, Admin admin) throws Exception {

    Workbook mfWb = new XSSFWorkbook(mfStream);
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,
                                                        // ZD01, ZP01, ZS02, &
                                                        // ZP02
    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP02.toString().equals(addrTyp.trim())) {
            ZP02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    String isuCd = null;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(0, 2);
          }
          if (!StringUtils.isEmpty(isuCd) && !validISUCodes.contains(isuCd)) {
            log.error("Mass file has invalid ISU Code in row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU_CD, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZD01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZS02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP02_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP02 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  public boolean validateMassUpdateFileJP(InputStream mfStream, Data data, Admin admin) throws Exception {
    // noop
    return true;
  }

  private List<String> getValidISUCodes() {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("REFT_ISU.VALIDCODES");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      return query.getResults(String.class);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
  }

  private void setMassUpdateList(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    DataFormatter df = new DataFormatter();

    // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
    massUpdtRdcOnly = "Y";
    int cmrNoFld = DATA_FLD.get("CMR_NO") - 1;
    int inacCdNo = DATA_FLD.get("INAC_CD") - 1;
    int isuCdNo = DATA_FLD.get("ISU_CD") - 1;
    int clientTierNo = DATA_FLD.get("CLIENT_TIER") - 1;
    dataRows: for (Row rdcRow : dataSheet) {
      if (rdcRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(rdcRow)) {
          for (Cell cell : rdcRow) {
            int cellIndex = cell.getColumnIndex();
            if (!ArrayUtils.contains(new Integer[] { cmrNoFld, inacCdNo, isuCdNo, clientTierNo }, cellIndex)) {
              String val = df.formatCellValue(cell);
              if (val != null && !StringUtils.isEmpty(val.trim())) {
                massUpdtRdcOnly = "N";
                break dataRows;
              }
            }
          }
        }
      }
    }

    // Get the cmr record/s
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          int seqNo = cmrRow.getRowNum() + 1;
          MassUpdateModel model = new MassUpdateModel();
          model.setParReqId(reqId);
          model.setSeqNo(seqNo);
          model.setIterationId(newIterId);
          model.setErrorTxt("");
          model.setRowStatusCd("");
          model.setCmrNo(df.formatCellValue(cmrRow.getCell(cmrNoFld)));
          model.setAbbrevNm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ABBREV_NM") - 1)));
          model.setCustNm1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_NM1") - 1)));
          model.setCustNm2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_NM2") - 1)));
          model.setIsicCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISIC_CD") - 1)));
          model.setOutCityLimit(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("OUT_CITY_LIMIT") - 1)));
          model.setPccArDept(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("PCC_AR_DEPT") - 1)));
          model.setRestrictTo(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("RESTRICT_TO") - 1)));
          model.setRestrictInd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("RESTRICT_IND") - 1)));
          model.setSvcArOffice(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SVC_AR_OFFICE") - 1)));
          model.setAffiliate(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("AFFILIATE") - 1)));
          model.setMktgArDept(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MKTG_AR_DEPT") - 1)));
          model.setCsoSite(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CSO_SITE") - 1)));
          model.setMktgDept(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MKTG_DEPT") - 1)));
          model.setMiscBillCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MISC_BILL_CD") - 1)));
          model.setIccTaxClass(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ICC_TAX_CLASS") - 1)));
          model.setIccTaxExemptStatus(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ICC_TAX_EXEMPT_STATUS") - 1)));
          model.setTaxCd1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD1") - 1)));
          model.setTaxCd2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD2") - 1)));
          model.setTaxCd3(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD3") - 1)));
          model.setEnterprise(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ENTERPRISE") - 1)));
          model.setIsuCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)));
          model.setInacCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INAC_CD") - 1)));
          model.setClientTier(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CLIENT_TIER") - 1)));
          model.setTaxExemptStatus(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_EXEMPT_STATUS_1") - 1)));
          model.setTaxExemptStatus2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_EXEMPT_STATUS_2") - 1)));
          model.setTaxExemptStatus3(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_EXEMPT_STATUS_3") - 1)));

          String ordBlkTxt = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ORD_BLK") - 1));
          StringBuilder ordBlkVal = new StringBuilder();
          if (StringUtils.isNotBlank(ordBlkTxt)) {
            ordBlkVal.append(ordBlkTxt.substring(0, 1));
            if (ordBlkTxt.contains("INSTALL")) {
              ordBlkVal.append("S");
            } else if (ordBlkTxt.contains("INVOICE")) {
              ordBlkVal.append("I");
            } else if (ordBlkTxt.contains("BOTH")) {
              ordBlkVal.append("B");
            }
          }
          model.setOrdBlk(ordBlkVal.toString());

          // CREATCMR-4569
          model.setSearchTerm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SEARCH_TERM") - 1)));
          // CREATCMR-3942
          model.setCustClass(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_CLASS") - 1)));
          // CREATCMR-4879
          model.setEducAllowCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("EDUC_ALLOW_CD") - 1)));
          // CREATCMR-6397
          model.setCompany(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COMPANY") - 1)));
          // CREATCMR-6130
          model.setBpAcctTyp(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("BP_ACCT_TYP") - 1)));
          String txtBpNm = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("BP_NAME") - 1));
          if (!StringUtils.isEmpty(txtBpNm)) {
            if (!"@".equals(txtBpNm.trim())) {
              String[] txtSplit = txtBpNm.split(" - ");
              txtBpNm = txtSplit[0].trim();
            }
          }
          model.setBpName(txtBpNm);

          // if (!"Y".equals(massUpdtRdcOnly)) {
          List<MassUpdateAddressModel> addrList = new ArrayList<>();
          MassUpdateAddressModel zs01Addr = new MassUpdateAddressModel();
          zs01Addr.setParReqId(reqId);
          zs01Addr.setSeqNo(seqNo);
          zs01Addr.setIterationId(newIterId);
          zs01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZS01.toString());
          zs01Addr.setDivn(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("DIVN") - 1)));
          zs01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("DEPT") - 1)));
          zs01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT") - 1)));
          zs01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT2") - 1)));
          zs01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CITY1") - 1)));
          zs01Addr.setCounty(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("COUNTY") - 1)));
          zs01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("STATE_PROV") - 1)));
          zs01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("POST_CD") - 1)));
          addrList.add(zs01Addr);

          MassUpdateAddressModel zi01Addr = new MassUpdateAddressModel();
          zi01Addr.setParReqId(reqId);
          zi01Addr.setSeqNo(seqNo);
          zi01Addr.setIterationId(newIterId);
          zi01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
          zi01Addr.setDivn(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("DIVN") - 1)));
          zi01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("DEPT") - 1)));
          zi01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT") - 1)));
          zi01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT2") - 1)));
          zi01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CITY1") - 1)));
          zi01Addr.setCounty(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("COUNTY") - 1)));
          zi01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("STATE_PROV") - 1)));
          zi01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("POST_CD") - 1)));
          addrList.add(zi01Addr);

          model.setMassUpdatAddr(addrList);
          // }
          modelList.add(model);
        }
      }
    }
  }

  private void setMassUpdateListEMEA(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    DataFormatter df = new DataFormatter();

    // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
    massUpdtRdcOnly = "Y";
    int cmrNoFld = DATA_FLD.get("CMR_NO") - 1;
    int inacCdNo = DATA_FLD.get("INAC_CD") - 1;
    int isuCdNo = DATA_FLD.get("ISU_CD") - 1;
    dataRows: for (Row rdcRow : dataSheet) {
      if (rdcRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(rdcRow)) {
          for (Cell cell : rdcRow) {
            int cellIndex = cell.getColumnIndex();
            if (!ArrayUtils.contains(new Integer[] { cmrNoFld, inacCdNo, isuCdNo }, cellIndex)) {
              String val = df.formatCellValue(cell);
              if (val != null && !StringUtils.isEmpty(val.trim())) {
                massUpdtRdcOnly = "N";
                break dataRows;
              }
            }
          }
        }
      }
    }

    // Get the cmr record/s
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          int seqNo = cmrRow.getRowNum() + 1;
          MassUpdateModel model = new MassUpdateModel();
          model.setParReqId(reqId);
          model.setSeqNo(seqNo);
          model.setIterationId(newIterId);
          model.setErrorTxt("");
          model.setRowStatusCd("");
          model.setCmrNo(df.formatCellValue(cmrRow.getCell(cmrNoFld)));
          model.setAbbrevNm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ABBREV_NM") - 1)));
          model.setAbbrevLocn(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ABBREV_LOCN") - 1)));
          model.setIsicCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISIC_CD") - 1)));
          model.setVat(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("VAT") - 1)));
          model.setSpecialTaxCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SPECIAL_TAX_CD") - 1)));
          model.setEngineeringBo(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ENGINEERING_BO") - 1)));
          model.setEnterprise(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ENTERPRISE") - 1)));
          model.setInacCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INAC_CD") - 1)));
          model.setIsuCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)));
          model.setSalesBoCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SALES_BO_CD") - 1)));
          model.setRepTeamMemberNo(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("REP_TEAM_MEMBER_NO") - 1)));
          model.setCollectionCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COLLECTION_CD") - 1)));
          model.setOrdBlk(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ORD_BLK") - 1)));
          model.setInstallBranchOff(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INSTALL_BRANCH_OFF") - 1)));
          model.setModeOfPayment(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MODE_OF_PAYMENT") - 1)));
          model.setCompany(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COMPANY") - 1)));
          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            model.setClientTier(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(2, 3));
          }

          // if (!"Y".equals(massUpdtRdcOnly)) {
          List<MassUpdateAddressModel> addrList = new ArrayList<>();
          MassUpdateAddressModel zs01Addr = new MassUpdateAddressModel();
          zs01Addr.setParReqId(reqId);
          zs01Addr.setSeqNo(seqNo);
          zs01Addr.setIterationId(newIterId);
          zs01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZS01.toString());
          zs01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zs01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zs01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT") - 1)));
          zs01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT2") - 1)));
          zs01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CITY1") - 1)));
          zs01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("POST_CD") - 1)));
          addrList.add(zs01Addr);

          MassUpdateAddressModel zi01Addr = new MassUpdateAddressModel();
          zi01Addr.setParReqId(reqId);
          zi01Addr.setSeqNo(seqNo);
          zi01Addr.setIterationId(newIterId);
          zi01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
          zi01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zi01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zi01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT") - 1)));
          zi01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT2") - 1)));
          zi01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CITY1") - 1)));
          zi01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("POST_CD") - 1)));
          addrList.add(zi01Addr);

          MassUpdateAddressModel zp01Addr = new MassUpdateAddressModel();
          zp01Addr.setParReqId(reqId);
          zp01Addr.setSeqNo(seqNo);
          zp01Addr.setIterationId(newIterId);
          zp01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZP01.toString());
          zp01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zp01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zp01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("ADDR_TXT") - 1)));
          zp01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("ADDR_TXT2") - 1)));
          zp01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("CITY1") - 1)));
          zp01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("POST_CD") - 1)));
          addrList.add(zp01Addr);

          MassUpdateAddressModel zd01Addr = new MassUpdateAddressModel();
          zd01Addr.setParReqId(reqId);
          zd01Addr.setSeqNo(seqNo);
          zd01Addr.setIterationId(newIterId);
          zd01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZD01.toString());
          zd01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zd01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zd01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("ADDR_TXT") - 1)));
          zd01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("ADDR_TXT2") - 1)));
          zd01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("CITY1") - 1)));
          zd01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("POST_CD") - 1)));
          addrList.add(zd01Addr);

          MassUpdateAddressModel zs02Addr = new MassUpdateAddressModel();
          zs02Addr.setParReqId(reqId);
          zs02Addr.setSeqNo(seqNo);
          zs02Addr.setIterationId(newIterId);
          zs02Addr.setAddrType(CmrConstants.ADDR_TYPE.ZS02.toString());
          zs02Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zs02Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zs02Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZS02_FLD.get("ADDR_TXT") - 1)));
          zs02Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZS02_FLD.get("ADDR_TXT2") - 1)));
          zs02Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZS02_FLD.get("CITY1") - 1)));
          zs02Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZS02_FLD.get("POST_CD") - 1)));
          addrList.add(zs02Addr);
          model.setMassUpdatAddr(addrList);
          // }
          modelList.add(model);
        }
      }
    }
  }

  private void setMassUpdateListMCO(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private void setMassUpdateListCEMEA(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private void setMassUpdateListNORDX(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private void setMassUpdateListFR(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private void setMassUpdateListJP(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private void setMassUpdateListCA(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    // noop
  }

  private boolean isRowValid(Row row) {
    // if row is empty, skip
    DataFormatter df = new DataFormatter();
    boolean hasContents = false;
    for (Cell cell : row) {
      String val = df.formatCellValue(cell);
      if (val != null && !StringUtils.isEmpty(val.trim())) {
        hasContents = true;
        break;
      }
    }
    return hasContents;
  }

  private void performLegacyDirectMassUpdate(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    Map<String, Object> massUpdateMap = model.getLegacyDirectMassUpdtList();

    if (massUpdateMap != null || massUpdateMap.size() != 0) {
      List<MassUpdateModel> models = (List<MassUpdateModel>) massUpdateMap.get("dataModels");
      List<MassUpdateAddressModel> addrModels = (List<MassUpdateAddressModel>) massUpdateMap.get("addrModels");

      try {
        for (MassUpdateModel massModel : models) {
          MassUpdt massUpdt = new MassUpdt();
          MassUpdtPK massUpdtPK = new MassUpdtPK();
          massUpdt.setId(massUpdtPK);
          massService.copyValuesToEntity(massModel, massUpdt);
          createEntity(massUpdt, entityManager);

          MassUpdtData massUpdtData = new MassUpdtData();
          MassUpdtDataPK massUpdtDataPK = new MassUpdtDataPK();
          massUpdtData.setId(massUpdtDataPK);
          massDataService.copyValuesToEntity(massModel, massUpdtData);
          createEntity(massUpdtData, entityManager);
        }

        for (MassUpdateAddressModel addrModel : addrModels) {
          MassUpdtAddr massUpdtAddr = new MassUpdtAddr();
          MassUpdtAddrPK massUpdtAddrPK = new MassUpdtAddrPK();
          massUpdtAddr.setId(massUpdtAddrPK);

          massAddrService.copyValuesToEntity(addrModel, massUpdtAddr);
          createEntity(massUpdtAddr, entityManager);
        }
      } catch (Exception e) {
        e.printStackTrace();
        // throw new
        // Exception("Error reading and saving mass update lists. Please check
        // if the lists or their attributes are not null or empty.");
      }

    } else {
      LOG.error("ERROR: Mass update lists are empty. Cannot proceed with saves to mass update tables.");
      throw new Exception("Mass update lists are empty. Cannot proceed with saves to mass update tables.");
    }
  }

  private void performMassUpdate(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    // Insert record to mass tables
    for (MassUpdateModel massModel : model.getMassUpdateList()) {
      MassUpdt massUpdt = new MassUpdt();
      MassUpdtPK massUpdtPK = new MassUpdtPK();
      massUpdt.setId(massUpdtPK);
      massService.copyValuesToEntity(massModel, massUpdt);
      if (SystemLocation.UNITED_STATES.equals(model.getCmrIssuingCntry())) {
        massUpdt.setRowStatusCd("READY");
      }
      createEntity(massUpdt, entityManager);

      MassUpdtData massUpdtData = new MassUpdtData();
      MassUpdtDataPK massUpdtDataPK = new MassUpdtDataPK();
      massUpdtData.setId(massUpdtDataPK);
      massDataService.copyValuesToEntity(massModel, massUpdtData);
      createEntity(massUpdtData, entityManager);

      // if (!"Y".equals(massUpdtRdcOnly)) {
      if (massModel.getMassUpdatAddr() != null && massModel.getMassUpdatAddr().size() > 0) {
        for (MassUpdateAddressModel addrModel : massModel.getMassUpdatAddr()) {
          MassUpdtAddr massUpdtAddr = new MassUpdtAddr();
          MassUpdtAddrPK massUpdtAddrPK = new MassUpdtAddrPK();
          massUpdtAddr.setId(massUpdtAddrPK);

          massAddrService.copyValuesToEntity(addrModel, massUpdtAddr);
          createEntity(massUpdtAddr, entityManager);
        }
      }
      // }

    }
  }

  private void performMassUpdateValidationsEMEA(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    int iterationId = 0;
    String sql = null;
    PreparedQuery query = null;
    model.setEmeaSeqNo(0);

    try {
      // get latest iteration id
      sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ITERID.EMEA");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", model.getReqId());
      List<Integer> iterId = query.getResults(Integer.class);
      if (iterId != null && iterId.size() > 0) {
        iterationId = iterId.get(0);
      }
      model.setIterId(iterationId);

      List<MassUpdateModel> results = massDataService.doSearchCurrentMassUpdRec(model.getReqId(), iterationId, entityManager);

      // uki mass update type requests validations
      if (model.getReqReason().equalsIgnoreCase("PB")) {

        for (MassUpdateModel massModel : results) {
          model.setEmeaSeqNo(massModel.getSeqNo());
          if (StringUtils.isBlank(massModel.getSalesBoCd())) {
            log.error("Selling Branch Office field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_SBO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getInstallBranchOff())) {
            log.error("Installing Branch Office field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_IBO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getEngineeringBo())) {
            log.error("Engineering Branch Office field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CEBO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getRepTeamMemberNo())) {
            log.error("Salesman Number field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_SALESMAN_NO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getOrdBlk())) {
            log.error("Embargo Code field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMBARGO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getModeOfPayment())) {
            log.error("Mode of Payment field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_MODE_OF_PAYMNT, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getVat())) {
            log.error("Vat Number field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_VAT, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getIsuCd())) {
            log.error("Industry Solution Unit (ISU) field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getSpecialTaxCd())) {
            log.error("Tax Code field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_TAX_CD, Integer.toString(massModel.getSeqNo()));
          }
          List<MassUpdateAddressModel> resultsAddr = massAddrService.doSearchById(entityManager, massModel);
          for (MassUpdateAddressModel addrModel : resultsAddr) {
            if (StringUtils.isBlank(addrModel.getPostCd())) {
              log.error(addrModel.getAddrType() + " Postal Code field is required : row " + (massModel.getSeqNo()));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_POSTAL_CD, Integer.toString(massModel.getSeqNo()));
            }
          }
        }
      } else if (model.getReqReason().equalsIgnoreCase("MOVE")) {
        for (MassUpdateModel massModel : results) {
          model.setEmeaSeqNo(massModel.getSeqNo());
          if (StringUtils.isBlank(massModel.getSalesBoCd())) {
            log.error("Selling Branch Office field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_SBO, Integer.toString(massModel.getSeqNo()));
          }

          if (StringUtils.isBlank(massModel.getRepTeamMemberNo())) {
            log.error("Salesman Number field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_SALESMAN_NO, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getCompany())) {
            log.error("Company field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_COMPANY, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getIsuCd())) {
            log.error("Industry Solution Unit (ISU) field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ISU, Integer.toString(massModel.getSeqNo()));
          }
          if (StringUtils.isBlank(massModel.getInacCd())) {
            log.error("INAC Code field is required : row " + (massModel.getSeqNo()));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_INAC_CD, Integer.toString(massModel.getSeqNo()));
          }
        }
      }
    } catch (Exception e) {
      this.log.error("Error in processing file for mass change.", e);
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        throw cmre;
      } else {
        throw e;
      }
    }

  }

  public void processMassCreateErrorLog(HttpServletResponse response, long reqId, int iterationId) throws Exception {
    EntityManager entityManager = JpaManager.getEntityManager();

    try {
      String errorFile = "MassCreate_" + reqId + "_Iter" + iterationId + "_log.xlsm";
      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + errorFile + "\"");

      MassCreateUtil.convertToErrorLog(entityManager, response.getOutputStream(), reqId, iterationId);

    } catch (Exception e) {
      response.reset();
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Unknown error encountered when creating error log", e);
        throw new CmrException(MessageUtil.ERROR_ERROR_LOG_DOWNLOAD);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  public void processMassUpdateErrorLog(Admin admin, HttpServletResponse response, String template) throws Exception {
    long reqId = admin.getId().getReqId();
    int iterationId = admin.getIterationId();
    String errorFile = "MassUpdate_" + reqId + "_Iter" + iterationId + "_log.xlsx";

    // Get records that failed...
    List<MassUpdateModel> errorList = getErrorList(reqId, iterationId);

    try {
      InputStream is = ConfigUtil.getResourceStream(template);
      Workbook efWb;
      try {
        efWb = new XSSFWorkbook(is);
      } catch (Exception e) {
        log.error("Error reading the template stream.");
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      } finally {
        is.close();
      }

      Sheet dataSheet = efWb.getSheet(CMR_SHEET_NAME);
      Sheet cfgSheet = efWb.getSheet(CONFIG_SHEET_NAME);

      // validate Sheets
      if (dataSheet == null || cfgSheet == null) {
        log.error("Mass file template does not contain valid sheet names.");
        throw new CmrException(MessageUtil.ERROR_MASS_FILE);
      }

      // Set config fields
      DataFormatter df = new DataFormatter();
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
          }
        }
      }

      // Set the cmr record/s
      int rownum = CMR_ROW_NO;
      for (MassUpdateModel errData : errorList) {
        Row errRow = dataSheet.createRow(rownum++);
        errRow.createCell(DATA_FLD.get("CMR_NO") - 1).setCellValue(errData.getCmrNo());
        errRow.createCell(DATA_FLD.get("ABBREV_NM") - 1).setCellValue(errData.getAbbrevNm());
        errRow.createCell(DATA_FLD.get("CUST_NM1") - 1).setCellValue(errData.getCustNm1());
        errRow.createCell(DATA_FLD.get("CUST_NM2") - 1).setCellValue(errData.getCustNm2());
        errRow.createCell(DATA_FLD.get("ISIC_CD") - 1).setCellValue(errData.getIsicCd());
        errRow.createCell(DATA_FLD.get("OUT_CITY_LIMIT") - 1).setCellValue(errData.getOutCityLimit());
        errRow.createCell(DATA_FLD.get("PCC_AR_DEPT") - 1).setCellValue(errData.getPccArDept());
        errRow.createCell(DATA_FLD.get("RESTRICT_TO") - 1).setCellValue(errData.getRestrictTo());
        errRow.createCell(DATA_FLD.get("RESTRICT_IND") - 1).setCellValue(errData.getRestrictInd());
        errRow.createCell(DATA_FLD.get("SVC_AR_OFFICE") - 1).setCellValue(errData.getSvcArOffice());
        errRow.createCell(DATA_FLD.get("AFFILIATE") - 1).setCellValue(errData.getAffiliate());
        errRow.createCell(DATA_FLD.get("MKTG_AR_DEPT") - 1).setCellValue(errData.getMktgArDept());
        errRow.createCell(DATA_FLD.get("CSO_SITE") - 1).setCellValue(errData.getCsoSite());
        errRow.createCell(DATA_FLD.get("MKTG_DEPT") - 1).setCellValue(errData.getMktgDept());
        errRow.createCell(DATA_FLD.get("MISC_BILL_CD") - 1).setCellValue(errData.getMiscBillCd());
        errRow.createCell(DATA_FLD.get("ICC_TAX_CLASS") - 1).setCellValue(errData.getIccTaxClass());
        errRow.createCell(DATA_FLD.get("ICC_TAX_EXEMPT_STATUS") - 1).setCellValue(errData.getIccTaxExemptStatus());
        errRow.createCell(DATA_FLD.get("TAX_CD1") - 1).setCellValue(errData.getTaxCd1());
        errRow.createCell(DATA_FLD.get("TAX_CD2") - 1).setCellValue(errData.getTaxCd2());
        errRow.createCell(DATA_FLD.get("TAX_CD3") - 1).setCellValue(errData.getTaxCd3());
        errRow.createCell(DATA_FLD.get("ENTERPRISE") - 1).setCellValue(errData.getEnterprise());
        errRow.createCell(DATA_FLD.get("ISU_CD") - 1).setCellValue(errData.getIsuCd());
        errRow.createCell(DATA_FLD.get("INAC_CD") - 1).setCellValue(errData.getInacCd());
        errRow.createCell(DATA_FLD.get("CLIENT_TIER") - 1).setCellValue(errData.getClientTier());
        errRow.createCell(DATA_FLD.get("ERROR_TXT") - 1).setCellValue(errData.getErrorTxt());

        if (errData.getMassUpdatAddr() != null) {
          for (MassUpdateAddressModel errAddr : errData.getMassUpdatAddr()) {
            if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZS01_FLD.get("DIVN") - 1).setCellValue(errAddr.getDivn());
              errRow.createCell(ZS01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
              errRow.createCell(ZS01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZS01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZS01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZS01_FLD.get("COUNTY") - 1).setCellValue(errAddr.getCounty());
              errRow.createCell(ZS01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZS01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZI01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZI01_FLD.get("DIVN") - 1).setCellValue(errAddr.getDivn());
              errRow.createCell(ZI01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
              errRow.createCell(ZI01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZI01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZI01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZI01_FLD.get("COUNTY") - 1).setCellValue(errAddr.getCounty());
              errRow.createCell(ZI01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZI01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZD01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZD01_FLD.get("DIVN") - 1).setCellValue(errAddr.getDivn());
              errRow.createCell(ZD01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
              errRow.createCell(ZD01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZD01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZD01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZD01_FLD.get("COUNTY") - 1).setCellValue(errAddr.getCounty());
              errRow.createCell(ZD01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZD01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZP01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZP01_FLD.get("DIVN") - 1).setCellValue(errAddr.getDivn());
              errRow.createCell(ZP01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
              errRow.createCell(ZP01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZP01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZP01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZP01_FLD.get("COUNTY") - 1).setCellValue(errAddr.getCounty());
              errRow.createCell(ZP01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZP01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZS02.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZS02_FLD.get("DIVN") - 1).setCellValue(errAddr.getDivn());
              errRow.createCell(ZS02_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
              errRow.createCell(ZS02_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZS02_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZS02_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZS02_FLD.get("COUNTY") - 1).setCellValue(errAddr.getCounty());
              errRow.createCell(ZS02_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZS02_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            }
          }
        }
      }

      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + errorFile + "\"");
      efWb.write(response.getOutputStream());

    } catch (Exception e) {
      log.error("Error when loading mass template.");
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
  }

  public void processMassUpdateErrorLogEMEA(Admin admin, HttpServletResponse response, String template) throws Exception {
    long reqId = admin.getId().getReqId();
    int iterationId = admin.getIterationId();
    String errorFile = "MassUpdate_" + reqId + "_Iter" + iterationId + "_log.xlsx";

    // Get records that failed...
    List<MassUpdateModel> errorList = getErrorList(reqId, iterationId);

    try {
      InputStream is = ConfigUtil.getResourceStream(template);
      Workbook efWb;
      try {
        efWb = new XSSFWorkbook(is);
      } catch (Exception e) {
        log.error("Error reading the template stream.");
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      } finally {
        is.close();
      }

      Sheet dataSheet = efWb.getSheet(CMR_SHEET_NAME);
      Sheet cfgSheet = efWb.getSheet(CONFIG_SHEET_NAME);

      // validate Sheets
      if (dataSheet == null || cfgSheet == null) {
        log.error("Mass file template does not contain valid sheet names.");
        throw new CmrException(MessageUtil.ERROR_MASS_FILE);
      }

      // Set config fields
      DataFormatter df = new DataFormatter();
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
          }
        }
      }

      // Set the cmr record/s
      int rownum = CMR_ROW_NO;
      for (MassUpdateModel errData : errorList) {
        Row errRow = dataSheet.createRow(rownum++);
        errRow.createCell(DATA_FLD.get("CMR_NO") - 1).setCellValue(errData.getCmrNo());
        errRow.createCell(DATA_FLD.get("ABBREV_NM") - 1).setCellValue(errData.getAbbrevNm());
        errRow.createCell(DATA_FLD.get("ABBREV_LOCN") - 1).setCellValue(errData.getAbbrevLocn());
        errRow.createCell(DATA_FLD.get("ISIC_CD") - 1).setCellValue(errData.getIsicCd());
        errRow.createCell(DATA_FLD.get("VAT") - 1).setCellValue(errData.getVat());
        errRow.createCell(DATA_FLD.get("SPECIAL_TAX_CD") - 1).setCellValue(errData.getSpecialTaxCd());
        errRow.createCell(DATA_FLD.get("ENGINEERING_BO") - 1).setCellValue(errData.getEngineeringBo());
        errRow.createCell(DATA_FLD.get("ENTERPRISE") - 1).setCellValue(errData.getEnterprise());
        errRow.createCell(DATA_FLD.get("INAC_CD") - 1).setCellValue(errData.getInacCd());
        errRow.createCell(DATA_FLD.get("ISU_CD") - 1).setCellValue(errData.getIsuCd());
        errRow.createCell(DATA_FLD.get("SALES_BO_CD") - 1).setCellValue(errData.getSalesBoCd());
        errRow.createCell(DATA_FLD.get("REP_TEAM_MEMBER_NO") - 1).setCellValue(errData.getRepTeamMemberNo());
        errRow.createCell(DATA_FLD.get("COLLECTION_CD") - 1).setCellValue(errData.getCollectionCd());
        errRow.createCell(DATA_FLD.get("ORD_BLK") - 1).setCellValue(errData.getOrdBlk());
        errRow.createCell(DATA_FLD.get("INSTALL_BRANCH_OFF") - 1).setCellValue(errData.getInstallBranchOff());
        errRow.createCell(DATA_FLD.get("MODE_OF_PAYMENT") - 1).setCellValue(errData.getModeOfPayment());
        errRow.createCell(DATA_FLD.get("COMPANY") - 1).setCellValue(errData.getCompany());
        errRow.createCell(DATA_FLD.get("ERROR_TXT") - 1).setCellValue(errData.getErrorTxt());

        if (errData.getMassUpdatAddr() != null) {
          for (MassUpdateAddressModel errAddr : errData.getMassUpdatAddr()) {
            if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZS01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZS01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZS01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZS01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZS01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZS01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZI01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZI01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZI01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZI01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZI01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZI01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZI01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZD01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZD01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZD01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZD01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZD01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZD01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZD01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZP01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZP01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZP01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZP01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZP01_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZP01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZP01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZS02.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZS02_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZS02_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZS02_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZS02_FLD.get("ADDR_TXT2") - 1).setCellValue(errAddr.getAddrTxt2());
              errRow.createCell(ZS02_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZS02_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
            }
          }
        }
      }

      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + errorFile + "\"");
      efWb.write(response.getOutputStream());

    } catch (Exception e) {
      log.error("Error when loading mass template.");
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
  }

  public void processMassUpdateErrorLogDECND(Admin admin, HttpServletResponse response, String template) throws Exception {
    long reqId = admin.getId().getReqId();
    int iterationId = admin.getIterationId();
    String errorFile = "MassUpdate_" + reqId + "_Iter" + iterationId + "_log.xlsx";

    // Get records that failed...
    List<MassUpdateModel> errorList = getErrorList(reqId, iterationId);

    try {
      InputStream is = ConfigUtil.getResourceStream(template);
      Workbook efWb;
      try {
        efWb = new XSSFWorkbook(is);
      } catch (Exception e) {
        log.error("Error reading the template stream.");
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      } finally {
        is.close();
      }

      Sheet dataSheet = efWb.getSheet(CMR_SHEET_NAME);
      Sheet cfgSheet = efWb.getSheet(CONFIG_SHEET_NAME);

      // validate Sheets
      if (dataSheet == null || cfgSheet == null) {
        log.error("Mass file template does not contain valid sheet names.");
        throw new CmrException(MessageUtil.ERROR_MASS_FILE);
      }

      // Set config fields
      DataFormatter df = new DataFormatter();
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
          }
        }
      }

      // Set the cmr record/s
      int rownum = CMR_ROW_NO;
      for (MassUpdateModel errData : errorList) {
        Row errRow = dataSheet.createRow(rownum++);
        errRow.createCell(DATA_FLD.get("CMR_NO") - 1).setCellValue(errData.getCmrNo());
        errRow.createCell(DATA_FLD.get("ABBREV_NM") - 1).setCellValue(errData.getAbbrevNm());
        errRow.createCell(DATA_FLD.get("ISIC_CD") - 1).setCellValue(errData.getIsicCd());
        // errRow.createCell(DATA_FLD.get("SUB_INDUSTRY_CD") -
        // 1).setCellValue(errData.getSubIndustryCd());
        errRow.createCell(DATA_FLD.get("ENTERPRISE") - 1).setCellValue(errData.getEnterprise());
        errRow.createCell(DATA_FLD.get("ISU_CD") - 1).setCellValue(errData.getIsuCd());
        errRow.createCell(DATA_FLD.get("INAC_CD") - 1).setCellValue(errData.getInacCd());
        errRow.createCell(DATA_FLD.get("CLIENT_TIER") - 1).setCellValue(errData.getInacCd());
        errRow.createCell(DATA_FLD.get("VAT") - 1).setCellValue(errData.getVat());
        // errRow.createCell(DATA_FLD.get("SEARCH_TERM") -
        // 1).setCellValue(errData.getSearchTerm());
        // errRow.createCell(DATA_FLD.get("CUST_CLASS") -
        // 1).setCellValue(errData.getCustClass());
        errRow.createCell(DATA_FLD.get("ERROR_TXT") - 1).setCellValue(errData.getErrorTxt());

        if (errData.getMassUpdatAddr() != null) {
          for (MassUpdateAddressModel errAddr : errData.getMassUpdatAddr()) {
            if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZS01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZS01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZS01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              errRow.createCell(ZS01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              errRow.createCell(ZS01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZS01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
              errRow.createCell(ZS01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZI01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZI01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZI01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZI01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              // errRow.createCell(ZI01_FLD.get("IERP_SITE_PRTY_ID") -
              // 1).setCellValue(errAddr.getIerpSitePrtyId());
              errRow.createCell(ZI01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              // errRow.createCell(ZI01_FLD.get("BLDG") -
              // 1).setCellValue(errAddr.getBldg());
              errRow.createCell(ZI01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZI01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
              errRow.createCell(ZI01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZP01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZP01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZP01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZP01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              // errRow.createCell(ZP01_FLD.get("IERP_SITE_PRTY_ID") -
              // 1).setCellValue(errAddr.getIerpSitePrtyId());
              errRow.createCell(ZP01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              // errRow.createCell(ZP01_FLD.get("BLDG") -
              // 1).setCellValue(errAddr.getBldg());
              errRow.createCell(ZP01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZP01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
              errRow.createCell(ZP01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
            } else if (errAddr.getAddrType() != null && CmrConstants.ADDR_TYPE.ZD01.toString().equalsIgnoreCase(errAddr.getAddrType().trim())) {
              errRow.createCell(ZD01_FLD.get("CUST_NM1") - 1).setCellValue(errAddr.getCustNm1());
              errRow.createCell(ZD01_FLD.get("CUST_NM2") - 1).setCellValue(errAddr.getCustNm2());
              errRow.createCell(ZD01_FLD.get("ADDR_TXT") - 1).setCellValue(errAddr.getAddrTxt());
              // errRow.createCell(ZD01_FLD.get("IERP_SITE_PRTY_ID") -
              // 1).setCellValue(errAddr.getIerpSitePrtyId());
              errRow.createCell(ZD01_FLD.get("CITY1") - 1).setCellValue(errAddr.getCity1());
              // errRow.createCell(ZD01_FLD.get("BLDG") -
              // 1).setCellValue(errAddr.getBldg());
              errRow.createCell(ZD01_FLD.get("STATE_PROV") - 1).setCellValue(errAddr.getStateProv());
              errRow.createCell(ZD01_FLD.get("POST_CD") - 1).setCellValue(errAddr.getPostCd());
              errRow.createCell(ZD01_FLD.get("DEPT") - 1).setCellValue(errAddr.getDept());
            }
          }
        }
      }

      response.setContentType("application/octet-stream");
      response.addHeader("Content-Type", "application/octet-steam");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + errorFile + "\"");
      efWb.write(response.getOutputStream());

    } catch (Exception e) {
      log.error("Error when loading mass template.");
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
  }

  private List<MassUpdateModel> getErrorList(long reqId, int iterationId) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();

    try {
      List<MassUpdateModel> errorList = massService.doSearchFailed(reqId, iterationId, entityManager);
      if (errorList.size() > 0) {
        for (MassUpdateModel massModel : errorList) {
          massDataService.doSearchById(entityManager, massModel);

          List<MassUpdateAddressModel> massUpdatAddrList = massAddrService.doSearchById(entityManager, massModel);
          massModel.setMassUpdatAddr(massUpdatAddrList);
        }
      } else {
        throw new CmrException(MessageUtil.INFO_ERROR_LOG_EMPTY);
      }
      return errorList;
    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Error:", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  public Admin getCurrentRecordById(long reqId) throws CmrException {
    return getCurrentRecordById(null, reqId);
  }

  public Admin getCurrentRecordById(EntityManager entityManagerInput, long reqId) throws CmrException {
    EntityManager entityManager = entityManagerInput == null ? JpaManager.getEntityManager() : entityManagerInput;
    Admin admin = null;
    try {

      admin = adminService.getCurrentRecordById(reqId, entityManager);

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      if (entityManagerInput == null) {
        // empty the manager
        entityManager.clear();
        entityManager.close();
      }
    }
    return admin;
  }

  public Data getCurrentDataRecordById(EntityManager entityManagerInput, long reqId) throws CmrException {
    EntityManager entityManager = entityManagerInput == null ? JpaManager.getEntityManager() : entityManagerInput;
    Data data = null;
    try {

      data = dataService.getCurrentRecordById(reqId, entityManager);

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      if (entityManagerInput == null) {
        // empty the manager
        entityManager.clear();
        entityManager.close();
      }
    }
    return data;
  }

  private void updateAdminMassFields(HttpServletRequest request, EntityManager entityManager, long reqId, int newIterId, String fileName)
      throws CmrException {
    AppUser user = AppUser.getUser(request);
    Admin admin = getCurrentRecordById(entityManager, reqId);
    admin.setIterationId(newIterId);
    admin.setFileName(fileName);
    admin.setMassUpdtRdcOnly(massUpdtRdcOnly);
    if (user != null) {
      admin.setLastUpdtBy(user.getIntranetId());
    }
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    updateEntity(admin, entityManager);
    // adminService.updateMassFields(entityManager, reqId, newIterId, fileName,
    // massUpdtRdcOnly);

  }

  /**
   * Checks if the request has been claimed already
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  private boolean canClaim(EntityManager entityManager, RequestEntryModel model) {
    String sql = ExternalizedQuery.getSql("REQENTRY.ISCLAIMED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    Object[] result = query.getSingleResult(Object[].class);
    return "Y".equals(result[0]) || !model.getReqStatus().equals(result[1]);
  }

  /**
   * Sets the ApprovalResult for the record
   */
  public void setApprovalResult(RequestEntryModel model) {
    ApprovalService service = new ApprovalService();
    service.setApprovalResult(model);
  }

  private void performUpdateByEnt(RequestEntryModel model, EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.DATA.BY.ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", model.getReqId());
    query.setParameter("ITERATION_ID", 0);
    query.setParameter("SEQ_NO", 0);
    MassUpdtData massUpdtData = query.getSingleResult(MassUpdtData.class);
    massUpdtData.setEnterprise(model.getEntNo());

    if (!StringUtils.isBlank(model.getEntUpdTyp())) {
      if (model.getEntUpdTyp().equalsIgnoreCase("A")) {
        String cname1 = "";
        String cname2 = "";
        massUpdtData.setCompany(model.getComp());
        massUpdtData.setEntpUpdtTyp(model.getEntUpdTyp());
        cname1 = model.getCname1();
        cname2 = "";
        if (!StringUtils.isBlank(cname1)) {
          splitName(massUpdtData, cname1, cname2, 28, 24);
        }
      } else if (model.getEntUpdTyp().equalsIgnoreCase("B")) {
        massUpdtData.setCompany(model.getComp1());
        massUpdtData.setNewEntp(model.getNewEntp());
        massUpdtData.setEntpUpdtTyp(model.getEntUpdTyp());
      } else if (model.getEntUpdTyp().equalsIgnoreCase("C")) {
        if (model.getCmrIssuingCntry().equalsIgnoreCase("897")) {
          if (model.getNewEntpName().length() > 28) {
            massUpdtData.setNewEntpName1(model.getNewEntpName().substring(0, 28));
          } else {
            massUpdtData.setNewEntpName1(model.getNewEntpName());
          }

          if (model.getNewEntpNameCont().length() > 24) {
            massUpdtData.setNewEntpName2(model.getNewEntpNameCont().substring(0, 24));
          } else {
            massUpdtData.setNewEntpName2(model.getNewEntpNameCont());
          }
          massUpdtData.setEntpUpdtTyp(model.getEntUpdTyp());
        } else {
          massUpdtData.setNewEntpName1(model.getNewEntpName());
          massUpdtData.setNewEntpName2(model.getNewEntpNameCont());
          massUpdtData.setEntpUpdtTyp(model.getEntUpdTyp());
        }
      }
      updateEntity(massUpdtData, entityManager);
    }
  }

  public void setEnterpriseModel(long reqId, RequestEntryModel reqModel) throws CmrException {
    MassUpdtData massUpdtData = getCurrentRecordByIdEnt(Long.toString(reqId));
    reqModel.setEntNo(massUpdtData.getEnterprise());
    reqModel.setNewEntp(massUpdtData.getNewEntp());
    reqModel.setNewEntpName(massUpdtData.getNewEntpName1());
    reqModel.setNewEntpNameCont(massUpdtData.getNewEntpName2());

    if (massUpdtData.getCustNm1() != null || massUpdtData.getCustNm2() != null) {
      if (massUpdtData.getCustNm2() != null) {
        reqModel.setCname1(massUpdtData.getCustNm1() + " " + massUpdtData.getCustNm2());
      } else {
        reqModel.setCname1(massUpdtData.getCustNm1());
      }

    }

    if (massUpdtData.getEntpUpdtTyp() != null) {
      if (massUpdtData.getEntpUpdtTyp().equalsIgnoreCase("A")) {
        reqModel.setComp(massUpdtData.getCompany());
      } else if (massUpdtData.getEntpUpdtTyp().equalsIgnoreCase("B")) {
        reqModel.setComp1(massUpdtData.getCompany());
      }
      reqModel.setEntUpdTyp(massUpdtData.getEntpUpdtTyp());
    }
  }

  public MassUpdtData getCurrentRecordByIdEnt(String reqId) throws CmrException {
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    MassUpdtData massUpdtData = null;
    try {
      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      massUpdtData = massDataService.getCurrentRecordByIdEnt(reqId, entityManager);

      // commit once
      transaction.commit();
      log.debug(" - transaction committed.");

    } catch (Exception e) {
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        this.log.error("Unexpected error occurred", e);
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
    return massUpdtData;
  }

  private void splitName(MassUpdtData massUpdtData, String name1, String name2, int length1, int length2) {
    String name = name1 + " " + (name2 != null ? name2 : "");
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

    massUpdtData.setCustNm1(namePart1);
    massUpdtData.setCustNm2(namePart2);
  }

  /**
   * 
   * @param model
   * @param admin
   * @deprecated - checking for automatic processing now done on request type
   *             level
   */
  @Deprecated
  protected void setDisableProc(RequestEntryModel model, Admin admin) {
    if (LAHandler.isLACountry(model.getCmrIssuingCntry()) || PageManager.fromGeo("EMEA", model.getCmrIssuingCntry())
        || DEHandler.isIERPCountry(model.getCmrIssuingCntry()) || CNDHandler.isCNDCountry(model.getCmrIssuingCntry())) {
    }
    /* 1413568 */
    if ("758".equalsIgnoreCase(model.getCmrIssuingCntry())) {
      admin.setDisableAutoProc("Y");
    }
  }

  public void clearDplResults(long reqId, EntityManager entityManager, boolean isServiceCall) {
    EntityTransaction transaction = null;
    try {
      if (entityManager == null) {
        entityManager = JpaManager.getEntityManager();
        // start the transaction
        transaction = entityManager.getTransaction();
        transaction.begin();
      }
      String scoreClearSql = ExternalizedQuery.getSql("DPL.CLEARSCORECARD");
      PreparedQuery query = new PreparedQuery(entityManager, scoreClearSql);
      query.setParameter("REQ_ID", reqId);
      query.executeSql();
      if (!isServiceCall) {
        transaction.commit();
      }
    } catch (Exception e) {
      if (!isServiceCall) {
        if (transaction != null && transaction.isActive()) {
          transaction.rollback();
        }
      }
      throw e;
    } finally {
      // try to rollback, for safekeeping
      if (!isServiceCall) {
        if (transaction != null && transaction.isActive()) {
          transaction.rollback();
        }
        // empty the manager
        entityManager.clear();
        entityManager.close();
      }
    }
  }

  public void processMassDPLFileForDownload(String dplLogFile, HttpServletResponse response, long reqId) throws CmrException {
    String filetoSend = dplLogFile.substring(dplLogFile.lastIndexOf("/") + 1);
    final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
    File dplWorkBookFile = new File(dplLogFile);
    if (!dplWorkBookFile.exists()) {
      initLogger().info("File not found : " + dplLogFile);
      throw new CmrException(MessageUtil.ERROR_FILE_DL_ERROR);
    }
    String docType = "";
    try {
      docType = MIME_TYPES.getContentType(dplWorkBookFile);
    } catch (Exception ex) {
    }
    if (StringUtils.isEmpty(docType)) {
      docType = "application/octet-stream";
    }
    response.setContentType(docType);
    response.addHeader("Content-Type", docType);
    response.addHeader("Content-Disposition", "attachment; filename=\"" + filetoSend + "\"");
    try (FileInputStream fileInStream = new FileInputStream(dplWorkBookFile)) {
      IOUtils.copy(fileInStream, response.getOutputStream());
    } catch (Exception ex) {
      initLogger().error(ex.getMessage(), ex);
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }
  }

  public boolean validateMassUpdateFileDECND(InputStream mfStream, Data data, Admin admin) throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);

    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = mfWb.getSheet(CONFIG_SHEET_NAME);
    String reqReason = (admin.getReqReason() != null && !"".equalsIgnoreCase(admin.getReqReason())) ? admin.getReqReason() : "";
    String cntry = data.getCmrIssuingCntry() != null ? data.getCmrIssuingCntry() : "";

    // Get valid ISU codes
    List<String> validISUCodes = getValidISUCodes();

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLmt = new HashMap<>();
    HashMap<String, Integer> addrLmt = new HashMap<>(); // For ZS01 , ZI01 ,
                                                        // ZD01, ZP01
    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
          int length = (int) cfgRow.getCell(3).getNumericCellValue();

          if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
            ZS01_FLD.put(field, fieldNo);
            addrLmt.put(field, length);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
            ZI01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrTyp.trim())) {
            ZP01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrTyp.trim())) {
            ZD01_FLD.put(field, fieldNo);
          } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS02.toString().equals(addrTyp.trim())) {
            ZS02_FLD.put(field, fieldNo);
          } else {
            DATA_FLD.put(field, fieldNo);
            dataLmt.put(field, length);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;

          String val = "";
          int countZS01 = 0;
          int countZI01 = 0;
          int countZP01 = 0;
          int countZD01 = 0;
          int countDataFld = 0;

          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val != "") {
              countZS01++;
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val != "") {
              countZI01++;
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val != "") {
              countZP01++;
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val != "") {
              countZD01++;
            }
          }
          if (countZS01 > 0) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
                && StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("IERP_SITE_PRTY_ID") - 1)).trim())) {
              log.error("CMR number or Site ID field is required for Sold To Fields : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_SITE_ID_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (!StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
              log.error("CMR number field should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ALPHANUMERIC_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          if (countZI01 > 0) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("IERP_SITE_PRTY_ID") - 1)).trim())) {
              log.error("ZI01 Site ID field are required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SITE_ID_ZI01, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          if (countZP01 > 0) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("IERP_SITE_PRTY_ID") - 1)).trim())) {
              log.error("ZP01 Site ID field are required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SITE_ID_ZP01, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }
          if (countZD01 > 0) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("IERP_SITE_PRTY_ID") - 1)).trim())) {
              log.error("ZD01 Site ID field are required : row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_SITE_ID_ZD01, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          for (String key : dataLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
            if (val.length() > dataLmt.get(key)) {
              log.error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            if (val != null && val != "") {
              countDataFld++;
            }
          }

          String isuCd = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim();
          String clientTier = df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CLIENT_TIER") - 1)).trim();

          if (countZD01 > 0 || countZI01 > 0 || countZP01 > 0 || countDataFld > 0) {
            if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
                || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
              log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }

            if (!StringUtils.isBlank(isuCd)) {
              if ("5K".equals(isuCd)) {
                if (!"@".equals(clientTier)) {
                  log.error("Client Tier should be '@' for the selected ISU Code: row " + (cmrRow.getRowNum() + 1));
                  throw new CmrException(MessageUtil.ERROR_MASS_FILE_INVALID_ISU_CTC, Integer.toString(cmrRow.getRowNum() + 1));
                }
              }
            }
          }

          for (String key : addrLmt.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZD01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZD01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZP01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLmt.get(key)) {
              log.error(key + " ZP01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }

          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            log.error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  private void setMassUpdateListDECND(List<MassUpdateModel> modelList, InputStream mfStream, long reqId, int newIterId, String filePath)
      throws Exception {
    Workbook mfWb = new XSSFWorkbook(mfStream);
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    DataFormatter df = new DataFormatter();

    // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
    massUpdtRdcOnly = "Y";
    int cmrNoFld = DATA_FLD.get("CMR_NO") - 1;
    int inacCdNo = DATA_FLD.get("INAC_CD") - 1;
    int isuCdNo = DATA_FLD.get("ISU_CD") - 1;
    dataRows: for (Row rdcRow : dataSheet) {
      if (rdcRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(rdcRow)) {
          for (Cell cell : rdcRow) {
            int cellIndex = cell.getColumnIndex();
            if (!ArrayUtils.contains(new Integer[] { cmrNoFld, inacCdNo, isuCdNo }, cellIndex)) {
              String val = df.formatCellValue(cell);
              if (val != null && !StringUtils.isEmpty(val.trim())) {
                massUpdtRdcOnly = "N";
                break dataRows;
              }
            }
          }
        }
      }
    }

    // Get the cmr record/s
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          int seqNo = cmrRow.getRowNum() + 1;
          MassUpdateModel model = new MassUpdateModel();
          model.setParReqId(reqId);
          model.setSeqNo(seqNo);
          model.setIterationId(newIterId);
          model.setErrorTxt("");
          model.setRowStatusCd("");
          model.setCmrNo(df.formatCellValue(cmrRow.getCell(cmrNoFld)));
          model.setAbbrevNm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ABBREV_NM") - 1)));
          model.setIsicCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISIC_CD") - 1)));
          // model.setSubIndustryCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SUB_INDUSTRY_CD")
          // - 1)));
          model.setEnterprise(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ENTERPRISE") - 1)));
          model.setIsuCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)));
          model.setInacCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INAC_CD") - 1)));
          if (!StringUtils.isBlank(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim())
              && df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().length() == 3) {
            model.setClientTier(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISU_CD") - 1)).trim().substring(2, 3));
          }
          model.setVat(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("VAT") - 1)));
          // model.setSearchTerm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SEARCH_TERM")
          // - 1)));
          // model.setCustClass(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_CLASS")
          // - 1)));
          List<MassUpdateAddressModel> addrList = new ArrayList<>();
          MassUpdateAddressModel zs01Addr = new MassUpdateAddressModel();
          zs01Addr.setParReqId(reqId);
          zs01Addr.setSeqNo(seqNo);
          zs01Addr.setIterationId(newIterId);
          zs01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZS01.toString());
          zs01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM1") - 1)));
          zs01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CUST_NM2") - 1)));
          zs01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT") - 1)));
          // zs01Addr.setIerpSitePrtyId(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("IERP_SITE_PRTY_ID")
          // - 1)));
          zs01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CITY1") - 1)));
          // zs01Addr.setBldg(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("BLDG")
          // - 1)));
          zs01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("STATE_PROV") - 1)));
          zs01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("POST_CD") - 1)));
          zs01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("DEPT") - 1)));
          addrList.add(zs01Addr);

          MassUpdateAddressModel zi01Addr = new MassUpdateAddressModel();
          zi01Addr.setParReqId(reqId);
          zi01Addr.setSeqNo(seqNo);
          zi01Addr.setIterationId(newIterId);
          zi01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
          zi01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CUST_NM1") - 1)));
          zi01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CUST_NM2") - 1)));
          zi01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT") - 1)));
          // zi01Addr.setIerpSitePrtyId(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("IERP_SITE_PRTY_ID")
          // - 1)));
          zi01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CITY1") - 1)));
          // zi01Addr.setBldg(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("BLDG")
          // - 1)));
          zi01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("STATE_PROV") - 1)));
          zi01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("POST_CD") - 1)));
          zi01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("DEPT") - 1)));
          addrList.add(zi01Addr);

          MassUpdateAddressModel zp01Addr = new MassUpdateAddressModel();
          zp01Addr.setParReqId(reqId);
          zp01Addr.setSeqNo(seqNo);
          zp01Addr.setIterationId(newIterId);
          zp01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZP01.toString());
          zp01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("CUST_NM1") - 1)));
          zp01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("CUST_NM2") - 1)));
          zp01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("ADDR_TXT") - 1)));
          // zp01Addr.setIerpSitePrtyId(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("IERP_SITE_PRTY_ID")
          // - 1)));
          zp01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("CITY1") - 1)));
          // zp01Addr.setBldg(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("BLDG")
          // - 1)));
          zp01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("STATE_PROV") - 1)));
          zp01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("POST_CD") - 1)));
          zp01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZP01_FLD.get("DEPT") - 1)));
          addrList.add(zp01Addr);

          MassUpdateAddressModel zd01Addr = new MassUpdateAddressModel();
          zd01Addr.setParReqId(reqId);
          zd01Addr.setSeqNo(seqNo);
          zd01Addr.setIterationId(newIterId);
          zd01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZD01.toString());
          zd01Addr.setCustNm1(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("CUST_NM1") - 1)));
          zd01Addr.setCustNm2(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("CUST_NM2") - 1)));
          zd01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("ADDR_TXT") - 1)));
          // zd01Addr.setIerpSitePrtyId(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("IERP_SITE_PRTY_ID")
          // - 1)));
          zd01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("CITY1") - 1)));
          // zd01Addr.setBldg(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("BLDG")
          // - 1)));
          zd01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("STATE_PROV") - 1)));
          zd01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("POST_CD") - 1)));
          zd01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZD01_FLD.get("DEPT") - 1)));
          addrList.add(zd01Addr);

          model.setMassUpdatAddr(addrList);
          // }
          modelList.add(model);
        }
      }
    }
  }

  private void performMassUpdateValidationsDECND(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    int iterationId = 0;
    String sql = null;
    PreparedQuery query = null;
    model.setEmeaSeqNo(0);
    try {
      // get latest iteration id
      sql = ExternalizedQuery.getSql("GET.MASS.UPDATE.ITERID.DECND");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", model.getReqId());
      List<Integer> iterId = query.getResults(Integer.class);
      if (iterId != null && iterId.size() > 0) {
        iterationId = iterId.get(0);
      }
      model.setIterId(iterationId);
    } catch (Exception e) {
      this.log.error("Error in processing file for mass change.", e);
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        throw cmre;
      } else {
        throw e;
      }
    }
  }

  private boolean validateMassUpdateFileLA(InputStream inputStream, Data data, Admin admin) throws Exception {
    Workbook wBookToParse = new XSSFWorkbook(inputStream);
    Sheet dataSheet = wBookToParse.getSheet(CMR_SHEET_NAME);
    Sheet cfgSheet = wBookToParse.getSheet(CONFIG_SHEET_NAME);

    // validate Sheets
    if (dataSheet == null || cfgSheet == null) {
      log.error("Mass file does not contain valid sheet names.");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
    }

    // Set config fields
    DataFormatter df = new DataFormatter();
    HashMap<String, Integer> dataLimit = new HashMap<>();
    HashMap<String, Integer> addrLimit = new HashMap<>(); // For ZS01 , ZI01

    try {
      for (Row cfgRow : cfgSheet) {
        if (cfgRow.getRowNum() >= CONFIG_ROW_NO) {
          String addrTyp = df.formatCellValue(cfgRow.getCell(0));
          String field = df.formatCellValue(cfgRow.getCell(1));
          int fieldNo = 0;
          int length = 0;
          if (!StringUtils.isEmpty(field)) {
            fieldNo = (int) cfgRow.getCell(2).getNumericCellValue();
            length = (int) cfgRow.getCell(3).getNumericCellValue();
            log.debug("addrTyp:" + addrTyp + ", field:" + field + ", fieldNo:" + fieldNo + ", length:" + length);
            if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrTyp.trim())) {
              ZS01_FLD.put(field, fieldNo);
              addrLimit.put(field, length);
            } else if (!StringUtils.isEmpty(addrTyp) && CmrConstants.ADDR_TYPE.ZI01.toString().equals(addrTyp.trim())) {
              ZI01_FLD.put(field, fieldNo);
            } else {
              DATA_FLD.put(field, fieldNo);
              dataLimit.put(field, length);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error reading the config sheet, Column and Field Length should be numeric");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_CONFIG);
    }

    // Required: Check cmrNo, and field lengths
    int cmrRecords = 0;
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          cmrRecords++;
          String val = "";
          if (StringUtils.isEmpty(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)).trim())
              || !StringUtils.isAlphanumeric(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CMR_NO") - 1)))) {
            log.error("CMR number field is required and should be alphanumeric: row " + (cmrRow.getRowNum() + 1));
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_CMR_ROW, Integer.toString(cmrRow.getRowNum() + 1));
          }

          for (String key : dataLimit.keySet()) {
            if (!StringUtils.equalsIgnoreCase(key, "SGDN") && !StringUtils.equalsIgnoreCase(key, "ERROR_TXT")) {
              val = df.formatCellValue(cmrRow.getCell(DATA_FLD.get(key) - 1));
              if (val.length() > dataLimit.get(key)) {
                initLogger().debug(val.length() + " data limit : " + dataLimit.get(key) + " current actual row : " + key + " : "
                    + Integer.toString(cmrRow.getRowNum()));
                initLogger().error(key + " value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
                throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
              }
            }
          }

          for (String key : addrLimit.keySet()) {
            val = df.formatCellValue(cmrRow.getCell(ZS01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLimit.get(key)) {
              initLogger().error(key + " ZS01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
            val = df.formatCellValue(cmrRow.getCell(ZI01_FLD.get(key) - 1));
            if (val != null && val.length() > addrLimit.get(key)) {
              initLogger().error(key + " ZI01 value on row no. " + (cmrRow.getRowNum() + 1) + " exceeded the limit");
              throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROW, Integer.toString(cmrRow.getRowNum() + 1));
            }
          }

          // validate#ofRows if <= MASS_UPDATE_MAX_ROWS
          String maxRows = SystemConfiguration.getValue("MASS_UPDATE_MAX_ROWS");
          if (cmrRecords > (Integer.parseInt(maxRows) + 1)) {
            initLogger().error("Total cmrRecords exceed theh maximum limit of " + maxRows);
            throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, maxRows);
          }
        }
      }
    }

    // validate if has CMR rows
    if (cmrRecords <= 0) {
      log.error("No valid records to process...");
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_EMPTY);
    }

    log.debug("Total cmrRecords = " + cmrRecords);
    return true;
  }

  private void setMassUpdateListLA(List<MassUpdateModel> modelList, InputStream inputStream, long reqId, int newIterId, String filePath)
      throws Exception {
    Workbook mfWb = new XSSFWorkbook(inputStream);
    Sheet dataSheet = mfWb.getSheet(CMR_SHEET_NAME);
    DataFormatter df = new DataFormatter();

    massUpdtRdcOnly = "Y";
    int cmrNoFld = DATA_FLD.get("CMR_NO") - 1;
    int inacCdNo = DATA_FLD.get("INAC_CD") - 1;
    dataRows: for (Row rdcRow : dataSheet) {
      if (rdcRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(rdcRow)) {
          for (Cell cell : rdcRow) {
            int cellIndex = cell.getColumnIndex();
            if (!ArrayUtils.contains(new Integer[] { cmrNoFld, inacCdNo }, cellIndex)) {
              String val = df.formatCellValue(cell);
              if (val != null && !StringUtils.isEmpty(val.trim())) {
                massUpdtRdcOnly = "N";
                break dataRows;
              }
            }
          }
        }
      }
    }

    // Get the cmr record/s
    for (Row cmrRow : dataSheet) {
      if (cmrRow.getRowNum() >= CMR_ROW_NO) {
        if (isRowValid(cmrRow)) {
          int seqNo = cmrRow.getRowNum() + 1;
          MassUpdateModel model = new MassUpdateModel();
          model.setParReqId(reqId);
          model.setSeqNo(seqNo);
          model.setIterationId(newIterId);
          model.setErrorTxt("");
          model.setRowStatusCd("");
          model.setCmrNo(df.formatCellValue(cmrRow.getCell(cmrNoFld)));
          model.setAbbrevNm(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ABBREV_NM") - 1)));
          model.setCustNm1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_NM1") - 1)));
          model.setCustNm2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CUST_NM2") - 1)));
          model.setIsicCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("ISIC_CD") - 1)));
          model.setSubIndustryCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SUB_INDUSTRY_CD") - 1)));
          model.setVat(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("VAT") - 1)));
          model.setTaxCd1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD1") - 1)));
          model.setTaxCd2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD2") - 1)));
          model.setTaxCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_CD") - 1)));
          model.setTaxNum(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_NUM") - 1)));
          model.setTaxSeparationIndc(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("TAX_SEPARATION_INDC") - 1)));
          model.setBillingPrintIndc(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("BILLING_PRINT_INDC") - 1)));
          model.setContractPrintIndc(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CONTRACT_PRINT_INDC") - 1)));
          model.setCountryUse(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CNTRY_USE") - 1)));
          model.setContactName1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CONTACT_NAME1") - 1)));
          model.setPhone1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("PHONE1") - 1)));
          model.setEmail1(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("EMAIL1") - 1)));
          model.setContactName2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CONTACT_NAME2") - 1)));
          model.setPhone2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("PHONE2") - 1)));
          model.setEmail2(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("EMAIL2") - 1)));
          model.setContactName3(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CONTACT_NAME3") - 1)));
          model.setPhone3(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("PHONE3") - 1)));
          model.setEmail3(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("EMAIL3") - 1)));
          model.setInacCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("INAC_CD") - 1)));
          model.setCompany(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COMPANY") - 1)));
          model.setMrktChannelInd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("MARKETING_CHNL_INDC_VALUE") - 1)));
          model.setCollBoId(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COLL_BO_ID") - 1)));
          model.setSalesBusOffCd(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("SALES_BO_CD") - 1)));
          model.setRepTeamMemberNo(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("REP_TEAM_MEMBER_NO") - 1)));
          model.setCollectorNameNo(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COLLECTOR_NO") - 1)));
          model.setCodCondition(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COD_CONDITION") - 1)));
          model.setCodCondition(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("COD_RSN") - 1)));
          model.setCreditCode(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("CREDIT_CD") - 1)));
          model.setIbmBankNumber(df.formatCellValue(cmrRow.getCell(DATA_FLD.get("IBM_BANK_NO") - 1)));

          // if (!"Y".equals(massUpdtRdcOnly)) {
          List<MassUpdateAddressModel> addrList = new ArrayList<>();
          MassUpdateAddressModel zs01Addr = new MassUpdateAddressModel();
          zs01Addr.setParReqId(reqId);
          zs01Addr.setSeqNo(seqNo);
          zs01Addr.setIterationId(newIterId);
          zs01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZS01.toString());
          zs01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT") - 1)));
          zs01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("ADDR_TXT2") - 1)));
          zs01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CITY2") - 1)));
          zs01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("CITY1") - 1)));
          zs01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("STATE_PROV") - 1)));
          zs01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZS01_FLD.get("POST_CD") - 1)));
          addrList.add(zs01Addr);

          MassUpdateAddressModel zi01Addr = new MassUpdateAddressModel();
          zi01Addr.setParReqId(reqId);
          zi01Addr.setSeqNo(seqNo);
          zi01Addr.setIterationId(newIterId);
          zi01Addr.setAddrType(CmrConstants.ADDR_TYPE.ZI01.toString());
          zi01Addr.setAddrTxt(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT") - 1)));
          zi01Addr.setAddrTxt2(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("ADDR_TXT2") - 1)));
          zi01Addr.setDept(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CITY2") - 1)));
          zi01Addr.setCity1(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("CITY1") - 1)));
          zi01Addr.setStateProv(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("STATE_PROV") - 1)));
          zi01Addr.setPostCd(df.formatCellValue(cmrRow.getCell(ZI01_FLD.get("POST_CD") - 1)));
          addrList.add(zi01Addr);

          model.setMassUpdatAddr(addrList);
          // }
          modelList.add(model);
        }
      }
    }
  }

  private void processObsoleteApprovals(EntityManager entityManager, long reqId, AppUser user) throws Exception {
    ApprovalService service = new ApprovalService();
    service.makeApprovalsObsolete(entityManager, reqId, user);
  }

  public boolean validateDRMassUpdateFile(String path, Data data, Admin admin, String cmrIssuingCntry) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;
        StringBuilder errTxt = new StringBuilder();
        String str;
        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            if (validation.hasErrors()) {
              if (StringUtils.isEmpty(errTxt.toString())) {
                errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
              } else {
                errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
              }
            }
          }
        }
        if (!StringUtils.isEmpty(errTxt.toString())) {
          throw new Exception(errTxt.toString());
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {

        LOG.error(e.getMessage());
        LOG.error("An error occurred in validating DR Mass Update File.");
        // return false;

        throw new Exception(e.getMessage());
        // throw new CmrException(MessageUtil.ERROR_GENERAL, e.getMessage());
        // return false;
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }

  }

  // CMR-800
  public boolean validateATMassUpdateFile(String path, Data data, Admin admin) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidators(SystemLocation.AUSTRIA);
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(SystemLocation.AUSTRIA);

      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;
        StringBuilder errTxt = new StringBuilder();

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            for (ValidationRow row : validation.getRows()) {
              if (!row.isSuccess()) {
                if (StringUtils.isEmpty(errTxt.toString())) {
                  errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
                } else {
                  errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
                }
              }
            }
          }
        }

        if (!StringUtils.isEmpty(errTxt.toString())) {
          throw new Exception(errTxt.toString());
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {
        LOG.error("An error occurred in validating AT Mass Update File.");
        throw new Exception(e.getMessage());
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }

  }

  public boolean validateSWISSMassUpdateFile(String path, Data data, Admin admin) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidatorsSwiss();
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate("SWISS");
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;
        StringBuilder errTxt = new StringBuilder();

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            for (ValidationRow row : validation.getRows()) {
              if (!row.isSuccess()) {
                if (StringUtils.isEmpty(errTxt.toString())) {
                  errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
                } else {
                  errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
                }
              }
            }
          }
        }

        if (!StringUtils.isEmpty(errTxt.toString())) {
          throw new Exception(errTxt.toString());
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {
        LOG.error("An error occurred in validating Swiss Mass Update File.");
        throw new Exception(e.getMessage());
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }
  }

  public boolean validateLegacyDirectMassUpdateFile(String path, Data data, Admin admin, StringBuilder errTxt) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidators(data.getCmrIssuingCntry());
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(data.getCmrIssuingCntry());
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;

        if (errTxt == null) {
          errTxt = new StringBuilder();
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));

          if (SystemLocation.ISRAEL.equals(country)) {
            for (TemplateValidation validation : validations) {
              if (validation.hasErrors()) {
                if (StringUtils.isEmpty(errTxt.toString())) {
                  errTxt.append("[TAB: " + validation.getTabName() + "]" + validation.getAllError() + "");
                } else {
                  errTxt.append("<br>[TAB: " + validation.getTabName() + "]" + validation.getAllError() + "");
                }
              }
            }
          } else {
            for (TemplateValidation validation : validations) {
              if (validation.hasErrors()) {
                if (StringUtils.isEmpty(errTxt.toString())) {
                  errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
                } else {
                  errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
                }
              }
            }
          }
        }

        if (!StringUtils.isEmpty(errTxt.toString())) {
          // LOG.debug(errTxt);
          throw new Exception(errTxt.toString());
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {
        LOG.error(e.getMessage());
        LOG.error("An error occurred in validating Legacy Direct Mass Update File.");
        // return false;
        throw new Exception(e.getMessage());
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }
  }

  // DENNIS - LD Mass Update
  /**
   * @param entityManager
   * @param request
   * @throws Exception
   */
  public void processLegacyDirectMassFile(EntityManager entityManager, HttpServletRequest request, long reqId, String token, List<FileItem> items)
      throws Exception {
    DiskFileItemFactory factory = new DiskFileItemFactory();

    this.log.debug("Uploading mass change file..");
    String massUpdtDir = SystemConfiguration.getValue("MASS_UPDATE_FILES_DIR");
    String tmpDir = massUpdtDir + "/" + "tmp";
    String cmrIssuingCntry = "";
    File tmp = new File(tmpDir);
    if (!tmp.exists()) {
      tmp.mkdirs();
    }
    // Set factory constraints
    factory.setSizeThreshold(5000);
    factory.setRepository(tmp);

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    int newIterId = 0;

    boolean massCreate = false;
    LOG.debug("Mass File: Req ID = " + reqId);

    try {
      Admin admin = getCurrentRecordById(entityManager, reqId);
      Data data = getCurrentDataRecordById(entityManager, reqId);
      Scorecard scoring = null;
      FileInputStream fis = null;

      if (admin != null) {
        newIterId = admin.getIterationId() + 1;

      }

      if (reqId > 0 && newIterId > 0) {
        File uploadDir = prepareUploadDir(reqId);

        // MASS FILE | set filename
        String fileName = null;
        if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(admin.getReqType())) {
          fileName = "MassCreate_" + reqId + "_Iter" + (newIterId) + ".xlsm";
          massCreate = true;
        } else {
          fileName = "MassUpdate_" + reqId + "_Iter" + (newIterId) + ".xlsx";
        }

        for (FileItem item : items) {
          if (!item.isFormField()) {
            if ("massFile".equals(item.getFieldName())) {

              String filePath = uploadDir.getAbsolutePath() + "/" + fileName;
              filePath = filePath.replaceAll("[\\\\]", "/");

              // MASS FILE | write the file
              File file = new File(filePath);

              if (file.exists()) {
                file.delete();
                log.info("Existing mass file will be replaced.");
              }
              FileOutputStream fos = new FileOutputStream(file);
              try {
                IOUtils.copy(item.getInputStream(), fos);
              } finally {
                fos.close();
              }

              // MASS FILE | validate the mass file
              if (massCreate) {
                if (!validateMassCreateFile(item.getInputStream(), reqId, newIterId)) {
                  throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                }
                log.info("mass create file validated");
              } else {
                if (PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileEMEA(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                  log.info("mass update file validated");
                } else if (CNDHandler.isCNDCountry(cmrIssuingCntry)) {
                  if (!validateMassUpdateFileDECND(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                  log.info("mass update file validated");
                } else if (PageManager.fromGeo("MCO1", cmrIssuingCntry) || PageManager.fromGeo("MCO2", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileMCO(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("CEMEA", cmrIssuingCntry) && !SystemLocation.AUSTRIA.equals(cmrIssuingCntry)) {// CMR-803
                  if (!validateMassUpdateFileCEMEA(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("NORDX", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileNORDX(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else if (PageManager.fromGeo("JP", cmrIssuingCntry)) {
                  if (!validateMassUpdateFileJP(item.getInputStream(), data, admin)) {
                    throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                  }
                } else {
                  if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    StringBuilder errTxt = new StringBuilder();
                    // if (!validateLegacyDirectMassUpdateFile(filePath, data,
                    // admin, errTxt)) {
                    // // DTN:We now start creating the error file before throw
                    // // of exception
                    // throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    // }
                    try {
                      validateLegacyDirectMassUpdateFile(filePath, data, admin, errTxt);
                    } catch (Exception e) {
                      throw new CmrException(e);
                    }
                  } else if (SwissUtil.isCountrySwissEnabled(entityManager, data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    if (SystemLocation.SWITZERLAND.equals(data.getCmrIssuingCntry()) && !validateSWISSMassUpdateFile(filePath, data, admin)) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                    // CMR-800
                  } else if (ATUtil.isCountryATEnabled(entityManager, data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    if (SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry()) && !validateATMassUpdateFile(filePath, data, admin)) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  } else if (IERPRequestUtils.isCountryDREnabled(entityManager, data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    if (!validateDRMassUpdateFile(filePath, data, admin, data.getCmrIssuingCntry())) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  } else if (LAHandler.isLACountry(data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    if (!validateLAMassUpdateFile(filePath, data, admin, data.getCmrIssuingCntry())) { // chie1
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  } else if (FranceUtil.isCountryFREnabled(entityManager, data.getCmrIssuingCntry())) {
                    fis = new FileInputStream(filePath);
                    try {
                      if (SystemLocation.FRANCE.equals(data.getCmrIssuingCntry())) {
                        validateFRMassUpdateFile(filePath, data, admin);
                      }
                    } catch (Exception e) {
                      throw new CmrException(e);
                    }
                  } else {
                    if (!validateMassUpdateFile(item.getInputStream())) {
                      throw new CmrException(MessageUtil.ERROR_MASS_FILE);
                    }
                  }
                  log.info("mass update file validated");
                }

              }

              // if file has been created,
              if (file.exists()) {
                // MASS FILE | update DB records
                if (massCreate) {
                  log.info("mass create file saved");
                  updateAdminMassFields(request, entityManager, reqId, newIterId, filePath);
                  request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_UPLOADED));

                } else {
                  log.info("mass update file saved");
                  List<MassUpdateModel> modelList = new ArrayList<>();
                  Map<String, Object> legacyDirectModelCol = new HashMap<String, Object>();

                  if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, data.getCmrIssuingCntry())) {
                    setMassUpdateListForLegacyDirect(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath,
                        data.getCmrIssuingCntry());
                  } else if (ATUtil.isCountryATEnabled(entityManager, data.getCmrIssuingCntry())) {// CMR-800
                    setMassUpdateListForAT(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath);
                  } else if (FranceUtil.isCountryFREnabled(entityManager, data.getCmrIssuingCntry())) {
                    setMassUpdateListForFR(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath);
                  } else if (SwissUtil.isCountrySwissEnabled(entityManager, data.getCmrIssuingCntry())) {
                    setMassUpdateListForSWISS(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath);
                  } else if (IERPRequestUtils.isCountryDREnabled(entityManager, data.getCmrIssuingCntry())) {
                    setMassUpdateListForDR(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath, data.getCmrIssuingCntry());
                  } else if (LAHandler.isLACountry(data.getCmrIssuingCntry())) {
                    setMassUpdateListForLA(entityManager, legacyDirectModelCol, filePath, reqId, newIterId, filePath, data.getCmrIssuingCntry());
                  } else {
                    if (!PageManager.fromGeo("JP", cmrIssuingCntry)) {
                      setMassUpdateList(modelList, item.getInputStream(), reqId, newIterId, filePath);
                    }
                  }
                  /*
                   * DENNIS: COMMENTED THE FOLLOWING BECAUSE DYNAMIC MASS UPDATE
                   * IS NOT NEEDED YET FOR OTHER COUNTRIES
                   */
                  // if (PageManager.fromGeo("EMEA", cmrIssuingCntry)) {
                  // setMassUpdateListEMEA(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else if (DEHandler.isIERPCountry(cmrIssuingCntry) ||
                  // CNDHandler.isCNDCountry(cmrIssuingCntry)) {
                  // setMassUpdateListDECND(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else if (LAHandler.isLACountry(cmrIssuingCntry)) {
                  // setMassUpdateListLA(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else if (PageManager.fromGeo("MCO1", cmrIssuingCntry) ||
                  // PageManager.fromGeo("MCO2", cmrIssuingCntry)) {
                  // setMassUpdateListMCO(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else if (PageManager.fromGeo("CEMEA", cmrIssuingCntry)) {
                  // setMassUpdateListCEMEA(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else if (PageManager.fromGeo("NORDX", cmrIssuingCntry)) {
                  // setMassUpdateListNORDX(modelList, item.getInputStream(),
                  // reqId, newIterId, filePath);
                  // } else {
                  // if
                  // (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager,
                  // data.getCmrIssuingCntry())) {
                  // setMassUpdateListForLegacyDirect(entityManager,
                  // legacyDirectModelCol, filePath, reqId, newIterId,
                  // filePath);
                  // } else {
                  // setMassUpdateList(modelList, item.getInputStream(), reqId,
                  // newIterId, filePath);
                  // }
                  // }

                  if (!"760".equalsIgnoreCase(cmrIssuingCntry)) {
                    RequestEntryModel model = new RequestEntryModel();
                    model.setReqId(reqId);
                    model.setAction("SUBMIT_MASS_FILE");
                    model.setLegacyDirectMassUpdtList(legacyDirectModelCol);
                    processTransaction(model, request);
                  }
                  updateAdminMassFields(request, entityManager, reqId, newIterId, filePath);
                  request.getSession().setAttribute(token, "Y," + MessageUtil.getMessage(MessageUtil.INFO_MASS_FILE_UPLOADED));
                }
              }

              // DENNIS: After reading and processing, set to zip.
              if (massCreate) {
                if (file.exists()) {
                  file.delete();
                  log.info("Existing mass file will be replaced.");
                }

                try {
                  IOUtils.copy(item.getInputStream(), fos);
                } finally {
                  fos.close();
                }
              } else { // create the zip filename String
                String zipFileName = filePath + ".zip";
                file = new File(zipFileName);

                if (file.exists()) {
                  file.delete();
                  log.info("Existing mass file will be replaced.");
                }

                fos = new FileOutputStream(new File(zipFileName));
                try {
                  ZipOutputStream zos = new ZipOutputStream(fos);

                  try {
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);

                    // put the file as the single entry
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    InputStream is = item.getInputStream();
                    try {
                      while ((length = is.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                      }
                    } finally {
                      is.close();
                    }
                    zos.closeEntry();

                  } finally {
                    zos.close();
                  }
                } finally {
                  fos.close();
                }
                file = new File(zipFileName);
              }
            }
          }
        }
      } else {
        this.log.error("Error in processing file for mass change.");
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    } catch (Exception e) {
      this.log.error("Error in processing file for mass change.", e);
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        request.getSession().setAttribute(token, "N," + cmre.getMessage());
      } else if (e.getMessage() != null && !e.getMessage().isEmpty()) {
        request.getSession().setAttribute(token, "N," + e.getMessage());
      } else {
        request.getSession().setAttribute(token, "N," + MessageUtil.getMessage(MessageUtil.ERROR_GENERAL));
      }
    }
  }

  /**
   * @param modelList
   * @param mfStream
   * @param reqId
   * @param newIterId
   * @param filePath
   */
  private void setMassUpdateListForLegacyDirect(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId,
      int newIterId, String filePath, String cmrIssuingCntry) throws Exception {

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
      List<TemplateTab> tabs = template.getTabs();

      Map<String, List<String>> cmrPhoneMap = new ConcurrentHashMap<>();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
            if ("Data".equals(tab.getName())) {
              // call method that will set to Data table
              dataSheetIteration(entityManager, reqId, newIterId, cmrIssuingCntry, cmrPhoneMap, models, tab, dataSheet);
            } else {
              // if it is not Data, that means it is an address
              addressSheetIteration(entityManager, reqId, newIterId, cmrIssuingCntry, cmrPhoneMap, addrModels, tab, dataSheet);
            }
          }
        }
        // if cmrIssuingCntry UKI then billing phone in data sheet
        createAddrModelsFromMapForUKI(reqId, newIterId, cmrIssuingCntry, cmrPhoneMap, addrModels);
        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createAddrModelsFromMapForUKI(long reqId, int newIterId, String cmrIssuingCntry, Map<String, List<String>> cmrPhoneMap,
      List<MassUpdateAddressModel> addrModels) {
    if (!cmrPhoneMap.isEmpty() && (cmrIssuingCntry.equals(SystemLocation.UNITED_KINGDOM) || cmrIssuingCntry.equals(SystemLocation.IRELAND))) {
      cmrPhoneMap.forEach((cmr, list) -> {
        String addSeqNo = getAddSeqNoForMassUpdateUKI(cmrIssuingCntry, cmr);
        MassUpdateAddressModel addrModel = new MassUpdateAddressModel();
        addrModel.setParReqId(reqId);
        addrModel.setAddrSeqNo(addSeqNo);
        addrModel.setCmrNo(cmr);
        addrModel.setCustPhone(list.get(0));
        addrModel.setSeqNo(Integer.valueOf(list.get(1)));
        addrModel.setIterationId(newIterId);
        addrModel.setAddrType("ZS01");
        addrModels.add(addrModel);
      });

    }
  }

  private String getAddSeqNoForMassUpdateUKI(String cmrIssuingCntry, String cmr) {
    String addSeqNo = "";
    EntityManager entityManager = JpaManager.getEntityManager();
    PreparedQuery query = cmrIssuingCntry.equals(SystemLocation.UNITED_KINGDOM) ? getAddrSeqNosUK(cmrIssuingCntry, cmr, entityManager)
        : getAddrSeqNosIE(cmrIssuingCntry, cmr, entityManager);
    List<String> result = query.getResults(String.class);
    List<String> addSeqNos = Optional.ofNullable(result).orElseGet(Collections::emptyList).stream().filter(Objects::nonNull)
        .filter(item -> !item.isEmpty()).collect(Collectors.toList());
    if (!addSeqNos.isEmpty()) {
      return addSeqNos.contains("00001") ? "00001" : addSeqNos.get(0);
    }
    return addSeqNo;
  }

  private PreparedQuery getAddrSeqNosUK(String cmrIssuingCntry, String cmr, EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("QUERY.GET_SEQ_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", cmrIssuingCntry);
    query.setParameter("RCUXA", cmr);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    return query;
  }

  private PreparedQuery getAddrSeqNosIE(String cmrIssuingCntry, String cmr, EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("QUERY.GET_SEQ_NO_IE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", cmrIssuingCntry);
    query.setParameter("RCUXA", cmr);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    return query;
  }

  private void addressSheetIteration(EntityManager entityManager, long reqId, int newIterId, String cmrIssuingCntry,
      Map<String, List<String>> cmrPhoneMap, List<MassUpdateAddressModel> addrModels, TemplateTab tab, Sheet dataSheet) throws Exception {
    MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

    for (Row cmrRow : dataSheet) {
      int seqNo = cmrRow.getRowNum() + 1;

      if (seqNo > 1) {
        // 4. then for every sheet, get the fields
        addrModel = new MassUpdateAddressModel();
        addrModel.setParReqId(reqId);
        addrModel.setSeqNo(seqNo);
        addrModel.setIterationId(newIterId);
        addrModel.setAddrType(tab.getTypeCode());
        addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);
        if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
          setAddrModelCustPhoneAndRemoveFromMap(cmrIssuingCntry, cmrPhoneMap, addrModel);
          addrModels.add(addrModel);
        }
      }
    }
  }

  private void dataSheetIteration(EntityManager entityManager, long reqId, int newIterId, String cmrIssuingCntry,
      Map<String, List<String>> cmrPhoneMap, List<MassUpdateModel> models, TemplateTab tab, Sheet dataSheet) throws Exception {
    MassUpdateModel model;
    for (Row cmrRow : dataSheet) {
      int seqNo = cmrRow.getRowNum() + 1;

      if (seqNo > 1) {
        model = new MassUpdateModel();
        model.setParReqId(reqId);
        model.setSeqNo(seqNo);
        model.setIterationId(newIterId);
        model.setErrorTxt("");
        model.setRowStatusCd("");
        // 4. then for every sheet, get the fields
        model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

        if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
          setCMRPhoneInMap(cmrRow, cmrPhoneMap, model, cmrIssuingCntry, seqNo);
          models.add(model);
        }
      }
    }
  }

  private void setAddrModelCustPhoneAndRemoveFromMap(String cmrIssuingCntry, Map<String, List<String>> cmrPhoneMap,
      MassUpdateAddressModel addrModel) {
    if (!"ZD01".equals(addrModel.getAddrType())
        && (cmrIssuingCntry.equals(SystemLocation.UNITED_KINGDOM) || cmrIssuingCntry.equals(SystemLocation.IRELAND))) {
      String custPhone = cmrPhoneMap.get(addrModel.getCmrNo()) != null ? cmrPhoneMap.get(addrModel.getCmrNo()).get(0) : "";
      addrModel.setCustPhone(custPhone);
      cmrPhoneMap.remove(addrModel.getCmrNo());
    }
  }

  private void setCMRPhoneInMap(Row cmrRow, Map<String, List<String>> cmrPhoneMap, MassUpdateModel model, String cmrIssuingCntry, int seqNo) {
    List<String> list = null;
    DataFormatter df = new DataFormatter();
    String phoneNo = df.formatCellValue(cmrRow.getCell(14));
    if ((cmrIssuingCntry.equals(SystemLocation.UNITED_KINGDOM) || cmrIssuingCntry.equals(SystemLocation.IRELAND)) && !StringUtils.isEmpty(phoneNo)) {
      list = new ArrayList<>();
      list.add(phoneNo);
      list.add(String.valueOf(seqNo));
      cmrPhoneMap.put(model.getCmrNo(), list);

    }

  }

  // CMR-800
  /**
   * @param modelList
   * @param mfStream
   * @param reqId
   * @param newIterId
   * @param filePath
   */
  private void setMassUpdateListForAT(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId, int newIterId,
      String filePath) throws Exception {

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidators(SystemLocation.AUSTRIA);
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(SystemLocation.AUSTRIA);
      List<TemplateTab> tabs = template.getTabs();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
            if ("Data".equals(tab.getName())) {

              // call method that will set to Data table
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
                    models.add(model);
                  }
                }
              }
            } else {
              // if it is not Data, that means it is an address
              MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  // 4. then for every sheet, get the fields
                  addrModel = new MassUpdateAddressModel();
                  addrModel.setParReqId(reqId);
                  addrModel.setSeqNo(seqNo);
                  addrModel.setIterationId(newIterId);
                  addrModel.setAddrType(tab.getTypeCode());
                  addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);

                  if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
                    addrModels.add(addrModel);
                  }
                }
              }
            }
          }
        }
        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setMassUpdateListForDR(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId, int newIterId,
      String filePath, String cmrIssuingCntry) throws Exception {

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
      List<TemplateTab> tabs = template.getTabs();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
            if ("Data".equals(tab.getName())) {
              // call method that will set to Data table
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
                    models.add(model);
                  }
                }
              }
            } else {
              // if it is not Data, that means it is an address
              MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  // 4. then for every sheet, get the fields
                  addrModel = new MassUpdateAddressModel();
                  addrModel.setParReqId(reqId);
                  addrModel.setSeqNo(seqNo);
                  addrModel.setIterationId(newIterId);
                  addrModel.setAddrType(tab.getTypeCode());
                  addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);

                  if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
                    addrModels.add(addrModel);
                  }
                }
              }
            }
          }
        }
        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setMassUpdateListForFR(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId, int newIterId,
      String filePath) throws Exception {

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidators(SystemLocation.FRANCE);
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(SystemLocation.FRANCE);
      List<TemplateTab> tabs = template.getTabs();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
            if ("Data".equals(tab.getName())) {
              // call method that will set to Data table
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
                    models.add(model);
                  }
                }
              }
            } else {
              // if it is not Data, that means it is an address
              MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  // 4. then for every sheet, get the fields
                  addrModel = new MassUpdateAddressModel();
                  addrModel.setParReqId(reqId);
                  addrModel.setSeqNo(seqNo);
                  addrModel.setIterationId(newIterId);
                  addrModel.setAddrType(tab.getTypeCode());
                  addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);

                  if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
                    addrModels.add(addrModel);
                  }
                }
              }
            }
          }
        }
        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @param modelList
   * @param mfStream
   * @param reqId
   * @param newIterId
   * @param filePath
   */
  private void setMassUpdateListForSWISS(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId, int newIterId,
      String filePath) throws Exception {

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidatorsSwiss();
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate("SWISS");
      List<TemplateTab> tabs = template.getTabs();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER only
            if ("Data".equals(tab.getName())) {
              // call method that will set to Data table
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
                    models.add(model);
                  }
                }
              }
            } else {
              // if it is not Data, that means it is an address
              MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  // 4. then for every sheet, get the fields
                  addrModel = new MassUpdateAddressModel();
                  addrModel.setParReqId(reqId);
                  addrModel.setSeqNo(seqNo);
                  addrModel.setIterationId(newIterId);
                  addrModel.setAddrType(tab.getTypeCode());
                  addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);

                  if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
                    addrModels.add(addrModel);
                  }
                }
              }
            }
          }
        }
        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setMassUpdateListForLA(EntityManager entityManager, Map<String, Object> massUpdtCol, String filepath, long reqId, int newIterId,
      String filePath, String cmrIssuingCntry) throws Exception {
    LOG.debug("setMassUpdateListFor LA....");

    // 1. get the config file and get all the valid tabs
    try {
      MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);
      // change to the ID of the config you are generating
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
      List<TemplateTab> tabs = template.getTabs();

      InputStream mfStream = new FileInputStream(filepath);

      // 2. loop through all the tabs returned by the config
      if (tabs != null && tabs.size() > 0) {
        MassUpdateModel model = new MassUpdateModel();
        List<MassUpdateAddressModel> addrModels = new ArrayList<MassUpdateAddressModel>();
        List<MassUpdateModel> models = new ArrayList<MassUpdateModel>();
        List<MassUpdateModel> modelList = new ArrayList<MassUpdateModel>();

        try (Workbook mfWb = new XSSFWorkbook(mfStream)) {
          for (int i = 0; i < tabs.size(); i++) {
            // 3. For every sheet, do: Sheet dataSheet =
            // mfWb.getSheet(CMR_SHEET_NAME);
            TemplateTab tab = tabs.get(i);
            Sheet dataSheet = mfWb.getSheet(tab.getName());

            // Check row for ISU_CD, INAC_CD, and/or CLIENT_TIER
            // only ==========================================
            if ("Data".equals(tab.getName())) {

              // call method that will set to Data table
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");

                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0) {
                    models.add(model);
                    modelList.add(model);
                  }
                }
              }
            } else if ("AccountReceivable".equals(tab.getName())) {
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");

                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  for (MassUpdateModel dataModel : modelList) {
                    if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0
                        && dataModel.getSeqNo() >= model.getSeqNo()) {
                      if (!StringUtils.isEmpty(dataModel.getCmrNo()) && dataModel.getCmrNo().equals(model.getCmrNo())) {
                        dataModel.setSeqNo(model.getSeqNo());
                        dataModel.setCollectionCd(model.getCollectionCd());
                        dataModel.setModeOfPayment(model.getModeOfPayment());
                        dataModel.setCreditCode(model.getCreditCode());
                        dataModel.setCodCondition(model.getCodCondition());
                        dataModel.setCodReason(model.getCodReason());
                        dataModel.setIbmBankNumber(model.getIbmBankNumber());
                        dataModel.setCollectorNameNo(model.getCollectorNameNo());
                        models.set(model.getSeqNo() - 2, dataModel);
                        modelList.set(model.getSeqNo() - 2, dataModel);
                        break;
                      } else {
                        LOG.debug("skipping since no cmr no that is same in Data tab is found.");
                      }
                    }
                  }
                }
              }
            } else if ("Email".equals(tab.getName())) {
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  for (MassUpdateModel dataModel : modelList) {
                    if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0
                        && dataModel.getSeqNo() >= model.getSeqNo()) {
                      if (!StringUtils.isEmpty(dataModel.getCmrNo()) && dataModel.getCmrNo().equals(model.getCmrNo())) {
                        dataModel.setSeqNo(model.getSeqNo());
                        dataModel.setEmail1(model.getEmail1());
                        dataModel.setEmail2(model.getEmail2());
                        dataModel.setEmail3(model.getEmail3());

                        models.set(model.getSeqNo() - 2, dataModel);
                        break;
                      } else {
                        LOG.debug("skipping since no cmr no that is same in Data tab is found.");
                      }
                    }
                  }
                }
              }
            } else if ("TaxInfo".equals(tab.getName())) {
              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  model = new MassUpdateModel();
                  model.setParReqId(reqId);
                  model.setSeqNo(seqNo);
                  model.setIterationId(newIterId);
                  model.setErrorTxt("");
                  model.setRowStatusCd("");
                  // 4. then for every sheet, get the fields
                  model = setMassUpdateData(entityManager, cmrRow, model, tab, reqId);

                  for (MassUpdateModel dataModel : modelList) {
                    if (!StringUtils.isEmpty(model.getCmrNo()) && model.getCmrNo().length() <= 8 && model.getCmrNo().length() != 0
                        && dataModel.getSeqNo() >= model.getSeqNo()) {
                      if (!StringUtils.isEmpty(dataModel.getCmrNo()) && dataModel.getCmrNo().equals(model.getCmrNo())) {
                        dataModel.setSeqNo(model.getSeqNo());
                        dataModel.setTaxCd(model.getTaxCd());
                        dataModel.setTaxNum(model.getTaxNum());
                        dataModel.setTaxSeparationIndc(model.getTaxSeparationIndc());
                        dataModel.setBillingPrintIndc(model.getBillingPrintIndc());
                        dataModel.setContractPrintIndc(model.getContractPrintIndc());
                        dataModel.setCountryUse(model.getCountryUse());

                        models.set(model.getSeqNo() - 2, dataModel);
                        break;
                      } else {
                        LOG.debug("skipping since no cmr no that is same in Data tab is found.");
                      }
                    }
                  }
                }
              }
            } else {

              // if it is not Data, that means it is an address
              MassUpdateAddressModel addrModel = new MassUpdateAddressModel();

              for (Row cmrRow : dataSheet) {
                int seqNo = cmrRow.getRowNum() + 1;

                if (seqNo > 1) {
                  // 4. then for every sheet, get the fields
                  addrModel = new MassUpdateAddressModel();
                  addrModel.setParReqId(reqId);
                  addrModel.setSeqNo(seqNo);
                  addrModel.setIterationId(newIterId);
                  addrModel.setAddrType(tab.getTypeCode());
                  addrModel = setMassUpdateAddr(entityManager, cmrRow, addrModel, tab, reqId);

                  if (!StringUtils.isEmpty(addrModel.getCmrNo()) && addrModel.getCmrNo().length() <= 8 && addrModel.getCmrNo().length() != 0) {
                    addrModels.add(addrModel);
                  }
                }
              }
            }
          }
        }

        massUpdtCol.put("dataModels", models);
        massUpdtCol.put("addrModels", addrModels);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private MassUpdateAddressModel setMassUpdateAddr(EntityManager entityManager, Row cmrRow, MassUpdateAddressModel muaModel, TemplateTab dataTab,
      long reqId) throws Exception {

    if (muaModel == null) {
      muaModel = new MassUpdateAddressModel();
    }

    List<TemplateColumn> columns = dataTab.getColumns();
    DataFormatter df = new DataFormatter();

    for (int i = 0; i < columns.size(); i++) {
      TemplateColumn col = columns.get(i);
      String tempVal = "";

      if (!StringUtils.isEmpty(col.getLovId()) || !StringUtils.isEmpty(col.getBdsId())) {
        String lov = col.getLovId();

        String txt = df.formatCellValue(cmrRow.getCell(i));

        if (txt != null && txt.contains("|")) {
          txt = txt.replace("|", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[1].trim();

          if (txt.contains("-")) {
            txtSplit = txt.split("-");

            if ("LAND_CNTRY".equals(col.getDbColumn())) {
              txt = txtSplit[1].trim();
            } else {
              txt = txtSplit[0].trim();
            }
          }
        } else if (txt != null && txt.contains("-")) {
          txt = txt.replace("-", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        }

        tempVal = txt;
      } else if (!StringUtils.isEmpty(col.getBdsId())) {
        String bds = col.getBdsId();

        String txt = df.formatCellValue(cmrRow.getCell(i));

        if (txt != null && txt.contains("|")) {
          txt = txt.replace("|", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        } else if (txt != null && txt.contains("-")) {
          txt = txt.replace("-", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        }

        tempVal = txt;
      } else {
        tempVal = df.formatCellValue(cmrRow.getCell(i));
      }

      if ((tempVal != null && tempVal.equals("DO NOT INPUT ANYTHING BEYOND THIS LINE. THIS IS THE MAXIMUM ALLOWED NUMBER OF ENTRIES."))
          || (StringUtils.isEmpty(tempVal) && "CMR_NO".equals(col.getDbColumn()))) {
        muaModel = new MassUpdateAddressModel();
        break;
      }

      switch (col.getDbColumn()) {
      case "CUST_NM1":
        muaModel.setCustNm1(tempVal);
        break;
      case "CUST_NM2":
        muaModel.setCustNm2(tempVal);
        break;
      case "CUST_NM3":
        muaModel.setCustNm3(tempVal);
        break;
      case "CUST_NM4":
        muaModel.setCustNm4(tempVal);
        break;
      case "ADDR_TXT":
        muaModel.setAddrTxt(tempVal);
        break;
      case "ADDR_TXT2":
        muaModel.setAddrTxt2(tempVal);
        break;
      case "DEPT":
        muaModel.setDept(tempVal);
        break;
      case "CITY1":

        if (StringUtils.isEmpty(muaModel.getCity1()) && !StringUtils.isEmpty(tempVal)) {
          muaModel.setCity1(tempVal);
        }
        break;
      case "POST_CD":

        if (StringUtils.isEmpty(muaModel.getPostCd()) && !StringUtils.isEmpty(tempVal)) {
          muaModel.setPostCd(tempVal);
        }
        break;
      case "CMR_NO":
        muaModel.setCmrNo(tempVal);
        break;
      case "ADDR_SEQUENCE_NO":
        muaModel.setAddrSeqNo(tempVal);
        break;
      case "ADDR_TYPE":
        muaModel.setAddrType(tempVal);
        break;
      case "LAND_CNTRY":
        muaModel.setLandCntry(tempVal);
        break;
      case "PO_BOX":
        muaModel.setPoBox(tempVal);
        break;
      case "FLOOR":// For Tureky,used store taxOffice
        muaModel.setFloor(tempVal);
        break;
      case "BLDG":
        muaModel.setBldg(tempVal);
        break;
      case "CUST_LANG_CD":
        muaModel.setCustLangCd(tempVal);
        break;
      case "CUST_PHONE":
        muaModel.setCustPhone(tempVal);
        break;
      case "CUST_FAX":
        muaModel.setCustFax(tempVal);
        break;
      case "HW_INSTL_MSTR_FLG":
        muaModel.setHwInstlMstrFlg(tempVal);
        break;
      case "COUNTY":
        muaModel.setCounty(tempVal);
        break;
      case "DIVN":
        muaModel.setDivn(tempVal);
        break;
      case "STATE_PROV":
        muaModel.setStateProv(tempVal);
        break;
      default:
        LOG.debug("Default condition was executed [nothing is saved] for DB column >> " + col.getLabel());
        break;
      }
    }
    /*
     * if (StringUtils.isNotEmpty(muaModel.getDept()) ||
     * StringUtils.isNotEmpty(muaModel.getBldg()) ||
     * StringUtils.isNotEmpty(muaModel.getFloor())){
     */
    String name3 = "";
    if (StringUtils.isNotBlank(muaModel.getDept()) && !StringUtils.equals(muaModel.getDept(), "@")) {
      name3 += muaModel.getDept();
      if (StringUtils.isNotBlank(muaModel.getBldg()) && !StringUtils.equals(muaModel.getBldg(), "@")) {
        name3 += ", ";
      } else if (StringUtils.isNotBlank(muaModel.getFloor()) && !StringUtils.equals(muaModel.getFloor(), "@")) {
        name3 += ", ";
      }
    }
    if (StringUtils.isNotBlank(muaModel.getBldg()) && !StringUtils.equals(muaModel.getBldg(), "@")) {
      name3 += muaModel.getBldg();
      if (StringUtils.isNotBlank(muaModel.getFloor()) && !StringUtils.equals(muaModel.getFloor(), "@")) {
        name3 += ", ";
      }
    }
    if (StringUtils.isNotBlank(muaModel.getFloor()) && !StringUtils.equals(muaModel.getFloor(), "@")) {
      name3 += muaModel.getFloor();
    }
    if (StringUtils.isNotBlank(name3)) {
      muaModel.setCustNm3(name3);
    }

    if (StringUtils.isNotBlank(muaModel.getDept()) && StringUtils.isNotBlank(muaModel.getBldg()) && StringUtils.isNotBlank(muaModel.getFloor())) {
      if (muaModel.getDept().equals("@") && muaModel.getBldg().equals("@") && muaModel.getFloor().equals("@")) {
        LOG.debug("Set value CustName3=@ for row no.: " + cmrRow.getRowNum());
        muaModel.setCustNm3("@");
      }
    }
    return muaModel;
  }

  private MassUpdateModel setMassUpdateData(EntityManager entityManager, Row cmrRow, MassUpdateModel muModel, TemplateTab dataTab, long reqId)
      throws Exception {

    if (muModel == null) {
      muModel = new MassUpdateModel();
    }

    List<TemplateColumn> columns = dataTab.getColumns();
    DataFormatter df = new DataFormatter();
    Data data = getCurrentDataRecordById(entityManager, reqId);

    for (int i = 0; i < columns.size(); i++) {
      TemplateColumn col = columns.get(i);
      String tempVal = "";

      if (!StringUtils.isEmpty(col.getLovId()) || !StringUtils.isEmpty(col.getBdsId())) {
        String lov = col.getLovId();

        String txt = df.formatCellValue(cmrRow.getCell(i));

        if (txt != null && txt.contains("|")) {
          txt = txt.replace("|", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[1].trim();

          if (txt.contains("-")) {
            txtSplit = txt.split("-");
            txt = txtSplit[0].trim();
          }

        } else if (txt != null && txt.contains("-")) {
          txt = txt.replace("-", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        }

        tempVal = txt;// getLovCode(entityManager, txt,
                      // data.getCmrIssuingCntry(), lov);
      } else if (!StringUtils.isEmpty(col.getBdsId())) {
        String bds = col.getBdsId();

        String txt = df.formatCellValue(cmrRow.getCell(i));

        if (txt != null && txt.contains("|")) {
          txt = txt.replace("|", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        } else if (txt != null && txt.contains("-")) {
          txt = txt.replace("-", ",");
          String[] txtSplit = txt.split(",");
          txt = txtSplit[0].trim();
        }

        tempVal = txt;// getBdsCode(entityManager, txt, bds);
      } else {
        tempVal = df.formatCellValue(cmrRow.getCell(i));
      }

      if (tempVal != null && tempVal.equals("DO NOT INPUT ANYTHING BEYOND THIS LINE. THIS IS THE MAXIMUM ALLOWED NUMBER OF ENTRIES.")) {
        muModel = new MassUpdateModel();
        break;
      }

      if (StringUtils.isEmpty(tempVal) && "CMR_NO".equals(col.getDbColumn())) {
        muModel = new MassUpdateModel();
        break;
      }

      switch (col.getDbColumn()) {
      case "ABBREV_NM":
        muModel.setAbbrevNm(tempVal);
        break;
      case "ABBREV_LOCN":
        muModel.setAbbrevLocn(tempVal);
        break;
      case "ISIC_CD":
        muModel.setIsicCd(tempVal);

        // auto-populate Subindustry
        String subInd = LegacyDirectUtil.getMappedSubindOnMassUpdt(entityManager, tempVal);
        muModel.setSubIndustryCd(subInd != null ? subInd : "");
        break;
      case "VAT":
        muModel.setVat(tempVal);
        break;
      case "SPECIAL_TAX_CD":
        muModel.setSpecialTaxCd(tempVal);
        break;
      case "ENGINEERING_BO":
        // muModel.setEngineeringBo(tempVal);
        muModel.setCustNm2(tempVal);
        break;
      case "ENTERPRISE":
        muModel.setEnterprise(tempVal);
        break;
      case "INAC_CD":
        muModel.setInacCd(tempVal);
        break;
      case "ISU_CD":
        muModel.setIsuCd(tempVal);
        break;
      case "SALES_BO_CD":
        // DTN: This is temporary until we can get the right field
        muModel.setCustNm1(tempVal);
        break;
      case "REP_TEAM_MEMBER_NO":
        muModel.setRepTeamMemberNo(tempVal);
        break;
      case "COLLECTION_CD":
        muModel.setCollectionCd(tempVal);
        break;
      case "ORD_BLK":
        muModel.setOrdBlk(tempVal);
        break;
      case "INSTALL_BRANCH_OFF":
        muModel.setInstallBranchOff(tempVal);
        break;
      case "MODE_OF_PAYMENT":
        muModel.setModeOfPayment(tempVal);
        break;
      case "COMPANY":
        muModel.setCompany(tempVal);
        break;
      case "CLIENT_TIER":
        muModel.setClientTier(tempVal);
        break;
      case "SEARCH_TERM":
        muModel.setSearchTerm(tempVal);
        break;
      case "CMR_NO":
        muModel.setCmrNo(tempVal);
        break;
      case "EMBARGO_CD":
        muModel.setMiscBillCd(tempVal);
        break;
      case "MAILING_COND":
        muModel.setOutCityLimit(tempVal);
        break;
      case "CUST_CLASS":
        muModel.setCustClass(tempVal);
        break;
      case "CURRENCY_CD":
        // Turkey use this represent Type of Customer
        muModel.setCurrencyCd(tempVal);
        break;
      case "OUT_CITY_LIMIT":
        muModel.setOutCityLimit(tempVal);
        break;
      case "AFFILIATE":
        muModel.setAffiliate(tempVal);
        break;
      case "NEW_ENTP_NAME1":
        // muModel.setNewEntpName1(tempVal);
        muModel.setNewEntpName1(tempVal);
        break;
      case "ENTP_UPDT_TYP":
        muModel.setEntpUpdtTyp(tempVal);
        break;
      case "EMAIL1":
        muModel.setEmail1(tempVal);
        break;
      case "EMAIL2":
        muModel.setEmail2(tempVal);
        break;
      case "EMAIL3":
        muModel.setEmail3(tempVal);
        break;
      // CMR-1728: CSO_SITE and RESTRICT_TO is temp used to store EconomicCD and
      // CoF for Turkey
      case "CSO_SITE":
        muModel.setCsoSite(tempVal);
        break;
      case "RESTRICT_TO":
        muModel.setRestrictTo(tempVal);
        break;
      case "CUST_NM3":
        muModel.setCustNm3(tempVal);
        break;
      case "SUB_INDUSTRY_CD":
        muModel.setSubIndustryCd(tempVal);
        break;
      case "TAX_CD1":
        muModel.setTaxCd1(tempVal);
        ;
        break;
      case "MILITARY":
        muModel.setMilitary(tempVal);
        break;
      case "SVC_AR_OFFICE":
        muModel.setSvcArOffice(tempVal);
        break;
      case "TAX_CD3":
        muModel.setTaxCd3(tempVal);
        break;
      case "CREDIT_CD":
        muModel.setCreditCode(tempVal);
        break;
      case "IBM_BANK_NO":
        muModel.setIbmBankNumber(tempVal);
        break;
      case "COLLECTOR_NO":
        muModel.setCollectorNameNo(tempVal);
        break;
      case "COD_CONDITION":
        muModel.setCodCondition(tempVal);
        break;
      case "COD_RSN":
        muModel.setCodReason(tempVal);
        break;
      case "TAX_CD":
        muModel.setTaxCd(tempVal);
        break;
      case "TAX_NUM":
        muModel.setTaxNum(tempVal);
        break;
      case "TAX_SEPARATION_INDC":
        muModel.setTaxSeparationIndc(tempVal);
        break;
      case "BILLING_PRINT_INDC":
        muModel.setBillingPrintIndc(tempVal);
        break;
      case "CONTRACT_PRINT_INDC":
        muModel.setContractPrintIndc(tempVal);
        break;
      case "CNTRY_USE":
        muModel.setCountryUse(tempVal);
        break;
      default:
        LOG.debug("Default condition was executed [nothing was saved] for DB column >> " + col.getLabel());
        break;
      }
    }
    return muModel;
  }

  private String getBdsCode(EntityManager entityManager, String desc, String field) {
    String cd = "";
    String sql = ExternalizedQuery.getSql("BDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("FIELD_ID", "##" + field);
    List<BdsTblInfo> info = query.getResults(1, BdsTblInfo.class);
    PreparedQuery bdsQuery = null;
    StringBuilder sb = new StringBuilder();

    if (info != null && info.size() > 0) {
      BdsTblInfo bds = info.get(0);
      sb.append("SELECT TRIM(" + bds.getCd() + ") CD ");
      sb.append("FROM " + bds.getSchema() + "." + bds.getTbl() + " ");
      sb.append("WHERE " + bds.getDesc() + " = '" + desc + "' ");
      bdsQuery = new PreparedQuery(entityManager, sb.toString());
    }

    if (bdsQuery != null) {
      bdsQuery.setForReadOnly(true);
      List<Object[]> results = query.getResults(-1);

      if (results != null && results.size() > 0) {
        Object[] result = results.get(0);
        cd = result[0] != null ? result[0].toString() : "";
      }
    }

    return cd;
  }

  private String getLovCode(EntityManager entityManager, String desc, String cntry, String field) {
    String cd = "";
    field = "##" + field;
    String sql = ExternalizedQuery.getSql("GET.LOV_CD.BY_DESC_CNTRY_FIELDID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setParameter("DESC", desc);
    query.setParameter("FIELD", field);
    query.setForReadOnly(true);

    List<Object[]> results = query.getResults();

    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      cd = result[0] != null ? (String) result[0] : "";
    }

    return cd;
  }

  public static boolean isMassRequestAutomationEnabled(EntityManager entityManager, String country, String req) {
    int count = 0;
    String req_typ = (!StringUtils.isBlank(req)) ? "%" + req + "%" : "";
    String sql = ExternalizedQuery.getSql("AUTOMATION.CHECK_MASS_REQUESTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    /* query.setForReadOnly(true); */
    query.setParameter("CNTRY", country != null && country.length() > 3 ? country.substring(0, 3) : country);
    query.setParameter("REQ_TYP", req_typ);
    count = query.getSingleResult(Integer.class);
    if (count >= 1) {
      return true;
    } else {
      return false;
    }

  }

  public boolean validateFRMassUpdateFile(String path, Data data, Admin admin) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidators(SystemLocation.FRANCE);
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(SystemLocation.FRANCE);

      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;
        StringBuilder errTxt = new StringBuilder();

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            for (ValidationRow row : validation.getRows()) {
              if (!row.isSuccess()) {
                if (StringUtils.isEmpty(errTxt.toString())) {
                  errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
                } else {
                  errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
                }
              }
            }
          }
        }

        if (!StringUtils.isEmpty(errTxt.toString())) {
          // LOG.debug(errTxt);
          throw new Exception(errTxt.toString());
        }
        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {
        LOG.error("An error occurred in validating FR Mass Update File.");
        throw new Exception(e.getMessage());
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }

  }

  /**
   * Appends extra model entries if needed. These attributes can be accessed on
   * the page via request.getAttribute('name') or ${name}
   * 
   * @param mv
   * @param model
   * @throws CmrException
   */
  public void appendExtraModelEntries(ModelAndView mv, RequestEntryModel model) throws CmrException {
    // TODO Auto-generated method stub
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      String autoEngineIndc = RequestUtils.getAutomationConfig(entityManager, model.getCmrIssuingCntry());
      mv.addObject("autoEngineIndc", autoEngineIndc);
    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in processing transaction " + model, e);
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    }
  }

  private void setLockByName(Admin admin) {
    if (StringUtils.isBlank(admin.getLockByNm()) && !StringUtils.isBlank(admin.getLockBy())) {
      try {
        Person person = BluePagesHelper.getPerson(admin.getLockBy());
        if (person != null) {
          admin.setLockByNm(person.getName());
        }
      } catch (CmrException e) {
        this.log.warn("Name for " + admin.getLockBy() + " cannot be retrieved.");
      }
    }
  }

  /*
   * private boolean validateMassCreateCA(InputStream fileStream) throws
   * Exception { XSSFWorkbook book = new XSSFWorkbook(fileStream); XSSFSheet
   * sheet = book.getSheet(MASS_DATA); XSSFCell rowCell = null; XSSFRow sheetRow
   * = null;
   * 
   * int rowIndex = 0; int maxRows =
   * Integer.parseInt(SystemConfiguration.getValue("MASS_CREATE_MAX_ROWS",
   * "100")); StringBuilder sbErrorRow = new StringBuilder(); boolean
   * isInvalidRow = false; for (Row row : sheet) { sheetRow = (XSSFRow) row; //
   * validate CMR No if numeric if (rowIndex > 0) { rowCell =
   * sheetRow.getCell(0); if (rowCell == null) { rowCell =
   * sheetRow.createCell(0); }
   * 
   * if (rowIndex > maxRows) {
   * log.error("Total cmrRecords exceed the maximum limit of " + maxRows); throw
   * new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, "" + maxRows); }
   * 
   * String cellValue = rowCell.getStringCellValue(); if
   * (StringUtils.isBlank(cellValue) || (StringUtils.isNotBlank(cellValue) &&
   * !StringUtils.isNumeric(cellValue))) { isInvalidRow = true;
   * sbErrorRow.append("" + rowIndex); sbErrorRow.append(","); } }
   * 
   * rowIndex++; }
   * 
   * book.close(); if (isInvalidRow) { String errorRow = sbErrorRow.toString();
   * errorRow = StringUtils.removeEnd(errorRow, ",");
   * log.error("CMR number field is required and should be numeric: row " +
   * errorRow); throw new
   * CmrException(MessageUtil.ERROR_MASS_FILE_INVALID_CMRNO, errorRow); }
   * 
   * return !isInvalidRow; }
   */

  private boolean validateMassUpdateCA(InputStream fileStream) throws Exception {
    XSSFWorkbook book = new XSSFWorkbook(fileStream);
    XSSFSheet sheet = book.getSheet("Update");
    XSSFCell rowCell = null;
    XSSFRow sheetRow = null;

    int rowIndex = 0;
    int maxRows = Integer.parseInt(SystemConfiguration.getValue("MASS_CREATE_MAX_ROWS", "100"));
    StringBuilder sbErrorRow = new StringBuilder();
    boolean isInvalidRow = false;
    for (Row row : sheet) {
      sheetRow = (XSSFRow) row;
      // validate CMR No if numeric
      if (rowIndex > 0) {
        rowCell = sheetRow.getCell(0);
        if (rowCell == null) {
          rowCell = sheetRow.createCell(0);
        }

        if (rowIndex > maxRows) {
          log.error("Total cmrRecords exceed the maximum limit of " + maxRows);
          throw new CmrException(MessageUtil.ERROR_MASS_FILE_ROWS, "" + maxRows);
        }

        // String cellValue = rowCell.getStringCellValue();

        try {
          rowCell.getNumericCellValue();
        } catch (IllegalStateException ie) {
          String sRowCellValue = rowCell.getStringCellValue();
          if (!StringUtils.isNumeric(sRowCellValue)) {
            isInvalidRow = true;
            sbErrorRow.append("" + rowIndex);
            sbErrorRow.append(",");
          }
        } catch (Exception e) {
          isInvalidRow = true;
          sbErrorRow.append("" + rowIndex);
          sbErrorRow.append(",");
        }

        // if (StringUtils.isBlank(cellValue) ||
        // (StringUtils.isNotBlank(cellValue) &&
        // !StringUtils.isNumeric(cellValue))) {
        // isInvalidRow = true;
        // sbErrorRow.append("" + rowIndex);
        // sbErrorRow.append(",");
        // }
      }

      rowIndex++;
    }

    book.close();
    if (isInvalidRow) {
      String errorRow = sbErrorRow.toString();
      errorRow = StringUtils.removeEnd(errorRow, ",");
      log.error("CMR number field is required and should be numeric: row " + errorRow);
      throw new CmrException(MessageUtil.ERROR_MASS_FILE_INVALID_CMRNO, errorRow);
    }

    return !isInvalidRow;
  }

  // katr10 <> '' skip this CMR record.
  private boolean isSkipCMRno(String cmr) {
    boolean isSkip = false;
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("QUERY.US.GETMASSUPDTSKIP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmr);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    List<String> katr10s = query.getResults(String.class);
    if (katr10s == null || katr10s.isEmpty()) {
      LOG.error("The CMR No: " + cmr + " is not IBM record.");
      isSkip = true;
    }

    return isSkip;
  }

  public boolean validateLAMassUpdateFile(String path, Data data, Admin admin, String cmrIssuingCntry) throws Exception {
    List<Boolean> isErr = new ArrayList<Boolean>();
    try (FileInputStream fis = new FileInputStream(path)) {
      MassChangeTemplateManager.initTemplatesAndValidators(cmrIssuingCntry);
      MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate(cmrIssuingCntry);
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = data.getCmrIssuingCntry();
        LOG.debug("Validating " + path);
        byte[] bookBytes = template.cloneStream(fis);

        List<TemplateValidation> validations = null;
        StringBuilder errTxt = new StringBuilder();

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {

          validations = template.validate(em, is, admin, country, 2000);
          LOG.debug(new ObjectMapper().writeValueAsString(validations));

          for (TemplateValidation validation : validations) {
            if (validation.hasErrors()) {
              if (StringUtils.isEmpty(errTxt.toString())) {
                errTxt.append("Tab name :" + validation.getTabName() + ", " + validation.getAllError());
              } else {
                errTxt.append("\nTab name :" + validation.getTabName() + ", " + validation.getAllError());
              }
            }
          }

        }
        if (!StringUtils.isEmpty(errTxt.toString())) {
          throw new Exception(errTxt.toString());
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream(path)) {
            LOG.debug("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } catch (Exception e) {

        LOG.error(e.getMessage());
        LOG.error("An error occurred in validating DR Mass Update File.");
        // return false;

        throw new Exception(e.getMessage());
        // throw new CmrException(MessageUtil.ERROR_GENERAL, e.getMessage());
        // return false;
      } finally {
        em.close();
      }
    }

    if (isErr.contains(false)) {
      return false;
    } else {
      return true;
    }

  }

}
