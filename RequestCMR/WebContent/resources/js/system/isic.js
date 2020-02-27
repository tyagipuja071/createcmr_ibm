var app = angular.module('IsicApp', [ 'ngRoute' ]);

app.filter('isicFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(isic, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (isic.reftUnsicCd.indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.filter('isicDescFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(isic, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (isic.reftUnsicAbbrevDesc.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.filter('subIndFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(isic, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (isic.indclCd.indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.filter('subIndDescFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(isic, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      if (isic.indclAbbrevDesc.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.controller('IsicController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.isicList = [];
  $scope.subIndustries = [];
  $scope.adding = false;
  $scope.geoCd = 'US';
  $scope.searching = false;
  $scope.showIsic = function() {
    if (!$scope.geoCd) {
      cmr.showAlert('Please select a GEO.');
      return;
    }
    $scope.searching = true;
    cmr.showProgress('Loading ISIC list for ' + $scope.geoCd);
    $scope.searchIsic = '';
    $scope.searchIsicDesc = '';
    $scope.searchSubInd = '';
    $scope.searchSubIndDesc = '';
    $timeout($scope.retreiveIsic, 500);
  }
  $scope.newSic = {
    reftUnsicKey : 0,
    reftUnsicCd : '',
    reftUnsicAbbrevDesc : '',
    reftIndclKey : '',
    indclCd : '',
    indclAbbrevDesc : '',
    geoCd : '',
    subObj : null
  };

  $scope.retreiveIsic = function() {
    $scope.isicList = [];
    var list = cmr.query('CODE.ISIC.GET_ALL', {
      GEO : $scope.geoCd,
      _qall : 'Y'
    });
    if (list && list.length > 0) {
      list.forEach(function(isic, i) {
        $scope.isicList.push({
          reftUnsicKey : isic.ret1,
          reftUnsicCd : isic.ret2,
          reftUnsicAbbrevDesc : isic.ret3,
          reftIndclKey : isic.ret4,
          indclCd : isic.ret5,
          indclAbbrevDesc : isic.ret6,
          geoCd : isic.ret7,
          edit : false,
          subObj : null
        });
      });
    }
    $scope.isicList.forEach(function(isic, i) {
      $scope.subIndustries.forEach(function(sub, j) {
        if (isic.reftIndclKey == sub.id) {
          isic.subObj = sub;
        }
      });
    });
    cmr.hideProgress();

  }

  $scope.editIsic = function(isic) {
    isic.old = {
      reftUnsicAbbrevDesc : isic.reftUnsicAbbrevDesc,
      reftIndclKey : isic.reftIndclKey,
      subObj : isic.subObj
    };
    isic.edit = true;
  }

  $scope.undoEdit = function(isic) {
    if (!isic.old) {
      return;
    }
    isic.reftUnsicAbbrevDesc = isic.old.reftUnsicAbbrevDesc;
    isic.reftIndclKey = isic.old.reftIndclKey;
    isic.subObj = isic.old.subObj;
    isic.edit = false;
  }

  $scope.undoEditNew = function(isic) {
    $scope.newSic = {
      reftUnsicKey : 0,
      reftUnsicCd : '',
      reftUnsicAbbrevDesc : '',
      reftIndclKey : '',
      indclCd : '',
      indclAbbrevDesc : '',
      geoCd : '',
      subObj : null
    };
    $scope.adding = false;
  }

  $scope.saveIsicNew = function() {
    if (!$scope.newSic.reftUnsicCd || !$scope.newSic.reftUnsicAbbrevDesc || !$scope.newSic.subObj) {
      alert('Please specify ISIC Code, Description, and map the corresponding Industry.');
      return;
    }
    if ($scope.newSic.reftUnsicCd.length != 4) {
      alert('ISIC Code should be exactly 4 characters.');
      return;
    }
    var ret = false;
    $scope.isicList.forEach(function(isic, i) {
      if (isic.reftUnsicCd == $scope.newSic.reftUnsicCd && isic.geoCd == $scope.geoCd) {
        alert('ISIC ' + $scope.newSic.reftUnsicCd + ' already mapped under this GEO.');
        ret = true;
        return;
      }
    });
    if (ret) {
      return;
    }
    $scope.saveIsic($scope.newSic, true);
  }

  $scope.saveIsic = function(isic, refresh) {
    var params = {
      reftUnsicKey : isic.reftUnsicKey,
      reftUnsicAbbrevDesc : isic.reftUnsicAbbrevDesc,
      reftIndclKey : isic.subObj.id,
      geoCd : isic.geoCd ? isic.geoCd : $scope.geoCd,
      reftUnsicCd : isic.reftUnsicCd,
    };

    console.log(params);
    cmr.showProgress('Saving SIC details, please wait..');
    $http.post(cmr.CONTEXT_ROOT + '/code/isic/process.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        if (!refresh) {
          cmr.showAlert('Record saved successfully.', null, null, true);
          isic.edit = false;
          isic.reftIndclKey = isic.subObj.id;
          isic.indclCd = isic.subObj.code;
          isic.indclAbbrevDesc = isic.subObj.description;
        } else {
          alert('Record saved successfully. The list will be refreshed.');
          $scope.undoEditNew();
          $scope.retreiveIsic();
        }
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  }

  $scope.getSubindustries = function() {
    var subs = cmr.query('CODE.ISIC.GET_SUBIND', {
      _qall : 'Y'
    });
    $scope.subIndustries = [];
    if (subs && subs.length > 0) {
      subs.forEach(function(sub, i) {
        $scope.subIndustries.push({
          id : sub.ret1,
          name : sub.ret2 + ' - ' + sub.ret3,
          code : sub.ret2,
          description : sub.ret3
        })
      });
    }
  };

  $scope.newIsic = function() {
    $scope.adding = true;
  };

  $scope.changeGEO = function() {
    if (!confirm('Switching GEOs will clear the current list and lose all unsaved changes. Proceed?')) {
      return;
    }
    $scope.undoEditNew();
    $scope.isicList = [];
    $scope.searching = false;
  };

  $scope.getSubindustries();

} ]);
