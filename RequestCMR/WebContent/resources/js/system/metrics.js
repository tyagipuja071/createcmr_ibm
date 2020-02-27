var CmrMetrics = (function() {
  var chart = null;
  var currentDataSet = null;

  var buildFilters = function() {
    dojo.byId('filterlabels').innerHTML = '';
    var filter = '<table cellspacing="0" cellpadding="0" border="0">';
    var labels = new Array();
    currentDataSet.forEach(function(ds, i) {
      labels.push(ds.label);
    });
    labels = labels.sort();
    labels.forEach(function(label, i) {
      filter += '<tr>';
      filter += '<td style="width:130px"><input type="checkbox" name="filter" value="' + label + '" checked>' + label + '</td>';
      filter += '</tr>';
    });
    filter += '</table>';
    dojo.byId('filterlabels').innerHTML = filter;

    cmr.showNode('filters');
  };
  return {
    getCurrentChart : function() {
      return chart;
    },
    getRandomColor : function() {
      var r = Math.floor(Math.random() * 255);
      var g = Math.floor(Math.random() * 255);
      var b = Math.floor(Math.random() * 255);
      return 'rgb(' + r + ',' + g + ',' + b + ')';
    },
    initChart : function() {
      if (!dojo.byId('canvas')) {
        cmr.showAlert('Chart cannot be initialized.');
        return;
      }
    },
    generateReport : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      var from = FormManager.getActualValue('dateFrom');
      var to = FormManager.getActualValue('dateTo');
      var reportType = FormManager.getActualValue('reportType');
      var countType = FormManager.getActualValue('countType');
      var groupByGeo = FormManager.getActualValue('groupByGeo');
      cmr.showProgress('Generating report, please wait..');

      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/metrics/generate.json',
        handleAs : 'json',
        method : 'POST',
        content : {
          dateFrom : from,
          dateTo : to,
          reportType : reportType,
          countType : countType,
          groupByGeo : groupByGeo
        },
        timeout : 50000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          if (data.success) {
            var chartTitle = 'Requests ' + (countType == 'C' ? 'Created ' : 'Processed ') + 'by ';
            switch (reportType) {
            case 'G':
              chartTitle += 'GEO ';
              break;
            case 'C':
              chartTitle += 'Country ';
              break;
            case 'T':
              chartTitle += 'Request Type ';
              break;
            case 'S':
              chartTitle += 'Request Status ';
              break;
            }
            chartTitle += ' from ' + from + ' to ' + to;
            if (data.chart.labels.length == 0) {
              cmr.showAlert('No Data for this period.');
              return;
            }
            if (data.chart.datasets.length == 0) {
              cmr.showAlert('No Data for this period.');
              return;
            }
            data.chart.datasets.forEach(function(dataset, i) {
              var color = CmrMetrics.getRandomColor();
              dataset.backgroundColor = color;
            });

            var dsString = JSON.stringify(data.chart.datasets);
            currentDataSet = JSON.parse(dsString);

            if (groupByGeo != '') {
              chartTitle += ' (' + groupByGeo + ')';
            }
            var ctx = dojo.byId("canvas").getContext("2d");
            if (chart != null) {
              chart.data = data.chart;
              chart.options.title.text = chartTitle;
              chart.update();
            } else {
              chart = new Chart(ctx, {
                type : 'bar',
                data : data.chart,
                options : {
                  title : {
                    display : true,
                    text : chartTitle
                  },
                  tooltips : {
                    mode : 'index',
                    intersect : false
                  },
                  legend : {
                    position : 'top',
                    labels : {
                      boxWidth : 15,
                      fontFamily : 'Calibri'
                    }
                  },
                  responsive : true,
                  scales : {
                    xAxes : [ {
                      stacked : true
                    } ],
                    yAxes : [ {
                      stacked : true
                    } ]
                  }
                }
              });
            }
            buildFilters();
          } else {
            console.log(data);
            cmr.showAlert(data.error);
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          console.log(error);
        }
      });
      return;
    },
    updateChart : function() {
      var checkboxes = document.getElementsByName('filter');
      var newDs = new Array();
      if (checkboxes) {
        checkboxes.forEach(function(chk, i) {
          if (chk.checked) {
            for ( var i = 0; i < currentDataSet.length; i++) {
              if (currentDataSet[i].label == chk.value) {
                newDs.push(currentDataSet[i]);
              }
            }
          }
        });
        chart.data.datasets = newDs;
        chart.options.scales.yAxes[0].ticks.suggestedMax = chart.scales['y-axis-0'].max;
        chart.update();
      }
    },
    selectFilters : function(check) {
      var checkboxes = document.getElementsByName('filter');
      if (checkboxes) {
        checkboxes.forEach(function(chk, i) {
          chk.checked = check;
        });
      }
    },
    exportReport : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    },
    exportReport : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    },
    exportStats : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].action = cmr.CONTEXT_ROOT + "/metrics/statexport";
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    },
    exportSquadStats : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].action = cmr.CONTEXT_ROOT + "/metrics/squadexport";
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    }
  };
})();