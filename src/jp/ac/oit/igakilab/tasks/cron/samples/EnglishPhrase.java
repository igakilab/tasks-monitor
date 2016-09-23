package jp.ac.oit.igakilab.tasks.cron.samples;

public class EnglishPhrase {
	public static class Entry{
		private String sentence;
		private String translation;

		public Entry(){
			this("", "");
		}

		public Entry(String s0, String t0){
			sentence = s0;
			translation = t0;
		}

		public String getSentence(){return sentence;}
		public void setSentence(String s0){sentence = s0;}
		public String getTranslation(){return translation;}
		public void setTranslation(String t0){translation = t0;}
	}

	public static Entry[] DEFAULT_ENTRIES = {
		new Entry("Yeah, people often tell me I'm guyish.", "男っぽいってよく言われるんですよー"),
		new Entry("I only got 2 hours of sleep last night.", "昨日、2時間しか寝てないよ"),
		new Entry("I could do with another me.", "自分のコピーほしいわー"),
		new Entry("This just come in today.", "この商品、今日入荷したばっかりなんですよ。"),
		new Entry("If I put my mind to it, I can do anything.", "私やればできる子なので"),
		new Entry("I'm allergic to metal so I can only wear pure gold.", "私金属アレルギーだから純金しかダメなの"),
		new Entry("Can I squeeze the lemon?", "レモンしぼってもいい？"),
		new Entry("We wouldn't need the police if \"Sorry\" solved everything.", "ごめんで済んだら警察いらないよ"),
		new Entry("Yeah part 1 was better.", "やっぱPart1のほうがよかったな"),
		new Entry("Which one do you wanna hear first, good news or bad news?", "いいニュースと悪いニュース、どっちから聞きたい？"),
		new Entry("The opposite of love is not hate, It's Apathy.", "好きの反対は嫌いじゃなくて無関心"),
		new Entry("Yeah, there's something to it.", "なんかこう、伝わってくるものがありますね"),
		new Entry("What was the first CD you bought?", "初めて買ったCDって何？")
	};

	private Entry[] phrases;

	public EnglishPhrase(){
		this(DEFAULT_ENTRIES);
	}

	public EnglishPhrase(Entry[] entries){
		phrases = entries;
	}

	public Entry random(){
		return phrases[(int)(Math.random() * phrases.length)];
	}
}
