/*
 * ボードに必要な情報を表示します
 */
function showDashBoard(data){
	// *****
	//ボード名/ボードリンクの表示
	$(".board-title").append(
		$("<a></a>").text(data.boardName)
			.attr("href", boardUrl));


	// *****
	//目標日の表示
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
	$(".task-progress").attr("aria-valuenow", progress)
		.css("width", progress + "%");


	// *****
	// 進捗グラフの表示


}

/*
 * 進捗グラフのグラフ部分の要素を作成します
 */
function createTaskTimeline(id, dBegin, dDoing, dDone, dFinish){
	//ol要素を作成
	var $ol = $("<ol></ol>").attr("id", id);
	//doing,doneの文字列表現を作成
	var dDoingStr = Util.formatDate(dDoing, "YYYY/MM/DD");
	var dDoneStr = Util.formatDate(dDone, "YYYY/MM/DD");

	var dtmp = dBegin;
	//開始日から終了日まで繰り返し
	while( dtmp.getTime() <= finishDate.getTime() ){
		//data-dateの文字列表現を生成、doing,doneの表現と比較
		var dtmpStr = Util.formatDate(dtmp, "YYYY/MM/DD");
		var clazz = dtmpStr == dDoingStr ? "doing" : (dtmpStr == dDoneStr ? "done" : "");
		//要素を生成し追加
		$ol.append($("<li></li>").append(
			$("<a></a>").attr("href", "#0")
				.attr("data-date", dtmpStr)
				.addClass(clazz))
			.text(Util.formatDate(dtmp, "MM/DD")));

		//日付をインクリメント
		dtmp.setDate(dtmp.getDate() + 1);
	}

	//ラッパーに入れて返却
	return $("<div></div>").addClass("timeline").append(
		$("<div></div>").addClass("events-wrapper").append(
			$("<div></div>").addClass("events").append($ol)
		)
	);
}

/*
<div class="timeline">
	<div class="events-wrapper">
		<div class="events">
			<ol id="cc">
				<li><a href="#0" data-date="2016/10/28" class="selected ; doing">10/28<br>Doing登録</a></li>
				<li><a href="#0" data-date="2016/10/29" >10/29</a></li>
				<li><a href="#0" data-date="2016/10/30">10/30</a></li>
				<li><a href="#0" data-date="2016/10/31" class="done">10/31<br>Done登録</a></li>
				<li><a href="#0" data-date="2016/11/1">11/01</a></li>	<!--doing,doneはa要素にクラス追加で対応 制御jsの判定で使う-->
				<li><a href="#0" data-date="2016/11/2">11/02</a></li>	<!--クラス追加すればマーカーが付く-->
				<li><a href="#0" data-date="2016/11/3">11/03</a></li>
			</ol>
			<span id="cc" class="filling-line" aria-hidden="true"></span>
		</div> <!-- .events -->
	</div> <!-- .events-wrapper -->
</div> <!-- .timeline -->*/













