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
 * カードidで指定されたカードが終了しているか確認します。
 * resultとカードidを指定します
*/
function isFinished(result, cardId){
	for(var i=0; i<result.finishedCards.length; i++){
		if( result.finishedCards[i] == cardId ){
			return true;
		}
	}
	return false;
}

/*
 * タスク完了表にタスクを追加します
 * カードのオブジェクトと、それが完了したかどうかを与えます
 */
function addCardToTaskTable(card, finished){
	var tr_type = finished ? "success" : "danger";
	var mark = finished ? Util.bsGlyphicon("ok") : Util.bsGlyphicon("remove");

	var tr = $("<tr></tr>").append(
		$("<td></td>").append(mark),
		$("<td></td>").text(card.name)
	).addClass(tr_type);

	$(".finished-tasks-table").append(tr);
}

/*
 * SprintFinisherから渡されるClosedSprintResultをhtml画面上に
 * 表示する関数。
*/
function setSprintResult(result){
	//振り返り日設定
	$(".feedback-date").text(
		Util.formatDate(result.createdAt, "MM/DD"));

	//全体の完了タスク数
	var fin = result.finishedCards.length;
	var all = result.remainedCards.length + fin;
	var progress = Math.floor((fin / all) * 100);
	$(".task-count-finished").text(fin);
	$(".task-count-all").text(" /" + all);
	$("#taskProgress").attr("aria-valuenow", progress)
	.css("width", progress + "%");

	//メンバー別完了タスク数
	$(".member-tasks-table").empty();
	for(var i=0; i<result.memberTasks.length; i++){
		var mt = result.memberTasks[i];
		addMemberTaskCount(mt.memberId, mt.finishedCards.length,
			mt.remainedCards.length + mt.finishedCards.length);
	}

	//タスク結果表を追加
	$(".finished-tasks-table").empty();
	for(var i=0; i<result.sprintCards.length; i++){
		var card = result.sprintCards[i];
		addCardToTaskTable(card, isFinished(result, card.id));
	}

}