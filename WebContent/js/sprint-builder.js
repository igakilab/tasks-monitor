var SprintBuilder;

/*
  仕様のメモ
  フィールド
    this.boardId - ボードIDを格納する
    this.cards - todoに入っているカード(スプリント計画の対象カード)を格納する
    this.members - ボードに参加しているメンバーのidとnameを格納する
  メソッド
    fetch - 情報を取得して、ビルダーを初期化する。callback関数を指定する
    selectCard - カードをスプリント対象カードにする
    addMemberToCard - カードにメンバーを追加する。自動的に対象カードになる
    unselectCard - カードをスプリント対象カードから除外する、メンバーはクリアされる
    removeMemberFromCard - カードから指定されたメンバーを消去する
    isMemberRegisted - 指定されたカードにメンバーが追加されているか調べる
    clearMemberFromCard - カードのメンバーをすべて消去する
    setFinishDate - 目標日を設定する
    getSelectedCards - 選択されたカード一覧を取得する
    getUnselectedCards - 選択されていないカード一覧を取得する
    getMemberList - ボードに参加しているメンバーのリストを取得する
    regist - システムにスプリントを登録する
*/

SprintBuilder = (function() {
	function _class(boardId) {
		this.boardId = boardId;
		this.cards = [];
		this.members = [];
		this.finishDate = null;
	}

	_class.defaultErrorHandler = function(msg){
		alert(msg);
	};

	_class.prototype.fetch(callback, errorHandler){

	}

	return _class;

})();
