
function DispMemberTasks(list){
	var $tbody = $("#memberTasksTbody");
	$tbody.empty();
	for(var i=0;i<list.length;i++){
		$tbody.append($("<tr></tr>").append(
			$("<td></td>").text(list[i].id),
			$("<td></td>").text(list[i].name),
			$("<td></td>").text(list[i].finished ? "達成" : "未達成")
		).addClass(list[i].finished ? "success" : "danger"));
	}
}