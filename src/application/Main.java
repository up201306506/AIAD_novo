package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main  {

	public static void main(String[] args) {
		String str = "X15Y2C3";
		Pattern p = Pattern.compile("\\w\\d+");
		Matcher m = p.matcher(str);

		while(m.find()){
			System.out.println(m.group());
		}
	}
}