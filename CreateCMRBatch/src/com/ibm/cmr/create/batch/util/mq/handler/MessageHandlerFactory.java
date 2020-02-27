package com.ibm.cmr.create.batch.util.mq.handler;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFDeleteMessageHandler;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFMessageHandler;
import com.ibm.cmr.create.batch.util.mq.handler.impl.WTAASMessageHandler;

/**
 * Instantiates the handler to be used given the {@link MqIntfReqQueue} record
 * 
 * @author Guo Feng
 * 
 */
public class MessageHandlerFactory {

  public static final List<String> SOF_COUNTRIES = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND, SystemLocation.ISRAEL,
      SystemLocation.GREECE, SystemLocation.CYPRUS, SystemLocation.TURKEY, SystemLocation.SPAIN, SystemLocation.PORTUGAL, SystemLocation.ITALY,
      SystemLocation.SOUTH_AFRICA, SystemLocation.MAURITIUS, SystemLocation.MALI, SystemLocation.EQUATORIAL_GUINEA, SystemLocation.ANGOLA,
      SystemLocation.SENEGAL, SystemLocation.BOTSWANA, SystemLocation.IVORY_COAST, SystemLocation.BURUNDI, SystemLocation.GABON,
      SystemLocation.DEMOCRATIC_CONGO, SystemLocation.CONGO_BRAZZAVILLE, SystemLocation.CAPE_VERDE_ISLAND, SystemLocation.DJIBOUTI,
      SystemLocation.GUINEA_CONAKRY, SystemLocation.CAMEROON, SystemLocation.ETHIOPIA, SystemLocation.MADAGASCAR, SystemLocation.MAURITANIA,
      SystemLocation.TOGO, SystemLocation.GHANA, SystemLocation.ERITREA, SystemLocation.GAMBIA, SystemLocation.KENYA, SystemLocation.MALAWI_CAF,
      SystemLocation.LIBERIA, SystemLocation.MOZAMBIQUE, SystemLocation.NIGERIA, SystemLocation.CENTRAL_AFRICAN_REPUBLIC, SystemLocation.ZIMBABWE,
      SystemLocation.SAO_TOME_ISLANDS, SystemLocation.RWANDA, SystemLocation.SIERRA_LEONE, SystemLocation.SOMALIA, SystemLocation.BENIN,
      SystemLocation.BURKINA_FASO, SystemLocation.SOUTH_SUDAN, SystemLocation.TANZANIA, SystemLocation.UGANDA, SystemLocation.MALTA,
      SystemLocation.SEYCHELLES, SystemLocation.GUINEA_BISSAU, SystemLocation.NIGER, SystemLocation.CHAD, SystemLocation.ZAMBIA,
      SystemLocation.FRANCE, SystemLocation.TUNISIA_SOF, SystemLocation.ABU_DHABI, SystemLocation.ALBANIA, SystemLocation.ARMENIA,
      SystemLocation.AUSTRIA, SystemLocation.AZERBAIJAN, SystemLocation.BAHRAIN, SystemLocation.BELARUS, SystemLocation.BOSNIA_AND_HERZEGOVINA,
      SystemLocation.BULGARIA, SystemLocation.CROATIA, SystemLocation.CZECH_REPUBLIC, SystemLocation.EGYPT, SystemLocation.GEORGIA,
      SystemLocation.HUNGARY, SystemLocation.IRAQ, SystemLocation.JORDAN, SystemLocation.KAZAKHSTAN, SystemLocation.KUWAIT,
      SystemLocation.KYRGYZSTAN, SystemLocation.LEBANON, SystemLocation.LIBYA, SystemLocation.MACEDONIA, SystemLocation.MOLDOVA,
      SystemLocation.MONTENEGRO, SystemLocation.MOROCCO, SystemLocation.OMAN, SystemLocation.PAKISTAN, SystemLocation.POLAND, SystemLocation.QATAR,
      SystemLocation.ROMANIA, SystemLocation.RUSSIAN_FEDERATION, SystemLocation.SAUDI_ARABIA, SystemLocation.SERBIA, SystemLocation.SLOVAKIA,
      SystemLocation.SLOVENIA, SystemLocation.SYRIAN_ARAB_REPUBLIC, SystemLocation.TAJIKISTAN, SystemLocation.TURKMENISTAN, SystemLocation.UKRAINE,
      SystemLocation.UNITED_ARAB_EMIRATES, SystemLocation.UZBEKISTAN, SystemLocation.YEMEN, SystemLocation.GULF, SystemLocation.SWEDEN,
      SystemLocation.NORWAY, SystemLocation.FINLAND, SystemLocation.DENMARK, SystemLocation.BELGIUM, SystemLocation.NETHERLANDS);

  public static final List<String> WTAAS_COUNTRIES = Arrays.asList(SystemLocation.AUSTRALIA, SystemLocation.BANGLADESH, SystemLocation.BRUNEI,
      SystemLocation.MYANMAR, SystemLocation.SRI_LANKA, SystemLocation.INDIA, SystemLocation.PHILIPPINES, SystemLocation.SINGAPORE,
      SystemLocation.VIETNAM, SystemLocation.THAILAND, SystemLocation.MACAO, SystemLocation.HONG_KONG, SystemLocation.NEW_ZEALAND,
      SystemLocation.MALAYSIA, SystemLocation.LAOS, SystemLocation.NEPAL, SystemLocation.CAMBODIA, SystemLocation.INDONESIA);

  // private static final List<String> MOBAS_COUNTRIES =
  // Collections.emptyList();

  public static void main(String[] args) throws Exception {
    String sof = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <softordc> <document form=\"xmlDoc\"> <created> <datetime dst=\"true\">20171030010003803</datetime> </created> <item name=\"Country\"> <text>756</text> </item> <item name=\"UniqueNumber\"> <text>1_236</text> </item> <item name=\"XML_DocumentNumber\"> <text>CREFO001981</text> </item> <item name=\"Status\"> <text>CRE</text> </item> <item name=\"CustomerNo\"> <text /> </item></document> </softordc>";
    String wtaas = "<RY090WM CMRRefNo=\"0000001073\" Status=\"CR Documment Successfully Inserted in DDB.\"/>";

    System.out.println(wtaas.contains("<" + WTAASMessageHandler.WTAAS_RESPONSE_ROOT_ELEM));
    System.out.println(extractUniqueNumber(sof));
    System.out.println(extractUniqueNumber(wtaas));
  }

  /**
   * Initializes the publish message handler
   * 
   * @param entityManager
   * @param queue
   * @return
   */
  public static MQMessageHandler createMessageHandler(EntityManager entityManager, MqIntfReqQueue queue) {
    MQMessageHandler messageHandler = null;
    if (SOF_COUNTRIES.contains(queue.getCmrIssuingCntry())) {
      if (MQMsgConstants.REQ_TYPE_DELETE.equals(queue.getReqType())) {
        messageHandler = new SOFDeleteMessageHandler(entityManager, queue);
      } else {
        messageHandler = new SOFMessageHandler(entityManager, queue);
      }
    }
    if (WTAAS_COUNTRIES.contains(queue.getCmrIssuingCntry())) {
      messageHandler = new WTAASMessageHandler(entityManager, queue);
    }
    return messageHandler;
  }

  /**
   * Extracts the unique number based on the XML received. This checks the XML
   * root and determines the correct parser to use
   * 
   * @param xmlData
   * @return
   * @throws Exception
   */
  public static String extractUniqueNumber(String xmlData) throws Exception {

    if (SOFMessageHandler.isHandled(xmlData)) {
      return SOFMessageHandler.extractUniqueNumber(xmlData);
    }
    if (WTAASMessageHandler.isHandled(xmlData)) {
      return WTAASMessageHandler.extractUniqueNumber(xmlData);
    }
    return null;
  }
}
