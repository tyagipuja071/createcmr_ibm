dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dojox.grid.DataGrid");
dojo.require("dojox.grid.EnhancedGrid");
dojo.require("dojox.grid.enhanced.plugins.Pagination");
dojo.require("dojox.grid.enhanced.plugins.Filter");
dojo.require("dijit.dijit");
dojo.require("dojox.layout.TableContainer");

var CmrGrid = (function() {

  var _ERRORMESSAGE = 'An error occurred while fetching the data. Please try again. If this error persists, please contact your system administrator.';
  var _CMRGRIDS = {};
  var showProgress = false;

  return {
    create : function(gridId, layout, searchUrl, hasCheckBox, params, gridHeight, hasPaging, loadOnStartup, useFilter, comparatorMap) {
      var actualParams = '';
      if (params) {
        var p = params.split('&');
        for ( var i = 0; i < p.length; i++) {
          var pair = p[i].split('=');
          if (pair.length == 2) {
            actualParams += actualParams.length > 0 ? '&' : '';
            actualParams += pair[0];
            if (pair[1].indexOf(':') == 0) {
              actualParams += '=' + encodeURIComponent(FormManager.getActualValue(pair[1].substr(1)));
            } else {
              actualParams += '=' + encodeURIComponent(pair[1]);
            }
          }
        }
      }
      var dataStore = new dojo.data.ItemFileReadStore({
        url : searchUrl + '?' + actualParams
      });
      if (comparatorMap) {
        dataStore.comparatorMap = comparatorMap;
      }
      if (loadOnStartup) {
        if (showProgress) {
          // cmr.showProgress('Loading data. Please wait...');
        }
        dataStore.fetch({
          onBegin : null,
          onComplete : function(data) {
            try {
              var loadFunc = eval(gridId + '_onLoad');
              if (loadFunc && typeof (loadFunc) == 'function') {
                loadFunc(data);
              }
            } catch (e) {

            }

          },
          onError : function(error, ioargs) {
            console.log(error);
            cmr.showAlert(_ERRORMESSAGE, 'Error');

          }
        });
      }
      var theGrid = new dojox.grid.EnhancedGrid({
        id : 'cmr_' + gridId,
        structure : layout,
        store : dataStore,
        width : 'auto',
        rowsPerPage : 25,
        noDataMessage : "<span class=\"dojoxGridNoData\">No records found.</span>",
        height : gridHeight ? gridHeight : '400px',
        selectable : true,
        onClick : function(event) {
          if (this.pagination == null) {
            return;
          }
          var trg = event.target;
          if (!trg) {
            return;
          }
          if (!(trg.localName == 'span' && trg.className == 'dojoxGridActivedSwitch')) {
            return;
          }
          console.log('refreshing grid..');
          if (this.pagination.plugin.pageSize == 100) {
            this.rowsPerPage = 100;
            theGrid._refresh();
          }
          if (this.pagination.plugin.pageSize == 75) {
            this.rowsPerPage = 75;
            theGrid._refresh();
          }
          if (this.pagination.plugin.pageSize == 50) {
            this.rowsPerPage = 50;
            theGrid._refresh();
          }
          if (this.pagination.plugin.pageSize == 25) {
            this.rowsPerPage = 25;
            theGrid._refresh();
          }
        },
        plugins : {
          pagination : hasPaging ? {
            pageSizes : [ "25", "50", "75", "100" ],
            defaultPageSize : 25,
            description : true,
            sizeSwitch : true,
            pageStepper : true,
            gotoButton : true,
            maxPageStep : 4,
            position : "bottom"
          } : null,
          filter : useFilter ? {
            // Show the closeFilterbarButton at the filter bar
            closeFilterbarButton : false,
            // Set the maximum rule count to 5
            ruleCount : 3
          } : null
        },
        canSort : function(col) {
          if (hasCheckBox) {
            return col != 1;
          } else {
            return true;
          }
        }
      }, gridId);
      /* fix for focusing the header 847753 */
      theGrid.focus._delayedCellFocus = function() {
        // do nothing, to not focus anything
      };
      if (loadOnStartup) {
        theGrid.startup();
      }
      _CMRGRIDS[gridId] = theGrid;
    },
    createCheckboxColumn : function(pkFields, gridId) {
      return {
        field : 'chk',
        name : '<input type="checkbox" id="gridchkall" name="gridchkall" onclick="CmrGrid.toggleCheckAll(this)">',
        width : '20px',
        sort : false,
        formatter : function(value, rowIndex) {
          var rowData = this.grid.getItem(rowIndex);
          var fields = pkFields.split(',');
          var chkVal = '';
          for ( var i = 0; i < fields.length; i++) {
            chkVal += (chkVal.length > 0 ? '&' : '');
            chkVal += fields[i] + '=' + (eval('rowData.' + fields[i]));
          }
          if (rowData) {
            // do nothing, just to remove warning.
          }
          var show = true;
          try {
            eval('var fn = ' + gridId + '_showCheck;');
            if (fn) {
              show = fn(value, rowIndex, this.grid);
            }
          } catch (e) {

          }
          if (show) {
            return '<input type="checkbox" name="gridchk" value="' + chkVal + '">';
          } else {
            return '';
          }
        }
      };
    },
    toggleCheckAll : function(chk) {
      var chkBoxes = document.getElementsByName('gridchk');
      if (chkBoxes) {
        for ( var i = 0; i < chkBoxes.length; i++) {
          chkBoxes[i].checked = chk.checked;
        }
      }
    },
    hasSelected : function(chkName) {
      if (!chkName) {
        chkName = 'gridchk';
      }
      var chkBoxes = document.getElementsByName(chkName);
      if (chkBoxes) {
        for ( var i = 0; i < chkBoxes.length; i++) {
          if (chkBoxes[i].checked) {
            return true;
          }
        }
      }
      return false;
    },
    load : function(gridId) {
      var grid = _CMRGRIDS[gridId + '_GRID'];
      if (grid) {
        if (showProgress) {
          // cmr.showProgress('Loading data. Please wait...');
        }
        grid.store.fetch({
          onBegin : null,
          onComplete : function(data) {
            try {
              var loadFunc = eval(gridId + '_GRID_onLoad');
              if (loadFunc && typeof (loadFunc) == 'function') {
                loadFunc(data);
              }
            } catch (e) {

            }

          },
          onError : function(error, ioargs) {
            cmr.showAlert(_ERRORMESSAGE, 'Error');

          }
        });
        grid.startup();
        grid.resize();
      }
    },
    refresh : function(gridId, newUrl, params) {
      var grid = _CMRGRIDS[gridId + '_GRID'];
      var dataStore = null;
      if (grid) {
        datastore = grid.store;
        if (newUrl) {
          var actualParams = '';
          if (params) {
            var p = params.split('&');
            for ( var i = 0; i < p.length; i++) {
              var pair = p[i].split('=');
              if (pair.length == 2) {
                actualParams += actualParams.length > 0 ? '&' : '';
                actualParams += pair[0];
                if (pair[1].indexOf(':') == 0) {
                  actualParams += '=' + encodeURIComponent(FormManager.getActualValue(pair[1].substr(1)));
                } else {
                  actualParams += '=' + encodeURIComponent(pair[1]);
                }
              }
            }
          }
          dataStore = new dojo.data.ItemFileReadStore({
            url : newUrl + '?' + actualParams
          });
          if (showProgress) {
            // cmr.showProgress('Loading data. Please wait...');
          }
          dataStore.fetch({
            onBegin : null,
            onComplete : function(data) {
              try {
                var loadFunc = eval(gridId + '_GRID_onLoad');
                if (loadFunc && typeof (loadFunc) == 'function') {
                  loadFunc(data);
                }
              } catch (e) {

              }
              if (grid.pagination != null && grid.pagination.plugin != null) {
                grid.pagination.plugin.init();
              }
              grid.store = dataStore;
              grid._refresh();
            },
            onError : function(error, ioargs) {
              console.log(error);
              cmr.showAlert(_ERRORMESSAGE, 'Error');
              dataStore = new dojo.data.ItemFileReadStore({
                data : {
                  items : []
                }
              });
              if (grid.pagination != null && grid.pagination.plugin != null) {
                grid.pagination.plugin.init();
              }
              grid.store = dataStore;
              grid._refresh();
            }
          });
        } else {
          dataStore = new dojo.data.ItemFileReadStore({
            url : grid.store._jsonFileUrl
          });
          if (showProgress) {
            // cmr.showProgress('Loading data. Please wait...');
          }
          dataStore.fetch({
            onBegin : null,
            onComplete : function(data) {
              if (grid.pagination != null && grid.pagination.plugin != null) {
                grid.pagination.plugin.init();
              }
              grid.store = dataStore;
              grid._refresh();
              try {
                var loadFunc = eval(gridId + '_GRID_onLoad');
                if (loadFunc && typeof (loadFunc) == 'function') {
                  loadFunc(data);
                }
              } catch (e) {

              }

            },
            onError : function(error, ioargs) {
              console.log(error);
              cmr.showAlert(_ERRORMESSAGE, 'Error');
              dataStore = new dojo.data.ItemFileReadStore({
                data : {
                  items : []
                }
              });
              if (grid.pagination != null && grid.pagination.plugin != null) {
                grid.pagination.plugin.init();
              }
              grid.store = dataStore;
              grid._refresh();
            }
          });
        }

        // grid.resize();
      }
    },
    GRIDS : _CMRGRIDS,
    correct : function(gridId) {
      var grid = _CMRGRIDS[gridId + '_GRID'];
      if (grid) {
        grid.resize();
      }
    },
    showProgressInd : function() {
      showProgress = true;
    },
    getCurrentStoreURL : function(gridId) {
      _CMRGRIDS[gridId + '_GRID'].store._jsonFileUrl;
    }
  };
})();