var app = angular.module('CrisReportApp', ['ngRoute']);

app.controller('CrisReportController', ['$scope', '$document', '$http', '$timeout', function ($scope, $document, $http, $timeout) {

    $scope.listed = false;
    $scope.files = [];

    $scope.getReport = function() {
        if (!$scope.timeframe) {
          alert('A timeframe must be selected.');
          return;
        }

        cmr.showProgress('Exporting request details. Please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/crisreport/export.json?timeframe=' + $scope.timeframe,
          method : 'GET',
          responseType : 'blob' // Set the responseType to blob
        }).then(function(response) {
          cmr.hideProgress();
          console.log('success');

          // Handle file download
          var blob = new Blob([ response.data ], {
            type : 'text/plain'
          }); // Adjust the type if needed
          var downloadUrl = window.URL.createObjectURL(blob);
          var a = document.createElement("a");
          a.href = downloadUrl;
          a.download = "CRISReport.txt";
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);

        }, function(response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
          alert('An error occurred while processing. Please try again later.');
        });
      };

}]);
