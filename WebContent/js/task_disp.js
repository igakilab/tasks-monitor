function sendMsg(){
	var msg = DashBoard.getKanban();
	for(var i=0;i<msg.length;i++){
	document.write("<ul class=\"list-group\"><li class=\"list-group-item\"><button class=\"btn btn-default btn-xs\" style=\"float:right\"><span class=\"glyphon glyphicon-arrow-right\">"+msg[i]+"</span></button></li></ul>");
	}
	return;
}
sendMsg();