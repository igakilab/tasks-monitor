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
	console.log(card);

	var tr = $("<tr></tr>").append(
		$("<td></td>").append(mark),
		$("<td></td>").text(card.name),
		$("<td></td>").text(card.workingMinutes + "分")
	).addClass(tr_type);

	$(".finished-tasks-table").append(tr);
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
		addMemberTaskCount(val.name, val.remainedCount,
			val.remainedCount + val.finishedCount);
	});

	//タスク結果表を追加
	$(".finished-tasks-table").empty();
	result.getCards().forEach(function(val, idx, ary){
		addCardToTaskTable(val, val.finished);
	});
}