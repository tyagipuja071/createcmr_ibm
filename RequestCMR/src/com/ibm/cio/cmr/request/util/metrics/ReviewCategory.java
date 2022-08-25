/**
 * 
 */
package com.ibm.cio.cmr.request.util.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

/**
 * @author 136786PH1
 *
 */
public class ReviewCategory {

  private static final Logger LOG = Logger.getLogger(ReviewCategory.class);
  private String name;
  private List<String> reasons = new ArrayList<String>();
  private List<String> patterns = new ArrayList<String>();

  private List<String> reqTypes = new ArrayList<String>();
  private boolean approvalCategory;
  private String approvalStatus;

  public static void main(String[] args) {
    String s = "The request needs further review due to some issues found during automated checks: \n";
    s += "Value for Department Line exceeds 24 characters. Final value needs to be reviewed.\n";
    s += "\n";
    s += "Please view system processing results for more details.";
    System.out.println(s.replaceAll("\n", "").matches(".*Value.*for.*exceeds.*"));
  }

  public ReviewCategory(String name, String... types) {
    this.name = name;
    if (types != null) {
      this.reqTypes.addAll(Arrays.asList(types));
    }
  }

  public ReviewCategory(String name) {
    this(name, (String[]) null);
  }

  public void addReason(String reason) {
    this.reasons.add(reason.toUpperCase());
  }

  public void addPattern(String pattern) {
    try {
      Pattern.compile(pattern);
      String toAdd = ".*" + pattern.trim().replaceAll("[ ]", ".*") + ".*";
      this.patterns.add(toAdd);
    } catch (PatternSyntaxException e) {
      LOG.warn("The pattern " + pattern + " was not added.");
    }
  }

  public boolean matches(String reqType, String comment, String approvalStatus) {
    if (!this.reqTypes.isEmpty() && !this.reqTypes.contains(reqType)) {
      return false;
    }
    if (this.patterns.isEmpty() && this.reasons.isEmpty() && !this.reqTypes.isEmpty()) {
      return true;
    }
    if (this.approvalCategory) {
      return approvalStatus != null && approvalStatus.contains(this.approvalStatus);
    } else {
      if (comment == null) {
        return false;
      }
      for (String reason : this.reasons) {
        if (reason.length() > comment.length()) {
          continue;
        }
        if (comment.toUpperCase().contains(reason)) {
          return true;
        }
      }
      for (String pattern : this.patterns) {
        String toCompare = comment.replaceAll("\n", " ");
        toCompare = toCompare.replaceAll("\r", "");
        if (toCompare.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public boolean isApprovalCategory() {
    return approvalCategory;
  }

  public void setApprovalCategory(boolean approvalCategory) {
    this.approvalCategory = approvalCategory;
  }

  public String getApprovalStatus() {
    return approvalStatus;
  }

  public void setApprovalStatus(String approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  public String display(ReviewGroup group) {
    StringBuilder sb = new StringBuilder();
    if (this.approvalCategory) {
      sb.append(group.getName() + "\t" + group.getPriority());
      sb.append("\t" + this.name);
      sb.append("\tApproval\t" + this.approvalStatus);
      sb.append("\t");
      if (!this.reqTypes.isEmpty()) {
        sb.append(this.reqTypes.get(0));
      } else {
        sb.append("");
      }
      sb.append("\n");
    } else {
      for (String reason : this.reasons) {
        sb.append(group.getName() + "\t" + group.getPriority());
        sb.append("\t" + this.name);
        sb.append("\tPartial Text\t" + reason.toUpperCase());
        sb.append("\t");
        if (!this.reqTypes.isEmpty()) {
          sb.append(this.reqTypes.get(0));
        } else {
          sb.append("");
        }
        sb.append("\n");
      }
      for (String pattern : this.patterns) {
        sb.append(group.getName() + "\t" + group.getPriority());
        sb.append("\t" + this.name);
        sb.append("\tText Pattern\t" + pattern.replaceAll("[.]", "").replaceAll("[*]", " * ").toUpperCase());
        sb.append("\t");
        if (!this.reqTypes.isEmpty()) {
          sb.append(this.reqTypes.get(0));
        } else {
          sb.append("");
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

}
