var app = angular.module('DashboardApp', [ 'ngRoute', 'ngSanitize' ]);

app.filter('processFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(text, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      val = val.toUpperCase();
      if (text.reqStatus.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (text.reqType.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (text.cmrIssuingCntry.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (text.custNm.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (text.sourceSystId.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (text.processingTyp.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      return false;
    });

  };
});


app.controller('DashboardController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.report = null;
  $scope.process = null;
  $scope.countries = null;
  $scope.stuckProcess = null;
  $scope.totalPending = 0;
  $scope.querying = false;
  
  $scope.fCountries = [];
  $scope.fPartners = [];
  $scope.fProcess = [];
  
  $scope.filterStatus = 'Checking system status..';
  
  $scope.checkAll = function(){ 
    $scope.filterCountry = '';
    $scope.filterProcess = '';
    $scope.filterPartner = '';
    $scope.extract();
  };
  $scope.extract = function(){
    if ($scope.querying){
      cmr.showAlert('Please wait for the results to load first before checking again.');
      return;
    }
    $scope.report = null;
    $scope.totalPending = 0;
    $scope.filterStatus = 'Checking system status..';
    $scope.querying = true;
    $http({
      url : cmr.CONTEXT_ROOT + '/monitor.json', 
      method: 'GET',
      params : {
        listRecords : 'Y',
        source : $scope.filterPartner,
        cntry: $scope.filterCountry,
        procType : $scope.filterProcess
      }
    }).then(function(response) {
      $scope.querying = false;
      if ($scope.filterProcess || $scope.filterCountry || $scope.filterPartner){
        var filters = '';
        if ($scope.filterCountry){
          filters += 'Country: '+$scope.filterCountry;
        }
        if ($scope.filterPartner){
          filters += filters.length > 0 ? ', ' : '';
          filters += 'Partner: '+$scope.filterPartner;
        }
        if ($scope.filterProcess){
          filters += filters.length > 0 ? ', ' : '';
          filters += 'Process: '+$scope.filterProcess;
        }
        $scope.filterStatus = 'System Checks for '+filters;
      } else {
        $scope.filterStatus = 'System Checks for ALL Components';
      }
      if (response.data && response.data.dashboardResult){
        $scope.report = response.data.dashboardResult;
        $scope.process = $scope.report.processing;
        $scope.fCountries = $scope.report.countries;
        $scope.fPartners = $scope.report.partners;
        $scope.fProcess = $scope.report.procTypes;
      }
      
      if ($scope.process && $scope.process.pendingCounts){
        var countryList = [];
        for (var i in $scope.process.pendingCounts){
          if ($scope.process.pendingCounts.hasOwnProperty(i)){
            countryList.push(i);
            $scope.totalPending += $scope.process.pendingCounts[i];
          }
        }
        countryList.sort();
        $scope.countries = countryList;
      }
      if ($scope.process && $scope.process.stuckCounts){
        var stuck = [];
        for (var i in $scope.process.stuckCounts){
          if ($scope.process.stuckCounts.hasOwnProperty(i) && $scope.process.stuckCounts[i] > 0){
            stuck.push(i);
          }
        }
        stuck.sort();
        $scope.stuckProcess = stuck;
      }
    }, function(error) {
      $scope.querying = false;
      var msg = 'System checks cannot be done at the moment due to an error. Please try again later.';
      cmr.showAlert(msg);
    });
  };
  
  $scope.extract();
}]);
