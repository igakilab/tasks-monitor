package jp.ac.oit.igakilab.tasks.hubot;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.ac.oit.igakilab.tasks.members.MemberSlackIdTable;

public class ChannelNotification {
	private HubotSendMessage msg;
	private MemberSlackIdTable stable;
	private boolean test;

	/**
	 * コンストラクター
	 * @param msg hubotのメッセージ送信インスタンス
	 */
	public ChannelNotification(HubotSendMessage msg){
		this.msg = msg;
		this.test = false;
	}

	/**
	 * スラックのid変換テーブルを設定します
	 * nullを設定してメンションの指定を無効にすることもできます
	 * @param stable slackid変換テーブル
	 */
	public void setSlackIdTable(MemberSlackIdTable stable){
		this.stable = stable;
	}

	/**
	 * テストモードの設定用メソッドです
	 * テストモードでは実際にテキストが送信されずに標準出力に表示されます
	 * @param t テストモード有効化の時にtrue
	 */
	public void setTestMode(boolean t){
		this.test = t;
	}

	/**
	 * メッセージを送信するためのメソッドです
	 * @param dest 送信先チャンネル
	 * @param text 送信テキスト
	 * @return 成功時にtrue
	 */
	private boolean sendMessage(String dest, String text){
		if( test ){
			System.out.println(text);
			return true;
		}

		//送信
		try{
			msg.send(dest, text);
		}catch(IOException e0){
			e0.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * タスクの表を与えられたstringbufferに追加します
	 * @param buffer 追加先のバッファー
	 * @param cards 追加するカードのリスト
	 */
	private void appendTaskNotifyText(StringBuffer buffer, List<NotifyTrelloCard> cards){
		//バッファとインスタンスを初期化
		DateFormat df = new SimpleDateFormat("M/d");

		for(int i=0; i<cards.size(); i++){
			NotifyTrelloCard card = cards.get(i);

			buffer.append("[");

			//期限と現在のリストの位置を追加する
			buffer.append("<" + card.getList().getName() + ">");
			if( card.getCard().getDue() != null ){
				buffer.append(" " + df.format(card.getCard().getDue()) + "まで");
			}

			//担当者のメンションを追加する
			if( stable != null ){
				for(String mid : card.getMemberIds()){
					String tmp = stable.getSlackId(mid);
					if( tmp != null ){
						buffer.append(" @" + tmp);
					}
				}
			}

			//改行
			buffer.append("]\n");

			//タスク名を追加する
			buffer.append(card.getCard().getName());

			//改行
			if( i < cards.size() - 1 ){
				buffer.append("\n\n");
			}
		}
	}

	public boolean taskNotification(String dest, String head, List<NotifyTrelloCard> cards){
		//メッセージを作成
		StringBuffer buffer = new StringBuffer();
		buffer.append(head + "\n");
		appendTaskNotifyText(buffer, cards);

		return sendMessage(dest, buffer.toString());
	}

	public boolean sprintBeginNotification
	(String dest, Date beginDate, Date finishDate, List<NotifyTrelloCard> cards){
		//メッセージ作成
		StringBuffer buffer = new StringBuffer();
		DateFormat df = new SimpleDateFormat("M/d");

		buffer.append("新しいスプリントが開始しました\n");
		buffer.append(df.format(beginDate) + " ～ " + df.format(finishDate) + "\n\n");

		appendTaskNotifyText(buffer, cards);

		return sendMessage(dest, buffer.toString());
	}
}
