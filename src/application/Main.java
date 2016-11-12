package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main  {

	public static void main(String[] args) {
		String str = "XI1YI2XF3YF4N5";
		Pattern p = Pattern.compile("[a-zA-Z]+\\d+");
		Matcher m = p.matcher(str);

		while(m.find()){
			System.out.println(m.group());
		}
	}
}