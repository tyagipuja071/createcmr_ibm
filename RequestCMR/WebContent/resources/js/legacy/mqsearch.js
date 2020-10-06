var app = angular.module('QueryApp', [ 'ngRoute' ]);

app.controller('QueryController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.countries = [];
  $scope.results = [];
  $scope.dataElems = [];
  $scope.addresses = [];
  $scope.system = '';
  $scope.loaded = false;

  $scope.getCountries = function() {
    $scope.countries = [];
    var countries = cmr.query('GET_MQ_COUNTRIES', {
      _qall : 'Y'
    });
    countries.forEach(function(cntry, i) {
      $scope.countries.push({id : cntry.ret1, name : cntry.ret2});
    });
  };
  
  $scope.search = function(){
    $scope.dataElems = [];
    $scope.addresses = [];
    $scope.loaded = false;
    $scope.system = '';

    if (!$scope.country || !$scope.cmrNo){
      alert('CMR Issuing Country and CMR No. are both required.');
      return;
    }
    cmr.showProgress('Searching, please wait...');
    $http({
      url : cmr.CONTEXT_ROOT + '/mqsearch/process.json?country='+$scope.country+'&cmrNo='+$scope.cmrNo,
      method : 'GET'
    }).then(function(response) {
      cmr.hideProgress();
      console.log('success');
      console.log(response.data);
      if (response.data && response.data.result && response.data.result.success) {
        var result = response.data.result.record;
        $scope.loaded = true;
        $scope.system = response.data.result.system;
        $scope.dataElems = [];
        console.log(result);
        if ($scope.system == 'SOF'){
          for (const prop in result.dataElements){
            $scope.dataElems.push({name : result.dataElements[prop].name, value : result.dataElements[prop].value});  
          }
          result.addresses.forEach(function(addr, i){
            var atts = [];
            for (const prop in addr.attributes){
              atts.push({name : addr.attributes[prop].name, value : addr.attributes[prop].value});  
            }
            $scope.addresses.push({type : addr.type, seqNo : addr.seqNo, atts : atts});
          });
        } else if ($scope.system == 'WTAAS'){
          for (const prop in result.values){
            $scope.dataElems.push({name : prop, value : result.values[prop]});  
          }
          var sequences = [];
          var useMap = {};
          result.addresses.forEach(function(addr, i){
            if (sequences.indexOf(addr.addressNo) < 0){
              sequences.push(addr.addressNo);
              useMap[addr.addressNo] =  addr.addressUse;

              var atts = []; 
              for (const prop in addr.values){
                atts.push({name : prop, value : addr.values[prop]});  
              }
              atts.sort(function(a,b){
                return a.name > b.name;
              });
              $scope.addresses.push({type : addr.addressUse, seqNo : addr.addressNo, atts : atts});
            
            } else {
              useMap[addr.addressNo] =  useMap[addr.addressNo] + addr.addressUse
            }
          });
          $scope.addresses.forEach(function(addr, i){
            addr.type = useMap[addr.seqNo];
          });
        }
      } else {
        if (response.data.result && response.data.result.msg){
          alert('Error when querying: '+response.data.result.msg);
        } else {
          alert('An error occurred while processing. Please try again later.');
        }
      }
    }, function(response) {
      cmr.hideProgress();
      console.log('error: ');
      console.log(response);
      alert('An error occurred while processing. Please try again later.');
    });
    
  };
  $scope.getCountries();

} ]);

