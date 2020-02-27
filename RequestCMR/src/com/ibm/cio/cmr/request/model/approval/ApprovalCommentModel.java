/**
 * 
 */
package com.ibm.cio.cmr.request.model.approval;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ApprovalCommentModel {

  private long approvalId;
  private long reqId;
  private String status;
  private String createTs;
  private String commentBy;
  private String comments;

  public long getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(long approvalId) {
    this.approvalId = approvalId;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCreateTs() {
    return createTs;
  }

  public void setCreateTs(String createTs) {
    this.createTs = createTs;
  }

  public String getCommentBy() {
    return commentBy;
  }

  public void setCommentBy(String commentBy) {
    this.commentBy = commentBy;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}
