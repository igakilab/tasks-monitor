/*
 * ボードに必要な情報を表示します
 */
function showDashBoard(data){
	// *****
	//ボード名/ボードリンクの表示
	$(".board-title").empty();
	$(".board-title").append(
		$("<a></a>").text(data.boardName + " ")
			.attr("href", data.boardUrl)
			.css("text-decoration", "none")
	);


	// *****
	//目標日の表示
	$("finish-date").empty();
	if( !Util.isNull(data.finishDate) ){
		$(".finish-date").append(
			$("<h1></h1>").text(Util.formatDate(
				data.finishDate, "YYYY/MM/DD")));
	}else{
		//スプリントがない場合はスプリント計画ページへのリンクを表示
		$(".finish-date").append(
			$("<p></p>").text("現在進行中のスプリントはありません"),
			$("<a></a>").text("新しいスプリントを作成...")
				.attr("href", "sprint.html?bid=" + data.boardId));
	}


	// *****
	//進捗バーの表示
	var fin, all;
	if( data.sprintCards.length > 0 ){
		//スプリントが進行中の場合、完了タスク/全タスクの進捗を表示する
		all = data.sprintCards.length;
		fin = 0;
		//完了カードをカウント
		data.sprintCards.forEach(function(val, idx, ary){
			if( val.finished ) fin++;
		});
	}else{
		//スプリントが進行中でない場合、doneのタスク数/全タスク数の進捗を表示する
		//カウンタを作成
		var cnt = 0;
		var counter = function(val, idx, ary){
			if( !val.closed ) cnt++;
		}

		//doneのタスク数をカウント
		data.kanban.done.forEach(counter);
		fin = cnt;
		//todo+doingを合わせたタスク数をカウント
		data.kanban.todo.forEach(counter);
		data.kanban.doing.forEach(counter);
		all = cnt;
	}

	//パーセンテージを計算
	var progress = Math.floor(fin / all * 100);
	//プログレスパーに設定
	$("#taskProgressMsg").text("現在の進捗: " + progress + "%");
	$("#taskProgress").attr("aria-valuenow", progress)
		.css("width", progress + "%");


	// *****
	// 進捗グラフの表示
	if( data.sprintCards.length > 0 ){
		drawSprintTasksTimeline(data.sprintCards, data.beginDate, data.finishDate);
	}

	// *****
	// かんばんの表示
	$("#todoList").empty();
	addCardToListGroup($("#todoList"), data.kanban.todo);
	$("#doingList").empty();
	addCardToListGroup($("#doingList"), data.kanban.doing);
	$("#doneList").empty();
	addCardToListGroup($("#doneList"), data.kanban.done);
}



/*
 * $divの位置にメンバーごとのmemberTasksへのリンクを配置します
 * membersには以下のフォーマットでデータを指定します。
 * [{id:<メンバーID>, name:<メンバー名>}, ..]
 */
function putMemberTasksButton($div, members){
	members.forEach(function(member){
		$div.append(
			$("<a></a>").addClass("btn btn-default btn-sm")
				.append(Util.bsGlyphicon("user"), " " + member.name)
				.attr("href", "membertasks.html?mid=" + member.id));
	});
}

/*
 * 進捗グラフのグラフ部分の要素を作成します
 */
//function createTaskTimeline(id, dBegin, dDoing, dDone, dFinish){
//	//日付フォーマット作成関数
//	var formatDate = function(date){
//		return date.getFullYear() + "/" + (date.getMonth()+1) + "/" + date.getDate();
//	};
//	//ol要素を作成
//	var $ol = $("<ol></ol>").attr("id", id);
//	//doing,doneの文字列表現を作成
//	var dDoingStr = Util.isNull(dDoing) ? "null" : formatDate(dDoing);
//	var dDoneStr = Util.isNull(dDone) ? "null" : formatDate(dDone);
//
//	//開始日から終了日まで繰り返し
//	var dtmp = new Date(dBegin.getTime());
//	var selflg = true;
//	while( dtmp.getTime() <= dFinish.getTime() ){
//		//data-dateの文字列表現を生成、doing,doneの表現と比較
//		var dtmpStr = formatDate(dtmp);
//		var label = Util.formatDate(dtmp, "MM/DD");
//		var clazz = "";
//
//		//doing,doneの表現と比較、それぞれの処理
//		if( dtmpStr == dDoneStr ){
//			clazz = "done";
//			label += "<br/>Done登録";
//		}else if( dtmpStr == dDoingStr ){
//			clazz = "doing";
//			label += "<br/>Doing登録";
//		}
//
//		//初回の要素にselectedクラスを付加する
//		if( selflg ){
//			clazz = clazz + " selected";
//			selflg = false;
//		}
//
//		//要素を生成し追加
//		$ol.append($("<li></li>").append(
//			$("<a></a>").attr("href", "#0")
//				.attr("data-date", dtmpStr)
//				.addClass(clazz)
//				.append(label)
//		));
//
//
//		//日付をインクリメント
//		dtmp.setDate(dtmp.getDate() + 1);
//	}
//
//	//ラッパーに入れて返却
//	return $("<div></div>").addClass("timeline").append(
//		$("<div></div>").addClass("events-wrapper").append(
//			$("<div></div>").addClass("events").append($ol,
//				$("<span></span>").attr("id", id)
//					.addClass("filling-line")
//					.attr("aria-hidden", "true"))
//		)
//	);
//}


