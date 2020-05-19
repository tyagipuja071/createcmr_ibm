<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<cmr:section id="CUST_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />
  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="CN">
      <cmr:column span="2" containerForField="AbbrevName">
        <p>

          <label for="abbrevNm"> <cmr:fieldLabel fieldId="AbbrevName" />: <cmr:delta text="${rdcdata.abbrevNm}"
              oldValue="${reqentry.abbrevNm}" /> </label>
          <cmr:field fieldId="AbbrevName" id="abbrevNm" path="abbrevNm" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:row>
    <cmr:view exceptForGEO="CEMEA" exceptForCountry="848">
      <cmr:column span="2" containerForField="CustLang">
        <p>
          <cmr:label fieldId="custPrefLang">
            <cmr:fieldLabel fieldId="CustLang" />:
             <cmr:delta text="${rdcdata.custPrefLang}" oldValue="${reqentry.custPrefLang}" code="L" />
             <cmr:view forCountry="624">
                <cmr:info text="${ui.info.PostalBELUX}" />
              </cmr:view>
          </cmr:label>
          <cmr:field path="custPrefLang" id="custPrefLang" fieldId="CustLang" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>

    <cmr:view forCountry="618">
      <cmr:column span="2" containerForField="CustLang">
        <p>
          <cmr:label fieldId="custPrefLang">
            <cmr:fieldLabel fieldId="CustLang" />:
             <cmr:delta text="${rdcdata.custPrefLang}" oldValue="${reqentry.custPrefLang}" code="L" />          
          </cmr:label>
          <cmr:field path="custPrefLang" id="custPrefLang" fieldId="CustLang" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>

    <cmr:column span="2" containerForField="Subindustry">
      <p>
        <cmr:label fieldId="subIndustryCd">
          <cmr:fieldLabel fieldId="Subindustry" />:
            <cmr:delta text="${rdcdata.subIndustryCd}" oldValue="${reqentry.subIndustryCd}" code="L" />
        </cmr:label>
        <cmr:field path="subIndustryCd" id="subIndustryCd" fieldId="Subindustry" tabId="MAIN_CUST_TAB" size="500" placeHolder="Select Subindustry" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:view exceptForCountry="643,749,778,818,834,852,856,646,714,720,760">
      <cmr:column span="2" containerForField="SensitiveFlag">
        <p>
          <cmr:label fieldId="sensitiveFlag">
            <cmr:fieldLabel fieldId="SensitiveFlag" />:
             <cmr:delta text="${rdcdata.sensitiveFlag}" oldValue="${reqentry.sensitiveFlag}" />
          </cmr:label>
          <cmr:field path="sensitiveFlag" id="sensitiveFlag" fieldId="SensitiveFlag" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="JP">
      <cmr:column span="2" containerForField="JSICCd">
        <p>
          <cmr:label fieldId="jsicCd">
            <cmr:fieldLabel fieldId="JSICCd" />:
          </cmr:label>
         <cmr:field path="jsicCd" id="jsicCd" fieldId="JSICCd" tabId="MAIN_CUST_TAB" />
       </p>
     </cmr:column>
	</cmr:view>
    <cmr:column span="4" containerForField="ISIC" forCountry="897">
      <p>
        <label for="usSicmen"> <cmr:fieldLabel fieldId="ISIC" />: <cmr:delta text="${rdcdata.usSicmen}" oldValue="${reqentry.usSicmen}" />
        </label>
        <cmr:field path="usSicmen" id="usSicmen" fieldId="ISIC" tabId="MAIN_CUST_TAB" size="500" />
      </p>

    </cmr:column>
    <cmr:column span="4" containerForField="ISIC" exceptForCountry="897">
      <p>
        <label for="isicCd"> <cmr:fieldLabel fieldId="ISIC" />: <cmr:delta text="${rdcdata.isicCd}" oldValue="${reqentry.isicCd}" code="L" />
        </label>
        <cmr:field path="isicCd" id="isicCd" fieldId="ISIC" tabId="MAIN_CUST_TAB" size="500" />
      </p>

    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <!-- // 1164561 -->

    <cmr:view exceptForCountry="631,643,749,778,818,834,852,856,646,714,720,666,726,754,755,862,866,641,702,624,848" exceptForGEO="MCO,MCO1,MCO2,CEMEA,JP">
      <cmr:column span="2" containerForField="LocalTax1">
        <p>
          <label for="taxCd1"> <cmr:fieldLabel fieldId="LocalTax1" />: <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
            <cmr:view forCountry="706">
              <cmr:info text="${ui.info.sIRETFR}" />
            </cmr:view> <cmr:view forCountry="758">
              <cmr:info text="${ui.info.fiscalCdInfoIT}" />
            </cmr:view> </label>
          <cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax1" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>

    <cmr:view forCountry="897">
      <!-- // 1164558 -->
      <cmr:column span="2" containerForField="LocalTax3">
        <p>
          <label for="taxCd3"> <cmr:fieldLabel fieldId="LocalTax3" />: </label>
          <cmr:field path="taxCd3" id="taxCd3" fieldId="LocalTax3" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      
       <cmr:column span="2" containerForField="SpecialTaxCd">
        <p>
          <label for="specialTaxCd"> <cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
          <cmr:field path="specialTaxCd" id="specialTaxCd" fieldId="SpecialTaxCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>

  </cmr:row>

  <cmr:row addBackground="true">
    <!-- // 1164558 -->
    <cmr:view exceptForCountry="631,848" exceptForGEO="LA,JP">
      <cmr:view exceptForGEO="EMEA,AP,MCO1,MCO,MCO2,CEMEA,NORDX,BELUX,NL,CN">
        <cmr:column span="2" containerForField="LocalTax2">
          <p>
            <label for="taxCd2"> <cmr:fieldLabel fieldId="LocalTax2" />: <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd2}" />
            </label>
            <cmr:field path="taxCd2" id="taxCd2" fieldId="LocalTax2" tabId="MAIN_CUST_TAB" />
          </p>
        </cmr:column>
      </cmr:view>
      <cmr:view forCountry="897">
         <cmr:column span="2" containerForField="USSicmen">
        <p>
          <label for="isicCd">
            <%--
               <cmr:fieldLabel fieldId="USSicmen" />: <cmr:delta text="${rdcdata.isicCd}" oldValue="${reqentry.isicCd}"/>
             --%>
           </label>
          <cmr:field path="isicCd" id="isicCd" fieldId="USSicmen" tabId="MAIN_CUST_TAB"  size="500" />
        </p>
      </cmr:column>
      </cmr:view>
      <cmr:column span="2" containerForField="VAT" exceptForCountry="897">
        <p>
          <label for="vat"> <cmr:fieldLabel fieldId="VAT" />: <cmr:delta text="${rdcdata.vat}" oldValue="${reqentry.vat}" /> <cmr:view
              forCountry="755">
              <cmr:info text="${ui.info.vatNumberCodeFormat}" />
            </cmr:view> <cmr:view forCountry="726">
              <cmr:info text="${ui.info.vatNumberCodeFormatGR}" />
            </cmr:view> <cmr:view forCountry="666">
              <cmr:info text="${ui.info.vatNumberCodeFormatCY}" />
            </cmr:view> <cmr:view forCountry="862">
              <cmr:info text="${ui.info.vatNumberCodeFormatTR}" />
            </cmr:view> <cmr:view forCountry="822">
              <cmr:info text="${ui.info.vatNumberCodeFormatPT}" />
            </cmr:view> <cmr:view forCountry="838">
              <cmr:info text="${ui.info.vatNumberCodeFormatES}" />
            </cmr:view> <cmr:view forCountry="616">
              <cmr:info text="${ui.info.abnAU}" />
             </cmr:view> <cmr:view forCountry="706">
              <cmr:info text="${ui.info.vATFR}" />
            </cmr:view> <cmr:view forCountry="618">
              <cmr:info text="${ui.info.vatAT}" />
            </cmr:view> <cmr:view forCountry="846">
              <cmr:info text="${ui.info.vatSE}" />
            </cmr:view> <cmr:view forCountry="702">
              <cmr:info text="${ui.info.vatFIN}" />
            </cmr:view> <cmr:view forCountry="678">
              <cmr:info text="${ui.info.vatDEN}" />
            </cmr:view> <cmr:view forCountry="806">
              <cmr:info text="${ui.info.vatNO}" />
             </cmr:view><cmr:view forCountry="788">
              <cmr:info text="${ui.info.vatNL}" />
             </cmr:view> <cmr:view forCountry="624">
             <a id = 'vatInfoBubble'>
                <cmr:info text="${ui.info.vatBELUX}" />
                </a>
              </cmr:view>
          </label>
          <cmr:field path="vat" id="vat" fieldId="VAT" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:view forGEO="EMEA,MCO,CEMEA,NL,BELUX,NORDX">
        <cmr:column span="1" containerForField="VATExempt">
          <p>
            <cmr:label fieldId="vatExempt2">&nbsp;</cmr:label>
            <cmr:field fieldId="VATExempt" id="vatExempt" path="vatExempt" tabId="MAIN_CUST_TAB" />
            <cmr:label fieldId="vatExempt" forRadioOrCheckbox="true">
              <cmr:fieldLabel fieldId="VATExempt" />
              <cmr:delta text="${rdcdata.vatExempt}" oldValue="${reqentry.vatExempt == 'Y' ? 'Yes' : 'No'}" />
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:view>
    </cmr:view>
    <cmr:view forCountry="724,619">
      <c:if test="${reqentry.reqType != 'U'}">
        <cmr:column span="1" containerForField="VATExempt">
          <p>
            <cmr:label fieldId="vatExempt2">&nbsp;</cmr:label>
            <cmr:field fieldId="VATExempt" id="vatExempt" path="vatExempt" tabId="MAIN_CUST_TAB" />
            <cmr:label fieldId="vatExempt" forRadioOrCheckbox="true">
              <cmr:fieldLabel fieldId="VATExempt" />
              <cmr:delta text="${rdcdata.vatExempt}" oldValue="${reqentry.vatExempt == 'Y' ? 'Yes' : 'No'}" />
            </cmr:label>
          </p>
        </cmr:column>
      </c:if>
    </cmr:view>

  </cmr:row>

  <!-- Include Here Customer Specific fields for GEOs -->

  <!--  US fields -->
  <jsp:include page="US/us_customer.jsp" />

  <!--  LA fields -->
  <jsp:include page="LA/la_customer.jsp" />

  <!--  EMEA fields -->
  <jsp:include page="EMEA/emea_customer.jsp" />

  <!--  CND fields -->
  <jsp:include page="CND/cnd_customer.jsp" />

  <!--  DE fields -->
  <jsp:include page="DE/de_customer.jsp" />

  <!--  CN fields -->
  <jsp:include page="CN/cn_customer.jsp" />

  <!--  AP fields -->
  <jsp:include page="AP/ap_customer.jsp" />

  <!--  MCO fields -->
  <jsp:include page="MCO/mco_customer.jsp" />

  <!--  FR fields -->
  <jsp:include page="FR/fr_customer.jsp" />

  <!--  JP fields -->
  <jsp:include page="JP/jp_customer.jsp" />

  <!--  CEMEA fields -->
  <jsp:include page="CEMEA/cemea_customer.jsp" />

  <!--  NORDX fields -->
  <jsp:include page="NORDX/nordx_customer.jsp" />

  <!--  BELUX fields -->
  <jsp:include page="BELUX/belux_customer.jsp" />

  <!--  NL fields -->
  <jsp:include page="NL/nl_customer.jsp" />
  
   <!--  SWISS fields -->
    <jsp:include page="SWISS/ch_customer.jsp" />


</cmr:section>