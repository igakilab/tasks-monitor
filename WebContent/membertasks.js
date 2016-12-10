/*
 * メンバーの達成未達成タスクのグラフを描画します
 * dataには以下のフォーマットのデータの配列を指定します。
 * {label:<表示名>, remainedCount:<未達成タスク数>, finishedCount:<達成タスク数>}
 */
function drawMemberTasksChart(data, destId){
	//データの配列の初期化
	var table = [ ["スプリントID", "達成タスク数", "未達成タスク数"] ];

	//データ配列に引数のデータを挿入
	for(var i=0; i<data.length; i++){
		table[table.length] =
			[data[i].label, data[i].finishedCount, data[i].remainedCount];
	}

	//オプションを生成
	var options = {
		width: "100%", height: 300,
		legend: {position: "top", maxLines: 3},
		bar: {groupWidth: "75%"}, isStacked: true
	};

	//チャートを描画
	var chart = new google.visualization.ColumnChart(document.getElementById(destId || "graph"));
	chart.draw(google.visualization.arrayToDataTable(table), options);
}


/*
 * スプリントの結果パネルを生成します
 */
function generateSprintPanel(data){
	//パネル生成
	var $panel = $("<div></div>");
	$panel.addClass("panel");

	//ヘッダを生成&追加
	var $header = $("<div></div>");
	$header.addClass("panel-heading");
	$header.text("[" + data.board.name + "] " +
		Util.formatDate(data.sprint.beginDate, "MM/DD") + " ～ " +
		Util.formatDate(data.sprint.finishDate, "MM/DD") + "(" +
		data.sprint.id + ")");
	$panel.append($header);

	//テーブルを生成
	var $table = $("<table></table>");
	$table.addClass("table");
	$table.append($("<thead></thead>").append(
		$("<tr></tr>").append(
			$("<th></th>").text("達成/未達成"),
			$("<th></th>").text("カードID"),
			$("<th></th>").text("タスク名")
	)));

	//テーブルボディとラベルを生成
	var $tbody = $("<tbody></tbody>");
	var genLabelFin = function(){
		return $("<span></span>").text("達成")
			.addClass("label label-success");
	};
	var genLabelRem = function(){
		return $("<span></span>").text("未達成")
			.addClass("label label-danger");
	};

	var rem = 0, fin = 0;
	for(var i=0; i<data.cards.length; i++){
		var card = data.cards[i];
		$tbody.append(
			$("<tr></tr>").append(
				$("<td></td>").append(card.finished ? genLabelFin() : genLabelRem),
				$("<td></td>").text(card.id),
				$("<td></td>").text(card.name)
			)
		);

		//達成枚数カウント
		if( card.finished ) fin++; else rem++;
	}
	$table.append($tbody);

	//パネルにテーブルを追加
	$panel.append($table);

	//パネルの色を指定
	if( rem == 0 ){
		$panel.addClass("panel-success");
	}else if( fin == 0 ){
		$panel.addClass("panel-danger");
	}else{
		$panel.addClass("panel-warning");
	}

	return $panel;
}


/*
 * div要素の中にメンバー未達成達成タスクのパネルを追加します。
 */
function showMemberTasks(data){
	var $div = $(".sprint-histories");

	$div.empty();

	for(var i=0; i<data.length; i++){
		var $panel = generateSprintPanel(data[i]);
		$div.append($panel);
	}
}