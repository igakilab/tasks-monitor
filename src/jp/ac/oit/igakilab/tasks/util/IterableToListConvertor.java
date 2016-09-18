package jp.ac.oit.igakilab.tasks.util;

import java.util.ArrayList;

public class IterableToListConvertor {
	public <T> ArrayList<T> convertToArrayList(Iterable<T> iterable){
		ArrayList<T> list = new ArrayList<T>();
		iterable.forEach((data) -> list.add(data));
		return list;
	}
}
