var keywords=["aaaa","b","c","d"];

addSkillsTable(null, keywords);

function autokeyword(name, keyword){
	for (var i = 0; i < keyword.length; i++) {
		regexp = new RegExp(keyword[i] + '.*?', 'i');
		if (name.match(regexp)){
			console.log('OK');
			return i;
		}else{
			console.log('NG');
		}
	};
}

function addSkillsTable(card, keyword){
	$(".got-skills").append(
		$("<tr></tr>").append(
			$("<td></td>").text("aaaaをする"),
			$("<td></td>").text(keywords[autokeyword("aaaaをする",keywords)])
		)
	)
}