package jp.ac.oit.igakilab.tasks.sprints;

import java.util.ArrayList;
import java.util.List;

public class CardTagsAggregator {
	public static class TagCount {
		private String tagString;
		private int count;

		TagCount(String t0, int initCount){
			tagString = t0;
			count = initCount;
		}

		boolean apply(String t0){
			if( tagString.equals(t0) ){
				count++;
				return true;
			}
			return false;
		}

		public String getTagString(){
			return tagString;
		}

		public int getCount(){
			return count;
		}
	}


	private List<TagCount> tagCounts;


	public CardTagsAggregator(){
		tagCounts = new ArrayList<TagCount>();
	}

	public void apply(String tagString){
		boolean registed = false;

		for(TagCount counter : tagCounts){
			if( counter.apply(tagString) ){
				registed = true;
				break;
			}
		}

		if( !registed ){
			tagCounts.add(new TagCount(tagString, 1));
		}
	}

	public void apply(SprintResultCard srcard){
		srcard.getTags().forEach(tag -> apply(tag));
	}

	public List<TagCount> getTagCounts(){
		return tagCounts;
	}
}
