function generateCardListItem(card){
	var item = $("<li></li>").addClass("list-group-item");
	item.append($("<h4></h4>")
		.addClass("list-group-item-heading").text(card.name));
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
	$(".board-title")
		.append(board.name + " ")
		.append($("<small></small>").text(board.id));

	//generate list divs
	var listDivs = [];
	for(var i=0; i<board.lists.length; i++){
		var list = board.lists[i];

		//search cards
		var cards = [];
		board.cards.forEach(function(e, i, a){
			if( e.listId == list.id ) cards.push(e);
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