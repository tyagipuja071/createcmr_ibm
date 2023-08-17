var app = angular.module('CodeApp', [ 'ngRoute' ]);

app.filter('groupFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(group, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (group.name.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      for (var i = 0; i < group.links.length; i++) {
        var link = group.links[i];
        if (link.name.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
          return true;
        }
        if (link.description && link.description.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
          return true;
        }
        if (link.table && link.table.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
          return true;
        }
        if (link.parent && link.parent.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
          return true;
        }
      }
      return false;
    });

  };
});

app.filter('linkFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(link, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (link.name.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (link.description && link.description.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (link.table && link.table.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (link.parent && link.parent.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.controller('CodeController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.groups = _allGroups;

  $scope.getAll = function() {
    var trs = $('#codeTable tbody tr');
    var groups = [];
    trs.each(function(i, tr) {
      var th = $(tr).find('th');
      var td = $(tr).find('td');
      if (th && th.length > 0 && (!td || td.length == 0)) {
        groups.push({
          name : th[0].innerHTML.trim(),
          cmde : false,
          links : []
        });
      }
      var td = $(tr).find('td');
      if (td && td.length > 0) {
        var href = null;
        var name = null;
        var description = null;
        var table = null;
        var subType = false;
        if (th && th.length > 0) {
          name = th[0].innerHTML;
          subType = true;
        } else {
          href = $(td[0]).find('a')[0].href;
          name = $(td[0]).find('a')[0].innerHTML;
          description = td[1].innerHTML;
          table = td[2].innerHTML;
        }

        var group = groups[groups.length - 1];
        var sub = {
          href : href,
          name : name.trim(),
          description : description ? description.trim() : null,
          table : table,
          subType : subType
        };
        // console.log(sub);
        group.links.push(sub);
      }
    });
    // console.log(JSON.stringify(groups));
  };

  $scope.goTo = function(url) {
    goToUrl(url);
  }
  $scope.getAll();
} ]);

