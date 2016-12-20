var ModalPrompt = (function(){

	/*
	 * コンストラクタです
	 * 引数には構成したもダルを一時的に保持しておくdiv要素のjqueryオブジェクトを指定します
	 */
	function _class($workspace){
		this.$workspace = $workspace;
		this.modalId = "modalPrompt01";

		this.initComponents();
	}

	/*
	 * コンポーネントを初期化します。(内部的な関数)
	 * 初期化される要素は、コールバック関数、キャンセル/サブミットのボタン部品です
	 */
	_class.prototype.initComponents = function(){
		var thisp = this;

		this.onButtonPressed = function(result){
			if( result == true ){
				console.log("OK BUTTON PRESSED");
			}else if( result == false ){
				console.log("CANCEL BUTTON PRESSED");
			}else{
				console.log("INPUT DATA: " + result);
			}
		};

		this.$cancelButton = $("<button></button>").addClass("btn btn-default")
			.attr({"type":"button", "data-dismiss":"modal"}).text("Close")
			.on("click", function(){
				thisp.onButtonPressed(thisp.$prompt ? null : false);
			});

		this.$submitButton = $("<button></button>").addClass("btn btn-primary")
			.attr({"type":"button", "data-dismiss":"modal"}).text("OK")
			.on("click", function(){
				thisp.onButtonPressed(thisp.$prompt ? thisp.$prompt.val() : true);
			});
	}

	/*
	 * 各種ボタンの表示文字列を設定します。
	 * actionには"cancel"か"submit"のいずれかを指定します。
	 */
	_class.prototype.setButtonText = function(action, text){
		if( action == "cancel" ){
			this.$cancelButton.text(text);
		}else if( action == "submit" ){
			this.$submitButton.text(text);
		}
	}

	/*
	 * 各種ボタンのbootstrapに基づいたボタンスタイルを設定します。
	 * actionには"cancel"か"submitのいずれかを設定します。
	 * 選択できるスタイル: default, primary, success, info, warning, danger, link
	 */
	_class.prototype.setButtonStyle = function(action, style){
		if( action == "cancel" ){
			this.$cancelButton.attr("class", "btn btn-" + style);
		}else if( action == "submit" ){
			this.$submitButton.attr("class", "btn btn-" + style);
		}
	}

	/*
	 * modalをビルドする関数です。(内部的な関数)
	 * title: ダイアログタイトル
	 * $body: ダイアログの内容(jqueryオブジェクト/テキスト)
	 * $prompt: テキストボックス(inputのjqueryオブジェクト、指定しなければ表示されない)
	 * cancelEnabled: falseの時はcancelボタンを表示しない
	 */
	_class.prototype.buildModal = function(title, $body, $prompt, cancelEnabled){
		var $modalContent = $("<div></div>").addClass("modal-content");

		//modal header
		$modalContent.append(
			$("<div></div>").addClass("modal-header").append(
				$("<button></button>").addClass("close").attr({
					"type":"button", "data-dismiss":"modal", "aria-label":"close"
				}).append(
					$("<span></span>").attr("aria-hidden", "true").append("&times;")
				),
				$("<h4></h4>").addClass("modal-title").text(title || "dialog")
			)
		);

		//modal body
		var $modalBody = $("<div></div>").addClass("modal-body");
		if( $body ) $modalBody.append($body);
		if( $prompt ){
			$modalBody.append($("<br></br>"));
			$modalBody.append($prompt);
		}
		$modalContent.append($modalBody);

		//modal footer
		var $modalFooter = $("<div></div>").addClass("modal-footer");
		if( cancelEnabled ) $modalFooter.append(this.$cancelButton);
		$modalFooter.append(this.$submitButton);
		$modalContent.append($modalFooter);

		//wrap content
		var $modal = $("<div></div>").addClass("modal fade")
		.attr({"id":this.modalId, "role":"dialog"}).append(
			$("<div></div>").addClass("modal-dialog").attr("role", "document").append(
				$modalContent
			)
		);

		//modal set
		this.$workspace.empty().append($modal);

		return this.modalId;
	}

	/*
	 * alertのダイアログを表示します
	 * onButtonPressedの引数の値: true
	 */
	_class.prototype.alert = function($body, title){
		this.$prompt = null;
		$("#" + this.buildModal(title || "alert", $body, null, false)).modal();
	}

	/*
	 * 2択のダイアログを表示します
	 * onButtonPressedの引数の値:
	 *   cancelボタン押下: false
	 *   submitボタン押下: true
	 */
	_class.prototype.confirm = function($body, title){
		this.$prompt = null;
		$("#" + this.buildModal(title || "confirm", $body, null, true)).modal();
	}

	/*
	 * プロンプト(文字列入力ボックス)を表示します
	 * onButtonPressedの引数の値:
	 *   cancelボタン押下: null
	 *   submitボタン押下: テキストボックスへの入力文字列
	 */
	_class.prototype.prompt = function($body, title){
		this.$prompt = $("<input></input>").addClass("form-control").attr({"type":"text"});
		$("#" + this.buildModal(title || "prompt", $body, this.$prompt, true)).modal();
	}

	return _class;
})();
