<%@page import="com.ibm.cio.cmr.request.model.automation.DuplicateCheckModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" href="${resourcesPath}/css/quick_search.css?${cmrv}"/>

<%
  boolean viewOnly = "Y".equals(request.getParameter("viewOnly"));
%>

<style> 
td.dnb-label {
  text-align: right;
  padding-right: 3px;
  font-weight: bold !important;
}

img.exp-col {
  width: 15px;
  height: 15px;
  cursor: pointer;
  vertical-align: sub;
}
div.ibm-columns {
  width:95%;
}
 div.use-m, div.use-b, div.use-i, div.use-s, div.use-e, div.use-l, div.use-gen {
   width:12px; 
   display: inline-block;
   border: 1px Solid #666;
   border-radius: 4px;
   padding-left: 2px;
   padding-right: 2px;
   margin-right: 2px;
   font-weight: bold;
   text-align: center;
   height: 18px;
   line-height: 18px;
   cursor: help;
   font-size: 10px;
 }
 div.use-m {
   background: rgb(255,171,87)
 }
 div.use-b {
   background: rgb(60,191,255)
 }
  div.use-i {
   background: rgb(128,255,128)
 }
 div.use-s {
   background: rgb(170,170,255)
 }
 div.use-e {
   background: rgb(255,174,174)
 }
 div.use-l {
   background: rgb(255,255,128)
 }
 div.use-gen {
   background: rgb(215,215,215)
 }

td.inner-det {
  border: none !important;
  padding: 2px !important;
}
span.bold {
  font-weight: bold;
}
</style>

