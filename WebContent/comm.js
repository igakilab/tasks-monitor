function TextCont(){
	var chList = [document.judge.radio1.checked,document.judge.radio2.checked,document.judge.radio3.checked,document.judge.radio4.checked];
	tar = document.getElementById("comm");

	for(var i=0;i<chList.length;i++){
		if(chList[1]==true||chList[2]==true||chList[3]==true){
			tar.innerHTML = "<input type=\"text\" name=\"comm\" size=\"50\">";
		}
		else{
			tar.innerHTML = "";
		}
	}
}