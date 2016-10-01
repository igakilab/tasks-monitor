package jp.ac.oit.igakilab.tasks.util;

import java.util.function.Predicate;

public class RandomIdGenerator {
	public static void main(String[] args){
		RandomIdGenerator gen = new RandomIdGenerator(CHARSET_HEX);
		System.out.println(gen.generate(1, 10, (str ->
			!str.matches("[0123456789abcde]"))));
	}

	public static String CHARSET_HEX = "0123456789abcdef";
	public static String CHARSET_NUMBER = "0123456789";
	public static String CHARSET_EUPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static String CHARSET_ELOWER = "abcdefghijklmnopqrstuvwxyz";

	public String charset;

	public RandomIdGenerator(String charset){
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void appendCharset(String charset){
		this.charset += charset;
	}

	public String generate(int length){
		StringBuffer result = new StringBuffer();
		for(int i=0; i<length; i++){
			int rnd = (int)(Math.random() * charset.length());
			result.append(charset.charAt(rnd));
		}
		return result.toString();
	}

	public String generate(int length, int overflow, Predicate<String> predicate){
		String result = null;

		for(int i=0; i<overflow; i++){
			String tmp = generate(length);
			if( predicate.test(tmp) ){
				result = tmp;
				break;
			}
		}

		return result;
	}
}
