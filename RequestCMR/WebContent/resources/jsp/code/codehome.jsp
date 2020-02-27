<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<style>
  table.ibm-data-table th, table.ibm-data-table td, table.ibm-data-table a, .ibm-type table caption em {
    letter-spacing: 1px;
  }
  table.ibm-data-table td:NTH-CHILD(3) {
    font-size: 12px;
  }
  th.subhead {
    font-size: 12px;
    text-transform: uppercase;
  }
</style>
<div class="ibm-columns">
  <!-- Main Content -->
  <div class="ibm-col-1-1">
    <div id="wwq-content">

      <div class="ibm-columns">
        <div class="ibm-col-1-1" style="width: 915px">
          <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
            <caption>
              <em>Code Table Maintenance</em>
            </caption>
            <thead>
              <tr>
                <th scope="col" width="25%">Name</th>
                <th scope="col" width="*">Description</th>
                <th scope="col" width="20%">Table Name</th>
              </tr>
            </thead>
            <tbody>
            <%if (SystemConfiguration.isAdmin(request)){ %> 
              <tr>
                <th colspan="3">General Configurations</th>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/field_info')">Field Information</a>
                </td>
                <td>Maintains field information for the configurable request fields</td>
                <td>CREQCMR.FIELD_INFO</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/field_lbl')">Field Label</a>
                </td>
                <td>Maintains labels displayed on the screen for configurable request fields</td>
                <td>CREQCMR.FIELD_LBL</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/cmrinternaltypes')">CMR Internal Types</a>
                </td>
                <td>Maintains the internal request types computed by the system</td>
                <td>CREQCMR.CMR_INTERNAL_TYPES</td>
              </tr>
              <tr>
                <td><a  href="javascript: goToUrl('${contextPath}/code/claimroles')">Claim Roles</a></td>
                <td>Maintains claim roles for CreateCMR users</td>
                <td>CREQCMR.CLAIM_ROLES</td>
              </tr>              
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/scenarios')">Customer Scenarios</a>
                </td>
                <td>Maintains defined customer scenarios (non-US)</td>
                <td>CREQCMR.CUST_SCENARIOS</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/copy')">Copy Configurations</a>
                </td>
                <td>Utility to copy one configuration set to others</td>
                <td>N/A</td>
              </tr>
              <tr>
                <th colspan="3">GEO Configurations</th>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/cntrygeodef')">Country-GEO Definition</a>
                </td>
                <td>Maintains Countries associated per defined GEO</td>
                <td>CREQCMR.CNTRY_GEO_DEF</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/proccenters')">Processing Center</a>
                </td>
                <td>Maintains Processing Center Records</td>
                <td>CREQCMR.PROC_CENTER</td>
              </tr>            
              <tr>
                <th colspan="3">List of Values</th>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/bds_tbl_info')">Business Data Source</a>
                </td>
                <td>Maintains BDS references for dropdowns</td>
                <td>CREQCMR.BDS_TBL_INFO</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/lovs')">LOV</a>
                </td>
                <td>Maintains LOV Records</td>
                <td>CREQCMR.LOV</td>
              </tr>
              <tr>
                <th colspan="3">Reference Values</th>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/status_desc')">Status Description</a>
                </td>
                <td>Maintains overall status descriptions for requests</td>
                <td>CREQCMR.STATUS_DESC</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/statusAct')">Status-Action</a>
                </td>
                <td>Maintains descriptions for Your Actions entries</td>
                <td>CREQCMR.STATUS_ACT</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/roles')">Roles</a>
                </td>
                <td>Maintains system defined Roles</td>
                <td>CMMA.ROLES</td>
              </tr>         
              <%} %>
              <tr>
                <th colspan="3">CMDE Maintainable</th>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">System</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td><a  href="javascript: goToUrl('${contextPath}/code/validationurls')">Validation URLS</a></td>
                <td>Maintains links used for routine validation per country</td>
                <td>CREQCMR.VALIDATION_URLS</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/sysparameters')">System Parameters</a>
                </td>
                <td>Maintains System Parameters</td>
                <td>CREQCMR.SYST_PARAMETERS</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/mqstatus')">MQ Interface Status</a>
                </td>
                <td>Shows the current status of records processed via the MQ Interface</td>
                <td>CREQCMR.MQ_INTF_REQ_QUEUE</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/auto/config/base')">Automation Engines</a>
                </td>
                <td>Maintains Automation Engine configurations</td>
                <td>CREQCMR.AUTO_CONFIG_DEFN</td>
              </tr> 
              <tr>
                <td><a  href="javascript: goToUrl('${contextPath}/code/suppcountry')">Supported Countries</a></td>
                <td>Maintains supported countries on the application and automation engine settings</td>
                <td>CREQCMR.SUPP_CNTRY</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/salesBo')">Sales BO Maintenance</a>
                </td>
                <td>Maintains per country mappings between SBO, ISU, CTC and MRC</td>
                <td>CREQCMR.SALES_BRANCH_OFF</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/isic')">ISIC Maintenance</a>
                </td>
                <td>Maintains WW and US ISIC Mappings</td>
                <td>CMMA.REFT_UNSIC_W</td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">Approvals</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/approval_types')">Approval Types</a>
                </td>
                <td>Maintains Approval Types usable by the application</td>
                <td>CREQCMR.APPROVAL_TYP</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/defaultappr')">Default Approvals</a>
                </td>
                <td>Maintains Default Approval configurations</td>
                <td>CREQCMR.DEFAULT_APPROVALS</td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">United States</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/scclist')">SCC Information</a>
                </td>
                <td>Maintains SCC Entries for Standard City Service (US Only)</td>
                <td>CREQCMR.A11T0SCC</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/div_dept')">IBM Internal Division/Department</a>
                </td>
                <td>Maintains IBM Internal Division/Department mappings (US Only)</td>
                <td>CREQCMR.LOV</td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">Latin America</td>
                <td>&nbsp;</td>
              </tr>
			        <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/collectornodef')">LA Collector Numbers</a>
                </td>
                <td>Maintains Latin America Selectable Collector Numbers</td>
                <td>CREQCMR.COLLECTOR_NAME_NO</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/geocitieslistdef')">LA City Maintenance</a>
                </td>
                <td>Maintains Latin America Selectable City Values Under the Latin American State/Province</td>
                <td>CREQCMR.GEO_CITIES</td>
              </tr>  
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">Brazil</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/cnae')">BR CNAE Maintenance</a>
                </td>
                <td>Maintains CNAE and corresponding values for Brazil</td>
                <td>CMMA.REFT_BR_CNAE</td>
              </tr> 
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/brSboCollector')">BR SBO/Collector Maintenance</a>
                </td>
                <td>Maintains SBO/Collector and corresponding values for Brazil</td>
                <td>CREQCMR.REFT_BR_SBO_COLLECTOR</td>
              </tr> 
              <tr>
                <td>&nbsp;</td>
                <th class="subhead">Asia Pacific</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td><a href="javascript: goToUrl('${contextPath}/code/apClusterMap')">Cluster/ISU/Client Tier Mapping</a>
                </td>
                <td>Maintains coverage cycle changes by updating cluster mapping </td>
                <td>CREQCMR.AP_CUST_CLUSTER_TIER_MAP</td>
              </tr>
              </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
