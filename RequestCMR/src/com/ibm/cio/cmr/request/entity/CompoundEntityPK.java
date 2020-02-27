/**
 * 
 */
package com.ibm.cio.cmr.request.entity;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CompoundEntityPK extends BaseEntityPk {

  @Override
  protected boolean allKeysAssigned() {
    return true;
  }

}
