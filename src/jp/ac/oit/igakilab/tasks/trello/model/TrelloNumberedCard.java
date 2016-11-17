package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrelloNumberedCard extends TrelloCard{
	public static void main(String[] args){
		TrelloNumberedCard card = new TrelloNumberedCard();
		card.setName("#34 これが #35 タスクだ! #36");

		System.out.println(card.getNumber()+"("+card.getName()+")");

		card.setNumber(938);
		System.out.println(card.getNumber()+"("+card.getName()+")");

		card.clearNumber();
		System.out.println(card.getNumber()+"("+card.getName()+")");

	}

	public static final String TASKNUM_REGEX = "#([0-9]+)";
	public static final String REMOVE_TASKNUM_REGEX = "\\s*#([0-9]+)\\s*";
	public TrelloNumberedCard(){
		super();
	}

	public TrelloNumberedCard(TrelloCard card){
		super(card);
	}

	public boolean hasNumber(){
		return getNumber() != null;

	}

	public Integer getNumber(){
		Matcher m = Pattern.compile(TASKNUM_REGEX).matcher(name);
		Integer num = null;
		if( m.find() ){
			try{
				num = Integer.parseInt(m.group(1));
			}catch(NumberFormatException e0){}
		}
		return num;
	}

	public void setNumber(int num){
		clearNumber();
		name = "#" + num + " " + name;
	}

	public void clearNumber(){
		name = name.replaceAll(REMOVE_TASKNUM_REGEX, "");
	}
}
