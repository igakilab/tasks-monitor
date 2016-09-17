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