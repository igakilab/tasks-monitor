
(function bars_stacked(container, horizontal) {

  var
    d1 = [[1,5],[2,7],[3,7]],
    d2 = [[1,3],[2,4],[3,9]],
    graph, i;

  graph = Flotr.draw(container,[
    { data : d1, label : 'Serie 1' },
    { data : d2, label : 'Serie 2' }
  ], {
    legend : {
      backgroundColor : '#D2E8FF' // Light blue 
    },
    bars : {
      show : true,
      stacked : true,
      horizontal : horizontal,
      barWidth : 0.6,
      lineWidth : 1,
      shadowSize : 0
    },
    grid : {
      verticalLines : horizontal,
      horizontalLines : !horizontal
    }
  });
})(document.getElementById("graph"));