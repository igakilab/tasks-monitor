var SprintResultAnalyzer;

/*
  仕様のメモ
  フィールド
  - boardData ボードのデータ
  - sprintData スプリントとスプリント結果のデータ
  - cards スプリントカードのデータ(配列)
  - members ボードに参加しているメンバーのデータ(配列)
  メソッド
  - fetch データを取得
  - analyze 引数に与えられたデータを自身のフィールドにセットする

  - getBoardData ボードのデータ
  - getSprintData スプリントのデータ
  - getMember メンバーidに基づくメンバー情報
  - getMembers すべてのメンバー情報
  - getCard カードidに基づくカードデータ
  - getCards すべてのカードデータ
  - getMemberCards メンバーidに基づくカードデータの配列
  - getDefaultTags デフォルトタグを返却
*/

SprintResultAnalyzer = (function() {
	/*
	 * コンストラクター
	 * 各フィールドを初期化する
	 */
	function _class(sprintId) {
		this.sprintId = sprintId;
		this.boardData = null;
		this.sprintData = null;
		this.cards = [];
		this.members = [];
		this.memberHistories = null;
		this.defaultTags = [];
	}


	/*
	 * デフォルトのエラーハンドラー(static function)
	 */
	_class.defaultErrorHandler = function(msg){
		alert(msg);
	};


	/*
	 * javaアプリケーションからデータを取得して持ってくる
	 * SprintHistoryのgetSprintResultAnalyzerFormが呼ばれ、
	 * 受け取ったデータはanalyze関数に渡す。
	 * sprintIdは自身のインスタンスの設定値を使用する
	 */
	_class.prototype.fetch = function(pcallback, perrorHandler){
		//エラーハンドラーの設定
		if( typeof perrorHandler != 'function' ){
			perrorHandler = _class.defaultErrorHandler;
		}

		//このオブジェクトのインスタンスを指定
		var thisp = this;

		//データの取得
		SprintHistory.getSprintResultAnalyzerData(this.sprintId, {
			callback: function(data){
				console.log("received.");
				console.log(data);
				thisp.analyze(data);
				pcallback();
			},
			errorHandler: perrorHandler
		});
	}


	/*
	 * スプリント結果のデータを解析します
	 * ボードとスプリントの基本データをそれぞれ、boardDataとsprintDataに
	 * カードごとの集計をcardsに
	 * メンバーごとの集計をmembersに格納します
	 */
	_class.prototype.analyze = function(data){
		//*****
		//ボードのデータを設定する
		this.boardData = data.boardData;

		//*****
		//スプリントのデータを設定する
		var stmp = {
			id: data.sprint.id,
			beginDate: data.sprint.beginDate,
			finishDate: data.sprint.finishDate,
			closedDate: data.sprint.closedDate,
			closedAt: data.result.createdAt
		};

		stmp.remainedCount = 0;
		stmp.finishedCount = 0;
		data.sprintCards.forEach(function(val, idx, ary){
			if( val.finished ){
				stmp.finishedCount++;
			}else{
				stmp.remainedCount++;
			}
		});

		this.sprintData = stmp;

		//*****
		//スプリントカードの配列を生成する
		this.cards = data.sprintCards;

		//*****
		//メンバーの配列を生成する
		this.members = [];
		var thisp = this;
		data.members.forEach(function(val, idx, ary){
			//メンバーごとのremainとfinishのカウントを行います
			var cards = thisp.getMemberCards(val.id);
			var rem = 0;
			var fin = 0;
			cards.forEach(function(val, idx, ary){
				if( val.finished ){
					fin++;
				}else{
					rem++;
				}
			});
			thisp.members.push({
				id: val.id,
				name: val.name,
				remainedCount: rem,
				finishedCount: fin
			});
		});

		//*****
		//メンバーの達成カード数履歴情報を格納する
		this.memberHistories = data.memberHistories;

		//**
		//デフォルトタグを格納する
		this.defaultTags = data.defaultTags;
	}


	/*
	 * ボードデータのオブジェクトを返します
	 */
	_class.prototype.getBoardData = function(){
		return this.boardData;
	}


	/*
	 * スプリントデータのオブジェクトを返します
	 */
	_class.prototype.getSprintData = function(){
		return this.sprintData;
	}


	/*
	 * midに指定されたメンバーidを持つメンバーを返します。
	 */
	_class.prototype.getMember = function(mid){
		for(var i=0; i<this.members.length; i++){
			if( this.members[i].id == mid ){
				return this.members[i];
			}
		}
		return null;
	}


	/*
	 * メンバーの一覧を返します
	 */
	_class.prototype.getMembers = function(){
		return this.members;
	}


	/*
	 * cidに指定されたカードidをもつカードを返します
	 */
	_class.prototype.getCard = function(cid){
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].id == mid ){
				return this.cards[i];
			}
		}return null;
	}


	/*
	 * カードの一覧を返します
	 */
	_class.prototype.getCards = function(){
		return this.cards;
	}

	/*
	 * 指定されたメンバーidが担当しているカード(タスク)一覧を返却します
	 */
	_class.prototype.getMemberCards = function(mid){
		var cards = [];
		for(var i=0; i<this.cards.length; i++){
			var card = this.cards[i];

			//メンバーを探す
			var flg = false;
			for(var j=0; j<card.memberIds.length; j++){
				if( card.memberIds[j] == mid ){
					flg = true;
					break;
				}
			}

			//カードを格納する
			if( flg ){
				cards.push(card);
			}
		}

		return cards;
	}

	/*
	 * midに指定されたメンバーのスプリント履歴情報を返します。
	 */
	_class.prototype.getMemberHistory = function(mid){
		for(var i=0; i<this.memberHistories.length; i++){
			if( this.memberHistories[i].memberId == mid ){
				return this.memberHistories[i];
			}
		}
	}

	/*
	 * デフォルトタグを返却します
	 */
	_class.prototype.getDefaultTags = function(){
		return this.defaultTags;
	}

	return _class;

})();
