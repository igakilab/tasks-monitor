/*
 * スキルタグの付加操作を簡単にできるクラスです。
 * フィールド
 * -defaultTags
 *   デフォルトのタグ
 * -cards
 *   {id: <カードID>, tags: <タグ>, modified: <変更されていたらtrue>}
 * メソッド
 * -isTagged(<カードID>, <タグ文字列>)
 *   指定されたカードIDの要素にタグが付加されていればtrueを返却します
 * -getCard(<カードID>)
 *   カードIDで指定されたカードを返却します。
 * -addDefaultTag(<タグ文字列>)
 *   デフォルトタグにタグを追加します。既に追加されている場合は何も処理しません
 * -addTag(<カードID>, <タグ文字列>)
 *   タグを新しく追加します。
 * -removeTag(<カードID>, <タグ文字列>)
 *   タグを除去します。
 * -turnTag(<カードID>, <タグ文字列>)
 *   タグの選択状態を反転させます。登録されていないタグの場合は新しく追加されます。
 *   この処理によりタグが新しく選択された場合はtrueを、
 *   選択が外された場合や失敗した場合はfalseが返却されます。
 * -isModified()
 *   タグが変更された要素があればtrueを返却します
 * -getModifiedCards()
 *   変更されたカードを返却します
 * -initBySprintAnalyzer(<受信データ>)
 *   SprintAnalyzerDataからこのクラスを初期化します。
 */
var TasksSkillManager = (function(){

	/*
	 * コンストラクタ
	 */
	function _class(){
		this.defaultTags = [];
		this.cards = [];
	}


	/*
	 * カードIDで指定されたカードを返却します
	 */
	_class.prototype.getCard = function(cardId){
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].id == cardId ){
				return this.cards[i];
			}
		}
		return null;
	}


	/*
	 * カードIDのカードに指定されたタグが登録されている場合trueを返却します
	 * タグが登録されていない場合や、指定されたIDのカードがない場合はfalseを返却します
	 */
	_class.prototype.isTagged = function(cardId, tagString){
		var card = this.getCard(cardId);
		if( card != null ){
			for(var i=0; i<card.tags.length; i++){
				if( card.tags[i] == tagString ) return true;
			}
		}
		return false;
	}


	/*
	 * デフォルトタグのリストにタグを追加します
	 * すでに登録されている場合は何も処理しません
	 */
	_class.prototype.addDefaultTag = function(tagString){
		for(var i=0; i<this.defaultTags.length; i++){
			if( this.defaultTags[i] == tagString ) return;
		}
		this.defaultTags.push(tagString);
	}


	/*
	 * カードIDで指定されたカードにタグを追加します。
	 * 同時にdefaultTagへの追加を行い、modifiedフラグをtrueにします。
	 */
	_class.prototype.addTag = function(cardId, tagString){
		if( !this.isTagged(cardId, tagString) ){
			var card = this.getCard(cardId);
			if( card != null ){
				card.tags.push(tagString);
				this.addDefaultTag(tagString);
				card.modified = true;
			}
		}
	}


	/*
	 * カードIDで指定されたカードからタグを除去します。
	 * 同時にmodifiedフラグをtrueにします。
	 */
	_class.prototype.removeTag = function(cardId, tagString){
		if( this.isTagged(cardId, tagString) ){
			var card = this.getCard(cardId);
			card.tags = card.tags.filter(function(e){
				return e != tagString;
			});
			card.modified = true;
		}
	}
	
	
	/*
	 *   タグの選択状態を反転させます。登録されていないタグの場合は新しく追加されます。
	 *   この処理によりタグが新しく選択された場合はtrueを、
	 *   選択が外された場合や失敗した場合はfalseが返却されます。
	 */
	_class.prototype.turnTag = function(cardId, tagString){
		if( this.isTagged(cardId, tagString) ){
			this.removeTag(cardId, tagString);
			return false;
		}else{
			if( this.getCard(cardId) != null ){
				this.addtag(cardId, tagString);
				return true;
			}else{
				return false;
			}
		}
	}


	/*
	 * 変更されたカードが一つでもあるとtrueを返却します
	 */
	_class.prototype.isModified = function(){
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].modified ) return true;
		}
		return false;
	}


	/*
	 * 変更されたカード(modifiedがtrue)をピックアップして返却します。
	 */
	_class.prototype.getModifiedCards = function(){
		var res = [];
		for(var i=0; i<this.cards.length; i++){
			if( this.cards[i].modified ){
				res.push(this.cards[i]);
			}
		}
		return res;
	}


	/*
	 * SprintResultAnaluzerからこのインスタンスを初期化します
	 */
	_class.prototype.initBySprintResultAnalyzer = function(analyzer){
		this.defaultTags = analyzer.getDefaultTags();

		this.cards = [];
		var sprintCards = analyzer.getCards();
		for(var i=0; i<sprintCards.length; i++){
			this.cards.push({
				id: sprintCards[i].id,
				name: sprintCards[i].name,
				tags: sprintCards[i].tags,
				modified: false
			});
		}
	}


	return _class;

})();
