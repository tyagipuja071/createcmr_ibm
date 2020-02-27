var SCENARIO_FIELDS = [ {
  id : '##AbbrevLocation',
  lbl : 'Abbreviated Location',
  name : 'abbrevLocn',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##AbbrevName',
  lbl : 'Abbreviated Name',
  name : 'abbrevNm',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##CMROwner',
  lbl : 'CMR Owner',
  name : 'cmrOwner',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CollectionCd',
  lbl : 'Collection Code',
  name : 'collectionCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CustLang',
  lbl : 'Preferred Language',
  name : 'custPrefLang',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##CustomerType',
  lbl : 'Customer Type',
  name : 'custType',
  tab : 'MAIN_GENERAL_TAB',
  address : false
}, {
  id : '##Department',
  lbl : 'Department',
  name : 'dept',
  tab : '',
  address : true
}, {
  id : '##Enterprise',
  lbl : 'Enterprise',
  name : 'enterprise',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##GovernmentType',
  lbl : 'Government Type',
  name : 'govType',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##INACCode',
  lbl : 'INAC',
  name : 'inacCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CustPhone',
  lbl : 'Phone #',
  name : 'custPhone',
  tab : '',
  address : true
}, {
  id : '##CustomerServiceCd',
  lbl : 'Customer Service Code',
  name : 'engineeringBo',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CoverageID',
  lbl : 'Cluster/Coverage Id',
  name : 'covId',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ProvinceCode',
  lbl : 'Province Code/BOID',
  name : 'territoryCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ISBU',
  lbl : 'ISBU',
  name : 'isbuCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ISIC',
  lbl : 'ISIC',
  name : 'isicCd',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##ISU',
  lbl : 'ISU',
  name : 'isuCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ISU2',
  lbl : 'ISU 2',
  name : 'dupIsuCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##LandedCountry',
  lbl : 'Landed Country',
  name : 'landCntry',
  tab : '',
  address : true
}, {
  id : '##LocalTax1',
  lbl : 'Tax Code 1 (Address)',
  name : 'taxCd1',
  tab : '',
  address : true
}, {
  id : '##LocalTax2',
  lbl : 'Tax Code 2 (Address)',
  name : 'taxCd2',
  tab : '',
  address : true
}, {
  id : '##LocalTax1',
  lbl : 'Tax Code 1',
  name : 'taxCd1',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##LocalTax2',
  lbl : 'Tax Code 2',
  name : 'taxCd2',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##MrcCd',
  lbl : 'MRC',
  name : 'mrcCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##PostalCode',
  lbl : 'Postal Code',
  name : 'postCd',
  tab : '',
  address : true
}, {
  id : '##SalRepName',
  lbl : 'Sales Rep Name',
  name : 'repTeamMemberName',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SalRepNameNo',
  lbl : 'Sales Rep No.',
  name : 'repTeamMemberNo',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SalesBusOff',
  lbl : 'Sales Business Office',
  name : 'salesBusOffCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SensitiveFlag',
  lbl : 'Sensitive Flag',
  name : 'sensitiveFlag',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##Subindustry',
  lbl : 'Subindustry',
  name : 'subIndustryCd',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##VAT',
  lbl : 'VAT (Address)',
  name : 'vat',
  tab : '',
  address : true
}, {
  id : '##VAT',
  lbl : 'VAT',
  name : 'vat',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##VATExempt',
  lbl : 'VAT Exempt',
  name : 'vatExempt',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##crosSubTyp',
  lbl : 'CROS Sub Type',
  name : 'crosSubTyp',
  tab : '',
  address : false
}, {
  id : '##ISR',
  lbl : 'ISR',
  name : 'repTeamMemberNo',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##crosTyp',
  lbl : 'CROS Type',
  name : 'crosTyp',
  tab : '',
  address : false
}, {
  id : '##ordBlk',
  lbl : 'Order Block',
  name : 'ordBlk',
  tab : '',
  address : false
}, {
  id : '##AcAdminBo',
  lbl : 'Account Admin Branch Office',
  name : 'acAdminBo',
  tab : 'tab',
  address : false
}, {
  id : '##Affiliate',
  lbl : 'Affiliate',
  name : 'affiliate',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##BPRelationType',
  lbl : 'BP Relationship Type',
  name : 'bpRelType',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Building',
  lbl : 'Building',
  name : 'bldg',
  tab : '',
  address : true
}, {
  id : '##BusinessType',
  lbl : 'Business Type',
  name : 'busnType',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##City1',
  lbl : 'City',
  name : 'city1',
  tab : '',
  address : true
}, {
  id : '##City2',
  lbl : 'District',
  name : 'city2',
  tab : '',
  address : true
}, {
  id : '##ClientTier',
  lbl : 'Client Tier',
  name : 'clientTier',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ClientTier2',
  lbl : 'Client Tier 2',
  name : 'dupClientTierCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CollBranchOff',
  lbl : 'Collector Branch Office',
  name : 'collBoId',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##CollectorNameNo',
  lbl : 'Collector Name/No',
  name : 'collectorNameNo',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Company',
  lbl : 'Company',
  name : 'company',
  tab : 'MAIN_IBM_TAB',
  address : false
}, , {
  id : '##ContactName1',
  lbl : 'Contact Name 1',
  name : 'contactName1',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##ContactName2',
  lbl : 'Contact Name 2',
  name : 'contactName2',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##ContactName3',
  lbl : 'Contact Name 3',
  name : 'contactName3',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##County',
  lbl : 'County',
  name : 'county',
  tab : '',
  address : true
}, {
  id : '##CurrencyCode',
  lbl : 'Currency Code',
  name : '##CurrencyCode',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##CustClassCode',
  lbl : 'Customer Class',
  name : 'custClassCode',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CustFAX',
  lbl : 'FAX No.',
  name : 'custFax',
  tab : '',
  address : true
}, {
  id : '##CustomerName1',
  lbl : 'Customer Name 1',
  name : 'custNm1',
  tab : '',
  address : true
}, {
  id : '##CustomerName2',
  lbl : 'Customer Name 2',
  name : 'custNm2',
  tab : '',
  address : true
}, {
  id : '##CustomerName3',
  lbl : 'Customer Name 3',
  name : 'custNm3',
  tab : '',
  address : true
}, {
  id : '##CustomerName4',
  lbl : 'Customer Name 4',
  name : 'custNm4',
  tab : '',
  address : true
}, {
  id : '##Division',
  lbl : 'Division',
  name : 'divn',
  tab : '',
  address : true
}, {
  id : '##EconomicCd',
  lbl : 'Economic Code',
  name : 'economicCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Email1',
  lbl : 'Email 1',
  name : 'email1',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##Email2',
  lbl : 'Email 2',
  name : 'email2',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##Email3',
  lbl : 'Email 3',
  name : 'email3',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##EngineeringBo',
  lbl : 'CEBO',
  name : 'engineeringBo',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Floor',
  lbl : 'Floor',
  name : '##Floor',
  tab : '',
  address : true
}, {
  id : '##IBMBankNumber',
  lbl : 'Bank Number',
  name : 'ibmBankNumber',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##ICMSContribution',
  lbl : 'ICMS Contribution',
  name : 'icmsInd',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##INACType',
  lbl : 'INAC Type',
  name : 'inacType',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##LocationNumber',
  lbl : 'Location Number',
  name : 'locationNumber',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##MRCISU',
  lbl : 'MRC/ISU',
  name : 'mrcCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##MainCustomerName1',
  lbl : 'Main Customer Name',
  name : 'mainCustomerName1',
  tab : 'MAIN_GEN_TAB',
  address : false
}, {
  id : '##MainCustomerName2',
  lbl : 'Main Customer Con\'t',
  name : 'mainCustomerName2',
  tab : 'MAIN_GEN_TAB',
  address : false
}, {
  id : '##MembLevel',
  lbl : 'Membership Level',
  name : 'membLevel',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##MrktChannelInd',
  lbl : 'Makreting Channel',
  name : 'MrktChannelInd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Office',
  lbl : 'Office',
  name : 'office',
  tab : '',
  address : true
}, {
  id : '##POBox',
  lbl : 'PO Box',
  name : 'poBox',
  tab : '',
  address : true
}, {
  id : '##POBoxCity',
  lbl : 'PO Box City',
  name : 'poBoxCity',
  tab : '',
  address : true
}, {
  id : '##POBoxPostalCode',
  lbl : 'PO Box Postal Code',
  name : 'poBoxPostCode',
  tab : '',
  address : true
}, {
  id : '##PPSCEID',
  lbl : 'PPSCEID',
  name : 'ppsceid',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CustPhone',
  lbl : 'Phone No.',
  name : 'custPhone',
  tab : '',
  address : true
}, {
  id : '##Phone1',
  lbl : 'Phone 1',
  name : 'phone1',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Phone2',
  lbl : 'Phone 2',
  name : 'phone2',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##Phone3',
  lbl : 'Phone 3',
  name : 'phone3',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SOENumber',
  lbl : 'SOE Number',
  name : 'soeReqNo',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##SearchTerm',
  lbl : 'Search Term (SORTL)',
  name : 'searchTerm',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SitePartyID',
  lbl : 'iERP Site Party ID',
  name : 'sitePartyID',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SourceCd',
  lbl : 'Source Code',
  name : 'sourceCd',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##SpecialTaxCd',
  lbl : 'Special Tax Code',
  name : 'specialTaxCd',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##StateProv',
  lbl : 'State/Province',
  name : 'stateProv',
  tab : '',
  address : true
}, {
  id : '##StreetAddress1',
  lbl : 'Street Address',
  name : 'addrTxt',
  tab : '',
  address : true
}, {
  id : '##StreetAddress2',
  lbl : 'Street Address Con\'t',
  name : 'addrTxt2',
  tab : '',
  address : true
}, {
  id : '##TransportZone',
  lbl : 'Transport Zone',
  name : 'transportZone',
  tab : '',
  address : true
}, {
  id : '##ModeOfPayment',
  lbl : 'Mode of Payment',
  name : 'modeOfPayment',
  tab : 'MAIN_IBM_TAB',
  address : false
}, {
  id : '##CommercialFinanced',
  lbl : 'Commercial Financed',
  name : 'commercialFinanced',
  tab : 'MAIN_CUST_TAB',
  address : false
}, {
  id : '##CmrNo',
  lbl : 'CMR Number',
  name : 'cmrNo',
  tab : 'MAIN_IBM_TAB',
  address : false
},{
  id : '####CrosSubTyp',
  lbl : 'Type Of Customer',
  name : 'crosSubTyp',
  tab : 'MAIN_IBM_TAB',
  address : false
},{
  id : '##Cluster',
  lbl : 'Cluster',
  name : 'apCustClusterId',
  tab : 'MAIN_IBM_TAB',
  address : false
} ,{
  id : '##IdentClient',
  lbl : 'Ident Client',
  name : 'identClient',
  tab : 'MAIN_IBM_TAB',
  address : false
} ];

function scenariofieldSort(field1, field2) {
  return field1.lbl.toUpperCase() < field2.lbl.toUpperCase() ? -1 : (field1.lbl.toUpperCase() > field2.lbl.toUpperCase() ? 1 : 0);
}
