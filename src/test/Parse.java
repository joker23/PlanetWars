import java.io.*;
import java.util.*;

public class Parse{
	
	private static BufferedReader in;
	private static Stack<String> list;

	public static void main(String[] args) throws IOException{
		list = new Stack<String>();
		in = new BufferedReader(new FileReader(args[0]));
		String str;
		while((str = in.readLine()) != null){
			list.push(str);
		}

		System.out.println(list.pop());
		System.out.println(list.pop());
	}
}
