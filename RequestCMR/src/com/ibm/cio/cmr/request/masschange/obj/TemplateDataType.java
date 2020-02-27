package com.ibm.cio.cmr.request.masschange.obj;

/**
 * Data type for a {@link TemplateTab} or {@link TemplateColumn} object
 * 
 * @author JeffZAMORA
 * 
 */
public enum TemplateDataType {
  /**
   * Type for values to be written on the mass change data table
   */
  Data,
  /**
   * Type for values to be written on the mass change address table
   */
  Address,
  /**
   * Type for values to be written on the mass change contact table
   */
  Contact
}
