package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.eu.VatLayerRequest;
import com.ibm.cmr.services.client.automation.eu.VatLayerResponse;

public class EUVatValidationElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(EUVatValidationElement.class);

  public EUVatValidationElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    LOG.debug("Entering EU Vat Validation Element");
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();

    GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    Addr zs01 = requestData.getAddress("ZS01");
    StringBuilder details = new StringBuilder();
    try {
      AutomationResponse<VatLayerResponse> response = getVatLayerInfo(admin, data, zs01);
      if (StringUtils.isBlank(data.getVat())) {
        validation.setSuccess(true);
        validation.setMessage("Execution done.");
        output.setDetails("No VAT specified on the request.");
        LOG.debug("No VAT specified on the request.");
      } else {
        if (response != null && response.isSuccess()) {
          if (response.getRecord().isValid()) {
            boolean addressMatch = isAddressMatched(handler, admin, data, zs01, response.getRecord());
            if (addressMatch) {
              validation.setSuccess(true);
              validation.setMessage("Execution done.");
              LOG.debug("Vat and company information verified through VAT Layer.");

              details.append("Vat and company information verified through VAT Layer.");
              details.append("\nCompany details from VAT Layer :");
              details.append(
                  "\nCompany Name = " + (StringUtils.isBlank(response.getRecord().getCompanyName()) ? "" : response.getRecord().getCompanyName()));
              // REQUIRES FIX
              // details.append("\nStreet = " +
              // (StringUtils.isBlank(response.getRecord().getStreet()) ? "" :
              // response.getRecord().getStreet()));
              // details.append("\nCity = " +
              // (StringUtils.isBlank(response.getRecord().getCity()) ? "" :
              // response.getRecord().getCity()));
              // details
              // .append("\nPostal Code = " +
              // (StringUtils.isBlank(response.getRecord().getPostalCode()) ? ""
              // :
              // response.getRecord().getPostalCode()));
              // details.append(
              // "\nCountry Name = " +
              // (StringUtils.isBlank(response.getRecord().getCountryName()) ?
              // ""
              // : response.getRecord().getCountryName()));
              // REQUIRES FIX -END
              details
                  .append("\nVAT number = " + (StringUtils.isBlank(response.getRecord().getVatNumber()) ? "" : response.getRecord().getVatNumber()));
              output.setDetails(details.toString());
              // admin.setCompVerifiedIndc("Y");
              // admin.setCompInfoSrc("VAT Layer");
              engineData.setCompanySource("VIES");
              updateEntity(admin, entityManager);
            } else {
              validation.setSuccess(false);
              validation.setMessage("Review needed.");
              output.setDetails("The registered company information is not the same as the one on the request.Need review.");
              output.setOnError(true);
              engineData.addRejectionComment("The registered company information is not the same as the one on the request.");
              LOG.debug("The registered company information is not the same as the one on the request.Need review.");
            }
          } else {
            validation.setSuccess(false);
            validation.setMessage("Review needed.");
            output.setDetails("Vat is invalid.Need review.");
            output.setOnError(true);
            engineData.addRejectionComment("Vat is invalid.");
            LOG.debug("Vat is invalid.Need review.");
          }
        } else {
          validation.setSuccess(false);
          validation.setMessage("Execution failed.");
          output.setDetails(response.getMessage());
          output.setOnError(true);
          engineData.addRejectionComment(response.getMessage());
          LOG.debug(response.getMessage());
        }
      }
    } finally {
      ChangeLogListener.clearManager();
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  private boolean isAddressMatched(GEOHandler handler, Admin admin, Data data, Addr addr, VatLayerResponse response) {
    boolean isMatched = true;
    /*
     * if (StringUtils.isNotBlank(getCustomerName(handler, admin, addr)) &&
     * StringUtils.isNotBlank(response.getCompanyName()) &&
     * Levenshtein.distance(getCustomerName(handler, admin, addr),
     * response.getCompanyName()) > 4) { isMatched = false; } String address =
     * addr.getAddrTxt() + (StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " +
     * addr.getAddrTxt2() : "");
     */
    // REQUIRES FIX
    // if (StringUtils.isNotBlank(address) &&
    // StringUtils.isNotBlank(response.getStreet()) &&
    // Levenshtein.distance(address, response.getStreet()) > 4) {
    // isMatched = false;
    // }
    // if (StringUtils.isNotBlank(addr.getPostCd()) &&
    // StringUtils.isNotBlank(response.getPostalCode())
    // && Levenshtein.distance(addr.getPostCd(), response.getPostalCode()) > 4)
    // {
    // isMatched = false;
    // }
    // if (StringUtils.isNotBlank(addr.getCity1()) &&
    // StringUtils.isNotBlank(response.getCity())
    // && Levenshtein.distance(addr.getCity1(), response.getCity()) > 4) {
    // isMatched = false;
    // }
    // REQUIRES FIX-END
    return isMatched;
  }

  private AutomationResponse<VatLayerResponse> getVatLayerInfo(Admin admin, Data data, Addr addr) throws Exception {
    AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    autoClient.setReadTimeout(1000 * 60 * 5);
    autoClient.setRequestMethod(Method.Get);

    VatLayerRequest request = new VatLayerRequest();
    request.setVat(data.getVat());
    // as France has sub-regions
    if (SystemLocation.FRANCE.equalsIgnoreCase(data.getCmrIssuingCntry()) && addr.getLandCntry() != null) {
      addr.setLandCntry("FR");
    }
    request.setCountry(StringUtils.isBlank(addr.getLandCntry()) ? "" : addr.getLandCntry());

    LOG.debug("Connecting to the EU VAT Layer Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.EU_VAT_SERVICE_ID, request, AutomationResponse.class);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(rawResponse);

    TypeReference<AutomationResponse<VatLayerResponse>> ref = new TypeReference<AutomationResponse<VatLayerResponse>>() {
    };
    return mapper.readValue(json, ref);
  }

  /**
   * returns concatenated customerName from admin or address as per country
   * settings
   * 
   * @param handler
   * @param admin
   * @param soldTo
   * @return
   */
  private String getCustomerName(GEOHandler handler, Admin admin, Addr soldTo) {
    String customerName = null;
    if (!handler.customerNamesOnAddress()) {
      customerName = admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2());
    } else {
      customerName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
    }
    return customerName;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.EU_VAT_VALIDATION;
  }

  @Override
  public String getProcessDesc() {
    return "EU - VAT Validation";
  }

}
