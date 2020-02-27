/**
 * 
 */
package com.ibm.cio.cmr.request.util.dnb;

import java.util.Comparator;

import com.ibm.cmr.services.client.dnb.DnbOrganizationId;

/**
 * Orders the list of organization ID
 * 
 * @author JeffZAMORA
 *
 */
public class OrgIdComparator implements Comparator<DnbOrganizationId> {

  @Override
  public int compare(DnbOrganizationId o1, DnbOrganizationId o2) {
    if (o1.getOrganizationIdCode() != null && o2.getOrganizationIdCode() == null) {
      return -1;
    }
    if (o1.getOrganizationIdCode() == null && o2.getOrganizationIdCode() != null) {
      return 1;
    }
    return o1.getOrganizationIdCode().length() > o2.getOrganizationIdCode().length() ? -1
        : (o1.getOrganizationIdCode().length() < o2.getOrganizationIdCode().length() ? 1
            : o1.getOrganizationIdCode().compareTo(o2.getOrganizationIdCode()));
  }

}
