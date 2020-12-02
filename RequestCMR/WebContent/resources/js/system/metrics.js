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
      var reqType = FormManager.getActualValue('reqType');
      var sourceSystId = FormManager.getActualValue('sourceSystId');
      
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
          excludeUnsubmitted : excludeUnsubmitted,
          reqType : reqType,
          sourceSystId : sourceSystId
        },
        timeout : 180000,
        sync : false,
        load : function(data, ioargs) {
          console.log('success');
          cmr.hideProgress();
          var weekly = data.weekly;
          var scenario = data.scenario;
          var data = data.data;
          if (!data){
            cmr.showAlert('No automation data for the time period.');
            return;
          }
          if (data){
            dojo.byId('charts-cont').innerHTML = '';
            var html = '';  
            for (var cntry in data){
              var stat = data[cntry];
              html += '<div class="ibm-columns">';
              html += '  <div class="ibm-col-6-3" style="padding-left:10px">';
              html += '    <div style="text-decoration:underline;font-weight:bold;font-size:13px">'+stat.country+'</div>';
              html += '  </div>';
              html += '</div>';
              html += '<div class="ibm-columns" style="padding:10px">';
              html += '  <div class="ibm-col-6-3" style="border:1px Solid #AAA;width:510px;margin-right:10px">';
              html += '    <canvas id="canvas-'+cntry+'" style="height:400px"></canvas>';
              html += '  </div>';
              html += '  <div class="ibm-col-6-3" style="border:1px Solid #AAA;width:510px">';
              html += '    <canvas id="canvas-rev-'+cntry+'" style="height:400px"></canvas>';
              html += '  </div>';
              html += '</div>';
              if (country != ''){
                html += '<div class="ibm-columns" style="padding:10px">';
                html += '  <div class="ibm-col-6-3" style="border:1px Solid #AAA;width:510px;margin-right:10px">';
                html += '    <canvas id="canvas-weekly-'+cntry+'" style="height:500px"></canvas>';
                html += '  </div>';
                html += '</div>';
                html += '<div class="ibm-columns" style="padding:10px">';
                html += '  <div class="ibm-col-1-1" style="border:1px Solid #AAA;width:1000px">';
                html += '    <canvas id="canvas-sc-'+cntry+'" style="height:400px"></canvas>';
                html += '  </div>';
                html += '</div>';
                html += '<div class="ibm-columns" style="padding:10px">';
                html += '  <div class="ibm-col-1-1" style="border:1px Solid #AAA;width:1000px">';
                html += '    <canvas id="canvas-sc2-'+cntry+'" style="height:400px"></canvas>';
                html += '  </div>';
                html += '</div>';
              }
              html += '<div class="ibm-columns">&nbsp;';
              html += '</div>';
            }  
            // create the charts
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
              
              var sorted = [];
              for (var rev in map){
                sorted.push({
                  val : map[rev],
                  rev : rev
                });
              }
              sorted.sort(function(a,b){
                return a.val < b.val;
              })
              sorted.forEach(function(s,i){
                var rev = s.rev;
                dataSet2.push(map[rev]);
                labelSet2.push(rev+ ' ('+(total2 > 0 ? ((map[rev]/total2)*100).toFixed(1) : 0)+'%)');
                colorSet2.push(CmrMetrics.getRandomColor());
              });
              for (var rev in map){
                //dataSet2.push(map[rev]);
                //labelSet2.push(rev+ ' ('+(total2 > 0 ? ((map[rev]/total2)*100).toFixed(1) : 0)+'%)');
                //colorSet2.push(CmrMetrics.getRandomColor());
              }
              var chart2 = new Chart(ctx2, {
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
              
              if (country != ''){
                // weekly trend
                var ctx3 = dojo.byId("canvas-weekly-"+cntry).getContext("2d");
                
                var wData = weekly[cntry];
                var allData = [];
                for (var dt in wData){
                  allData.push({
                    lbl : dt,
                    t : wData[dt].touchless,
                    r : wData[dt].review,
                    l : wData[dt].legacy,
                    n : wData[dt].noStatus
                  });
                }
                allData.sort(function(a,b,){
                  return a.lbl > b.lbl;
                });
                
                var tSet = {
                    label : 'Touchless',
                    fill : false,
                    backgroundColor : 'rgb(57,198,75)',
                    borderColor : 'rgb(57,198,75)',
                    data : []
                };
                var rSet = {
                    label : 'Review Required',
                    fill : false,
                    backgroundColor : 'rgb(185,70,73)',
                    borderColor : 'rgb(185,70,73)',
                    data : []
                };
                var lSet = {
                    label : 'Legacy Issues',
                    fill : false,
                    backgroundColor : 'rgb(237,221,18)',
                    borderColor : 'rgb(237,221,18)',
                    data : []
                };
                var nSet = {
                    label : 'Pending/Unknown',
                    fill : false,
                    backgroundColor : 'rgb(174,174,174)',
                    borderColor : 'rgb(174,174,174)',
                    data : []
                };
                var labels = [];
                allData.forEach(function(d,i){
                  labels.push(d.lbl);
                  var total = d.t + d.r + d.n + d.l;
                  tSet.data.push(((d.t/total)*100).toFixed(1));
                  rSet.data.push(((d.r/total)*100).toFixed(1));
                  nSet.data.push(((d.n/total)*100).toFixed(1));
                  lSet.data.push(((d.l/total)*100).toFixed(1));
                });
                
                var config = {
                    type: 'line',
                    data: {
                      labels: labels,
                      datasets: [tSet, rSet, nSet, lSet]
                    },
                    options: {
                      responsive: true,
                      title: {
                        display: true,
                        text: 'Weekly Trend of Automation'
                      },
                      tooltips: {
                        mode: 'index',
                        intersect: false,
                      },
                      hover: {
                        mode: 'nearest',
                        intersect: true
                      },
                      legend : {
                        display : true,
                        position : 'top',
                        labels : {
                          boxWidth : 16,
                          fontSize : 11,
                          padding : 6
                        }
                      },
                      scales: {
                        xAxes: [{
                          display: true,
                          scaleLabel: {
                            display: true,
                            labelString: 'Week of (Start)',
                            fontSize : 11
                          },
                          ticks : {
                            fontSize : 11
                          }
                        }],
                        yAxes: [{
                          display: true,
                          scaleLabel: {
                            display: true,
                            labelString: '% of Requests',
                            fontSize : 11
                          },
                          ticks: {
                            min: 0,
                            max: 100,
                            stepSize: 20,
                            fontSize : 11
                          }
                        }]
                      }
                    }
                  };
                
                  var chart3 = new Chart(ctx3, config);
                  
                  
                  // scenarios
                  var ctx4 = dojo.byId("canvas-sc-"+cntry).getContext("2d");
                  
                  var allData = [];
                  for (var dt in scenario){
                    allData.push({
                      lbl : dt,
                      t : scenario[dt].touchless,
                      r : scenario[dt].review,
                      l : scenario[dt].legacy,
                      n : scenario[dt].noStatus
                    });
                  }
                  allData.sort(function(a,b,){
                    return a.lbl < b.lbl;
                  });
                  
                  var tSet = {
                      label : 'Touchless',
                      fill : false,
                      backgroundColor : 'rgb(57,198,75)',
                      borderColor : 'rgb(57,198,75)',
                      data : []
                  };
                  var rSet = {
                      label : 'Review Required',
                      fill : false,
                      backgroundColor : 'rgb(185,70,73)',
                      borderColor : 'rgb(185,70,73)',
                      data : []
                  };
                  var lSet = {
                      label : 'Legacy Issues',
                      fill : false,
                      backgroundColor : 'rgb(237,221,18)',
                      borderColor : 'rgb(237,221,18)',
                      data : []
                  };
                  var nSet = {
                      label : 'Pending/Unknown',
                      fill : false,
                      backgroundColor : 'rgb(174,174,174)',
                      borderColor : 'rgb(174,174,174)',
                      data : []
                  };
                  var labels = [];
                  allData.forEach(function(d,i){
                    labels.push(d.lbl);
                    var total = d.t + d.r + d.n + d.l;
                    tSet.data.push(((d.t/total)*100).toFixed(1));
                    rSet.data.push(((d.r/total)*100).toFixed(1));
                    nSet.data.push(((d.n/total)*100).toFixed(1));
                    lSet.data.push(((d.l/total)*100).toFixed(1));
                  });
                  
                  var config2 = {
                      type: 'bar',
                      data: {
                        labels: labels,
                        datasets: [tSet, rSet, nSet, lSet]
                      },
                      options: {
                        responsive: true,
                        title: {
                          display: true,
                          text: 'Automation Per Scenario (%)'
                        },
                        tooltips: {
                          mode: 'index',
                          intersect: false,
                        },
                        hover: {
                          mode: 'nearest',
                          intersect: true
                        },
                        legend : {
                          display : true,
                          position : 'top',
                          labels : {
                            boxWidth : 16,
                            fontSize : 11,
                            padding : 6
                          }
                        },
                        scales: {
                          xAxes: [{
                            display: true,
                            scaleLabel: {
                              display: true,
                              labelString: 'Scenario',
                              fontSize : 11
                            },
                            ticks : {
                              fontSize : 11
                            },
                            stacked : true
                          }],
                          yAxes: [{
                            display: true,
                            scaleLabel: {
                              display: true,
                              labelString: '% of Requests',
                              fontSize : 11
                            },
                            ticks: {
                              min: 0,
                              max: 100,
                              stepSize: 25,
                              fontSize : 11
                            },
                            stacked : true
                          }]
                        }
                      }
                    };
                  
                    var chart4 = new Chart(ctx4, config2);

                    
                    // scenarios
                    var ctx5 = dojo.byId("canvas-sc2-"+cntry).getContext("2d");
                    
                    var allData = [];
                    for (var dt in scenario){
                      allData.push({
                        lbl : dt,
                        t : scenario[dt].touchless,
                        r : scenario[dt].review,
                        l : scenario[dt].legacy,
                        n : scenario[dt].noStatus
                      });
                    }
                    allData.sort(function(a,b,){
                      return a.lbl < b.lbl;
                    });
                    
                    var tSet = {
                        label : 'Touchless',
                        fill : false,
                        backgroundColor : 'rgb(57,198,75)',
                        borderColor : 'rgb(57,198,75)',
                        data : []
                    };
                    var rSet = {
                        label : 'Review Required',
                        fill : false,
                        backgroundColor : 'rgb(185,70,73)',
                        borderColor : 'rgb(185,70,73)',
                        data : []
                    };
                    var lSet = {
                        label : 'Legacy Issues',
                        fill : false,
                        backgroundColor : 'rgb(237,221,18)',
                        borderColor : 'rgb(237,221,18)',
                        data : []
                    };
                    var nSet = {
                        label : 'Pending/Unknown',
                        fill : false,
                        backgroundColor : 'rgb(174,174,174)',
                        borderColor : 'rgb(174,174,174)',
                        data : []
                    };
                    var labels = [];
                    var max = 0;
                    allData.forEach(function(d,i){
                      labels.push(d.lbl);
                      tSet.data.push(d.t);
                      rSet.data.push(d.r);
                      lSet.data.push(d.l);
                      nSet.data.push(d.n);
                      var total = d.t + d.r + d.l + d.n;
                      if (total > max) {
                        max = total;
                      }
                    });
                    
                    var config3 = {
                        type: 'bar',
                        data: {
                          labels: labels,
                          datasets: [tSet, rSet, nSet, lSet]
                        },
                        options: {
                          responsive: true,
                          title: {
                            display: true,
                            text: 'Automation Per Scenario (Totals)'
                          },
                          tooltips: {
                            mode: 'index',
                            intersect: false,
                          },
                          hover: {
                            mode: 'nearest',
                            intersect: true
                          },
                          legend : {
                            display : true,
                            position : 'top',
                            labels : {
                              boxWidth : 16,
                              fontSize : 11,
                              padding : 6
                            }
                          },
                          scales: {
                            xAxes: [{
                              display: true,
                              scaleLabel: {
                                display: true,
                                labelString: 'Scenario',
                                fontSize : 11
                              },
                              ticks : {
                                fontSize : 11
                              },
                              stacked : true
                            }],
                            yAxes: [{
                              display: true,
                              scaleLabel: {
                                display: true,
                                labelString: 'Total No. of Requests',
                                fontSize : 11
                              },
                              ticks: {
                                min: 0,
                                max: max,
                                stepSize: 50,
                                fontSize : 11
                              },
                              stacked : true
                            }]
                          }
                        }
                      };
                    
                      var chart5 = new Chart(ctx5, config3);
                    
                    
              }
              
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