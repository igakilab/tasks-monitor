package jp.ac.oit.igakilab.tasks.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TestHTTPRequest {
	public static void main(String[] args) {
        executeGet();
        executePost();
    }

	private static String listToString(List<String> list){
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<list.size(); i++){
			buffer.append(list.get(i));
			if( i < list.size()-1 ){
				buffer.append(", ");
			}
		}
		if( list.size() != 1 ){
			buffer.append(" (").append(list.size()).append(")");
		}
		return buffer.toString();
	}

    private static void executeGet() {
        System.out.println("===== HTTP GET Start =====");
        try {
            URL url = new URL("https://api.trello.com/1/members/me?key=67ad72d3feb45f7a0a0b3c8e1467ac0b&token=268c74e1d0d1c816558655dbe438bb77bcec6a9cd205058b85340b3f8938fd65");

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                HttpURLConnection.setFollowRedirects(true);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(isr);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            	System.out.println("code: " + connection.getResponseCode());
            	Map<String, List<String>> headers = connection.getHeaderFields();
            	for(String key : headers.keySet()){
            		System.out.println(key + ": " + listToString(headers.get(key)));
            	}
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("===== HTTP GET End =====");
    }

    private static void executePost() {
        System.out.println("===== HTTP POST Start =====");
        try {
            URL url = new URL("http://localhost:8080/post");

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),
                                                                                  StandardCharsets.UTF_8));
                writer.write("POST Body");
                writer.write("\r\n");
                writer.write("Hello Http Server!!");
                writer.write("\r\n");
                writer.flush();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(),
                                                                       StandardCharsets.UTF_8);
                         BufferedReader reader = new BufferedReader(isr)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("===== HTTP POST End =====");
    }


}