<cmr:window>
  <div ng-app="LegacySearchApp">
    <div ng-controller="LegacyDetailsController">
    
    
      <!-- Customer -->
      <table cellspacing="0" cellpadding="0" border="0" summary="Customer Information" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <caption>
          <em>Customer Details (CMRTCUST)
            <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="expCust = true" ng-show="!expCust">
            <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="expCust = false" ng-show="expCust">
          </em>
        </caption>
        <tbody ng-show="expCust">
          <tr>
            <td class="dnb-label">SOF Country Code (RYAA):</td>
            <td ng-bind-html="cust.id.sofCntryCode"></td>
            <td class="dnb-label">Customer No. (RCUXA):</td>
            <td ng-bind-html="cust.id.customerNo"></td>
            <td class="dnb-label">Real Country Code (REALCTY):</td>
            <td ng-bind-html="cust.realCtyCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">Status (STATUS):</td>
            <td ng-bind-html="cust.status"></td>
            <td class="dnb-label">Abreviated Name (NCUXB):</td>
            <td ng-bind-html="cust.abbrevNm"></td>
            <td class="dnb-label">Abbreviated Location (LCUXB):</td>
            <td ng-bind-html="cust.abbrevLocn"></td>
          </tr>
          <tr>
            <td class="dnb-label">Location Number (RLCXB):</td>
            <td ng-bind-html="cust.locNo"></td>
            <td class="dnb-label">DC Repeat Agreement (CAGXB):</td>
            <td ng-bind-html="cust.dcRepeatAgreement"></td>
            <td class="dnb-label">CE Division (CCEDA):</td>
            <td ng-bind-html="cust.ceDivision"></td>
          </tr>
          <tr>
            <td class="dnb-label">Alliance Type(CCOXA):</td>
            <td ng-bind-html="cust.abbrevAllianceType"></td>
            <td class="dnb-label">Currency Code (CCRXA):</td>
            <td ng-bind-html="cust.currencyCd"></td>
            <td class="dnb-label">Education Allowance (CEAXC):</td>
            <td ng-bind-html="cust.educAllowance"></td>
          </tr>
          <tr>
            <td class="dnb-label">Leasing Company (CIEDC):</td>
            <td ng-bind-html="cust.leasingInd"></td>
            <td class="dnb-label">Authorised Remarketer (CIEXJ):</td>
            <td ng-bind-html="cust.authRemarketerInd"></td>
            <td class="dnb-label">Economic Code (CIYXB):</td>
            <td ng-bind-html="cust.economicCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">IMS Code (IMS):</td>
            <td ng-bind-html="cust.imsCd"></td>
            <td class="dnb-label">Language Code (CLGXA):</td>
            <td ng-bind-html="cust.langCd"></td>
            <td class="dnb-label">Department Code (CLGXC):</td>
            <td ng-bind-html="cust.deptCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">Overseas Territory (CLGXD):</td>
            <td ng-bind-html="cust.overseasTerritory"></td>
            <td class="dnb-label">MRC Code (CMKDA):</td>
            <td ng-bind-html="cust.mrcCd"></td>
            <td class="dnb-label">Mode of Payment (CPMXA):</td>
            <td ng-bind-html="cust.modeOfPayment"></td>
          </tr>
          <tr>
            <td class="dnb-label">ISU (CRGAA):</td>
            <td ng-bind-html="cust.isuCd"></td>
            <td class="dnb-label">Credit Code (CRDXA):</td>
            <td ng-bind-html="cust.creditCd"></td>
            <td class="dnb-label">Tax Code (CTXXA):</td>
            <td ng-bind-html="cust.taxCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">Engineering BO (CEBO):</td>
            <td ng-bind-html="cust.ceBo"></td>
            <td class="dnb-label">Sales BO (SBO):</td>
            <td ng-bind-html="cust.sbo"></td>
            <td class="dnb-label">Installing BO (IBO):</td>
            <td ng-bind-html="cust.ibo"></td>
          </tr>
          <tr>
            <td class="dnb-label">Sales Rep No. (REMXA):</td>
            <td ng-bind-html="cust.salesRepNo"></td>
            <td class="dnb-label">Sales Group (REMXD):</td>
            <td ng-bind-html="cust.salesGroupRep"></td>
            <td class="dnb-label">Enterprise No. (RENXA):</td>
            <td ng-bind-html="cust.enterpriseNo"></td>
          </tr>
          <tr>
            <td class="dnb-label">Bank Account No. (RABXA):</td>
            <td ng-bind-html="cust.bankAcctNo"></td>
            <td class="dnb-label">Bank Branch No. (RBBXA):</td>
            <td ng-bind-html="cust.bankBranchNo"></td>
            <td class="dnb-label">Bank No. (RBKXA):</td>
            <td ng-bind-html="cust.bankNo"></td>
          </tr>
          <tr>
            <td class="dnb-label">Collection Code (CCLXA):</td>
            <td ng-bind-html="cust.collectionCd"></td>
            <td class="dnb-label">Mailing Condition (CMLXA):</td>
            <td ng-bind-html="cust.mailingCond"></td>
            <td class="dnb-label">District Code (CRGAC):</td>
            <td ng-bind-html="cust.districtCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">Account Admin BO(RACBO):</td>
            <td ng-bind-html="cust.accAdminBo"></td>
            <td class="dnb-label">Type of Customer (CCUAI):</td>
            <td ng-bind-html="cust.custType"></td>
            <td class="dnb-label">Embargo Code (CEMXA):</td>
            <td ng-bind-html="cust.embargoCd"></td>
          </tr>
          <tr>
            <td class="dnb-label">ISIC (CIYXA):</td>
            <td ng-bind-html="cust.isicCd"></td>
            <td class="dnb-label">INAC (RAIXA):</td>
            <td ng-bind-html="cust.inacCd"></td>
            <td class="dnb-label">Tel No. or VAT (RTPNO):</td>
            <td ng-bind-html="cust.telNoOrVat"></td>
          </tr>
          <tr>
            <td class="dnb-label">VAT (VAT):</td>
            <td ng-bind-html="cust.vat"></td>
            <td class="dnb-label">Leading Account No. (RLAXA):</td>
            <td ng-bind-html="cust.leadingAccNo"></td>
            <td class="dnb-label">No. of Invoices (QUBXD):</td>
            <td ng-bind-html="cust.invoiceCpyReqd"></td>
          </tr>
          <tr>
            <td class="dnb-label">Create Timestamp (CREATE_TS):</td>
            <td ng-bind-html="cust.createTs"></td>
            <td class="dnb-label">Last Update Timestamp (UPDATE_TS):</td>
            <td ng-bind-html="cust.updateTs"></td>
            <td class="dnb-label">Last Update Date (Status) (UPDSTATUS_TS):</td>
            <td ng-bind-html="cust.updStatusTs"></td>
          </tr>
        </tbody>
      </table>
      
      
      <!-- Addresses -->
      <table cellspacing="0" cellpadding="0" border="0" summary="Addresses" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <caption>
          <em>Addresses (CMRTADDR)
            <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="expAddr = true" ng-show="!expAddr">
            <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="expAddr = false" ng-show="expAddr">
          </em>
        </caption>
        <thead ng-show="expAddr">
          <tr>
            <th scope="col" width="6%">Addr No.</th>
            <th scope="col" width="9%">Uses</th>
            <th scope="col" width="*">Address Lines (1-6)</th>
            <th scope="col" width="20%">Address Lines (I,N,O,T,U)</th>
            <th scope="col" width="45%">Other Details</th>
          </tr>
        </thead>
        <tbody ng-show="expAddr">
          <tr ng-repeat="addr in addresses">
            <td>{{addr.id.addrNo}}</td>
            <td ng-bind-html="formatUses(addr)"></td>
            <td style="font-size:11px">
              <span class="bold">1:</span> {{addr.addrLine1}}<br>
              <span class="bold">2:</span> {{addr.addrLine2}}<br>
              <span class="bold">3:</span> {{addr.addrLine3}}<br>
              <span class="bold">4:</span> {{addr.addrLine4}}<br>
              <span class="bold">5:</span> {{addr.addrLine5}}<br>
              <span class="bold">6:</span> {{addr.addrLine6}}<br>
            </td>
            <td style="font-size:11px">
              <span class="bold">I:</span> {{addr.addrLineI}}<br>
              <span class="bold">N:</span> {{addr.addrLineN}}<br>
              <span class="bold">O:</span> {{addr.addrLineO}}<br>
              <span class="bold">T</span>: {{addr.addrLineT}}<br>
              <span class="bold">U:</span> {{addr.addrLineU}}<br>
            </td>
            <td>
              <table>
                <tr ng-show="cust.isicCd != '9500'">
	                <td class="dnb-label inner-det" width="20%">Name:</td>
	                <td class="inner-det" ng-bind-html="addr.name" colspan="3"></td>
                </tr>
                <tr ng-show="cust.isicCd == '9500'">
                  <td class="dnb-label inner-det">Person Name:</td>
                  <td class="inner-det"colspan="3">
                    {{addr.title}} {{addr.firstName}} {{addr.lastName}}
                  </td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">Street:</td>
                  <td width="30%" class="inner-det" ng-bind-html="addr.street"></td>
                  <td width="20%" class="dnb-label inner-det">Street No.:</td>
                  <td width="30%" class="inner-det" ng-bind-html="addr.streetNo"></td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">City:</td>
                  <td class="inner-det" ng-bind-html="addr.city"></td>
                  <td class="dnb-label inner-det">Postal Code:</td>
                  <td class="inner-det" ng-bind-html="addr.zipCode"></td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">District:</td>
                  <td class="inner-det" ng-bind-html="addr.district"></td>
                  <td class="dnb-label inner-det">PO Box:</td>
                  <td class="inner-det" ng-bind-html="addr.poBox"></td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">Phone:</td>
                  <td class="inner-det" ng-bind-html="addr.addrPhone"></td>
                  <td class="dnb-label inner-det">Language:</td>
                  <td class="inner-det" ng-bind-html="addr.language"></td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">Contact:</td>
                  <td class="inner-det" ng-bind-html="addr.contact"></td>
                  <td class="dnb-label inner-det">&nbsp;</td>
                  <td class="inner-det">&nbsp;</td>
                </tr>
                <tr>
                  <td class="dnb-label inner-det">Company State/Province (Italy):</td>
                  <td class="inner-det" ng-bind-html="addr.itCompanyProvCd"></td>
                  <td class="dnb-label inner-det">Company Postal Address (Italy):</td>
                  <td class="inner-det" ng-bind-html="addr.itPostalAddrss"></td>
                </tr>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
      
      
      <!-- Extension -->
      <table ng-show="ext" cellspacing="0" cellpadding="0" border="0" summary="Extension" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <caption>
          <em>Customer Details Extension (CMRTCEXT)
            <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="expExt = true" ng-show="!expExt">
            <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="expExt = false" ng-show="expExt">
          </em>
        </caption>
        <tbody ng-show="expExt">
          <tr>
            <td class="dnb-label">Billing Address Name (NORMABB):</td>
            <td ng-bind-html="ext.itBillingName"></td>
            <td class="dnb-label">Billing Address Street (INDABB):</td>
            <td ng-bind-html="ext.itBillingStreet"></td>
            <td class="dnb-label">Billing Billing Address City(CITABB):</td>
            <td ng-bind-html="ext.itBillingCity"></td>
          </tr>
          <tr>
            <td class="dnb-label">Billing Customer No. (CODCC):</td>
            <td ng-bind-html="ext.itBillingCustomerNo"></td>
            <td class="dnb-label">Company Customer No. (CODCP):</td>
            <td ng-bind-html="ext.itCompanyCustomerNo"></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td class="dnb-label">Codice IVA (IVA):</td>
            <td ng-bind-html="ext.itIVA"></td>
            <td class="dnb-label">Codice Fiscale (CODFIS):</td>
            <td ng-bind-html="ext.iTaxCode"></td>
            <td class="dnb-label">SSV Code (CODSSV):</td>
            <td ng-bind-html="ext.itCodeSSV"></td>
          </tr>
          <tr>
            <td class="dnb-label">Tipo Client (TIPOCL):</td>
            <td ng-bind-html="ext.tipoCliente"></td>
            <td class="dnb-label">Codice Destinatio/Uficio (CODDES):</td>
            <td ng-bind-html="ext.coddes"></td>
            <td class="dnb-label">Ident Client (IDCLI):</td>
            <td ng-bind-html="ext.itIdentClient"></td>
          </tr>
          
          
          <tr>
            <td class="dnb-label">SIRET/SIREN (SIRET):</td>
            <td ng-bind-html="ext.siret"></td>
            <td class="dnb-label">Nature Client (CNCLA):</td>
            <td ng-bind-html="ext.frNatureClient"></td>
            <td class="dnb-label">INAC Cont. (IAN01):</td>
            <td ng-bind-html="ext.frInacCont"></td>
          </tr>
          <tr>
            <td class="dnb-label">Penalties De Retard (AGIOS):</td>
            <td ng-bind-html="ext.frPenalties"></td>
            <td class="dnb-label">Affacturage (AFFAC):</td>
            <td ng-bind-html="ext.frAffacturage"></td>
            <td class="dnb-label">Type De Facturation (CTECH):</td>
            <td ng-bind-html="ext.frFactureType"></td>
          </tr>
          <tr>
            <td class="dnb-label">Tarif Particulier (TAPAR):</td>
            <td ng-bind-html="ext.frTarif"></td>
            <td class="dnb-label">Nombre De Jours (IGSNJ):</td>
            <td ng-bind-html="ext.frNoOfDays"></td>
            <td class="dnb-label">Forme Juridique (TADMI):</td>
            <td ng-bind-html="ext.frLegalForm"></td>
          </tr>
          <tr>
            <td class="dnb-label">Code APE (TACPA):</td>
            <td ng-bind-html="ext.frCodeAPE"></td>
            <td class="dnb-label">Top Liste Speciale (TLSPE):</td>
            <td ng-bind-html="ext.frTopList"></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td class="dnb-label">DIC (DIC):</td>
            <td ng-bind-html="ext.bankAcctNo"></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td class="dnb-label">Tale Coverage Representative (TCOVREP):</td>
            <td ng-bind-html="ext.teleCovRep"></td>
            <td class="dnb-label">AECISUB Date CEMA (AECISUB):</td>
            <td ng-bind-html="ext.aeciSubDt"></td>
            <td class="dnb-label">Affiliate Branch (AFFILIATE):</td>
            <td ng-bind-html="ext.affiliate"></td>
          </tr>
          <tr>
            <td class="dnb-label">PEC Email (PEC):</td>
            <td ng-bind-html="ext.pec"></td>
            <td class="dnb-label">Private Email (INDEMAIL):</td>
            <td ng-bind-html="ext.indEmail"></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
        </tbody>
      </table>
      

      <!-- Links (to follow) -->
      
      <!-- RDC -->
      <table cellspacing="0" cellpadding="0" border="0" summary="RDC" class="ibm-data-table ibm-sortable-table ibm-alternating search-results">
        <caption>
          <em>RDC Records (KNA1)
            <img class="exp-col" title="Expand Details" src="${resourcesPath}/images/add.png" ng-click="expRdc = true" ng-show="!expRdc">
            <img class="exp-col" title="Collapse Details" src="${resourcesPath}/images/collapse2.png" ng-click="expRdc = false" ng-show="expRdc">
          </em>
        </caption>
        <thead ng-show="expRdc">
          <tr>
            <th scope="col" width="6%">MANDT</th>
            <th scope="col" width="10%">KUNNR</th>
            <th scope="col" width="7%">Issuing Country</th>
            <th scope="col" width="9%">CMR No.</th>
            <th scope="col" width="*">Name 1-4</th>
            <th scope="col" width="18%">Address</th>
            <th scope="col" width="8%">Addr Type</th>
            <th scope="col" width="8%">Addr Seq</th>
            <th scope="col" width="7%">Order Block</th>
            <th scope="col" width="9%">Create By / Date</th>
          </tr>
        </thead>
        <tbody ng-show="expRdc">
          <tr ng-repeat="rdc in rdcRecords">
            <td>{{rdc.id.mandt}}</td>
            <td>{{rdc.id.kunnr}}</td>
            <td>{{rdc.katr6}}</td>
            <td>{{rdc.zzkvCusno}}</td>
            <td style="font-size:11px">
              {{rdc.name1}}<br>
              {{rdc.name2}}<br>
              {{rdc.name3}}<br>
              {{rdc.name4}}
            </td>
            <td style="font-size:11px">
              {{rdc.stras}}<br>
              {{rdc.ort01}}<br>
              {{rdc.regio}}<br>
              {{rdc.land1}} {{rdc.pstlz}}
            </td>
            <td>{{rdc.ktokd}}</td>
            <td>{{rdc.zzkvSeqno}}</td>
            <td>{{rdc.aufsd}}</td>
            <td>
              {{rdc.ernam}}<br>
              {{rdc.erdat}}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <cmr:row>
  </cmr:row>
  <cmr:windowClose />
<script src="${resourcesPath}/js/legacy/legacysearch.js?${cmrv}"
  type="text/javascript"></script>
</cmr:window>
