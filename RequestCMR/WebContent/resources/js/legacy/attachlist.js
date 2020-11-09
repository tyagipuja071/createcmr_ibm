var app = angular.module('AttachApp', [ 'ngRoute' ]);

app.controller('AttachController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.listed = false;
  $scope.files = [];
  $scope.listAttachments = function(){
    
    if (!$scope.reqId || isNaN($scope.reqId)){
      alert('A valid Request ID must be specified.');
      return;
    }
    cmr.showProgress('Getting available attachments, please wait...');
    $http({
      url : cmr.CONTEXT_ROOT + '/attachlist/get.json?reqId='+$scope.reqId,
      method : 'GET'
    }).then(function(response) {
      cmr.hideProgress();
      console.log('success');
      console.log(response.data);
      if (response.data.success && response.data.data) {
        $scope.listed = true;
        $scope.files = response.data.data.files;
        console.log(response.data.data.files);
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
  
  $scope.clearAll = function(){
    $scope.reqId = '';
    $scope.listed = false;
    $scope.files = [];
  };
  
  
  $scope.download = function(file){
    document.getElementById('attachFile').value = file;
    document.forms['frmAttach'].submit();
  };
  
} ]);
