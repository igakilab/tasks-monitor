function generateCardListItemText(card){
	var texts = [];
	if( card.desc != null )
		texts.push(card.desc);
	if( card.due != null )
		texts.push("期限: " + Util.formatDate(card.due, "YYYY/MM/DD hh:mm"));

	if( texts.length > 0 ){
		var text = $("<p></p>").addClass("list-group-item-text");
		for(var i=0; i<texts.length; i++){
			text.append(texts[i]);
			if( i != (texts.length - 1) ) text.append($("<br></br>"));
		}
		return text;
	}
	return null;
}

function generateCardListItem(card){
	var item = $("<li></li>").addClass("list-group-item");
	item.append($("<h4></h4>")
		.addClass("list-group-item-heading").text(card.name));

	var itemText = generateCardListItemText(card);
	if( itemText != null ){
		item.append(itemText);
	}

	return item;
}

function generateListDiv(list, cards){
	var listGroup =  $("<ul></ul>").addClass("list-group");
	if( typeof cards.forEach === 'function' ){
		cards.forEach(function(e, i, a){
			listGroup.append(generateCardListItem(e));
		});
	}

	var listDiv = $("<div></div>").addClass("col-md-4");
	listDiv.append($("<h2></h2>").text(list.name));
	listDiv.append(listGroup);

	return listDiv;
}

function setBoardData(board){
	//init
	$(".board-title").empty();
	$(".board-lists").empty();
	console.log("init");

	//set board title
	var boardUrl = (board.shortLink != null ?
		"http://trello.com/b/" + board.shortLink : null);
	$(".board-title")
		.append(board.name + " ")
		.append($("<small></small>").append(
			board.id + " ",
			(boardUrl != null ?
				$("<a></a>").attr("href", boardUrl).text(boardUrl)
				: "")));

	//generate list divs
	var listDivs = [];
	for(var i=0; i<board.lists.length; i++){
		var list = board.lists[i];
		if( list.closed ) continue;

		//search cards
		var cards = [];
		board.cards.forEach(function(e, i, a){
			if( e.listId == list.id && !cards.closed ) cards.push(e);
		})

		//generate list div
		console.log(list);
		var div = generateListDiv(list, cards);
		listDivs.push(div);
	}

	//set list divs
	listDivs.forEach(function(e, i, a){
		$(".board-lists").append(e);
		console.log("append");
		console.log(e);
	});
}

function generateBoardInfoRow(boardInfo){
	var viewerLink = "viewer.html?boardId=" + boardInfo.id;
	var trow = $("<tr></tr>")
		.append($("<td></td>").text(boardInfo.id))
		.append($("<td></td>").text(boardInfo.lastUpdate))
		.append($("<td></td>").append($("<a></a>")
			.addClass("btn btn-default")
			.attr("href", viewerLink)
			.append("VIEW")
			.append(Util.bsGlyphicon("chevron-right"))));
	return trow;
}

function setBoardInfo(boardInfos){
	//init
	$(".board-info-table-body").empty();

	//generate and set info row
	for(var i=0; i<boardInfos.length; i++){
		var row = generateBoardInfoRow(boardInfos[i]);
		$(".board-info-table-body").append(row);
	}
}