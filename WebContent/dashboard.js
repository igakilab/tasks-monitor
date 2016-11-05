/*
 * ボードに必要な情報を表示します
 */
function showDashBoard(data){
	// *****
	//ボード名/ボードリンクの表示
	$(".board-title").empty();
	$(".board-title").append(
		$("<a></a>").append(
			$("<h1></h1>").text(data.boardName))
			.attr("href", data.boardUrl));


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
				.attr("href", "sprint.html?boardId=" + data.boardId));
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
	$(".sprint-cards").empty();
	for(var i=0; i<data.sprintCards.length; i++){
		var card = data.sprintCards[i];

		//ラベル部分を作成
		var $label = $("<ol></ol>").append(
			$("<li></li>").append(
				$("<h2></h2>").append(card.name)
			)
		).css("margin", "0 auto");

		//タイムライングラフを作成
		var $timeline = createTaskTimeline("sc" + i,
			data.beginDate, card.movedDoingAt, card.movedDoneAt, data.finishDate);

		//左にタスク名、右にタイムライングラフを格納して、sprint-cardsに追加
		$(".sprint-cards").append(
			$("<div></div>").addClass("row").append(
				$("<section></section>").addClass("cd-horizontal-timeline").append(
					$("<div></div>").addClass("col-md-3").append($label),
					$("<div></div>").addClass("col-md-9").append($timeline)
				)
			)
		);
	}
	//タイムラインを初期化
	$(".cd-horizontal-timeline").initTimeline();

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
 * 進捗グラフのグラフ部分の要素を作成します
 */
function createTaskTimeline(id, dBegin, dDoing, dDone, dFinish){
	//日付フォーマット作成関数
	var formatDate = function(date){
		return date.getFullYear() + "/" + (date.getMonth()+1) + "/" + date.getDate();
	};
	//ol要素を作成
	var $ol = $("<ol></ol>").attr("id", id);
	//doing,doneの文字列表現を作成
	var dDoingStr = Util.isNull(dDoing) ? "null" : formatDate(dDoing);
	var dDoneStr = Util.isNull(dDone) ? "null" : formatDate(dDone);

	//開始日から終了日まで繰り返し
	var dtmp = new Date(dBegin.getTime());
	var selflg = true;
	while( dtmp.getTime() <= dFinish.getTime() ){
		//data-dateの文字列表現を生成、doing,doneの表現と比較
		var dtmpStr = formatDate(dtmp);
		var label = Util.formatDate(dtmp, "MM/DD");
		var clazz = "";

		//doing,doneの表現と比較、それぞれの処理
		if( dtmpStr == dDoneStr ){
			clazz = "done";
			label += "<br/>Done登録";
		}else if( dtmpStr == dDoingStr ){
			clazz = "doing";
			label += "<br/>Doing登録";
		}

		//初回の要素にselectedクラスを付加する
		if( selflg ){
			clazz = clazz + " selected";
			selflg = false;
		}

		//要素を生成し追加
		$ol.append($("<li></li>").append(
			$("<a></a>").attr("href", "#0")
				.attr("data-date", dtmpStr)
				.addClass(clazz)
				.append(label)
		));


		//日付をインクリメント
		dtmp.setDate(dtmp.getDate() + 1);
	}

	//ラッパーに入れて返却
	return $("<div></div>").addClass("timeline").append(
		$("<div></div>").addClass("events-wrapper").append(
			$("<div></div>").addClass("events").append($ol,
				$("<span></span>").attr("id", id)
					.addClass("filling-line")
					.attr("aria-hidden", "true"))
		)
	);
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











