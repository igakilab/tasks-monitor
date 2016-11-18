package jp.ac.oit.igakilab.tasks.trello.model;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrelloNumberedCard extends TrelloCard{
	public static String TASKNUMBER_REGEX = "#([0-9]+)\\s*";

	public static void main(String[] args){
		TrelloNumberedCard card = new TrelloNumberedCard();
		List<TrelloCard> cards = Arrays.asList(
			new TrelloCard(), new TrelloCard(), new TrelloCard());
		cards.get(0).setName("#3 task1");
		cards.get(1).setName("#21 task2");
		cards.get(2).setName("#22 task3");
		card.setName("task4");
		System.out.println(card.getNumber());
		System.out.println(card.getName(false));
		card.applyNumber(cards);
		System.out.println(card.getName(true));
	}

	public TrelloNumberedCard(){
		super();
	}

	public TrelloNumberedCard(TrelloCard card){
		super(card);
	}

	private static Integer sgetNumber(String str){
		if( str == null ) return null;
		Matcher m = Pattern.compile(TASKNUMBER_REGEX).matcher(str);
		if( m.find() ){
			String numstr = m.group(1);
			Integer num = null;
			try{
				num = Integer.parseInt(numstr);
			}catch(NumberFormatException e0){
				return null;
			}
			return num;
		}else{
			return null;
		}
	}

	private int getMaxTaskNumber(List<TrelloCard> cards){
		int max = 0;
		for(TrelloCard card : cards){
			Integer num = sgetNumber(card.name);
			if( num != null ){
				if( max < num ){
					max = num;
				}
			}
		}
		return max;
	}

	public boolean isNumbered(){
		return sgetNumber(this.name) != null;
	}

	public Integer getNumber(){
		return sgetNumber(this.name);
	}

	public void applyNumber(int num){
		if( this.name == null ) this.name = "";
		String nameonly = this.name.replaceAll(TASKNUMBER_REGEX, "");
		this.name = "#" + num + " " + nameonly;
	}

	public void applyNumber(List<TrelloCard> cards){
		int maxnum = getMaxTaskNumber(cards);
		applyNumber(maxnum + 1);
	}

	public void applyNumber(TrelloBoard board){
		applyNumber(board.getCards());
	}

	public void setName(String name){
		setName(name, null);
	}

	public void setName(String name, List<TrelloCard> cards){
		Integer argnum = sgetNumber(name);

		if( argnum == null ){
			Integer num = sgetNumber(this.name);

			if( num != null ){
				this.name = "#" + num + " " + name;
			}else{
				if( cards != null ){
					int maxnum = getMaxTaskNumber(cards);
					this.name = "#" + (maxnum+1) + " " + name;
				}else{
					this.name = name;
				}
			}
		}else{
			this.name = name;
		}
	}

	public String getName(){
		return getName(true);
	}

	public String getName(boolean withTaskNumber){
		if( this.name == null ) return null;
		if( withTaskNumber ){
			return this.name;
		}else{
			return this.name.replaceAll(TASKNUMBER_REGEX, "");
		}
	}
}