/*
 * タスクごとのタイムラインを描画します
 * beginDateには開始時刻、dataには下記のフォーマットを指定します
 * [{name:<タスク名>, listUpdates:<<analyzeListTimelineに依存>>},..]
 */
function drawSprintTasksTimeline(data, beginDate, finishDate, destId){
	//データテーブルを初期化
	var dataTable = new google.visualization.DataTable();
	dataTable.addColumn({type: "string", id: "TaskName"});
	dataTable.addColumn({type: "string", id: "Status"});
	dataTable.addColumn({type: "date", id: "Start"});
	dataTable.addColumn({type: "date", id: "End"});

	//タイムラインの終了時刻を計算
	var now = new Date();
	if( finishDate.getTime() < now.getTime() ){
		finishDate = now;
	}else{
		finishDate.setDate(finishDate.getDate() + 1);
		finishDate.setHours(0);
		finishDate.setMinutes(0);
		finishDate.setSeconds(0);
	}
	console.log("finishDate");
	console.log(finishDate);

	//カードごとにタイムラインを追加
	data.forEach(function(card, idx, ary){
		//タイムラインを解析
		var lines = analyzeListTimeline(card.listUpdates, beginDate, now);

		//終了時刻よりも前のデータしかない場合は空のデータを追加
		if( lines[lines.length-1].end.getTime() < finishDate ){
			lines.push({
				data:{name:"unknown", type:"unknown"},
				start:lines[lines.length-1].end, end:finishDate
			});
		}

		//タイムラインを追加
		lines.forEach(function(move){
			var label = move.data.type == "todo" ? "ToDo" :
				move.data.type == "doing" ? "Doing" :
				move.data.type == "done"  ? "Done" : " ";

			dataTable.addRow([card.name, label, move.start, move.end]);
		});
	});

	//オプションを生成
	var options = {width: "100%"};

	//描画
	var chart = new google.visualization.Timeline(document.getElementById(destId || "n-timeline"));
	chart.draw(dataTable);
}


/*
 * リスト移動履歴からカードがリストに到達した時間と離脱した時間を解析します
 * fStart,fEndには日時を、listMovementsには下記のフォーマットを指定します。
 * [{movedAt:<移動が発生した日時>,name:<移動先リスト名>,type:<移動先リストタイプ>},..]
 */
function analyzeListTimeline(listMovements, fStart, fEnd){
	var result = [];
	var tmp = null;
	var cnt = 0;

	//初期位置を探索
	while( cnt < listMovements.length && listMovements[cnt].movedAt <= fStart ){
		//console.log("skip");
		//console.log(listMovements[cnt]);
		cnt++;
	}

	//初期位置を設定
	if( cnt > 0 ){
		//console.log("init configure");
		//console.log(listMovements[cnt-1]);
		tmp = {data: listMovements[cnt-1], start: fStart};
	}else{
		tmp = {data: {name:"unknown", type:"unknown"}, start: fStart};
	}

	//移動履歴を探索
	while( cnt < listMovements.length && listMovements[cnt].movedAt < fEnd ){
		//console.log("analyze");
		//console.log(listMovements[cnt]);

		var move = listMovements[cnt];
		tmp.end = move.movedAt;
		result.push(tmp);

		tmp = {data: move, start: move.movedAt};
		cnt++;
	}

	//終端処理

	tmp.end = fEnd;
	result.push(tmp);

	return result;
}

/*
 * 与えられたtrellocardオブジェクトを変換し
 * $listgrpに追加します
 */
function addCardToListGroup($listgrp, cards){
	cards.forEach(function(val, idx, ary){
		//アイテムヘッダーとテキストを生成
		var $itemhead = $("<h4></h4>");
		$itemhead.addClass("list-group-item-heading");
		var $itemtext = $("<p></p>");
		$itemtext.addClass("list-group-item-text");

		//タイトルをヘッダーにセット
		$itemhead.text(val.name);

		//カード情報を配列に挿入
		var details = [];
		if( !Util.isNull(val.desc) && val.desc != "" ){
			details.push(val.desc);
		}
		if( !Util.isNull(val.due) ){
			details.push("期限: " + Util.formatDate(val.due, "YYYY/MM/DD"));
		}
		//アイテムテキストにセット
		$itemtext.append(Util.arrayToString(details, "<br/>"));

		//アイテムとして新しいjqueryオブジェクトを生成, クラスを設定
		var $item = $("<li></li>");
		$item.addClass("list-group-item");
		$item.append($itemhead, $itemtext);

		//listGroupにアイテムを追加
		$listgrp.append($item);
	});
}












