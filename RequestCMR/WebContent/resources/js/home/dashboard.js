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
  $scope.extract = function(){
    $http.get(cmr.CONTEXT_ROOT + '/monitor.json').then(function(response) {
      console.log(response);
      if (response.data && response.data.dashboardResult){
        $scope.report = response.data.dashboardResult;
        $scope.process = $scope.report.processing;
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
      var msg = 'System checks cannot be done at the moment due to an error. Please try again later.';
      cmr.showAlert(msg);
    });
  };
  
  $scope.extract();
}]);
