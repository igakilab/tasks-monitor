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