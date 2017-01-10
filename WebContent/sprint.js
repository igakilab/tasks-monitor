/*
 * メンバー選択ボタンを生成します。
 * builderから追加されているメンバーを識別してボタンのスタイルを変更します
 */
function generateMemberAssignButtonGroup(card, builder){
	//ボタングループの生成
	var bgroup = $("<div></div>").addClass("btn-group");
	//メンバーリストの取得
	var members = builder.getBoardMembers();

	//メンバーボタンを作成、メンバーidをブロックスコープに指定(with構文)
	for(var i=0; i<members.length; i++){with({mid: members[i].id}){
		//クラスの値
		var claz = "btn btn-sm";
		//ボタンが押された時のコールバック関数
		var onClick = null;

		//メンバーが登録されているかどうかで分岐、上記の変数を設定
		if( builder.isMemberRegisted(card.id, mid) ){
			claz = claz + " btn-info active";
			onClick = function(){removeMemberButtonPressed(builder, card.id, mid)};
		}else{
			claz = claz + " btn-default";
			onClick = function(){addMemberButtonPressed(builder, card.id, mid)};
		}

		//ボタンをグループに追加
		bgroup.append($("<button></button>").addClass(claz)
			.on('click', onClick)
			.text(members[i].name));
	}}
	return bgroup;
}

/*
 * 選択されているカードリストの要素を作成します
 */
function generateSelectedCardRow(card, builder){
	//取り消しボタンのコールバック関数
	var onButtonPressed = function(){
		unselectButtonPressed(builder, card.id);
	};

	return $("<tr></tr>").append(
		//取り消しボタン
		$("<td></td>").append($("<button></button>").addClass("btn btn-sm btn-danger")
			.on('click', onButtonPressed)
			.append(Util.bsGlyphicon("arrow-left"), " 取消")),
		//タスク名
		$("<td></td>").text(card.name),
		//メンバー選択ボタン
		$("<td></td>").append(generateMemberAssignButtonGroup(card, builder))
	);
}

/*
 * 選択されていないカードリストの要素を作成します
 */
function generateUnselectedCardRow(card, builder){
	//移動ボタンのコールバック関数
	var onButtonPressed = function(){
		selectButtonPressed(builder, card.id);
	};

	return $("<tr></tr>").append(
		//タスク名
		$("<td></td>").append(
			card.name + " ",
			(card.remainedTimes > 0 ?
				$("<span></span>")
					.addClass("label label-danger")
					.text("未達成:" + card.remainedTimes) :
				null
			)
		),
		//移動ボタン
		$("<td></td>").append(
			$("<button></button>").on('click', onButtonPressed)
				.append("移動 ", Util.bsGlyphicon("arrow-right"))
				.addClass("btn btn-sm btn-primary")
				.css("float", "right"))
	);
}

/*
 * カードリストを更新します
 */
function setCardList(builder){
	//未選択リストの更新
	var unselected = builder.getUnselectedCards();
	$(".unselected-card-list").empty();
	for(var i=0; i<unselected.length; i++){
		$(".unselected-card-list").append(
			generateUnselectedCardRow(unselected[i], builder));
	}

	//選択済みリストの更新
	var selected = builder.getSelectedCards();
	$(".selected-card-list").empty();
	for(var i=0; i<selected.length; i++){
		$(".selected-card-list").append(
			generateSelectedCardRow(selected[i], builder));
	}
}


/*
 * メンバーのタグリストを更新します
 */
function setMemberTags(builder){
	var $dl = $(".member-tags-list");

	$dl.empty();

	for(var i=0; i<builder.members.length; i++){
		var memberName = builder.members[i].name;
		var memberTags = builder.members[i].tags;

		var $ul = $("<ul></ul>").addClass("list-inline");
		for(var j=0; j<memberTags.length; j++){
			$ul.append(
				$("<li></li>").append(
					memberTags[j].tagName,
					$("<span></span>").addClass("badge").text(memberTags[j].count)
			));
		}

		$dl.append(
			$("<dt></dt>").text(memberName),
			$("<dd></dd>").append($ul)
		);
	}
}

function selectButtonPressed(builder, cardId){
	builder.selectCard(cardId);
	setCardList(builder);
}

function unselectButtonPressed(builder, cardId){
	builder.unselectCard(cardId);
	setCardList(builder);
}

function addMemberButtonPressed(builder, cardId, mid){
	builder.addMemberToCard(cardId, mid);
	setCardList(builder);
}

function removeMemberButtonPressed(builder, cardId, mid){
	builder.removeMemberFromCard(cardId, mid);
	setCardList(builder);
}