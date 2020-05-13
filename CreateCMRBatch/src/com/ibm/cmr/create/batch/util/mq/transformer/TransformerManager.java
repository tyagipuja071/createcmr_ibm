/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer;

import java.util.HashMap;
import java.util.Map;

import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.AustraliaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.BELUXTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.BangladeshTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.BruneiTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.CEETransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.CEMEATransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.CEWATransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.CambodiaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.CyprusTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.FranceTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.FstTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.GreeceTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.HongKongTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.IndiaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.IndonesiaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.IrelandTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.IsraelTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.ItalyTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.LaosTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.MacauTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.MalaysiaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.MyanmarTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.NLTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.NORDXTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.NepalTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.NewZealandTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.PhilippinesTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.PortugalTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.SingaporeTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.SouthAfricaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.SpainTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.SriLankaTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.ThailandTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.TurkeyTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.UnitedKingdomTransformer;
import com.ibm.cmr.create.batch.util.mq.transformer.impl.VietnamTransformer;

/**
 * Handles the retrieval of registered transformers
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TransformerManager {

  private static Map<String, MessageTransformer> transformers = new HashMap<String, MessageTransformer>();

  static {
    // initialize defined transformers
    addTransformer(new UnitedKingdomTransformer());
    addTransformer(new IrelandTransformer());
    addTransformer(new IsraelTransformer());
    addTransformer(new GreeceTransformer());
    addTransformer(new CyprusTransformer());
    addTransformer(new TurkeyTransformer());
    addTransformer(new FranceTransformer());
    // AP Transformer
    try {
      addTransformer(new AustraliaTransformer());
      addTransformer(new BruneiTransformer());
      addTransformer(new HongKongTransformer());
      addTransformer(new IndiaTransformer());
      addTransformer(new IndonesiaTransformer());
      addTransformer(new LaosTransformer());
      addTransformer(new MacauTransformer());
      addTransformer(new MalaysiaTransformer());
      addTransformer(new MyanmarTransformer());
      addTransformer(new NepalTransformer());
      addTransformer(new NewZealandTransformer());
      addTransformer(new PhilippinesTransformer());
      addTransformer(new SingaporeTransformer());
      addTransformer(new ThailandTransformer());
      addTransformer(new SpainTransformer());
      addTransformer(new PortugalTransformer());
      addTransformer(new SouthAfricaTransformer());
      addTransformer(new SriLankaTransformer());
      addTransformer(new BangladeshTransformer());
      addTransformer(new CambodiaTransformer());
      addTransformer(new VietnamTransformer());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    addTransformer(new FstTransformer(SystemLocation.MAURITIUS));
    addTransformer(new FstTransformer(SystemLocation.MALI));
    addTransformer(new FstTransformer(SystemLocation.EQUATORIAL_GUINEA));
    addTransformer(new FstTransformer(SystemLocation.SENEGAL));
    addTransformer(new FstTransformer(SystemLocation.IVORY_COAST));
    addTransformer(new FstTransformer(SystemLocation.GABON));
    addTransformer(new FstTransformer(SystemLocation.DEMOCRATIC_CONGO));
    addTransformer(new FstTransformer(SystemLocation.CONGO_BRAZZAVILLE));
    addTransformer(new FstTransformer(SystemLocation.DJIBOUTI));
    addTransformer(new FstTransformer(SystemLocation.GUINEA_CONAKRY));
    addTransformer(new FstTransformer(SystemLocation.CAMEROON));
    addTransformer(new FstTransformer(SystemLocation.MADAGASCAR));
    addTransformer(new FstTransformer(SystemLocation.MAURITANIA));
    addTransformer(new FstTransformer(SystemLocation.TOGO));
    addTransformer(new FstTransformer(SystemLocation.GAMBIA));
    addTransformer(new FstTransformer(SystemLocation.CENTRAL_AFRICAN_REPUBLIC));
    addTransformer(new FstTransformer(SystemLocation.BENIN));
    addTransformer(new FstTransformer(SystemLocation.BURKINA_FASO));
    addTransformer(new FstTransformer(SystemLocation.SEYCHELLES));
    addTransformer(new FstTransformer(SystemLocation.GUINEA_BISSAU));
    addTransformer(new FstTransformer(SystemLocation.NIGER));
    addTransformer(new FstTransformer(SystemLocation.CHAD));

    addTransformer(new CEWATransformer(SystemLocation.ANGOLA));
    addTransformer(new CEWATransformer(SystemLocation.BOTSWANA));
    addTransformer(new CEWATransformer(SystemLocation.BURUNDI));
    addTransformer(new CEWATransformer(SystemLocation.CAPE_VERDE_ISLAND));
    addTransformer(new CEWATransformer(SystemLocation.ETHIOPIA));
    addTransformer(new CEWATransformer(SystemLocation.GHANA));
    addTransformer(new CEWATransformer(SystemLocation.ERITREA));
    addTransformer(new CEWATransformer(SystemLocation.KENYA));
    addTransformer(new CEWATransformer(SystemLocation.MALAWI_CAF));
    addTransformer(new CEWATransformer(SystemLocation.LIBERIA));
    addTransformer(new CEWATransformer(SystemLocation.MOZAMBIQUE));
    addTransformer(new CEWATransformer(SystemLocation.NIGERIA));
    addTransformer(new CEWATransformer(SystemLocation.ZIMBABWE));
    addTransformer(new CEWATransformer(SystemLocation.SAO_TOME_ISLANDS));
    addTransformer(new CEWATransformer(SystemLocation.RWANDA));
    addTransformer(new CEWATransformer(SystemLocation.SIERRA_LEONE));
    addTransformer(new CEWATransformer(SystemLocation.SOMALIA));
    addTransformer(new CEWATransformer(SystemLocation.SOUTH_SUDAN));
    addTransformer(new CEWATransformer(SystemLocation.TANZANIA));
    addTransformer(new CEWATransformer(SystemLocation.UGANDA));
    addTransformer(new CEWATransformer(SystemLocation.MALTA));
    addTransformer(new CEWATransformer(SystemLocation.ZAMBIA));

    // France
    addTransformer(new FranceTransformer(SystemLocation.FRANCE));
    addTransformer(new FranceTransformer(SystemLocation.TUNISIA_SOF));
    addTransformer(new ItalyTransformer(SystemLocation.ITALY));

    // CEMEA

    addTransformer(new CEMEATransformer(SystemLocation.ABU_DHABI));
    addTransformer(new CEMEATransformer(SystemLocation.ALBANIA));
    addTransformer(new CEMEATransformer(SystemLocation.ARMENIA));
    addTransformer(new CEMEATransformer(SystemLocation.AUSTRIA));
    addTransformer(new CEMEATransformer(SystemLocation.AZERBAIJAN));
    addTransformer(new CEMEATransformer(SystemLocation.BAHRAIN));
    addTransformer(new CEMEATransformer(SystemLocation.BELARUS));
    addTransformer(new CEMEATransformer(SystemLocation.BOSNIA_AND_HERZEGOVINA));
    addTransformer(new CEMEATransformer(SystemLocation.BULGARIA));
    addTransformer(new CEMEATransformer(SystemLocation.CROATIA));
    addTransformer(new CEMEATransformer(SystemLocation.CZECH_REPUBLIC));
    addTransformer(new CEMEATransformer(SystemLocation.EGYPT));
    addTransformer(new CEMEATransformer(SystemLocation.GEORGIA));
    addTransformer(new CEMEATransformer(SystemLocation.HUNGARY));
    addTransformer(new CEMEATransformer(SystemLocation.IRAQ));
    addTransformer(new CEMEATransformer(SystemLocation.JORDAN));
    addTransformer(new CEMEATransformer(SystemLocation.KAZAKHSTAN));
    addTransformer(new CEMEATransformer(SystemLocation.KUWAIT));
    addTransformer(new CEMEATransformer(SystemLocation.KYRGYZSTAN));
    addTransformer(new CEMEATransformer(SystemLocation.LEBANON));
    addTransformer(new CEMEATransformer(SystemLocation.LIBYA));
    addTransformer(new CEMEATransformer(SystemLocation.MACEDONIA));
    addTransformer(new CEMEATransformer(SystemLocation.MOLDOVA));
    addTransformer(new CEMEATransformer(SystemLocation.MONTENEGRO));
    addTransformer(new CEMEATransformer(SystemLocation.MOROCCO));
    addTransformer(new CEMEATransformer(SystemLocation.OMAN));
    addTransformer(new CEMEATransformer(SystemLocation.PAKISTAN));
    addTransformer(new CEMEATransformer(SystemLocation.POLAND));
    addTransformer(new CEMEATransformer(SystemLocation.QATAR));
    addTransformer(new CEMEATransformer(SystemLocation.ROMANIA));
    addTransformer(new CEMEATransformer(SystemLocation.RUSSIAN_FEDERATION));
    addTransformer(new CEMEATransformer(SystemLocation.SAUDI_ARABIA));
    addTransformer(new CEMEATransformer(SystemLocation.SERBIA));
    // addTransformer(new CEMEATransformer(SystemLocation.SLOVAKIA));
    addTransformer(new CEMEATransformer(SystemLocation.SLOVENIA));
    addTransformer(new CEMEATransformer(SystemLocation.SYRIAN_ARAB_REPUBLIC));
    addTransformer(new CEMEATransformer(SystemLocation.TAJIKISTAN));
    addTransformer(new CEMEATransformer(SystemLocation.TURKMENISTAN));
    addTransformer(new CEMEATransformer(SystemLocation.UKRAINE));
    addTransformer(new CEMEATransformer(SystemLocation.UNITED_ARAB_EMIRATES));
    addTransformer(new CEMEATransformer(SystemLocation.UZBEKISTAN));
    addTransformer(new CEMEATransformer(SystemLocation.YEMEN));
    addTransformer(new CEMEATransformer(SystemLocation.GULF));

    // Nordics/BeLux/Netherlands

    addTransformer(new NORDXTransformer(SystemLocation.SWEDEN));
    addTransformer(new NORDXTransformer(SystemLocation.NORWAY));
    addTransformer(new NORDXTransformer(SystemLocation.FINLAND));
    addTransformer(new NORDXTransformer(SystemLocation.DENMARK));
    addTransformer(new BELUXTransformer(SystemLocation.BELGIUM));
    addTransformer(new NLTransformer(SystemLocation.NETHERLANDS));
    addTransformer(new TurkeyTransformer(SystemLocation.TURKEY));

    // CEE - LD
    addTransformer(new CEETransformer(SystemLocation.SLOVAKIA));
  }

  /**
   * Gets the transformers resgitered for this country
   * 
   * @param cmrIssuingCntry
   * @return
   */
  public static MessageTransformer getTransformer(String cmrIssuingCntry) {
    return transformers.get(cmrIssuingCntry);
  }

  /**
   * Adds the transformer to the handler
   * 
   * @param transformer
   */
  private static void addTransformer(MessageTransformer transformer) {
    transformers.put(transformer.getCmrIssuingCntry(), transformer);
  }
}
