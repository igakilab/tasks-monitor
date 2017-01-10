/*
 * メンバー別タスク完了表に数値を表示します。
 * 引数にはメンバー別タスク数をそれぞれ指定します
 */
function addMemberTaskCount(name, finished, all){
	$(".member-tasks-table").append(
		$("<tr></tr>").append(
			$("<td></td>").text(name),
			$("<td></td>").text(finished),
			$("<td></td>").text(Math.floor(finished / all * 100))));
}

/*
 * タスク完了表にタスクを追加します
 * カードのオブジェクトと、それが完了したかどうかを与えます
 */
function addCardToTaskTable(card, finished){
	var tr_type = finished ? "success" : "danger";
	var mark = finished ? Util.bsGlyphicon("ok") : Util.bsGlyphicon("remove");
	//console.log(card);

	var tr = $("<tr></tr>").append(
		$("<td></td>").append(mark),
		$("<td></td>").text(card.name),
		$("<td></td>").text(card.workingMinutes + "分")
	).addClass(tr_type);

	$(".finished-tasks-table").append(tr);
}

/*
 * member-historyのグラフを描画します
 * 引数dataには下記のフォーマットのデータを渡します
 * [{closedDate:<スプリント終了日時>, finishedCount:<完了タスク数>,remainedCount:<未達成タスク数>},..]
 */
function drawMemberHistoryGraph(data, memberName, destId){
	//データの配列の初期化
	var table = [ ["終了日", "達成タスク数", "未達成タスク数"] ];

	//データ配列に引数のデータを挿入
	data.forEach(function(val, idx, ary){
		table[table.length] = [
		    Util.formatDate(val.closedDate, "MM/DD"),
		    val.finishedCount, val.remainedCount
		];
	});

	//オプションを生成
	var options = {
		title: memberName,
		width: "100%", height: 300,
		legend: {position: "top", maxLines: 3},
		bar: {groupWidth: "75%"}, isStacked: true
	};

	//チャートを描画
	var chart = new google.visualization.ColumnChart(document.getElementById(destId || "graph"));
	chart.draw(google.visualization.arrayToDataTable(table), options);
}


/*
 * SprintFinisherから渡されるClosedSprintResultをhtml画面上に
 * 表示する関数。
 * resultにはsprintResultAnalyzerを指定します
*/
function setSprintResult(result){
	//振り返り日設定
	var sprintData = result.getSprintData();
	$(".feedback-date").text(
		Util.formatDate(sprintData.closedDate, "MM/DD"));

	//全体の完了タスク数
	var fin = sprintData.finishedCount;
	var all = sprintData.remainedCount + fin;
	var progress = Math.floor((fin / all) * 100);
	$(".task-count-finished").text(fin);
	$(".task-count-all").text(" /" + all);
	$("#taskProgress").attr("aria-valuenow", progress)
	.css("width", progress + "%");

	//メンバー別完了タスク数
	$(".member-tasks-table").empty();
	result.getMembers().forEach(function(val, idx, ary){
		addMemberTaskCount(val.name, val.finishedCount,
			val.remainedCount + val.finishedCount);
	});

	//タスク結果表を追加
	$(".finished-tasks-table").empty();
	result.getCards().forEach(function(val, idx, ary){
		addCardToTaskTable(val, val.finished);
	});

	//メンバー別スプリント達成履歴
	$(".member-histories").empty();
	result.getMembers().forEach(function(val, idx, ary){
		var mh = result.getMemberHistory(val.id);
		var divId = "histories-graph-" + val.id;

		$(".member-histories").append(
			$("<div></div>").addClass("col-md-6").attr("id", divId));

		drawMemberHistoryGraph(mh.results, val.name, divId);
	});
}