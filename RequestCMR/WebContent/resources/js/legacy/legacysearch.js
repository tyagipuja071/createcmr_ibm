var app = angular.module('LegacySearchApp', [ 'ngRoute', 'ngSanitize' ]);

app.filter('textFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(record, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      val = val.toUpperCase();
      if (record.realCtyCd.indexOf(val) >= '0'){
        return true;
      }
      if (record.customerNo.indexOf(val) >= '0'){
        return true;
      }
      if (record.addrNo.indexOf(val) >= '0'){
        return true;
      }
      if (record.abbrevNm && record.abbrevNm.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine1 && record.addrLine1.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine2 && record.addrLine2.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine3 && record.addrLine3.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine4 && record.addrLine4.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine5 && record.addrLine5.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.addrLine6 && record.addrLine6.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.vat && record.vat.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.isuCd && record.isuCd.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.isicCd && record.isicCd.indexOf(val) >= '0'){
        return true;
      }
      if (record.salesRepNo && record.salesRepNo.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      if (record.sbo && record.sbo.toUpperCase().indexOf(val) >= '0'){
        return true;
      }


      return false;
    });

  };
});

app.controller('LegacySearchController', [
    '$scope',
    '$document',
    '$http',
    '$timeout',
    '$sanitize',
    function($scope, $document, $http, $timeout, $sanitize) {

      $scope.loaded = false;

      $scope.crit = {};
      $scope.crit.status = 'A';
      $scope.crit.recCount = '50';
      $scope.countries = [];

      $scope.getCountries = function() {
        var ret = cmr.query('LEGACY.SEARCH.CNTRY', {
          _qall : 'Y'
        });
        if (ret && ret.length > 0) {
          ret.forEach(function(item, i) {
            $scope.countries.push({
              id : item.ret1,
              name : item.ret2
            });
          });
        }
      };

      $scope.search = function() {
        if (!$scope.crit.realCtyCd) {
          alert('Please specify CMR Issuing Country.');
          return;
        } 
        console.log($scope.crit.createTsFrom);
        if ($scope.crit.createTsFrom && $scope.crit.createTsTo && $scope.crit.createTsFrom > $scope.crit.createTsTo){
          alert('Create Date From must be earlier than Create Date To.');
          return;
        }
        if ($scope.crit.updateTsFrom && $scope.crit.updateTsTo && $scope.crit.updateTsFrom > $scope.crit.updateTsTo){
          alert('Create Date From must be earlier than Create Date To.');
          return;
        }
        var paramString = JSON.stringify($scope.crit);
        var params = JSON.parse(paramString);
        if ($scope.crit.createTsFrom){
          params.createTsFrom = moment($scope.crit.createTsFrom).format('YYYY-MM-DD HH:mm:ss');
        }
        if ($scope.crit.createTsTo){
          params.createTsTo = moment($scope.crit.createTsTo).format('YYYY-MM-DD HH:mm:ss');
        }
        if ($scope.crit.updateTsFrom){
          params.updateTsFrom = moment($scope.crit.updateTsFrom).format('YYYY-MM-DD HH:mm:ss');
        }
        if ($scope.crit.updateTsTo){
          params.updateTsTo = moment($scope.crit.updateTsTo).format('YYYY-MM-DD HH:mm:ss');
        }
        var addressUses = [];
        if (params.addressUseM) {
          addressUses.push('M');
        }
        if (params.addressUseB) {
          addressUses.push('B');
        }
        if (params.addressUseI) {
          addressUses.push('I');
        }
        if (params.addressUseS) {
          addressUses.push('S');
        }
        if (params.addressUseE) {
          addressUses.push('E');
        }
        if (params.addressUseF) {
          addressUses.push('F');
        }
        if (params.addressUseG) {
          addressUses.push('G');
        }
        if (params.addressUseH) {
          addressUses.push('H');
        }
        params.addressUses = addressUses;
        console.log(params);
        cmr.showProgress('Searching, please wait...');
        $scope.results = [];
        $scope.loaded = false;
        $http({
          url : cmr.CONTEXT_ROOT + '/legacysearch/process.json',
          method : 'POST',
          params : params
        }).then(function(response) {
          cmr.hideProgress();
          console.log('success');
          console.log(response.data);
          if (response.data && response.data.results) {
            $scope.results = response.data.results;
            $scope.loaded = true;
          } else {
            alert('An error occurred while processing. Please try again later.');
          }
        }, function(response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
          alert('An error occurred while processing. Please try again later.');
        });
      };

      $scope.formatAddrUse = function(rec) {
        var uses = rec.addressUses;
        var html = '';
        if (uses) {
          uses.forEach(function(use, index) {
            if (use == 'M') {
              html += '<div class="use-m" title="Mailing">M</div>';
            }
            if (use == 'B') {
              html += '<div class="use-b" title="Billing">B</div>';
            }
            if (use == 'I') {
              html += '<div class="use-i" title="Installing">I</div>';
            }
            if (use == 'S') {
              html += '<div class="use-s" title="Shipping">S</div>';
            }
            if (use == 'E') {
              html += '<div class="use-e" title="EPL">E</div>';
            }
            if (use == 'L') {
              html += '<div class="use-l" title="Lit Mailing">L</div>';
            }
            if (use.indexOf('U') == 0) {
              html += '<div class="use-gen" title="Use ' + use.substring(1) + '">' + use.substring(1) + '</div>';
            }
          });
        }
        return html;
      }

      $scope.openDetails = function(rec) {
        WindowMgr.open('LEGACY', rec.sofCntryCode + rec.customerNo, 'legacydetails?realCty='+rec.realCtyCd+'&country=' + rec.sofCntryCode + '&cmrNo=' + rec.customerNo, null,
            550);
      }

      $timeout($scope.getCountries,500);

    } ]);

