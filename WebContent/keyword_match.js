var keywords=["a","b","c","d"];

//addSkillsTable(null, keywords);

function autokeyword(name, keyword){
	for (var i = 0; i < keyword.length; i++) {
		regexp = new RegExp(keyword[i] + '.*?', 'i');
		if (name.match(regexp)){
			return i;
		}
	};
}

function addSkillsTable(card, keyword){
	$(".got-skills").append(
		$("<tr></tr>").append(
			$("<td></td>").text(card.name),
			$("<td></td>").text(keywords[autokeyword(card.name,keywords)])
		)
	)
}