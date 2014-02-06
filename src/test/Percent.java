import java.io.*;
import java.util.*;

public class Percent{

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		String str1;
		String str2;

		double avgwin = 0;
		double avgloss = 0;
		int one = 0;
		int two = 0;
		LinkedList<String> list = new LinkedList<String>();
	
		int map = 1;
		while((str1 = in.readLine()) != null){
			str2 = in.readLine();

			String[] sp1 = str1.split(" ");
			String[] sp2 = str2.split(" ");
			
			int player = Integer.parseInt(sp1[1]);
			int turns = Integer.parseInt(sp2[1]);
			
			if(player == 1){
				one++;
				avgwin += turns;
			} else {
				two++;
				avgloss += turns;
				list.add("lost on map " + map);
			}
			map ++;
		}
		double percent = ((double) one)/(one + two);
		if(one > 0) avgwin /= (double) one;
		if(two > 0) avgloss /= (double) two;
		System.out.println(args[0] + "-------------------------");
		System.out.println("wins : " + one);
		System.out.println("loss : " + two);
		System.out.println("win percent : " +  percent);
		System.out.println("average moves to win : " + avgwin);
		System.out.println("average moves to loss : " + avgloss);

		while(!list.isEmpty()){
			System.out.println(list.poll());
		}
	}
}
