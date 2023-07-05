/**
 * 
 */
package com.ibm.cio.cmr.request.util.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.model.system.AutomationStatsModel;

/**
 * @author 136786PH1
 *
 */
public class AutomationReviews {

  private static List<ReviewGroup> groups = new ArrayList<ReviewGroup>();

  static {
    // initialize all here. if need to add/modify do it here

    // D&B Check
    ReviewGroup group = new ReviewGroup("D&B Check");
    group.setPriority(2);

    ReviewCategory cat = new ReviewCategory("API Mismatch");
    cat.addReason("Matches against API were found but no record matched the request data");
    cat.addReason("Legal Name on request and API");
    cat.addReason("Processing error encountered as data is not company verified.");
    group.add(cat);

    cat = new ReviewCategory("D&B Match Not Found");
    cat.addReason("No high quality matches with D&B");
    cat.addReason("No high confidence D&B matches");
    cat.addReason("Please import from D&B search");
    group.add(cat);

    cat = new ReviewCategory("D&B Mismatch");
    cat.addReason("Legal Name on request and D&B doesn't match");
    cat.addReason("Matches against D&B were found but no record matched the request data");
    cat.addReason("Org ID value did not match with the highest confidence D&B match");
    cat.addReason("Name and Address Change(China Specific)");
    group.add(cat);

    cat = new ReviewCategory("D&B Override");
    cat.addReason("D&B matches were chosen to be overridden by the requester");
    group.add(cat);

    groups.add(group);

    // Data Check
    group = new ReviewGroup("Data Check");
    group.setPriority(4);

    cat = new ReviewCategory("City/County");
    cat.addPattern("City.*County");
    cat.addReason("County");
    group.add(cat);

    cat = new ReviewCategory("Invalid Data");
    cat.addReason("Customer Name in all addresses should contain");
    cat.addReason("Customer Names on Sold-to");
    cat.addReason("Billing and Installing addresses are not same");
    cat.addReason("member outside tax team");
    cat.addReason("Customer Name must be filled in");
    group.add(cat);

    cat = new ReviewCategory("No Data Change");
    cat.addReason("No data/address changes made on request");
    group.add(cat);

    cat = new ReviewCategory("VAT Mismatch");
    cat.addReason("VAT is invalid");
    cat.addReason("VAT value did not match");
    cat.addReason("State fiscal code is empty");
    cat.addReason("Enter valid fiscal code value");
    group.add(cat);

    cat = new ReviewCategory("Approval Rejection");
    cat.setApprovalCategory(true);
    cat.setApprovalStatus("REJ");
    group.add(cat);

    cat = new ReviewCategory("Conditional Approval");
    cat.setApprovalCategory(true);
    cat.setApprovalStatus("CAPR");
    group.add(cat);

    cat = new ReviewCategory("Approval Required");
    cat.setApprovalCategory(true);
    cat.setApprovalStatus("APR");
    group.add(cat);

    groups.add(group);

    // DPL Check
    group = new ReviewGroup("DPL Check");
    group.setPriority(5);
    cat = new ReviewCategory("Failed");
    cat.addReason("DPL Check Failed");
    group.add(cat);
    groups.add(group);

    // Duplicate Check
    group = new ReviewGroup("Duplicate Check");
    group.setPriority(1);
    cat = new ReviewCategory("CMR Found");
    cat.addPattern("record with Name.*Location.*exists");
    cat.addReason("Customer already exists");
    cat.addReason("duplicate CMR");
    group.add(cat);

    cat = new ReviewCategory("Duplicate Request");
    cat.addReason("possible duplicate request");
    group.add(cat);

    groups.add(group);

    // Non-Automated
    group = new ReviewGroup("Non-Automated");
    group.setPriority(0);
    cat = new ReviewCategory("Attachment review");
    cat.addReason("An attachment of type 'Chinese Name And Address change'");
    group.add(cat);

    cat = new ReviewCategory("BO Code");
    cat.addReason("Branch Office Codes need to be verified");
    group.add(cat);

    cat = new ReviewCategory("BP Process");
    cat.addReason("change on a Business Partner record needs validation");
    cat.addReason("Only BP with valid CEID is allowed");
    cat.addReason("Address updates for Business Partner scenario");
    group.add(cat);

    cat = new ReviewCategory("Delete", "Delete");
    cat.addReason("Requester not from CMDE");
    group.add(cat);
    cat = new ReviewCategory("Updt by Ent", "Update by Enterprise #");
    cat.addReason("Requester not from CMDE");
    group.add(cat);

    cat = new ReviewCategory("Federal Gov Regular");
    cat.addReason("request is for Fed Gov");
    group.add(cat);

    cat = new ReviewCategory("Foreign Request");
    cat.addReason("Foreign Request will be routed");
    group.add(cat);

    cat = new ReviewCategory("Government");
    cat.addPattern("Government.*needs further validation");
    group.add(cat);

    cat = new ReviewCategory("Mass Create", "Mass Create");
    group.add(cat);

    cat = new ReviewCategory("Mass Update", "Mass Update");
    // cat.addReason("Requester not from CMDE team");
    group.add(cat);

    cat = new ReviewCategory("Missing Scenario");
    cat.addReason("null scenario");
    group.add(cat);

    cat = new ReviewCategory("Power of Attorney");
    cat.addReason("Power of Attorney");
    group.add(cat);

    cat = new ReviewCategory("Private Person");
    cat.addReason("Private Person Scenario found on the request");
    group.add(cat);

    cat = new ReviewCategory("Reactivate", "Reactivate");
    cat.addReason("Requester not from CMDE team");
    group.add(cat);

    cat = new ReviewCategory("State & Local State");
    cat.addReason("request is for State & Local");
    group.add(cat);

    cat = new ReviewCategory("Third Party");
    cat.addReason("Third Party/Data Center request");
    cat.addReason("Third Party request need to be send to CMDE queue for review");
    group.add(cat);

    cat = new ReviewCategory("Cross-Border");
    cat.addReason("Cross-Border Scenario found on the request");
    group.add(cat);

    cat = new ReviewCategory("ISIC Change");
    cat.addReason("required since ISIC belongs");
    cat.addReason("Federal ISIC found");
    group.add(cat);

    groups.add(group);

    // Scenario Check
    group = new ReviewGroup("Scenario Check");
    group.setPriority(3);
    cat = new ReviewCategory("Address Mismatch");
    cat.addReason("Sold-to and Installing addresses are not identical");
    cat.addReason("Billing and Installing addresses are not same");
    cat.addReason("Customer Name and Landed Country on Sold-to and Bill-to address should be same");
    group.add(cat);

    cat = new ReviewCategory("Wrong Scenario");
    cat.addPattern("This request cannot be processed.*scenario sub-type because.*is not the same in all address");
    group.add(cat);

    groups.add(group);

    // System Error
    group = new ReviewGroup("System Error");
    group.setPriority(6);
    cat = new ReviewCategory("Automated Processing");
    cat.addPattern("error in execution of.*caused the process to stop");
    cat.addReason("system error occurred during the processing. A retry will be attempted shortly");
    group.add(cat);

    cat = new ReviewCategory("DB2 Error");
    cat.addReason("EclipseLink");
    cat.addReason("SQLSTATE");
    group.add(cat);

    cat = new ReviewCategory("Field Computation");
    cat.addReason("Field computations logics not defined for the country and needs to manually be completed");
    cat.addReason("Country Scenario check logic was not found and needs confirmation");
    cat.addReason("The override value could not be determined");
    cat.addReason("cannot be computed automatically");
    cat.addReason("cannot compute");
    group.add(cat);

    cat = new ReviewCategory("Field Limitation");
    cat.addPattern("Value for.*exceeds");
    cat.addReason("has a data field that is too long for the screen");
    cat.addReason("The following data fields are too long for the screen");
    group.add(cat);

    cat = new ReviewCategory("Legacy Error");
    cat.addReason("Error Screen:");
    cat.addPattern("Automatic Processing.*failed");
    cat.addReason("M- ");
    cat.addReason("E- ");
    cat.addReason("G- ");
    group.add(cat);

    cat = new ReviewCategory("RDC Error");
    cat.addPattern("Record with request ID.*has FAILED processing");
    cat.addPattern("Mass Update in RDc failed");
    cat.addReason("Internal error occurred");
    cat.addPattern("RDc.*processing");
    group.add(cat);

    cat = new ReviewCategory("Forceback");
    cat.addReason("forced status change");
    group.add(cat);

    groups.add(group);

    // Update Checks
    group = new ReviewGroup("Update Checks");
    group.setPriority(7);
    cat = new ReviewCategory("Address Update", "Update");
    cat.addReason("Updates to addresses cannot be checked automatically");
    cat.addReason("Updates to relevant addresses need verification");
    group.add(cat);

    cat = new ReviewCategory("Name Update", "Update");
    cat.addReason("Legal Name change should be validated");
    group.add(cat);

    cat = new ReviewCategory("VAT ID Update", "Update");
    cat.addReason("VAT updation requires cmde review");
    cat.addReason("VAT # on the request did not match");
    group.add(cat);

    cat = new ReviewCategory("CMDE Validation");
    cat.addReason("Updates to one or more fields cannot be validated");
    cat.addReason("Updated elements cannot be checked automatically");
    cat.addReason("Updates to CMR code fields need verification");
    cat.addReason("Updates to one or more fields need review");
    cat.addReason("IBM/Legacy codes values changed");
    cat.addReason("does not fulfill the criteria to be updated");
    cat.addReason("Matches for Global Buying Groups retrieved");
    cat.addReason("Coverage cannot be calculated");
    group.add(cat);

    groups.add(group);

    group = new ReviewGroup("Manual");
    group.setPriority(8);
    cat = new ReviewCategory("Auto Processing Disabled");
    cat.addReason("DISABLE AUTO PROC");
    group.add(cat);

    groups.add(group);

  }

  public static List<ReviewGroup> findCategory(AutomationStatsModel stat) {
    if (!"Y".equals(stat.getReview())) {
      return Collections.emptyList();
    }

    String toMatch = stat.getAutoComment();
    toMatch += !StringUtils.isBlank(stat.getErrorCmt()) ? stat.getErrorCmt() : "";
    toMatch += !StringUtils.isBlank(stat.getForceCmt()) ? stat.getForceCmt() : "";
    if ("Y".equals(stat.getDisableAutoProc())) {
      toMatch += "DISABLE AUTO PROC";
    }

    List<ReviewGroup> matches = new ArrayList<ReviewGroup>();
    for (ReviewGroup group : groups) {
      ReviewCategory cat = group.match(stat.getReqType(), toMatch, stat.getApprovals());
      if (cat != null) {
        matches.add(group);
      }
    }
    Collections.sort(matches);
    return matches;
  }

}
