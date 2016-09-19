var Util = {};

//与えられた引数がnullまたはundefinedかどうか調べます
Util.isNull = function(val){
	return (typeof val === 'undefined' || val === null);
}

//リクエストされたurlからパラメータを取り出します。
Util.getUrlParameters = function(){
	var arg = new Object;
	var pair=location.search.substring(1).split('&');
	for(var i=0;pair[i];i++) {
	    var kv = pair[i].split('=');
	    arg[kv[0]]=kv[1];
	}
	return arg;
};

//alertを表示します
Util.showAlert = function(msg){
	if( Util.isNull(msg) ){
		$(".alert-area").addClass("hidden");
	}else{
		$(".alert-area").text(msg).removeClass("hidden");
	}
}

//成功メッセージを表示します
Util.showSuccessAlert = function(msg){
	if( Util.isNull(msg) ){
		$(".alert-success-area").addClass("hidden");
	}else{
		$(".alert-success-area").text(msg).removeClass("hidden");
	}
}

//bootstrapのGlyphiconを示すjqueryオブジェクトを返します。
//もじglyphicon-chevron-rightのオブジェクトを取得したいならば
//Util.bsGlyphicon("chevron-right")を指定します。
Util.bsGlyphicon = function(name){
	return $("<span></span>")
		.addClass("glyphicon glyphicon-" + name)
		.attr("aria-hidden", "true");
}

//dateオブジェクトを文字列フォーマットに変換します
// http://qiita.com/osakanafish/items/c64fe8a34e7221e811d0
Util.formatDate = function (date, format) {
	/*
	 * http://qiita.com/osakanafish
	 * http://qiita.com/osakanafish/items/c64fe8a34e7221e811d0
	 */
	if (!format) format = 'YYYY-MM-DD hh:mm:ss.SSS';
	format = format.replace(/YYYY/g, date.getFullYear());
	format = format.replace(/MM/g, ('0' + (date.getMonth() + 1)).slice(-2));
	format = format.replace(/DD/g, ('0' + date.getDate()).slice(-2));
	format = format.replace(/hh/g, ('0' + date.getHours()).slice(-2));
	format = format.replace(/mm/g, ('0' + date.getMinutes()).slice(-2));
	format = format.replace(/ss/g, ('0' + date.getSeconds()).slice(-2));
	if (format.match(/S/g)) {
		var milliSeconds = ('00' + date.getMilliseconds()).slice(-3);
		var length = format.match(/S/g).length;
		for (var i = 0; i < length; i++) format = format.replace(/S/, milliSeconds.substring(i, i + 1));
	}
	return format;
};