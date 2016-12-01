
(function bars_stacked(container, horizontal) {

  var
    d1 = [[1,18],[2,23]],
    d2 = [[1,1],[2,1]],
    data = [
    { data : d1, label : '達成' },
    { data : d2, label : '未達成' }
    ],
    ticks = [[1,"aaaaa"],[2,"bbbbb"]],
    graph, i;

  graph = Flotr.draw(container,data, {
    legend : {
      backgroundColor : '#D2E8FF' // Light blue 
    },
    bars : {
      show : true,
      stacked : true,
      horizontal : horizontal,
      barWidth : 0.4,
      lineWidth : 1,
      shadowSize : 0
    },
    grid : {
      verticalLines : horizontal,
      horizontalLines : !horizontal
    },
    xaxis: {ticks: ticks},
    yaxis: {min: 0}
  });
})(document.getElementById("graph"));