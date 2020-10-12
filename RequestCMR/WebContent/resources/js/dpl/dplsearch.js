var app = angular.module('DPLSearchApp', [ 'ngRoute', 'ngSanitize' ]);

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
      if (record.companyName.toUpperCase().indexOf(val) >= '0'){
        return true;
      }
      return false;
    });

  };
});

app.controller('DPLSearchController', [ '$scope', '$document', '$http', '$timeout', '$sanitize',
    function($scope, $document, $http, $timeout, $sanitize) {

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

      
      $scope.reqId = $scope.getParameterByName('reqId');
      $scope.customerName = '';
      $scope.allTextFilter = '';
      $scope.request = {
          admin : {},
          data : {},
          addresses : {}
      };
      
      $scope.getRequestInfo = function(){
        $http({
          url : cmr.CONTEXT_ROOT + '/dplsearch/process.json?reqId='+$scope.reqId+'&processType=REQ',
          method : 'GET'
        }).then(function(response) {
          console.log('success');
          console.log(response.data);
          if (response.data) {
            $scope.request = response.data.data;
            $scope.customerName = $scope.request.admin.mainCustNm1 + ($scope.request.admin.mainCustNm2 ? ' '+$scope.request.admin.mainCustNm2 : '');
            switch ($scope.request.scorecard.dplChkResult) {
            case 'AF' :
              $scope.dplResult = 'All Failed';
              break;
            case 'SF' :
              $scope.dplResult = 'Some Failed';
              break;
            case 'NR' :
              $scope.dplResult = 'Not Required';
              break;
            case 'AP' :
              $scope.dplResult = 'All Passed';
              break;
            default :
              $scope.dplResult = 'Not Done';
              break;
            }
            
            var ts = $scope.request.scorecard.dplChkTs;
            if (ts){
              var dt = new Date(ts);
              $scope.dplCheckDate = moment(dt).format('YYYY-MM-DD HH:mm:ss');
            } else {
              $scope.dplCheckDate = '';
            }
            
            $scope.request.addresses.forEach(function(addr,i){
              var status = '';
              switch (addr.dplChkResult){
              case 'P':
                addr.dplChkResult = 'Passed';
                break;
              case 'F':
                addr.dplChkResult = 'Failed';
                break;
              case 'X':
                addr.dplChkResult = 'Not Required';
                break;
              default:
                addr.dplChkResult = 'Not Done';
                break;
              }
            });
            $scope.dplSearchRequest();
          } else {
            alert('An error occurred while processing. Please try again later.');
          }
        }, function(response) {
          console.log('error: ');
          console.log(response);
          alert('An error occurred while processing. Please try again later.');
        });
      };
      
      $scope.dplSearchRequest = function(){
        cmr.showProgress('Searching customer information on request from DPL database..')
        $http({
          url : cmr.CONTEXT_ROOT + '/dplsearch/process.json?reqId='+$scope.reqId+'&processType=SEARCH',
          method : 'GET'
        }).then(function(response) {
          cmr.hideProgress();
          console.log('success');
          console.log(response.data);
          if (response.data) {
            $scope.results = response.data.data;
            if ($scope.results){
              $scope.results.forEach(function(res, i){
                res.exp = true;
              });
            }
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
      
      $scope.getRequestInfo();
    } ]);
