package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.cmr.DuplicateCMRCheckResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;

public class UKIUtil extends AutomationUtil {
	private static final Logger LOG = Logger.getLogger(UKIUtil.class);
	public static final String SCENARIO_BUSINESS_PARTNER = "BUSPR";
	public static final String SCENARIO_COMMERCIAL = "COMME";
	public static final String SCENARIO_GOVERNMENT = "GOVRN";
	public static final String SCENARIO_DATACENTER = "DC";
	public static final String SCENARIO_IGF = "IGF";
	public static final String SCENARIO_INTERNAL_FSL = "INFSL";
	public static final String SCENARIO_INTERNAL = "INTER";
	public static final String SCENARIO_PRIVATE_PERSON = "PRICU";
	public static final String SCENARIO_THIRD_PARTY = "THDPT";
	public static final String SCENARIO_CROSSBORDER = "CROSS";
	public static final String SCENARIO_CROSS_GOVERNMENT = "XGOVR";
	public static final String SCENARIO_CROSS_IGF = "XIGF";
	public static final String SCENARIO_IBM_EMPLOYEE = "IBMEM";
	private static final List<String> SCENARIOS_TO_SKIP_COVERAGE = Arrays.asList(SCENARIO_INTERNAL,
			SCENARIO_PRIVATE_PERSON, SCENARIO_BUSINESS_PARTNER);
	private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO,
			CmrConstants.RDC_BILL_TO, CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO,
			CmrConstants.RDC_SECONDARY_SOLD_TO, CmrConstants.RDC_PAYGO_BILLING);
	private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Attn", "Phone #", "Hardware Master");
	private static final List<String> SCOTLAND_POST_CD = Arrays.asList("AB", "KA", "DD", "KW", "DG", "KY", "EH", "ML",
			"FK", "PA", "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "PH", "TD", "IV");
	public static final String NORTHERN_IRELAND_POST_CD = "BT";
	public static boolean covCalculatedFromRdc = false;

	@Override
	public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData,
			AutomationEngineData engineData, AutomationResult<ValidationOutput> result, StringBuilder details,
			ValidationOutput output) {
		Data data = requestData.getData();
		Admin admin = requestData.getAdmin();
		Addr zs01 = requestData.getAddress("ZS01");
		String custNm1 = zs01.getCustNm1();
		String custNm2 = !StringUtils.isBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "";
		String customerName = custNm1 + custNm2;
		String scenario = data.getCustSubGrp();

		Addr zi01 = requestData.getAddress("ZI01");
		custNm1 = zi01.getCustNm1();
		custNm2 = !StringUtils.isBlank(zi01.getCustNm2()) ? " " + zi01.getCustNm2() : "";
		String customerNameZI01 = custNm1 + custNm2;
		String custGrp = data.getCustGrp();
		// CREATCMR-6244 LandCntry UK(GB)
		if (zs01 != null) {
			String landCntry = zs01.getLandCntry();
			if (data.getVat() != null && !data.getVat().isEmpty() && landCntry.equals("GB")
					&& !data.getCmrIssuingCntry().equals("866") && custGrp != null && StringUtils.isNotEmpty(custGrp)
					&& ("CROSS".equals(custGrp))) {
				engineData.addNegativeCheckStatus("_vatUK",
						" request need to be send to CMDE queue for further review. ");
				details.append("Landed Country UK. The request need to be send to CMDE queue for further review.\n");
			}
		}
		if (StringUtils.isBlank(scenario)) {
			details.append("Scenario not correctly specified on the request");
			engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
			return true;
		}
		LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
		LOG.debug("Scenario to check: " + scenario);
		if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario)
				|| SCENARIO_PRIVATE_PERSON.equals(scenario))
				&& (!customerName.toUpperCase().equals(customerNameZI01.toUpperCase())
						|| customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$"))) {
			details.append(
					"This request cannot be processed as 'Commercial' scenario sub-type because 'Customer name' field is not the same in all address sequences. Even the smallest difference or typo mistake can cause that the sequences will be considered as of different entities."
							+ " \n"
							+ "If two different entities are needed in 'Billing' and 'Installing' sequences, please change the scenario sub-type to 'Third-party'."
							+ " \n"
							+ "If 'Billing' and 'Installing' should be the same entity in your CMR, please select 'Commercial' sub-type, and double-check all the 'Customer name' fields.")
					.append("\n");
			engineData.addRejectionComment("OTH",
					"This request cannot be processed as 'Commercial' scenario sub-type because 'Customer name' field is not the same in all address sequences. Even the smallest difference or typo mistake can cause that the sequences will be considered as of different entities."
							+ " \n"
							+ "If two different entities are needed in 'Billing' and 'Installing' sequences, please change the scenario sub-type to 'Third-party'."
							+ " \n"
							+ "If 'Billing' and 'Installing' should be the same entity in your CMR, please select 'Commercial' sub-type, and double-check all the 'Customer name' fields.",
					"", "");
			return false;
		} else if ((SCENARIO_COMMERCIAL.equals(scenario) || SCENARIO_GOVERNMENT.equals(scenario)
				|| SCENARIO_CROSSBORDER.equals(scenario) || SCENARIO_CROSS_GOVERNMENT.equals(scenario))
				&& !addressEquals(zs01, zi01)) {
			details.append(
					"Billing and Installing addresses are not same. Request will require CMDE review before proceeding.")
					.append("\n");
			engineData.addNegativeCheckStatus("BILL_INSTALL_DIFF", "Billing and Installing addresses are not same.");
		}

		if (!(SCENARIO_PRIVATE_PERSON.equals(scenario) || SCENARIO_IBM_EMPLOYEE.equals(scenario)
				|| "CROSS".equals(data.getCustGrp()) || SCENARIO_INTERNAL_FSL.equals(scenario)
				|| SCENARIO_INTERNAL.equals(scenario)) && "Y".equals(data.getRestrictInd())) {
			details.append("Request has been marked as CRN Exempt. Processor Review will be required.\n");
			engineData.addNegativeCheckStatus("_crnExempt", "Request has been marked as CRN Exempt.");
		}

		if (SCENARIOS_TO_SKIP_COVERAGE.contains(scenario)) {
			engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
			engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_COVERAGE);
		}

		switch (scenario) {
		case SCENARIO_BUSINESS_PARTNER:
			return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
		case SCENARIO_PRIVATE_PERSON:
			return doPrivatePersonChecks(engineData, data.getCmrIssuingCntry(), zs01.getLandCntry(), customerName,
					details, false);
		case SCENARIO_INTERNAL:
			if (!customerName.contains("IBM") && !customerNameZI01.contains("IBM")) {
				details.append("Mailing and Billing addresses should have IBM in them.");
				engineData.addRejectionComment("OTH", "Mailing and Billing addresses should have IBM in them.", "", "");
				return false;
			}
			break;
		case SCENARIO_THIRD_PARTY:
			if (customerName.toUpperCase().equals(customerNameZI01.toUpperCase())
					&& !customerNameZI01.toUpperCase().matches("^VR[0-9]{3}.+$")) {
				details.append("The request does not meet the criteria for Third Party Scenario.").append("\n");
				engineData.addRejectionComment("OTH",
						"The request does not meet the criteria for Third Party Scenario.", "", "");
				return false;
			}
			break;
		case SCENARIO_DATACENTER:
			if (customerName.toUpperCase().equals(customerNameZI01.toUpperCase())) {
				details.append(
						"Customer Names on installing and billing address should be different for Data Center Scenario")
						.append("\n");
				engineData.addRejectionComment("OTH",
						"Customer Names on installing and billing address should be different for Data Center Scenario",
						"", "");
				return false;
			} else if (!customerNameZI01.toUpperCase().contains("DATACENTER")
					&& !customerNameZI01.toUpperCase().contains("DATA CENTER")
					&& !customerNameZI01.toUpperCase().contains("DATACENTRE")
					&& !customerNameZI01.toUpperCase().contains("DATA CENTRE")) {
				details.append("The request does not meet the criteria for Data Center Scenario.").append("\n");
				engineData.addRejectionComment("OTH",
						"The request does not meet the criteria for Data Center Scenario.", "", "");
				return false;
			}
			break;
		case SCENARIO_IGF:
		case SCENARIO_CROSS_IGF:
			boolean requesterFromTeam = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(),
					SystemParameters.getList("UKI.SKIP_SCENARIO"));
			if (!requesterFromTeam) {
				details.append("Requester is not allowed to submit the request for IGF Scenario.").append("\n");
				engineData.addRejectionComment("OTH",
						"Requester is not allowed to submit the request for IGF Scenario.", "", "");
				return false;
			}
		case SCENARIO_IBM_EMPLOYEE:
			Person person = null;
			if (StringUtils.isNotBlank(zs01.getCustNm1())) {
				try {
					String mainCustName = zs01.getCustNm1()
							+ (StringUtils.isNotBlank(zs01.getCustNm2()) ? " " + zs01.getCustNm2() : "");
					person = BluePagesHelper.getPersonByName(mainCustName, data.getCmrIssuingCntry());
					if (person == null) {
						engineData.addRejectionComment("OTH", "Employee details not found in IBM BluePages.", "", "");
						details.append("Employee details not found in IBM BluePages.").append("\n");
						return false;
					} else {
						details.append("Employee details validated with IBM BluePages for " + person.getName() + "("
								+ person.getEmail() + ").").append("\n");
					}
				} catch (Exception e) {
					LOG.error("Not able to check name against bluepages", e);
					engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED",
							"Not able to check name against bluepages for scenario IBM Employee.");
					return false;
				}
			} else {
				LOG.warn("Not able to check name against bluepages, Customer Name 1 not found on the main address");
				engineData.addNegativeCheckStatus("BLUEPAGES_NOT_VALIDATED",
						"Customer Name 1 not found on the main address");
				return false;
			}
			break;
		}
		return true;

	}

	@Override
	protected List<String> getCountryLegalEndings() {
		return Arrays.asList("LLP", "LTD", "Ltd.", "CIC", "CIO", "Cyf", "CCC", "Unltd.", "Ultd.");
	}

	@Override
	public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData,
			RequestData requestData, RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
			ValidationOutput validation) throws Exception {
		Admin admin = requestData.getAdmin();
		Data data = requestData.getData();
		Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
		if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
			return true;
		}
		StringBuilder details = new StringBuilder();
		boolean cmdeReview = false;
		int coverageFieldUpdtd = 0;
		Set<String> resultCodes = new HashSet<String>();// D for Reject
		List<String> ignoredUpdates = new ArrayList<String>();
		for (UpdatedDataModel change : changes.getDataUpdates()) {
			switch (change.getDataField()) {
			case "Company Registration Number":
				if (!StringUtils.isBlank(change.getNewData()) && !(change.getNewData().equals(change.getOldData()))) {
					// UPDATE
					// Addr soldTo =
					// requestData.getAddress(CmrConstants.RDC_SOLD_TO);
					List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
					boolean matchesDnb = false;
					if (matches != null) {
						// check against D&B
						matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
					}
					if (!matchesDnb) {
						// resultCodes.add("R"); // commenting because of
						// CMR-7134
						cmdeReview = true;
						details.append("Company Registration Number on the request did not match D&B\n");
					} else {
						details.append("Company Registration Number on the request matches D&B\n");
					}
				} else if (!StringUtils.isBlank(change.getOldData()) && StringUtils.isBlank(change.getNewData())) {
					cmdeReview = true;
					details.append("Company Registration Number removed from the request.\n");
				}
				break;
			case "ISIC":
				cmdeReview = true;
				break;
			case "Tax Code":
				// noop, for switch handling only
				break;
			case "VAT #":
				if (requestData.getAddress("ZS01").getLandCntry().equals("GB")
						&& !data.getCmrIssuingCntry().equals("866")) {
					if (!AutomationUtil.isTaxManagerEmeaUpdateCheck(entityManager, engineData, requestData)) {
						engineData.addNegativeCheckStatus("_vatUK",
								" request need to be send to CMDE queue for further review. ");
						details.append(
								"Landed Country UK. The request need to be send to CMDE queue for further review.\n");
					}
				} else {
					if (!StringUtils.isBlank(change.getNewData())) {
						soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
						List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
						boolean matchesDnb = false;
						if (matches != null) {
							// check against D&B
							matchesDnb = ifaddressCloselyMatchesDnb(matches, soldTo, admin, data.getCmrIssuingCntry());
						}
						if (!matchesDnb) {
							cmdeReview = true;
							engineData.addNegativeCheckStatus("_atVATCheckFailed",
									"VAT # on the request did not match D&B");
							details.append("VAT # on the request did not match D&B\n");
						} else {
							details.append("VAT # on the request matches D&B\n");
						}
					}
				}
				break;
			case "INAC/NAC Code":
			case "ISU Code":
			case "Client Tier":
			case "SBO":
			case "Sales Rep No":
			case "Company Number":
				coverageFieldUpdtd++;
				break;
			case "Enterprise Number":
				// noop, for switch handling only
				break;
			case "Order Block Code":
				if ("C".equals(change.getOldData()) || "C".equals(change.getNewData())
						|| "E".equals(change.getOldData()) || "E".equals(change.getNewData())) {
					// noop, for switch handling only
				}
				break;
			default:
				ignoredUpdates.add(change.getDataField());
				break;
			}
		}

		if (coverageFieldUpdtd > 0) {
			List<String> managerID = SystemParameters.getList("ES_UKI_MGR_COV_UPDT");
			boolean managerCheck = BluePagesHelper.isBluePagesHeirarchyManager(admin.getRequesterId(), managerID);
			if (!managerCheck) {
				if (changes.isDataChanged("INAC/NAC Code") || changes.isDataChanged("Company Number")) {
					cmdeReview = true;
					admin.setScenarioVerifiedIndc("Y");
				} else {
					details.append("Updates to coverage fields cannot be validated. An Approval wil be required.\n");
					admin.setScenarioVerifiedIndc("N");
				}
			} else {
				admin.setScenarioVerifiedIndc("Y");
				details.append("Skipping validation for coverage fields update for requester - "
						+ admin.getRequesterId() + ".\n");
			}
		}

		if (resultCodes.contains("R")) {
			output.setOnError(true);
			validation.setSuccess(false);
			validation.setMessage("Rejected");
		} else if (cmdeReview) {
			engineData.addNegativeCheckStatus("_esDataCheckFailed",
					"Updates to one or more fields cannot be validated.");
			details.append("Updates to one or more fields cannot be validated.\n");
			validation.setSuccess(false);
			validation.setMessage("Not Validated");
		} else {
			validation.setSuccess(true);
			validation.setMessage("Successful");
		}
		if (!ignoredUpdates.isEmpty()) {
			details.append("Updates to the following fields skipped validation:\n");
			for (String field : ignoredUpdates) {
				details.append(" - " + field + "\n");
			}
		}
		output.setDetails(details.toString());
		output.setProcessOutput(validation);
		return true;
	}

	@Override
	public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData,
			RequestData requestData, RequestChangeContainer changes, AutomationResult<ValidationOutput> output,
			ValidationOutput validation) throws Exception {
		Admin admin = requestData.getAdmin();
		Data data = requestData.getData();
		if (handlePrivatePersonRecord(entityManager, admin, output, validation, engineData)) {
			return true;
		}
		List<Addr> addresses = null;
		int zi01count = 0;
		int zp01count = 0;
		int zd01count = 0;
		List<Integer> addrCount = getAddressCount(entityManager, SystemLocation.UNITED_KINGDOM,
				data.getCmrIssuingCntry(), data.getCmrNo());
		zi01count = addrCount.get(0);
		zp01count = addrCount.get(1);
		zd01count = addrCount.get(2);
		LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
		boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
		boolean isOnlyPayGoUpdated = changes != null && changes.isAddressChanged("PG01")
				&& !changes.isAddressChanged("ZS01") && !changes.isAddressChanged("ZI01");

		StringBuilder checkDetails = new StringBuilder();
		Set<String> resultCodes = new HashSet<String>();// R - review
		for (String addrType : RELEVANT_ADDRESSES) {
			if (changes.isAddressChanged(addrType)) {
				if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
					addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
				} else {
					addresses = requestData.getAddresses(addrType);
				}
				for (Addr addr : addresses) {
					List<String> addrTypesChanged = new ArrayList<String>();
					for (UpdatedNameAddrModel addrModel : changes.getAddressUpdates()) {
						if (!addrTypesChanged.contains(addrModel.getAddrTypeCode())) {
							addrTypesChanged.add(addrModel.getAddrTypeCode());
						}
					}
					boolean isZS01WithAufsdPG = (CmrConstants.RDC_SOLD_TO.equals(addrType)
							&& "PG".equals(data.getOrdBlk()));
					if ("N".equals(addr.getImportInd())) {
						// new address
						// CREATCMR-6586 checking duplicate for all addresses
						if (CmrConstants.RDC_BILL_TO.equals(addrType)
								|| CmrConstants.RDC_SECONDARY_SOLD_TO.equals(addrType)) {
							LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
							checkDetails.append("Addition of new Mailing and EPL (" + addr.getId().getAddrSeq()
									+ ") address skipped in the checks.\n");
						} else if (((zi01count == 0 && CmrConstants.RDC_INSTALL_AT.equals(addrType))
								|| (zd01count == 0 && CmrConstants.RDC_SHIP_TO.equals(addrType)))
								&& null == changes.getAddressChange(addrType, "Customer Name")
								&& null == changes.getAddressChange(addrType, "Customer Name Con't")) {
							LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
							checkDetails.append("Addition of new "
									+ (addrType.equalsIgnoreCase("ZI01") ? "Installing " : "Shipping ") + "("
									+ addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
						} else if (addressExists(entityManager, addr, requestData)) {
							LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
							checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
									+ ") provided matches an existing address.\n");
							resultCodes.add("R");
						} else {
							LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
							checkDetails
									.append("Addition of new address (" + addr.getId().getAddrSeq() + ") validated.\n");
						}
					} else if ("Y".equals(addr.getChangedIndc())) {
						// update address
						if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
							if (isRelevantAddressFieldUpdated(changes, addr)) {
								// CMDE Review
								checkDetails.append("Updates to address fields for " + addrType + "("
										+ addr.getId().getAddrSeq() + ") need to be verified.").append("\n");
								resultCodes.add("D");
							}
						} else if ((payGoAddredited
								&& addrTypesChanged.contains(CmrConstants.RDC_PAYGO_BILLING.toString()))
								|| isZS01WithAufsdPG) {
							if ("N".equals(addr.getImportInd())) {
								LOG.debug(
										"Checking duplicates for " + addrType + "(" + addr.getId().getAddrSeq() + ")");
								boolean duplicate = addressExists(entityManager, addr, requestData);
								if (duplicate) {
									LOG.debug(" - Duplicates found for " + addrType + "(" + addr.getId().getAddrSeq()
											+ ")");
									checkDetails.append("Address " + addrType + "(" + addr.getId().getAddrSeq()
											+ ") provided matches an existing Bill-To address.\n");
									resultCodes.add("D");
								} else {
									LOG.debug(" - NO duplicates found for " + addrType + "(" + addr.getId().getAddrSeq()
											+ ")");
									checkDetails.append(" - NO duplicates found for " + addrType + "("
											+ addr.getId().getAddrSeq() + ")" + "with same attentionTo");
									checkDetails.append("Updates to address fields for" + addrType + "("
											+ addr.getId().getAddrSeq() + ")  validated in the checks.\n");
								}
							} else {
								checkDetails.append("Updates to address fields for" + addrType + "("
										+ addr.getId().getAddrSeq() + ") validated in the checks.\n");
							}
						} else {
							checkDetails.append("Updates to non-address fields for " + addrType + "("
									+ addr.getId().getAddrSeq() + ") skipped in the checks.").append("\n");
						}

					} else if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
						if (isRelevantAddressFieldUpdated(changes, addr)) {
							Addr soldTo = requestData.getAddress(CmrConstants.RDC_SOLD_TO);
							List<DnBMatchingResponse> matches = getMatches(requestData, engineData, soldTo, true);
							boolean matchesDnb = false;
							if (matches != null) {
								// check against D&B
								matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin,
										data.getCmrIssuingCntry());
							}
							if (!matchesDnb) {
								LOG.debug("Update address for " + addrType + "(" + addr.getId().getAddrSeq()
										+ ") does not match D&B");
								resultCodes.add("D");
								checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq()
										+ ") did not match D&B records.\n");
							} else {
								checkDetails.append("Update address " + addrType + "(" + addr.getId().getAddrSeq()
										+ ") matches D&B records. Matches:\n");
								for (DnBMatchingResponse dnb : matches) {
									checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
									checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
									checkDetails.append(
											" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " "
													+ dnb.getDnbPostalCode() + " " + dnb.getDnbCountry() + "\n\n");
								}
							}
						} else {
							checkDetails.append("Updates to non-address fields for " + addrType + "("
									+ addr.getId().getAddrSeq() + ") skipped in the checks.").append("\n");
						}
					} else {
						// proceed
						LOG.debug("Update to Address " + addrType + "(" + addr.getId().getAddrSeq()
								+ ") skipped in the checks.\\n");
						checkDetails.append(
								"Updates to Address (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
					}
				}
			}
		}

		if (resultCodes.contains("R")) {
			output.setOnError(true);
			engineData.addRejectionComment("DUPADDR", "Add or update on the address is rejected", "", "");
			validation.setSuccess(false);
			validation.setMessage("Rejected");
		} else if (resultCodes.contains("D")) {
			validation.setSuccess(false);
			validation.setMessage("Not Validated");
			engineData.addNegativeCheckStatus("_atCheckFailed",
					"Updates to addresses cannot be checked automatically.");
		} else {
			validation.setSuccess(true);
			validation.setMessage("Successful");
		}
		String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
		details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
		output.setDetails(details);
		output.setProcessOutput(validation);
		return true;
	}

	/**
	 * Checks if relevant fields were updated
	 * 
	 * @param changes
	 * @param addr
	 * @return
	 */
	private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
		List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(),
				addr.getId().getAddrSeq());
		if (addrChanges == null) {
			return false;
		}
		for (UpdatedNameAddrModel change : addrChanges) {
			if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager,
			AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides,
			RequestData requestData, AutomationEngineData engineData) throws Exception {

		Admin admin = requestData.getAdmin();
		Data data = requestData.getData();
		String scenario = data.getCustSubGrp();
		if (!"C".equals(admin.getReqType())) {
			details.append("Field Computation skipped for Updates.");
			results.setResults("Skipped");
			results.setDetails(details.toString());
			return results;
		}

		String isicCd = data.getIsicCd();

		if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario)) {
			Addr zi01 = requestData.getAddress("ZI01");
			boolean highQualityMatchExists = false;
			List<DnBMatchingResponse> response = getMatches(requestData, engineData, zi01, false);
			if (response != null && response.size() > 0) {
				// actions to be performed only when matches with high
				// confidence are
				// found
				String custNmTrimmed = getCustomerFullName(zi01);
				if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}\\.+$")
						|| custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}/.+$")) {
					custNmTrimmed = custNmTrimmed.substring(6);
				} else if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}.+$")) {
					custNmTrimmed = custNmTrimmed.substring(5);
				}

				for (DnBMatchingResponse dnbRecord : response) {
					boolean closelyMatches = DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), zi01, admin,
							dnbRecord, custNmTrimmed, false);
					if (closelyMatches) {
						engineData.put("ZI01_DNB_MATCH", dnbRecord);
						highQualityMatchExists = true;
						details.append("High Quality DnB Match found for Installing address.\n");
						details.append("Overriding ISIC and Sub Industry Code using DnB Match retrieved.\n");
						LOG.debug("Connecting to D&B details service..");
						DnBCompany dnbData = DnBUtil.getDnBDetails(dnbRecord.getDunsNo());
						if (dnbData != null) {
							overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD",
									data.getIsicCd(), dnbData.getIbmIsic());
							details.append("ISIC =  " + dnbData.getIbmIsic() + " (" + dnbData.getIbmIsicDesc() + ")")
									.append("\n");
							isicCd = dnbData.getIbmIsic();
							String subInd = RequestUtils.getSubIndustryCd(entityManager, dnbData.getIbmIsic(),
									data.getCmrIssuingCntry());
							if (subInd != null) {
								overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA",
										"SUB_INDUSTRY_CD", data.getSubIndustryCd(), subInd);
								details.append("Subindustry Code  =  " + subInd).append("\n");
							}
						}
						if (SCENARIO_INTERNAL_FSL.equals(scenario) && dnbRecord.getOrgIdDetails() != null) {
							String crn = DnBUtil.getTaxCode1(dnbRecord.getDnbCountry(), dnbRecord.getOrgIdDetails());
							if (StringUtils.isNotBlank(crn)) {
								details.append("Overriding CRN to \'" + crn + "\'\n");
								overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "TAX_CD1",
										data.getTaxCd1(), crn);
							}
						}
						results.setResults("Calculated.");
						results.setProcessOutput(overrides);
						break;
					}
				}
			}
			if (!highQualityMatchExists && "C".equals(admin.getReqType())) {
				details.append("No high quality matches found for Installing Address, setting ISIC to 7499.");
				overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISIC_CD", data.getIsicCd(),
						"7499");
				isicCd = "7499";
				String subInd = RequestUtils.getSubIndustryCd(entityManager, "7499", data.getCmrIssuingCntry());
				if (subInd != null) {
					overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SUB_INDUSTRY_CD",
							data.getSubIndustryCd(), subInd);
				}
				engineData.addPositiveCheckStatus(AutomationEngineData.SKIP_GBG);
				results.setResults("Calculated.");
				results.setProcessOutput(overrides);
			}
		} else if (SCENARIO_INTERNAL.equalsIgnoreCase(scenario)) {
			// check value of Department under '##UKIInternalDepartment' LOV
			String sql = ExternalizedQuery.getSql("UKI.CHECK_DEPARTMENT");
			PreparedQuery query = new PreparedQuery(entityManager, sql);
			String dept = data.getIbmDeptCostCenter();
			if (StringUtils.isNotBlank(dept)) {
				query.setParameter("CD", dept);
				query.setParameter("CNTRY", data.getCmrIssuingCntry());
				String result = query.getSingleResult(String.class);
				if (result == null) {
					engineData.addRejectionComment("OTH", "IBM Department/Cost Center on the request is invalid.", "",
							"");
					details.append("IBM Department/Cost Center on the request is invalid.").append("\n");
				} else {
					details.append("IBM Department/Cost Center " + dept + " validated successfully.").append("\n");
					results.setResults("IBM Department/Cost Center " + dept + " validated successfully.");
				}
			}
		}

		List<String> isicList = Arrays.asList("7230", "7240", "7290", "7210", "7221", "7229", "7250", "7123", "9802");
		if (!(SCENARIO_INTERNAL.equals(scenario) || SCENARIO_PRIVATE_PERSON.equals(scenario)
				|| SCENARIO_BUSINESS_PARTNER.equals(scenario))) {
			if ("32".equals(data.getIsuCd()) && "S".equals(data.getClientTier()) && StringUtils.isNotBlank(isicCd)
					&& isicList.contains(isicCd)) {
				details.append("Setting ISU-CTC to '32N' for ISIC: " + isicCd).append("\n");
				overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(),
						"32");
				overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER",
						data.getClientTier(), "N");
				results.setProcessOutput(overrides);
				results.setResults("Calculated.");
			} else if ("32".equals(data.getIsuCd()) && "N".equals(data.getClientTier())
					&& StringUtils.isNotBlank(isicCd) && !isicList.contains(isicCd)) {
				details.append("Setting ISU-CTC to '32S' for ISIC: " + isicCd).append("\n");
				overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(),
						"32");
				overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER",
						data.getClientTier(), "S");
				results.setProcessOutput(overrides);
				results.setResults("Calculated.");
			}
		}

		if (details.toString().length() == 0) {
			details.append("No specific fields to calculate.");
			results.setResults("Skipped.");
			results.setProcessOutput(overrides);
		}

		results.setDetails(details.toString());
		LOG.debug(results.getDetails());
		return results;
	}

	@Override
	public void filterDuplicateCMRMatches(EntityManager entityManager, RequestData requestData,
			AutomationEngineData engineData, MatchingResponse<DuplicateCMRCheckResponse> response) {
		String scenario = requestData.getData().getCustSubGrp();
		String[] custClassValuesToCheck = { "43", "45", "46" };
		if (UKIUtil.SCENARIO_BUSINESS_PARTNER.equals(scenario)) {
			List<DuplicateCMRCheckResponse> matches = response.getMatches();
			List<DuplicateCMRCheckResponse> filteredMatches = new ArrayList<DuplicateCMRCheckResponse>();
			for (DuplicateCMRCheckResponse match : matches) {
				if (match.getCmrNo() != null && match.getCmrNo().startsWith("P") && "75".equals(match.getOrderBlk())) {
					filteredMatches.add(match);
				}
				if (StringUtils.isNotBlank(match.getCustClass())) {
					String custClass = match.getCustClass();
					if (Arrays.asList(custClassValuesToCheck).contains(custClass)) {
						filteredMatches.add(match);
					}
				}

			}
			// set filtered matches in response
			response.setMatches(filteredMatches);
		}

	}

	@Override
	public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement,
			EntityManager entityManager, AutomationResult<OverrideOutput> results, StringBuilder details,
			OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData, String covFrom,
			CoverageContainer container, boolean isCoverageCalculated) throws Exception {

		// override 32S logic
		if (!"C".equals(requestData.getAdmin().getReqType())) {
			details.append("Coverage Calculation skipped for Updates.");
			results.setResults("Skipped");
			results.setDetails(details.toString());
			return true;
		}

		Data data = requestData.getData();
		String scenario = data.getCustSubGrp();

		if (!SCENARIOS_TO_SKIP_COVERAGE.contains(scenario)) {
			if (!isCoverageCalculated) {
				details.setLength(0);
				overrides.clearOverrides();
			}
			UkiFieldsContainer fields = null;
			if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())) {
				if (isCoverageCalculated) {
					fields = getSBOSalesRepForUK(entityManager, data.getIsuCd(), data.getClientTier(), null,
							requestData);
				} else {
					fields = getSBOSalesRepForUK(entityManager, data.getIsuCd(), data.getClientTier(), data.getIsicCd(),
							requestData);
				}
			} else if (SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())) {
				if (isCoverageCalculated) {
					fields = getSBOSalesRepForIE(entityManager, data.getIsuCd(), data.getClientTier(), null,
							requestData);
				} else {
					fields = getSBOSalesRepForIE(entityManager, data.getIsuCd(), data.getClientTier(), data.getIsicCd(),
							requestData);
				}
			}

			if (fields != null) {
				if (covCalculatedFromRdc) {
					details.append("Coverage calculated successfully from found CMRs.").append("\n");
				} else {
					details.append("Coverage calculated successfully using 34Q logic.").append("\n");
				}
				details.append("Sales Rep : " + fields.getSalesRep()).append("\n");
				details.append("SBO : " + fields.getSbo()).append("\n");
				details.append("ISU : " + fields.getIsu()).append("\n");
				details.append("Client Tier : " + fields.getCtc()).append("\n");
				overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "SALES_BO_CD",
						data.getSalesBusOffCd(), fields.getSbo());
				overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "REP_TEAM_MEMBER_NO",
						data.getRepTeamMemberNo(), fields.getSalesRep());
				details.append("ISU : " + fields.getIsu()).append("\n");
				details.append("Client Tier : " + fields.getCtc()).append("\n");
				overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "ISU_CD", data.getIsuCd(),
						!StringUtils.isBlank(fields.getIsu()) ? fields.getIsu() : data.getIsuCd());
				overrides.addOverride(AutomationElementRegistry.GBL_CALC_COV, "DATA", "CLIENT_TIER",
						data.getClientTier(),
						!StringUtils.isBlank(fields.getCtc()) ? fields.getCtc() : data.getClientTier());
				results.setResults("Calculated");
				results.setDetails(details.toString());
			} else if (StringUtils.isNotBlank(data.getRepTeamMemberNo())
					&& StringUtils.isNotBlank(data.getSalesBusOffCd())) {
				details.append("Coverage could not be calculated using 34Q logic. Using values from request")
						.append("\n");
				details.append("Sales Rep : " + data.getRepTeamMemberNo()).append("\n");
				details.append("SBO : " + data.getSalesBusOffCd()).append("\n");
				results.setResults("Calculated");
				results.setDetails(details.toString());
			} else {
				String msg = "Coverage cannot be calculated. No valid 34Q mapping or existing CMRs found from request data.";
				details.append(msg);
				results.setResults("Cannot Calculate");
				results.setDetails(details.toString());
				engineData.addNegativeCheckStatus("_ukiCoverage", msg);
			}
		}
		return true;

	}

	private UkiFieldsContainer getSBOSalesRepForUK(EntityManager entityManager, String isuCd, String clientTier,
			String isicCd, RequestData requestData) {

		// Retrieving SBO Sales Rep from existing CMRs
		String salesRep = "";
		String sbo = "";
		String isu = "";
		String ctc = "";
		UkiFieldsContainer container = new UkiFieldsContainer();
		String cmrIssuingCntry = requestData.getData().getCmrIssuingCntry();
		String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
		String covSql = ExternalizedQuery.getSql("AUTO.COV.GET_COV_FROM_BG_UK");
		PreparedQuery queryCov = new PreparedQuery(entityManager, covSql);

		queryCov.setParameter("KEY", requestData.getData().getBgId());
		queryCov.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
		queryCov.setParameter("COUNTRY", cmrIssuingCntry);
		queryCov.setParameter("ISO_CNTRY", isoCntry);
		queryCov.setForReadOnly(true);

		Object[] result = queryCov.getSingleResult();
		if (result != null) {
			if (!StringUtils.isBlank((String) result[3])) {
				sbo = ((String) result[3]).substring(0, 3);
				salesRep = ((String) result[3]).substring(4);
				container.setSalesRep(salesRep);
				container.setSbo(sbo);
				covCalculatedFromRdc = true;
			}
			if (!StringUtils.isBlank((String) result[0])) {
				isu = (String) result[0];
				container.setIsu(isu);
				covCalculatedFromRdc = true;
			}
			if (!StringUtils.isBlank((String) result[1])) {
				ctc = (String) result[1];
				container.setCtc(ctc);
				covCalculatedFromRdc = true;
			}
			if (covCalculatedFromRdc) {
				return container;
			}
		}

		String scenario = requestData.getData().getCustSubGrp();
		Addr addr;
		if ((SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario))) {
			addr = requestData.getAddress("ZI01");
		} else {
			addr = requestData.getAddress("ZS01");
		}

		String PostCd = addr.getPostCd();

		if (PostCd != null && PostCd.length() > 2) {
			PostCd = PostCd.substring(0, 2);
		}

		if ("34".equals(isuCd) && StringUtils.isNotBlank(clientTier) && StringUtils.isNotBlank(isicCd)) {

			if ("Q".equals(clientTier) && SCOTLAND_POST_CD.contains(PostCd)) {
				container.setSbo("758");
				container.setSalesRep("SPA758");
				return container;
			} else if ("Q".equals(clientTier) && NORTHERN_IRELAND_POST_CD.equals(PostCd)) {
				container.setSbo("958");
				container.setSalesRep("MMIRE1");
				return container;
			} else {
				String sql = ExternalizedQuery.getSql("QUERY.UK.GET.SBOSR_FOR_ISIC");
				PreparedQuery query = new PreparedQuery(entityManager, sql);
				query.setParameter("ISU_CD", "%" + isuCd + "%");
				query.setParameter("ISIC_CD", isicCd);
				query.setParameter("CLIENT_TIER", "%" + clientTier + "%");
				query.setForReadOnly(true);
				List<Object[]> results = query.getResults();
				if (results != null && results.size() == 1) {
					sbo = (String) results.get(0)[0];
					salesRep = (String) results.get(0)[1];
					container.setSbo(sbo);
					container.setSalesRep(salesRep);
					return container;
				}
			}
		} else {
			String sql = ExternalizedQuery.getSql("QUERY.UK.GET.SBOSR_FOR_ISIC");
			String repTeamCd = "";
			String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "")
					+ (StringUtils.isNotBlank(clientTier) ? clientTier : "");
			// 2P0 in repTeamCd refers to 2.0 for distinguishing and fetching
			// the
			// values according to CREATCMR-4530 logic.
			repTeamCd = isuCtc + "2P0";
			PreparedQuery query = new PreparedQuery(entityManager, sql);
			query.setParameter("ISU_CD", "%" + isuCd + "%");
			query.setParameter("ISIC_CD", repTeamCd);
			query.setParameter("CLIENT_TIER", "%" + clientTier + "%");
			query.setForReadOnly(true);
			List<Object[]> results = query.getResults();
			if (results != null && results.size() == 1) {
				sbo = (String) results.get(0)[0];
				salesRep = (String) results.get(0)[1];
				container.setSbo(sbo);
				container.setSalesRep(salesRep);
				return container;
			}
		}
		return null;

	}

	private UkiFieldsContainer getSBOSalesRepForIE(EntityManager entityManager, String isuCd, String clientTier,
			String isicCd, RequestData requestData) {

		// Retrieving SBO Sales Rep from existing CMRs
		String salesRep = "";
		String sbo = "";
		String isu = "";
		String ctc = "";
		UkiFieldsContainer container = new UkiFieldsContainer();
		String cmrIssuingCntry = requestData.getData().getCmrIssuingCntry();
		String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCntry);
		String covSql = ExternalizedQuery.getSql("AUTO.COV.GET_COV_FROM_BG_IR");
		PreparedQuery queryCov = new PreparedQuery(entityManager, covSql);

		queryCov.setParameter("KEY", requestData.getData().getBgId());
		queryCov.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
		queryCov.setParameter("COUNTRY", cmrIssuingCntry);
		queryCov.setParameter("ISO_CNTRY", isoCntry);
		queryCov.setForReadOnly(true);

		Object[] result = queryCov.getSingleResult();
		if (result != null) {
			if (!StringUtils.isBlank((String) result[3])) {
				sbo = ((String) result[3]).substring(0, 3);
				salesRep = ((String) result[3]).substring(4);
				container.setSalesRep(salesRep);
				container.setSbo(sbo);
				covCalculatedFromRdc = true;
			}
			if (!StringUtils.isBlank((String) result[0])) {
				isu = (String) result[0];
				container.setIsu(isu);
				covCalculatedFromRdc = true;
			}
			if (!StringUtils.isBlank((String) result[1])) {
				ctc = (String) result[1];
				container.setCtc(ctc);
				covCalculatedFromRdc = true;
			}
			if (covCalculatedFromRdc) {
				return container;
			}
		}
		String isuCtc = (StringUtils.isNotBlank(isuCd) ? isuCd : "")
				+ (StringUtils.isNotBlank(clientTier) ? clientTier : "");
		if (isuCtc.equals("34Y") || isuCtc.equals("5K")) {
			String sql = ExternalizedQuery.getSql("QUERY.GET.SALESREP.IRELAND");
			PreparedQuery query = new PreparedQuery(entityManager, sql);
			query.setParameter("ISSUING_CNTRY", cmrIssuingCntry);
			query.setParameter("ISU_CD", "%" + isuCtc + "%");
			query.setForReadOnly(true);
			List<Object[]> results = query.getResults();
			if (results != null && results.size() == 1) {
				sbo = (String) results.get(0)[0];
				salesRep = (String) results.get(0)[1];
				container.setSbo(sbo);
				container.setSalesRep(salesRep);
				return container;
			}
		}
		return null;
	}

	@Override
	public String getAddressTypeForGbgCovCalcs(EntityManager entityManager, RequestData requestData,
			AutomationEngineData engineData) throws Exception {
		Data data = requestData.getData();
		String scenario = data.getCustSubGrp();
		String address = "ZS01";

		LOG.debug("Address for the scenario to check: " + scenario);
		if (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario)) {
			address = "ZI01";
		}
		return address;
	}

	@Override
	public void tweakGBGFinderRequest(EntityManager entityManager, GBGFinderRequest request, RequestData requestData,
			AutomationEngineData engineData) {
		Admin admin = requestData.getAdmin();
		Data data = requestData.getData();
		String scenario = data.getCustSubGrp();
		String crn = requestData.getData().getTaxCd1();
		if (!StringUtils.isBlank(crn)) {
			request.setOrgId(crn); // CRN
			LOG.debug("Passing CRN as " + request.getOrgId() + " with GBG finder request.");
		}
		if ("C".equals(admin.getReqType())
				&& (SCENARIO_THIRD_PARTY.equals(scenario) || SCENARIO_INTERNAL_FSL.equals(scenario))) {
			DnBMatchingResponse dnbRecord = (DnBMatchingResponse) engineData.get("ZI01_DNB_MATCH");
			if (dnbRecord != null) {
				request.setDunsNo(dnbRecord.getDunsNo());
			}
		}

	}

	private class UkiFieldsContainer {
		private String sbo;
		private String salesRep;
		private String isu;
		private String ctc;

		public String getIsu() {
			return isu;
		}

		public void setIsu(String isu) {
			this.isu = isu;
		}

		public String getCtc() {
			return ctc;
		}

		public void setCtc(String ctc) {
			this.ctc = ctc;
		}

		public String getSbo() {
			return sbo;
		}

		public void setSbo(String sbo) {
			this.sbo = sbo;
		}

		public String getSalesRep() {
			return salesRep;
		}

		public void setSalesRep(String salesRep) {
			this.salesRep = salesRep;
		}

	}

	@Override
	public GBGFinderRequest createRequest(Admin admin, Data data, Addr addr, Boolean isOrgIdMatchOnly) {
		GBGFinderRequest request = super.createRequest(admin, data, addr, isOrgIdMatchOnly);
		if (SCENARIO_THIRD_PARTY.equals(data.getCustSubGrp())) {
			String custNmTrimmed = getCustomerFullName(addr);
			if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}\\.+$")
					|| custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}/.+$")) {
				custNmTrimmed = custNmTrimmed.substring(6);
			} else if (custNmTrimmed.toUpperCase().matches("^VR[0-9]{3}.+$")) {
				custNmTrimmed = custNmTrimmed.substring(5);
			}
			request.setCustomerName(custNmTrimmed);
		}
		request.setOrgId(data.getTaxCd1());
		return request;
	}

	@Override
	public boolean useTaxCd1ForDnbMatch(RequestData requestData) {
		return true;
	}

	@Override
	public void tweakDnBMatchingRequest(GBGFinderRequest request, RequestData requestData,
			AutomationEngineData engineData) {
		Data data = requestData.getData();
		if (SCENARIO_THIRD_PARTY.equals(data.getCustSubGrp())) {
			String custName = request.getCustomerName();
			if (custName.toUpperCase().matches("^.+VR[0-9]{3}.*$")) {
				custName = custName.split("VR[0-9]{3}")[0];
				LOG.info("Using Cust name without VR999 section for DnB matching --> " + custName);
				request.setCustomerName(custName);
			}
		}
	}

	@Override
	public List<String> getSkipChecksRequestTypesforCMDE() {
		return Arrays.asList("C", "U", "M", "D", "R");
	}

	@Override
	public void performCoverageBasedOnGBG(CalculateCoverageElement covElement, EntityManager entityManager,
			AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides,
			RequestData requestData, AutomationEngineData engineData, String covFrom, CoverageContainer container,
			boolean isCoverageCalculated) throws Exception {
		Data data = requestData.getData();
		String bgId = data.getBgId();
		String gbgId = data.getGbgId();
		String country = data.getCmrIssuingCntry();
		String sql = ExternalizedQuery.getSql("QUERY.GET_GBG_FROM_LOV");
		PreparedQuery query = new PreparedQuery(entityManager, sql);
		query.setParameter("CD", gbgId);
		query.setParameter("COUNTRY", country);
		query.setForReadOnly(true);
		String result = query.getSingleResult(String.class);
		LOG.debug("perform coverage based on GBG-------------");
		LOG.debug("result--------" + result);
		if (result != null || bgId.equals("DB502GQG")) {
			LOG.debug("Setting isu ctc to 5K based on gbg matching.");
			details.append("Setting isu ctc to 5K based on gbg matching.");
			overrides.addOverride(covElement.getProcessCode(), "DATA", "ISU_CD", data.getIsuCd(), "5K");
			overrides.addOverride(covElement.getProcessCode(), "DATA", "CLIENT_TIER", data.getClientTier(), "");
		}
		LOG.debug("isu" + data.getIsuCd());
		LOG.debug("client tier" + data.getClientTier());
	}

	public List<Integer> getAddressCount(EntityManager entityManager, String cmrIssuingCntry, String realCntry,
			String cmrNo) {
		int zi01count = 0;
		int zp01count = 0;
		int zd01count = 0;
		String sql = ExternalizedQuery.getSql("QUERY.GET.COUNT.ADDRTYP");
		PreparedQuery query = new PreparedQuery(entityManager, sql);
		query.setParameter("REALCTY", realCntry);
		query.setParameter("RCYAA", cmrIssuingCntry);
		query.setParameter("RCUXA", cmrNo);
		List<Object[]> results = query.getResults();

		if (results != null && !results.isEmpty()) {
			Object[] sResult = results.get(0);
			zi01count = Integer.parseInt(sResult[0].toString());
			zp01count = Integer.parseInt(sResult[1].toString());
			zd01count = Integer.parseInt(sResult[2].toString());
		}

		return Arrays.asList(zi01count, zp01count, zd01count);
	}
