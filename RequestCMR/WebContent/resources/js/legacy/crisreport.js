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
          responseType : 'blob' // Expecting a binary data
        }).then(function(response) {
          cmr.hideProgress();
          console.log('success');

          // Determine file type from Content-Type header
          var contentType = response.headers('Content-Type');
          var fileExtension = '.txt'; // Default to .txt
          var mimeType = 'text/plain'; // Default MIME type

          if (contentType.includes('text/csv')) {
            fileExtension = '.csv';
            mimeType = 'text/csv';
          }

          // Determine the filename based on $scope.timeframe
          var fileName = "CRISReport"; // Default name
          switch ($scope.timeframe) {
            case 'ROLMONTHLY':
              fileName = 'rol_monthly';
              break;
            case 'ROLDAILY':
              fileName = 'rol_daily';
              break;
            case 'TAIGAMONTHLY':
              fileName = 'taiga_monthly';
              break;
            case 'TAIGADAILY':
              fileName = 'taiga_daily';
              break;
            case 'RAONDEMAND':
              fileName = 'RA_appended_ddname';
              break;
          }

          // Handle file download
          var blob = new Blob([response.data], {
            type: mimeType
          });
          var downloadUrl = window.URL.createObjectURL(blob);
          var a = document.createElement("a");
          a.href = downloadUrl;
          a.download = fileName + fileExtension;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);

        }, function (response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
          alert('An error occurred while processing. Please try again later.');
        });
      };

}]);
