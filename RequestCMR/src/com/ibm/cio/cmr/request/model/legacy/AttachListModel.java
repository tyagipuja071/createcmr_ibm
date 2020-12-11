/**
 * 
 */
package com.ibm.cio.cmr.request.model.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JeffZAMORA
 *
 */
public class AttachListModel {

  private long reqId;
  private List<String> files = new ArrayList<String>();

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }
}
