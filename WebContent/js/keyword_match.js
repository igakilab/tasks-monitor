var keywords=["a","b","c","d"];

function autokeyword(name, keyword){
	keyword = keywords;
	for (var i = keyword.length - 1; i >= 0; i--) {
		regexp = new RegExp(keyword[i] + '.*?', 'g');
		match = name.match(regexp);
	};
}