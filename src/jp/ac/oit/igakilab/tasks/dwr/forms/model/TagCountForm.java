package jp.ac.oit.igakilab.tasks.dwr.forms.model;

import jp.ac.oit.igakilab.tasks.sprints.CardTagsAggregator.TagCount;

public class TagCountForm{
	public static TagCountForm getInstance(TagCount tc){
		return getInstance(tc.getTagString(), tc.getCount());
	}

	public static TagCountForm getInstance(String tag, int cnt){
		TagCountForm form = new TagCountForm();
		form.setTagName(tag);
		form.setCount(cnt);
		return form;
	}

	private String tagName;
	private int count;

	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}