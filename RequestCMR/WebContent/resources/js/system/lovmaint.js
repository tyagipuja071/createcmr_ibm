var app = angular.module('LOVApp', [ 'ngRoute' ]);

app.controller('LOVController', [
    '$scope',
    '$document',
    '$http',
    '$timeout',
    function($scope, $document, $http, $timeout) {
      $scope.lov = lov;
      $scope.existing = false;
      if ($scope.lov == null) {
        $scope.lov = {};
      }
      $scope.lov.values = [];

      if ($scope.lov.cmrIssuingCntry != null && $scope.lov.fieldId != null) {
        $scope.existing = true;
        var results = cmr.query('LOV.GETLIST', {
          CNTRY : $scope.lov.cmrIssuingCntry,
          ID : $scope.lov.fieldId,
          _qall : 'Y'
        });
        if (results != null && results.length > 0) {
          results.forEach(function(result, index) {
            $scope.lov.values.push({
              cd : result.ret1 == '' ? '-BLANK-' : result.ret1,
              txt : result.ret2,
              defaultInd : result.ret3 == 'Y' ? true : false,
              dispOrder : index + 1,
              status : 'E'
            });
          });
        }
      } else {
        var countries = cmr.query('SYSTEM.SUPPCNTRY', {
          _qall : 'Y'
        });
        var ids = cmr.query('SYSTEM.FIELDIDLIST', {
          _qall : 'Y'
        });
        $scope.countries = countries;
        $scope.ids = ids;
      }

      $scope.handleDefault = function(entry) {
        var checked = entry.defaultInd;
        if (checked) {
          $scope.lov.values.forEach(function(item, index) {
            item.defaultInd = false;
          });
          entry.defaultInd = true;
        }
      };
      $scope.editValue = function(entry) {
        entry.status = 'M';
      };

      $scope.addValue = function(entry) {
        var order = entry.dispOrder + 1;
        var newValue = {
          dispOrder : order,
          cd : '',
          txt : '',
          defaultInd : false,
          status : 'N'
        };
        $scope.lov.values.sort(lovfieldSort);
        var increment = false;
        for (var i = 0; i < $scope.lov.values.length; i++) {
          // this marks the movement
          if ($scope.lov.values[i].dispOrder == order) {
            increment = true;
          }
          if (increment) {
            $scope.lov.values[i].dispOrder += 1;
          }
        }
        $scope.lov.values.push(newValue);
        $scope.lov.values.sort(lovfieldSort);

      };

      $scope.addBlankValue = function(entry) {
        var order = entry.dispOrder + 1;
        var newValue = {
          dispOrder : order,
          cd : '-BLANK-',
          txt : '',
          defaultInd : false,
          status : 'N'
        };
        $scope.lov.values.sort(lovfieldSort);
        var increment = false;
        for (var i = 0; i < $scope.lov.values.length; i++) {
          // this marks the movement
          if ($scope.lov.values[i].dispOrder == order) {
            increment = true;
          }
          if (increment) {
            $scope.lov.values[i].dispOrder += 1;
          }
        }
        $scope.lov.values.push(newValue);
        $scope.lov.values.sort(lovfieldSort);

      };

      $scope.unRemoveValue = function(entry) {
        entry.status = 'M';
      }

      $scope.removeValue = function(entry) {
        if (entry.cd != '') {
          if (!confirm('Remove value ' + entry.cd + '?')) {
            return;
          }
        }
        if (entry.status == 'E' || entry.status == 'M') {
          entry.status = 'D';
          return;
        }
        var order = entry.dispOrder;
        $scope.lov.values.splice(order - 1, 1);
        $scope.lov.values.sort(lovfieldSort);
        for (var i = 0; i < $scope.lov.values.length; i++) {
          $scope.lov.values[i].dispOrder = i + 1;
        }
        if ($scope.lov.values.length == 0) {
          $scope.addValue({
            dispOrder : 0
          });
        }
      };

      $scope.moveValueUp = function(entry) {
        if (entry.dispOrder == 1) {
          return;
        }
        var order = entry.dispOrder - 1;
        for (var i = 0; i < $scope.lov.values.length; i++) {
          // this marks the movement
          if ($scope.lov.values[i].dispOrder == order) {
            $scope.lov.values[i].dispOrder += 1;
            break;
          }
        }
        entry.dispOrder = order;
        $scope.lov.values.sort(lovfieldSort);
      };

      $scope.saveAll = function() {
        if ($scope.lov.cmrIssuingCntry == null || $scope.lov.cmrIssuingCntry == '') {
          cmr.showAlert('Please specify Field ID, CMR Issuing Country, and Display Type.');
          return;
        }
        if ($scope.lov.fieldId == null || $scope.lov.fieldId == '') {
          cmr.showAlert('Please specify Field ID, CMR Issuing Country, and Display Type.');
          return;
        }
        if ($scope.lov.dispType == null || $scope.lov.dispType == '') {
          cmr.showAlert('Please specify Field ID, CMR Issuing Country, and Display Type.');
          return;
        }

        var entry = null;
        var map = {};
        for (var i = 0; i < $scope.lov.values.length; i++) {
          entry = $scope.lov.values[i];
          if (entry.cd == null || entry.cd == '' || entry.txt == null || entry.txt == '') {
            cmr.showAlert('Please Code and Description for all LOV entries.');
            return;
          }
          if (map[entry.cd] == 'X') {
            cmr.showAlert('Please enter unique Codes for the entries.');
            return;
          }
          if (map[entry.cd] == null && entry.status != 'D') {
            map[entry.cd] = 'X';
          }

        }
        $scope.lov['delete'] = false;

        var temp = JSON.stringify($scope.lov);
        var toSend = JSON.parse(temp);

        if ($scope.lov.values == null || $scope.lov.values.length < 1) {
          cmr.showAlert('Please specify at least 1 entry.');
          return;
        } else if ($scope.lov.values.length == 1) {
          toSend.values.push({
            dispOrder : 2,
            cd : 'DUMMY',
            txt : 'DUMMY',
            defaultInd : false
          });
        }

        cmr.showProgress('Saving, please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/code/lovs/process.json',
          method : 'POST',
          params : toSend
        }).then(function(response) {
          cmr.hideProgress();
          if (response.data.success) {
            $scope.existing = true;
            cmr.showAlert('List of values saved successfully', null, "refreshLOVPage()", true);
          } else {
            cmr.showAlert('An error occurred while saving the values.');
          }
        }, function(response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
        });
      };

      $scope.deleteLOV = function() {
        $scope.lov['delete'] = true;
        if (!confirm('Remove the list of values for ' + $scope.lov.fieldId + ' under ' + $scope.lov.cmrIssuingCntry + '?')) {
          return;
        }
        cmr.showProgress();
        $http({
          url : cmr.CONTEXT_ROOT + '/code/lovs/process.json',
          method : 'POST',
          params : $scope.lov
        }).then(function(response) {
          cmr.hideProgress();
          $scope.lov['delete'] = false;
          var url = cmr.CONTEXT_ROOT + '/code/lovs?infoMessage=LOV list deleted successfully.';
          window.location = url;
          console.log(response);
        }, function(response) {
          cmr.hideProgress();
          $scope.lov['delete'] = false;
          console.log('error: ');
          console.log(response);
        });
      };

      $scope.backToList = function() {
        var url = cmr.CONTEXT_ROOT + '/code/lovs?';
        if ($scope.lov.fieldId != null && $scope.lov.fieldId != '') {
          url += 'fieldId=' + encodeURIComponent($scope.lov.fieldId);
        }
        if ($scope.lov.cmrIssuingCntry != null && $scope.lov.cmrIssuingCntry != '') {
          url += '&cmrIssuingCntry=' + encodeURIComponent($scope.lov.cmrIssuingCntry);
        }
        window.location = url;
      };

      $scope.checkExistingLov = function() {
        var fieldId = $scope.lov.fieldId;
        var cntry = $scope.lov.cmrIssuingCntry;
        if (fieldId && cntry) {
          var ret = cmr.query('CHECK_EXISTING_LOV', {
            ID : fieldId,
            CNTRY : cntry
          });
          if (ret && ret.ret1 == '1') {
            var msg = 'The selected Field ID <strong>' + fieldId + '</strong> and country <strong>' + cntry
                + '</strong> already has an existing list of values. Do you want to load the existing values or select another combination?';
            var cd = ret.ret2;
            $scope.lov.fieldId = null;
            $scope.lov.cmrIssuingCntry = null;
            cmr.showConfirm('selectAnotherLov()', msg, 'Warning', 'loadExistingLov("' + fieldId + '","' + cntry + '", "' + cd + '")', {
              OK : 'Select Another',
              CANCEL : 'Load Existing'
            });
          }
        }
      };
    } ]);

function selectAnotherLov() {
}

function loadExistingLov(fieldId, cntry, cd) {
  goToUrl(cmr.CONTEXT_ROOT + '/code/lovsmain?fieldId=' + encodeURIComponent(fieldId) + '&cmrIssuingCntry=' + cntry + '&cd=' + cd);
}

function refreshLOVPage() {
  window.location.reload();
}
function lovfieldSort(field1, field2) {
  return field1.dispOrder < field2.dispOrder ? -1 : (field1.dispOrder > field2.dispOrder ? 1 : 0);
}
