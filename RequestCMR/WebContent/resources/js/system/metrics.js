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
                  circumference : 4 * Math.PI,
                  legend : {
                    position : 'top',
                    labels : {
                      boxWidth : 15,
                      fontFamily : 'IBM Plex Sans'
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
    },
    exportRequesterStats : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      if (!confirm('The Requester Statistics does not consider the Processing Center selected and the option to exclude non-submitted requests. Proceed?')){
        return
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].action = cmr.CONTEXT_ROOT + "/metrics/requesterexport";
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    },
    exportAutoStats : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);
      document.forms['frmCMR'].action = cmr.CONTEXT_ROOT + "/metrics/autoexport";
      document.forms['frmCMR'].target = "exportFrame";
      document.forms['frmCMR'].submit();
    },
    visualizeAutoStats : function() {
      if (!FormManager.validate('frmCMR')) {
        return;
      }
      var from = FormManager.getActualValue('dateFrom');
      var to = FormManager.getActualValue('dateTo');
      var groupByGeo = FormManager.getActualValue('groupByGeo');
      var groupByProcCenter = FormManager.getActualValue('groupByProcCenter');
      var country = FormManager.getActualValue('country');
      var excludeUnsubmitted = FormManager.getActualValue('excludeUnsubmitted');
      
      cmr.showProgress('Generating charts, please wait...');

      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/metrics/autochart.json',
        handleAs : 'json',
        method : 'POST',
        content : {
          dateFrom : from,
          dateTo : to,
          groupByProcCenter : groupByProcCenter,
          country : country,
          groupByGeo : groupByGeo,
          excludeUnsubmitted : excludeUnsubmitted
        },
        timeout : 90000,
        sync : false,
        load : function(data, ioargs) {
          console.log('success');
          console.log(data);
          cmr.hideProgress();
          var data = data.data;
          if (data){
            dojo.byId('charts-cont').innerHTML = '';
            var html = '';  
            for (var cntry in data){
              var stat = data[cntry];
              html += '<div class="ibm-columns">';
              html += '  <div class="ibm-col-6-3">';
              html += '    <div style="text-decoration:underline;font-weight:bold;font-size:13px">'+stat.country+'</div>';
              html += '  </div>';
              html += '</div>';
              html += '<div class="ibm-columns">';
              html += '  <div class="ibm-col-6-3" style="border:1px Solid #AAA;width:450px">';
              html += '    <canvas id="canvas-'+cntry+'" style="height:400px"></canvas>';
              html += '  </div>';
              html += '  <div class="ibm-col-6-3" style="border:1px Solid #AAA;width:450px">';
              html += '    <canvas id="canvas-rev-'+cntry+'" style="height:400px"></canvas>';
              html += '  </div>';
              html += '</div>';
              html += '<div class="ibm-columns">&nbsp;';
              html += '</div>';
            }  
            dojo.byId('charts-cont').innerHTML = html;
            for (var cntry in data){
              var stat = data[cntry];
              var ctx = dojo.byId("canvas-"+cntry).getContext("2d");
              
              var dataSet = [];
              var labelSet = [];
              var colorSet = [];
              var totals = 0;

              var total = stat.touchless + stat.legacy + stat.review + stat.noStatus;
              dataSet.push(stat.touchless);
              labelSet.push('Touchless ('+(total > 0 ? ((stat.touchless/total)*100).toFixed(1) : 0)+'%)');
              colorSet.push('rgb(57,198,75)');

              dataSet.push(stat.legacy);
              labelSet.push('Legacy Issues ('+(total > 0 ? ((stat.legacy/total)*100).toFixed(1) : 0)+'%)');
              colorSet.push('rgb(237,221,18)');

              dataSet.push(stat.review);
              labelSet.push('Review Required ('+(total > 0 ? ((stat.review/total)*100).toFixed(1) : 0)+'%)');
              colorSet.push('rgb(185,70,73)');

              dataSet.push(stat.noStatus);
              labelSet.push('Pending/Unknown ('+(total > 0 ? ((stat.noStatus/total)*100).toFixed(1) : 0)+'%)');
              colorSet.push('rgb(174,174,174)');

              var chart = new Chart(ctx, {
                type: 'pie',
                data: {
                  labels: labelSet,
                  datasets: [{
                    backgroundColor: colorSet,
                    data: dataSet
                  }]
                },
                options : {
                  title : {
                    display : true,
                    text : 'Percentage of Touchless vs Reviewed (Total: '+total+')'
                  },
                  tooltips : {
                    mode : 'index',
                    intersect : false
                  },
                  legend : {
                    display : true,
                    position : 'left',
                    labels : {
                      boxWidth : 16,
                      fontSize : 11,
                      padding : 6
                    }
                  },
                  responsive : true,
                }
              });

              // review chart
              var ctx2 = dojo.byId("canvas-rev-"+cntry).getContext("2d");
              
              var dataSet2 = [];
              var labelSet2 = [];
              var colorSet2 = [];
              var totals2 = 0;

              var map = stat.reviewMap;
              var total2 = 0;
              for (var rev in map){
                total2 += map[rev];
              }
              for (var rev in map){
                dataSet2.push(map[rev]);
                labelSet2.push(rev+ ' ('+(total2 > 0 ? ((map[rev]/total2)*100).toFixed(1) : 0)+'%)');
                colorSet2.push(CmrMetrics.getRandomColor());
              }
              var chart = new Chart(ctx2, {
                type: 'pie',
                data: {
                  labels: labelSet2,
                  datasets: [{
                    backgroundColor: colorSet2,
                    data: dataSet2
                  }]
                },
                options : {
                  title : {
                    display : true,
                    text : 'Percentage of Review Reasons (Total: '+total2+')'
                  },
                  tooltips : {
                    mode : 'index',
                    intersect : false
                  },
                  legend : {
                    display : true,
                    position : 'left',
                    labels : {
                      boxWidth : 16,
                      fontSize : 11,
                      padding : 6
                    }
                  },
                  responsive : true,
                }
              });
            }
            
            
          } else {
            cmr.showAlert('An error occurred during chart generation. Please try again later.');
          }

        },
        error : function(error, ioargs) {
          console.log(error);
          cmr.showAlert('An error occurred during chart generation. Please try again later.');
          cmr.hideProgress();
        }
      });
      return;
    }
  };
})();