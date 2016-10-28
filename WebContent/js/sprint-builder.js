var SprintBuilder;

/*
  仕様のメモ
  フィールド
    this.boardId - ボードIDを格納する
    this.cards - todoに入っているカード(スプリント計画の対象カード)を格納する
    this.members - ボードに参加しているメンバーのidとnameを格納する
  メソッド
    fetch - 情報を取得して、ビルダーを初期化する。callback関数を指定する
    isBoardMember - 指定されたmemberIdがボードメンバーかどうか返却します
    getIndexByCardId - cardIdで指定されたカードがcards配列のどの位置にあるか検索します
    isMemberRegisted - 指定されたカードにメンバーが追加されているか調べる
    selectCard - カードをスプリント対象カードにする
    addMemberToCard - カードにメンバーを追加する。自動的に対象カードになる
    unselectCard - カードをスプリント対象カードから除外する、メンバーはクリアされる
    removeMemberFromCard - カードから指定されたメンバーを消去する
    clearMemberFromCard - カードのメンバーをすべて消去する
    setFinishDate - 目標日を設定する
    getSelectedCards - 選択されたカード一覧を取得する
    getUnselectedCards - 選択されていないカード一覧を取得する
    getMemberList - ボードに参加しているメンバーのリストを取得する
    regist - システムにスプリントを登録する
*/