var _allGroups = [ {
  "name" : "General Configurations",
  "cmde" : false,
  "links" : [ {
    "href" : cmr.CONTEXT_ROOT + '/code/field_info',
    "name" : "Field Information",
    "description" : "Maintains field information for the configurable request fields",
    "table" : "CREQCMR.FIELD_INFO",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/field_lbl',
    "name" : "Field Label",
    "description" : "Maintains labels displayed on the screen for configurable request fields",
    "table" : "CREQCMR.FIELD_LBL",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/cmrinternaltypes',
    "name" : "CMR Internal Types",
    "description" : "Maintains the internal request types computed by the system",
    "table" : "CREQCMR.CMR_INTERNAL_TYPES",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/claimroles',
    "name" : "Claim Roles",
    "description" : "Maintains claim roles for CreateCMR users",
    "table" : "CREQCMR.CLAIM_ROLES",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/scenarios',
    "name" : "Customer Scenarios",
    "description" : "Maintains defined customer scenarios (non-US)",
    "table" : "CREQCMR.CUST_SCENARIOS",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/copy',
    "name" : "Copy Configurations",
    "description" : "Utility to copy one configuration set to others",
    "table" : "N/A",
    "subType" : false
  } ]
}, {
  "name" : "GEO Configurations",
  "cmde" : false,
  "links" : [ {
    "href" : cmr.CONTEXT_ROOT + '/code/cntrygeodef',
    "name" : "Country-GEO Definition",
    "description" : "Maintains Countries associated per defined GEO",
    "table" : "CREQCMR.CNTRY_GEO_DEF",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/proccenters',
    "name" : "Processing Center",
    "description" : "Maintains Processing Center Records",
    "table" : "CREQCMR.PROC_CENTER",
    "subType" : false
  } ]
}, {
  "name" : "List of Values",
  "cmde" : false,
  "links" : [ {
    "href" : cmr.CONTEXT_ROOT + '/code/bds_tbl_info',
    "name" : "Business Data Source",
    "description" : "Maintains BDS references for dropdowns",
    "table" : "CREQCMR.BDS_TBL_INFO",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/lovs',
    "name" : "LOV",
    "description" : "Maintains LOV Records",
    "table" : "CREQCMR.LOV",
    "subType" : false
  } ]
}, {
  "name" : "Reference Values",
  "cmde" : false,
  "links" : [ {
    "href" : cmr.CONTEXT_ROOT + '/code/status_desc',
    "name" : "Status Description",
    "description" : "Maintains overall status descriptions for requests",
    "table" : "CREQCMR.STATUS_DESC",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/statusAct',
    "name" : "Status-Action",
    "description" : "Maintains descriptions for Your Actions entries",
    "table" : "CREQCMR.STATUS_ACT",
    "subType" : false
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/roles',
    "name" : "Roles",
    "description" : "Maintains system defined Roles",
    "table" : "CMMA.ROLES",
    "subType" : false
  } ]
}, {
  "name" : "CMDE Maintainable",
  "cmde" : true,
  "links" : [ {
    "href" : null,
    "name" : "System",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/validationurls',
    "name" : "Validation URLS",
    "description" : "Maintains links used for routine validation per country",
    "table" : "CREQCMR.VALIDATION_URLS",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/sysparameters',
    "name" : "System Parameters",
    "description" : "Maintains System Parameters",
    "table" : "CREQCMR.SYST_PARAMETERS",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/mqstatus',
    "name" : "MQ Interface Status",
    "description" : "Shows the current status of records processed via the MQ Interface",
    "table" : "CREQCMR.MQ_INTF_REQ_QUEUE",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/auto/config/base',
    "name" : "Automation Engines",
    "description" : "Maintains Automation Engine configurations",
    "table" : "CREQCMR.AUTO_CONFIG_DEFN",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/suppcountry',
    "name" : "Supported Countries",
    "description" : "Maintains supported countries on the application and automation engine settings",
    "table" : "CREQCMR.SUPP_CNTRY",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/salesBo',
    "name" : "Sales BO Maintenance",
    "description" : "Maintains per country mappings between SBO, ISU, CTC and MRC",
    "table" : "CREQCMR.SALES_BRANCH_OFF",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/repTeam',
    "name" : "Rep Team Maintenance",
    "description" : "Maintains per country current Team-Sales Rep/Cluster-Sales Rep/ etc. mappings",
    "table" : "CREQCMR.REP_TEAM",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/isic',
    "name" : "ISIC Maintenance",
    "description" : "Maintains WW and US ISIC Mappings",
    "table" : "CMMA.REFT_UNSIC_W",
    "subType" : false,
    "parent" : 'System',
  }, {
    "href" : null,
    "name" : "Approvals",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/approval_types',
    "name" : "Approval Types",
    "description" : "Maintains Approval Types usable by the application",
    "table" : "CREQCMR.APPROVAL_TYP",
    "subType" : false,
    "parent" : 'Approvals',
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/defaultappr',
    "name" : "Default Approvals",
    "description" : "Maintains Default Approval configurations",
    "table" : "CREQCMR.DEFAULT_APPROVALS",
    "subType" : false,
    "parent" : 'Approvals',
  }, {
    "href" : null,
    "name" : "United States",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/scclist',
    "name" : "SCC Information",
    "description" : "Maintains SCC Entries for Standard City Service (US Only)",
    "table" : "CREQCMR.US_CMR_SCC",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/bpintlist',
    "name" : "Business Partner Codes",
    "description" : "Maintains US Business Partner Codes Service (US Only)",
    "table" : "SAPR3.US_BP_INT_CODES",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/restrictlist',
    "name" : "Restrict Codes",
    "description" : "Maintains US Restrict Codes Service (US Only)",
    "table" : "SAPR3.US_RESTRICT_CODES",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/div_dept',
    "name" : "IBM Internal Division/Department",
    "description" : "Maintains IBM Internal Division/Department mappings (US Only)",
    "table" : "CREQCMR.LOV",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_bp_master',
    "name" : "US Business Partner Master",
    "description" : "Maintains US Business Partner Master Service (US Only)",
    "table" : "SAPR3.US_BP_MASTER",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_ibm_bo',
    "name" : "US IBM BO",
    "description" : "Maintains US IBM BO Service (US Only)",
    "table" : "SAPR3.US_IBM_BO",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_ibm_org',
    "name" : "US IBM ORG",
    "description" : "Maintains US IBM ORG Service (US Only)",
    "table" : "SAPR3.US_IBM_ORG",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_company',
    "name" : "US COMPANY",
    "description" : "Maintains US COMPANY (US Only)",
    "table" : "SAPR3.US_COMPANY",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_enterprise',
    "name" : "US ENTERPRISE",
    "description" : "Maintains US ENTERPRISE (US Only)",
    "table" : "SAPR3.US_ENTERPRISE",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/us_tcr_updt_queue',
    "name" : "US TCR UPDT QUEUE",
    "description" : "Maintains US TCR UPDT QUEUE Service (US Only)",
    "table" : "USINTERIM.US_TCR_UPDT_QUEUE",
    "subType" : false,
    "parent" : "United States"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/gcars_updt_queue',
    "name" : "GCARS UPDT QUEUE",
    "description" : "Maintains GCARS UPDT QUEUE Service (Brazil Only)",
    "table" : "CREQCMR.MASS_FTP_QUEUE",
    "subType" : false,
    "parent" : "Brazil"
  }, {
    "href" : null,
    "name" : "Latin America",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/collectornodef',
    "name" : "LA Collector Numbers",
    "description" : "Maintains Latin America Selectable Collector Numbers",
    "table" : "CREQCMR.COLLECTOR_NAME_NO",
    "subType" : false,
    "parent" : "Latin America"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/geocitieslistdef',
    "name" : "LA City Maintenance",
    "description" : "Maintains Latin America Selectable City Values Under the Latin American State/Province",
    "table" : "CREQCMR.GEO_CITIES",
    "subType" : false,
    "parent" : "Latin America"
  }, {
    "href" : null,
    "name" : "Brazil",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/cnae',
    "name" : "BR CNAE Maintenance",
    "description" : "Maintains CNAE and corresponding values for Brazil",
    "table" : "CMMA.REFT_BR_CNAE",
    "subType" : false,
    "parent" : "Brazil"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/brSboCollector',
    "name" : "BR SBO/Collector Maintenance",
    "description" : "Maintains SBO/Collector and corresponding values for Brazil",
    "table" : "CREQCMR.REFT_BR_SBO_COLLECTOR",
    "subType" : false,
    "parent" : "Brazil"
  }, {
    "href" : null,
    "name" : "Asia Pacific",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/apCluster',
    "name" : "AP Cluster/ISU/Client Tier Mapping",
    "description" : "Maintains coverage cycle changes by updating cluster mapping",
    "table" : "CREQCMR.AP_CUST_CLUSTER_TIER_MAP",
    "subType" : false,
    "parent" : "Asia Pacific"
  }, {
    "href" : null,
    "name" : "Germany",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/germanyDept',
    "name" : "IBM Internal Department",
    "description" : "Maintains Internal Department mappings",
    "table" : "CREQCMR.LOV",
    "subType" : false,
    "parent" : "Germany"
  }, {
    "href" : null,
    "name" : "Japan",
    "description" : null,
    "table" : null,
    "subType" : true,
    "parent" : null,
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/jpjsiccodemap',
    "name" : "List of JSIC Code Map",
    "description" : "Maintains JSIC code mappings",
    "table" : "CREQCMR.JP_JSIC_CODE_MAP",
    "subType" : false,
    "parent" : "Japan"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/jpofficesectorinacmap',
    "name" : "JP OfficeCd Sector Inac Map",
    "description" : "Maintains JP OfficeCd Sector Inac mappings",
    "table" : "CREQCMR.JP_OFFICE_SECTOR_INAC_MAPPING",
    "subType" : false,
    "parent" : "Japan"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/jpisictojsicmap',
    "name" : "JP ISIC To JSIC Map",
    "description" : "Maintains JP ISIC to JSIC map",
    "table" : "JPINTERIM.ISIC_TO_JSIC_MAP",
    "subType" : false,
    "parent" : "Japan"
  }, {
    "href" : cmr.CONTEXT_ROOT + '/code/jpbocodesmap',
    "name" : "JP BO Codes Map",
    "description" : "Maintains Bo codes map",
    "table" : "JPINTERIM.BO_CODES_MAP",
    "subType" : false,
    "parent" : "Japan"
  } ]
} ];