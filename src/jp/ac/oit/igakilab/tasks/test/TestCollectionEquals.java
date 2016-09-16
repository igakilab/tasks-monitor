package jp.ac.oit.igakilab.tasks.test;

import java.util.HashSet;
import java.util.Set;

public class TestCollectionEquals {
	public static void main(String[] args){
		Set<String> col1 = new HashSet<String>();
		Set<String> col2 = new HashSet<String>();

		col1.add("A");
		col1.add("B");
		col2.add("A");
		col2.add("B");

		System.out.println("equals? : " + col1.equals(col2));
		System.out.println("col1 contains A? : " + col1.contains("A"));
	}
}