SprintBuilder = (function() {
	/*
	 * コンストラクター
	 * 各フィールドを初期化する
	 */
	function _class(boardId) {
		this.boardId = boardId;
		this.cards = [];
		this.members = [];
		this.finishDate = null;
	}


	/*
	 * デフォルトのエラーハンドラー(static function)
	 */
	_class.defaultErrorHandler = function(msg){
		alert(msg);
	};


	/*
	 * javaアプリケーションからデータを取得して持ってくる
	 * getTodoTrelloCards -> getBoardMembersの順に問い合わせが行われ
	 * 各種結果が自身のインスタンスに格納される。
	 * 処理が終了すると引数のpcallbackに処理を渡す
	 */
	_class.prototype.fetch = function(pcallback, perrorHandler){
		//エラーハンドラーの設定
		if( typeof perrorHandler != 'function' ){
			perrorHandler = _class.defaultErrorHandler;
		}

		//このオブジェクトのインスタンスを指定
		var thisp = this;

		//getTodoTrelloCards(1回目通信)のコールバック関数
		var getTodoCallback = function(data){
			//カードキャッシュにデータを追加
			thisp.cards = [];
			//console.log(data); // DEBUG
			for(var i=0; i<data.length; i++){
				data[i].selected = (data[i].memberIds.length > 0);
				thisp.cards.push(data[i]);
			}

			//次にメンバー一覧を取得(2回目通信)
			SprintPlanner.getBoardMembers(thisp.boardId, {
				callback: getMembersCallback,
				errorHandler: perrorHandler
			});
		};

		//getBoardMembers(2回目通信)のコールバック関数
		var getMembersCallback = function(members){
			//console.log(members) //DEBUG
			//メンバーキャッシュにデータを追加
			thisp.members = [];
			for(var i=0; i<members.length; i++){
				thisp.members.push(members[i]);
			}

			//呼び出し元コールバックへ返却
			if( typeof pcallback == 'function' ){
				pcallback();
			}
		};

		//カード一覧を取得(1回目通信)
		SprintPlanner.getTodoTrelloCards(this.boardId, {
			callback: getTodoCallback,
			errorHandler: perrorHandler
		});
	}


	/*
	 * 指定されたメンバーidがこのボードに含まれているかどうか返す
	 */
	_class.prototype.isBoardMember = function(memberId){
		//メンバーidが一致すればtrueを返却
		for(var i=0; i<this.members.length; i++){
			if( this.members[i].id == memberId ){
				return true;
			}
		}
		return false;
	}


	/*
	 * カードリストのキャッシュから、指定されたcardIdを持つカードのインデックスを返す
	 * みつからなかった場合は-1を返す
	 */
	_class.prototype.getIndexByCardId = function(cardId){
		//カードidが一致すればインデックスを返却
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].id == cardId ){
				return i;
			}
		}
		return -1;
	}

	/*
	 * 指定されたカードに指定されたメンバーが担当者として設定されているかどうか返す
	 * メンバーが登録されている場合はtrueを、
	 * メンバーが登録されていないか、指定されたカードが見つからなかった場合にfalseを返す
	 */
	_class.prototype.isMemberRegisted = function(cardId, memberId){
		//カードを検索する
		var idx = this.getIndexByCardId(cardId);

		//カードが見つかれば、memberIdsにidがあるかどうか検索する
		if( idx >= 0 ){
			var card = this.cards[idx];

			//memberIdが一致すればtrueを返却
			for(var i=0; i<card.memberIds.length; i++){
				if( card.memberIds[i] == memberId ){
					return true;
				}
			}
			return false;
		}

		//カードが見つからなかった場合はデフォルトでfalseを返却
		return false;
	}


	/*
	 * 指定されたカードをスプリントの対象カードにする
	 */
	_class.prototype.selectCard = function(cardId){
		//カードを検索、見つかればselectedをtrueに
		var idx = this.getIndexByCardId(cardId);
		if( idx >= 0 ){
			this.cards[idx].selected = true;
		}
	}

	/*
	 * カードにmemberIdで指定されたメンバーを追加する
	 * 追加する際に、そのメンバーidがボードに参加しているかどうかを確認する
	 * また、対象カードになっていないカードの場合は、自動的に対象カードになる
	 */
	_class.prototype.addMemberToCard = function(cardId, memberId){
		//カードを検索
		var idx = this.getIndexByCardId(cardId);

		//カードが見つかったかどうか、ボードに指定されたメンバーが所属しているか
		//対象カードにすでに指定されたメンバーが登録されていないかどうか判定
		if( idx >= 0
			&& this.isBoardMember(memberId)
			&& !this.isMemberRegisted(cardId, memberId)
		){
			//メンバーを追加する
			this.cards[idx].memberIds.push(memberId);
		}
	}


	/*
	 * 指定されたカードを対象カードから除外する
	 * メンバーが追加されていた場合は、自動的にすべてのメンバーがカードから除去する
	 */
	_class.prototype.unselectCard = function(cardId){
		//カードを検索
		var idx = this.getIndexByCardId(cardId);

		//見つかれば、selectedをfalseに、メンバーidを空にする
		if( idx >= 0 ){
			this.cards[idx].selected = false;
			this.cards[idx].memberIds = [];
		}
	}


	/*
	 * カードから指定されたメンバーを除去する
	 */
	_class.prototype.removeMemberFromCard = function(cardId, memberId){
		//カードを検索
		var idx = this.getIndexByCardId(cardId);

		//見つかれば、指定されたメンバーが登録されているかどうか判定する
		if( idx >= 0 && this.isMemberRegisted(cardId, memberId) ){
			var card = this.cards[idx];

			//メンバーidのあるインデックスを検索
			var midx = -1;
			for(var i=0; i<card.memberIds.length; i++){
				if( card.memberIds[i] == memberId ){
					midx = i;
					break;
				}
			}

			//メンバーを削除する(インデックスが見つかっていれば)
			if( midx >= 0 ){
				card.memberIds.splice(midx, 1);
			}
		}
	}

	/*
	 * カードに追加されているメンバーをすべて除去する
	 */
	_class.prototype.clearMemberFromCard = function(cardId){
		//カードを検索、見つかればmemberIdsを空にする
		var idx = this.getIndexByCardId(cardId);
		if( idx >= 0 ){
			this.cards[idx].memberIds = [];
		}
	}


	/*
	 * 目標日を設定する
	 * finishDateはDate型で指定する
	 */
	_class.prototype.setFinishDate = function(finishDate){
		//目標日を設定する
		this.finishDate = finishDate;
	}


	/*
	 * 対象カードになっているカードのリストを取得する
	 * 対象カードのリストが返却される
	 */
	_class.prototype.getSelectedCards = function(){
		//cardsからselectedフラグがたっているものを配列に格納し、返却
		var selected = [];
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].selected ){
				selected.push(this.cards[i]);
			}
		}
		return selected;
	}


	/*
	 * 対象カードになっていないカードのリストを取得する
	 * 非対称カードのリストが返却される
	 */
	_class.prototype.getUnselectedCards = function(){
		//cardsからselectedフラグがたっていないものを配列に格納し、返却
		var unselected = [];
		for(var i=0; i<this.cards.length; i++){
			if( !this.cards[i].selected ){
				unselected.push(this.cards[i]);
			}
		}
		return unselected;
	}


	/*
	 * ボードに参加しているメンバーのオブジェクトをリストで返す
	 */
	_class.prototype.getBoardMembers = function(){
		//members配列を返す
		return this.members;
	}


	/*
	 * 設定された目標日や担当者の情報から、スプリントをシステムに登録する
	 * fetchと同様、処理が終了するとpcallbackに処理を渡す
	 * その際、新規作成されたスプリントのIDが引数として渡される
	 */
	_class.prototype.regist = function(pcallback, perrorHandler){
		//エラーハンドラーの設定
		if( typeof perrorHandler != 'function' ){
			perrorHandler = _class.defaultErrorHandler;
		}

		//finishDateが指定されているかチェック
		if( Util.isNull(this.finishDate) ){
			return;
		}

		//カード担当者情報を作成
		var cardAndMembers = [];
		var selectedList = this.getSelectedCards();
		for(var i=0; i<selectedList.length; i++){
			cardAndMembers.push({
				trelloCardId: selectedList[i].id,
				memberIds: selectedList[i].memberIds
			});
		}

		//通信開始
		SprintPlanner.createSprint(this.boardId, this.finishDate, cardAndMembers, {
			callback: pcallback,
			errorHandler: perrorHandler
		});
	}

	return _class;

})();