app.controller('LegacyDetailsController', [ '$scope', '$document', '$http', '$timeout', '$sanitize',
    function($scope, $document, $http, $timeout, $sanitize) {

      var extraUses = [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' ];
      $scope.expCust = true;
      $scope.expAddr = true;
      $scope.expExt = true;
      $scope.expLink = true;
      $scope.expRdc = true;

      $scope.getParameterByName = function(name, url) {
        if (!url) {
          url = window.location.href;
        }
        name = name.replace(/[\[\]]/g, '\\$&');
        var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
        var results = regex.exec(url);
        if (!results) {
          return null;
        }
        if (!results[2]) {
          return '';
        }
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
      };

      $scope.getDetails = function() {
        var country = $scope.getParameterByName('country');
        var cmrNo = $scope.getParameterByName('cmrNo');
        var realCty = $scope.getParameterByName('realCty');

        cmr.showProgress('Getting record details, please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/legacydetails/process.json?realCty='+realCty+'&country=' + country + '&cmrNo=' + cmrNo,
          method : 'GET'
        }).then(function(response) {
          cmr.hideProgress();
          console.log('success');
          console.log(response.data);
          if (response.data && response.data.details) {
            var details = response.data.details;
            $scope.cust = details.customer;
            $scope.cust.createTs = moment(new Date(new Number(details.customer.createTs))).format('YYYY-MM-DD HH:mm:ss');
            $scope.cust.updateTs = moment(new Date(new Number(details.customer.updateTs))).format('YYYY-MM-DD HH:mm:ss');
            $scope.cust.updStatusTs = moment(new Date(new Number(details.customer.updStatusTs))).format('YYYY-MM-DD HH:mm:ss');
            $scope.ext = details.customerExt;
            if ($scope.ext){
              $scope.ext.aeciSubDt = moment(new Date(new Number(details.customerExt.aeciSubDt))).format('YYYY-MM-DD HH:mm:ss');
            }
            $scope.addresses = details.addresses;
            $scope.addresses.sort(function(a, b){
              return new Number(a.addrNo) - new Number(b.addrNo);
            });
            $scope.links = details.links;
            $scope.rdcRecords = details.rdcRecords;
          } else {
            alert('An error occurred while processing. Please try again later.');
          }
        }, function(response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
          alert('An error occurred while processing. Please try again later.');
        });

      }

      $scope.formatUses = function(use) {
        var html = '';
        if (use.isAddrUseMailing == 'Y') {
          html += '<div class="use-m" title="Mailing">M</div>';
        }
        if (use.isAddrUseBilling == 'Y') {
          html += '<div class="use-b" title="Billing">B</div>';
        }
        if (use.isAddrUseInstalling == 'Y') {
          html += '<div class="use-i" title="Installing">I</div>';
        }
        if (use.isAddrUseShipping == 'Y') {
          html += '<div class="use-s" title="Shipping">S</div>';
        }
        if (use.isAddrUseEPL == 'Y') {
          html += '<div class="use-e" title="EPL">E</div>';
        }
        if (use.isAddrUseLitMailing == 'Y') {
          html += '<div class="use-l" title="Lit Mailing">L</div>';
        }
        for (var i = 0; i < extraUses.length; i++) {
          if (use['isAddressUse' + extraUses[i]] == 'Y') {
            html += '<div class="use-gen" title="Use ' + extraUses[i] + '">' + extraUses[i] + '</div>';
          }
        }
        return html;
      }

      $timeout($scope.getDetails, 500);
    } ]);
