package jp.ac.oit.igakilab.tasks.test;

public class TestStringMatch {
	public static void main(String[] args){
		String[] strings = {"tasksddb.port", "tasks.db.host", "tasks.name", "test.message", "tasks"};
		String head = "tasks.db.";

		String rhead = head.replaceAll("\\.", "\\\\.");
		System.out.println("replaced: " + head + " -> " + rhead);

		String regex = "^" + rhead + "\\..*";

		System.out.println("regex: " + regex);
		for(String str : strings){
			System.out.println(str + " -> " +
				(str.matches(regex) ? "matched!" : "nomatch"));
		}
	}
}
