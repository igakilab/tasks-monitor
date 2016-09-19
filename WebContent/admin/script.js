/* members functions
 * generateMemberTableRowButtons(idx)
 * generateMemberTableRow(member, idx)
 * setMembers(members)
 * setMemberFormValues(action, data)
 * showMemberFormAlert(msg)
 * generateMemberFormApplyButton(action)
 * setupMemberFormModal(action, data)
 * memberFormApplyButtonPressed(action)
 */

function generateMemberTableRowButtons(idx){
	return {
		edit: $("<button></button>")
			.addClass("btn btn-success")
			.attr("data-toggle", "modal")
			.attr("data-target", "#memberFormModal")
			.data("action", "edit")
			.data("rowindex", idx)
			.append(Util.bsGlyphicon("pencil"), " 編集"),
		remove: $("<button></button>")
			.addClass("btn btn-danger")
			.attr("data-toggle", "modal")
			.attr("data-target", "#memberFormModal")
			.data("action", "remove")
			.data("rowindex", idx)
			.append(Util.bsGlyphicon("trash"), " 削除")
	};
}

function generateMemberTableRow(member, idx){
	var row = $("<tr></tr>").append(
		$("<td></td>").text(member.id),
		$("<td></td>").text(member.name),
		$("<td></td>").text(
			Util.isNull(member.trelloId) ? "" : member.trelloId),
		$("<td></td>").text(
			Util.isNull(member.slackId) ? "" : member.slackId),
		$("<td></td>").text(
			member.admin ? "あり" : "なし"));

	var buttons = generateMemberTableRowButtons(idx);
	row.append($("<td></td>").append(
		buttons.edit, " ", buttons.remove));

	return row;
}

function setMembers(members){
	//init
	$(".members-tbody").empty();

	//set member info
	for(var i=0; i<members.length; i++){
		var row = generateMemberTableRow(members[i], i);

		$(".members-tbody").append(row);
	}
}

function setMemberFormValues(action, data){
	// config values
	var values = {};
	if( action == "add" ){
		values.id = "";
		values.name = "";
		values.trelloId = "";
		values.slackId = "";
		values.admin = false;
	}else if( action == "edit" || action == "remove" ){
		values.id = data.id;
		values.name = Util.isNull(data.name) ? "" : data.name;
		values.trelloId =
			Util.isNull(data.trelloId) ? "" : data.trelloId;
		values.slackId =
			Util.isNull(data.slackId) ? "" : data.slackId;
		values.isAdmin =
			Util.isNull(data.admin) ? false : data.admin;
	}
	var isRemoveOrEdit = (action == "edit" || action == "remove");
	var isRemove = (action == "remove");

	// set values
	$("#memberFormInputId").val(values.id)
		.prop("disabled", isRemoveOrEdit);
	$("#memberFormInputName").val(values.name)
		.prop("disabled", isRemove);
	$("#memberFormInputTrelloId").val(values.trelloId)
		.prop("disabled", isRemove);
	$("#memberFormInputSlackId").val(values.slackId)
		.prop("disabled", isRemove);
	$("#memberFormCheckboxAdmin").prop("checked", values.isAdmin)
		.prop("disabled", isRemove);

	//trello autocomplete control
	if( action == "add" || action == "edit" ){
		$("#memberFormInputTrelloIdHelp").empty().append(
			"trelloのusernameからtrello IDを自動入力することができます",
			$("<br></br>"),
			"trello IDの欄にusernameを入力し、自動入力をクリックしてください。",
			$("<button></button>").addClass("btn btn-default btn-xs")
				.attr("type", "button")
				.text("自動入力")
				.on("click", trelloIdAutoComplete)
		).removeClass("hidden");
	}else{
		$("#memberFormInputTrelloIdHelp").addClass("hidden");
	}
}

function showMemberFormAlert(msg){
	if( msg != null ){
		$("#memberFormAlertArea").empty()
			.append(msg)
			.removeClass("hidden");
	}else{
		$("#memberFormAlertArea").addClass("hidden");
	}
}

function generateMemberFormApplyButton(action){
	var button = $("<button></button>");
	//button.attr("data-dismiss", "modal");
	if( action == "add" ){
		button.addClass("btn btn-primary")
			.text("Save changes")
			.on("click", function(){
				memberFormApplyButtonPressed("add");
			});
	}else if( action == "edit" ){
		button.addClass("btn btn-primary")
			.text("Save changes")
			.on("click", function(){
				memberFormApplyButtonPressed("edit");
			});
	}else if( action == "remove" ){
		button.addClass("btn btn-danger")
			.text("Delete")
			.on("click", function(){
				memberFormApplyButtonPressed("remove");
			});
	}
	return button;
}

function setupMemberFormModal(action, data){
	//init
	showMemberFormAlert(null);
	$("#memberFormModalApplyBtnArea").empty();

	//set form values
	setMemberFormValues(action, data);

	//setTitle
	var mtitle = "unknown";
	switch( action ){
	case "add":
		mtitle = "メンバー追加"; break;
	case "edit":
		mtitle = "メンバー編集"; break;
	case "remove":
		mtitle = "メンバー削除"; break;
	}
	$("#memberFormModalTitle").text(mtitle);

	if( action == "remove" ){
		showMemberFormAlert("メンバーを削除してもよろしいですか?");
	}

	//set button
	$("#memberFormModalApplyBtnArea").append(
		generateMemberFormApplyButton(action));
}

function trelloIdAutoComplete(){
	var username = $("#memberFormInputTrelloId").val();
	$("#memberFormInputTrelloId").val("取得中...").prop("disabled", true);
	AdminMemberEditor.getTrelloIdByTrelloUserName(username, function(data){
		if( data != null ){
			showMemberFormAlert(null);
			$("#memberFormInputTrelloId").val(data).prop("disabled", false);
		}else{
			showMemberFormAlert("trello idを取得できませんでした");
			$("#memberFormInputTrelloId").val(username).prop("disabled", false);
		};
	})
}

function memberFormApplyButtonPressed(action){
	var member = $("#memberForm").serializeJson();
	member.admin = $("#memberFormCheckboxAdmin").prop("checked");

	if( action == "add" ){
		AdminMemberEditor.canAddMemberId(member.id, function(canAdd){
			if( canAdd ){
				AdminMemberEditor.addMember(member, {
					callback: function(ret){
						$("#memberFormModal").modal("hide");
						Util.showAlertToDiv(member.id + "を追加しました", "success");
					},
					errorHandler: Util.showAlertToDiv
				});
			}else{
				showMemberFormAlert("このメンバーidは使用できません");
			}
		});
	}else if( action == "edit" ){
		AdminMemberEditor.updateMember(member, {
			callback: function(ret){
				$("#memberFormModal").modal("hide");
				Util.showAlertToDiv(member.id + "を更新しました", "success");
			},
			errorHandler: Util.showAlertToDiv
		});
	}else if( action == "remove" ){
		AdminMemberEditor.deleteMemberById(member.id, {
			callback: function(ret){
				$("#memberFormModal").modal("hide");
				Util.showAlertToDiv(member.id + "を削除しました", "warning");
			},
			errorHandler: Util.showAlertToDiv
		});
	}
}
