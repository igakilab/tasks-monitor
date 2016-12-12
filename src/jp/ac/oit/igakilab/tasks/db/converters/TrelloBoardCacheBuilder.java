package jp.ac.oit.igakilab.tasks.db.converters;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import jp.ac.oit.igakilab.tasks.trello.TrelloDateFormat;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloBoard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloCard;
import jp.ac.oit.igakilab.tasks.trello.model.TrelloList;
import jp.ac.oit.igakilab.tasks.util.DocumentValuePicker;

public class TrelloBoardCacheBuilder
implements DocumentParser<TrelloBoard>{

	private TrelloBoard buildBoard(Document doc){
		if( !(doc.get("data") instanceof Document) ) return null;

		//ボードインスタンスの生成
		TrelloBoard board = new TrelloBoard();

		//ボードデータの更新
		DocumentValuePicker picker = new DocumentValuePicker((Document)doc.get("data"));
		board.setId(picker.getString("id", ""));
		board.setName(picker.getString("name", "unknown"));
		board.setDesc(picker.getString("desc", null));
		board.setShortLink(picker.getString("shortUrl", null));
		board.setClosed(picker.getBoolean("closed", false));

		//リストの更新
		List<Document> lists = picker. getDocumentArray("lists");
		TrelloList[] prelists = board.getLists().toArray(new TrelloList[0]);
		board.clearLists();
		for(Document docList : lists){
			//インスタンスを初期化等
			DocumentValuePicker pickerl = new DocumentValuePicker(docList);
			String lid = pickerl.getString("id", "");

			//既存リストインスタンスがないかチェック
			TrelloList list = null;
			for(TrelloList pl : prelists){
				if( lid != null && pl.getId().equals(lid) ){
					list = pl;
					break;
				}
			}
			//見つからなかった場合はリストを生成
			list = new TrelloList();

			//値を設定
			list.setId(lid);
			list.setName(pickerl.getString("name", "unknown"));
			list.setClosed(pickerl.getBoolean("closed", false));

			//ボードに追加
			board.addList(list);
		}

		//カードの解析
		TrelloDateFormat df = new TrelloDateFormat();
		List<Document> cards = picker.getDocumentArray("cards");
		TrelloCard[] precards = board.getCards().toArray(new TrelloCard[0]);
		for(Document docCard : cards){
			//インスタンス初期化
			DocumentValuePicker pickerc = new DocumentValuePicker(docCard);
			String cid = pickerc.getString("id", "");

			//既存カードインスタンスがないかチェック
			TrelloCard card = null;
			for(TrelloCard pc : precards){
				if( cid != null && pc.getId().equals(cid) ){
					card = pc;
					break;
				}
			}
			//見つからなかった場合はカードを生成
			card = new TrelloCard();

			//値を設定
			card.setId(cid);
			card.setListId(pickerc.getString("idList", null));
			card.setName(pickerc.getString("name", "unknown"));
			card.setDesc(pickerc.getString("desc", null));
			card.setClosed(pickerc.getBoolean("closed", false));
			//値を設定(期限)
			String dueString = pickerc.getString("due", null);
			if( dueString != null ){
				try{
					Date due = df.parse(dueString);
					card.setDue(due);
				}catch(ParseException e0){}
			}
			//値を設定(担当者trelloid)
			for(String mid : pickerc.getStringArray("idMembers")){
				card.addMemberId(mid);
			}

			//ボードに追加
			board.addCard(card);
		}

		return board;
	}

	@Override
	public TrelloBoard parse(Document doc) {
		if( doc != null ){
			return buildBoard(doc);
		}
		return null;
	}


}
