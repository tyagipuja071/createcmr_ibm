var app = angular.module('LegacySearchApp', [ 'ngRoute', 'ngSanitize' ]);

app.controller('LegacySearchController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', function($scope, $document, $http, $timeout, $sanitize) {

  $scope.loaded = false;

  $scope.crit = {};
  $scope.countries = [ {
    id : '838',
    name : 'Spain'
  }, {
    id : '866',
    name : 'United Kingdom'
  }, {
    id : '754',
    name : 'Ireland'
  } ];

  $scope.search = function() {
    var paramString = JSON.stringify($scope.crit);
    var params = JSON.parse(paramString);
    var addressUses = [];
    if (params.addressUseM){
      addressUses.push('M');
    }
    if (params.addressUseB){
      addressUses.push('B');
    }
    if (params.addressUseI){
      addressUses.push('I');
    }
    if (params.addressUseS){
      addressUses.push('S');
    }
    if (params.addressUseE){
      addressUses.push('E');
    }
    if (params.addressUseF){
      addressUses.push('F');
    }
    if (params.addressUseJ){
      addressUses.push('J');
    }
    params.addressUses = addressUses;
    console.log(params);
    cmr.showProgress('Processing...');
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
  
  $scope.formatAddrUse = function(rec){
    var uses = rec.addressUses;
    var html = '';
    if (uses){
      uses.forEach(function(use, index){
        if (use == 'M'){
          html += '<div class="use-m" title="Mailing">M</div>';
        }
        if (use == 'B'){
          html += '<div class="use-b" title="Billing">B</div>';
        }
        if (use == 'I'){
          html += '<div class="use-i" title="Installing">I</div>';
        }
        if (use == 'S'){
          html += '<div class="use-s" title="Shipping">S</div>';
        }
        if (use == 'E'){
          html += '<div class="use-e" title="EPL">E</div>';
        }
        if (use == 'L'){
          html += '<div class="use-l" title="Lit Mailing">L</div>';
        }
        if (use.indexOf('U') == 0) {
          html += '<div class="use-gen" title="Use '+use.substring(1)+'">'+use.substring(1)+'</div>';
        }
      });
    }
    return html;
  }

} ]);