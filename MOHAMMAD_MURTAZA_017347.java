
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class MOHAMMAD_MURTAZA_017347 {

	public static String start;
	public static ArrayList<String> Terminal = new ArrayList<String>();
	public static ArrayList<NonTerminal> NonTerminalList = new ArrayList<NonTerminal>();
	
	public static void ConvertCFGtoCNF(String filename) {
		LoadCFG(filename); // "CFG1.txt"
		step1();
		step2();
		step3();
		step4();
		PrintCNFCFG(NonTerminalList);
		
	}
	
	public static void LoadCFG(String filename) {
		
		Scanner file = null;
		String track;
		String flag="";
		try {
			file = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file");
		}
		
		do {
			track = file.nextLine();
			if (track.toUpperCase().equals("TERMINAL") ){
				flag = "Terminal";
				track = file.nextLine();
			}else if (track.toUpperCase().equals("NON-TERMINAL")) {
				flag = "NonTerminal";
				track = file.nextLine();
			}else if (track.toUpperCase().equals("RULES")) {
				flag = "rules";
				track = file.nextLine();
			}else if (track.toUpperCase().equals("START")) {
				flag = "start";
				track = file.nextLine();
			}else if (track.toUpperCase().equals("TRANSITIONS")){
				flag = "Tran";
				track = file.nextLine();
			}else if (track.toUpperCase().equals("END")){
				break;
			}
			
			if (flag == "Terminal") {
				Terminal.add(track);
			}else if (flag == "NonTerminal") {
				NonTerminalList.add(new NonTerminal(track));
			}
			else if (flag == "start") {
				start = track;
			}
			else if (flag == "rules") {
				String[] arr = track.split(":");
				for (NonTerminal j :NonTerminalList) {
					if(j.getName().equals(arr[0])) {
						j.getProduction().add(new Production(arr[0],arr[1]));
					}
				}					
			}
			
		}while(file.hasNextLine());
		
		file.close();
	}
	
	public static char chooseNonTerminalName() {
		String Nont = "";
		for (NonTerminal j : NonTerminalList){
			Nont = Nont + j.getName();
		}
		for (char name : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
			if (! Nont.contains(name+"")){
				return name;
			}
		}
		return '1';
	}
	
	public static void step1() {
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().contains(start)) {
					NonTerminal S = new NonTerminal(""+chooseNonTerminalName());
					Production p = new Production(S.getName(), start);
					S.getProduction().add(p);
					NonTerminalList.add(S);
					start = S.getName();
					return;
				}		
			}			
		}
	}
	
	public static String EpsilonShift(String s) {
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().equals(s)) {
					return t.getFrom();
				}
			}
		}
		return "";
	}
	public static void step2() {
		/*
		 * removing epsilon
		 * unit production
		 * useless production
		 */
		ArrayList<Production> epsilonList = new ArrayList<Production>();
		
		ArrayList<Production> epsilonShift = new ArrayList<Production>();
		
		String eShift = "" ;
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().contains("e")) {
					epsilonList.add(t);
					eShift = EpsilonShift(t.getFrom()); 
					if (! eShift.equals("")) {
						epsilonShift.add(new Production(eShift,"e"));
					}
						
					
				}		
			}
		}
		for (Production t: epsilonShift ) {
			for (NonTerminal j : NonTerminalList){
				if(j.getName().equals(t.getFrom())) {
					j.getProduction().add(t);
					epsilonList.add(t);
				}
			}
		}
		
		for (Production t: epsilonList) {
			step2removeEpsilon(t);
		}
			
		
		for (NonTerminal j : NonTerminalList){
			j.getProduction().removeIf(n->(n.getFrom().equals(n.getTo())));
		}
		
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().length()==1  && "ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(t.getTo())) {
					step2removeUnitProduction(t);
				}
			}			
		}
		
		// Remove useless/self production
		for (NonTerminal j : NonTerminalList){
			j.getProduction().removeIf(n->(n.getFrom().equals(n.getTo())));
		}
		

	}
	public static boolean isNonTerminal(String Name) {
		String nt = "";
		for (NonTerminal j : NonTerminalList){
			nt = nt + j.getName();
		}
		if (nt.contains(Name)){
			return true;
		}else {
			return false;
		}
		
	}
		
	public static void step3() {
		/*
		 * Eliminate terminals from the RHS of the production if they exist with other 
		 * non-terminals or terminals. For example, production S → aA can be decomposed as:
		 */
		ArrayList<String> toReplace = new ArrayList<String>();
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				for (String terminal : Terminal) {
					if (t.getTo().contains(terminal) && t.getTo().length()>1) {
						toReplace.add(terminal);
					}
				}
			}
		}
		String Replaced = "";
		for (String j : toReplace) {
			if (!Replaced.contains(j)) {
				step3replaceTerminals(j);
				Replaced= Replaced+j;
			}
		}
	}
	public static void step3replaceTerminals(String p) {
		NonTerminal n =  new NonTerminal(chooseNonTerminalName()+"");
		n.getProduction().add(new Production(n.getName(), p));
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().contains(p) && t.getTo().length()>1) {
						t.setTo(t.getTo().replace(p, n.getName()));
				}
			}
		}
		NonTerminalList.add(n);
		
	}
	
	public static void step4() {
		// Eliminate RHS with more than two non-terminals. For example, S → ASB can be decomposed as:
		ArrayList<String> templist = new ArrayList<String>();
		
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (t.getTo().length()>2){
//					System.out.println(t.getFrom()+" -> "+t.getTo());
					for (int i=0; i< t.getTo().length(); i+=2 ) {
						if (i+2 < t.getTo().length()) {
							String ss = t.getTo().substring(i, i+2);
//							System.out.println((!templist.contains(ss))+"--"+(i+2 < t.getTo().length()));
							if ((!templist.contains(ss))) {
								templist.add(ss);
							}
						}
					}
				}
			}
		}
		
		for(String p: templist) {
			NonTerminal n =  new NonTerminal(chooseNonTerminalName()+"");
			n.getProduction().add(new Production(n.getName(), p));
			for (NonTerminal j : NonTerminalList){
				for (Production t: j.getProduction()) {
					if (t.getTo().contains(p) && t.getTo().length()>2) {
							t.setTo(t.getTo().replace(p, n.getName()));
					}
				}
			}
			NonTerminalList.add(n);
		}
			
	}
	
	public static void step2removeEpsilon(Production rule) {
		ArrayList<Production> temp =  new ArrayList<Production>();
		ArrayList<String> tempSDuplicate =  new ArrayList<String>();
		
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				if (!t.getTo().equals("e")) {
					tempSDuplicate.add(t.getFrom()+":-:"+t.getTo());
					if (t.getTo().contains(rule.getFrom()) && t.getTo().length()>1) {
						Production tempP = new Production(t.getFrom(), t.getTo().replace(rule.getFrom(),"")); 
						temp.add(tempP);
					}
				}
			}
		}
		for (NonTerminal j : NonTerminalList){
			for (Production t: temp) {
				if (j.getName().equals(t.getFrom()) && (!tempSDuplicate.contains(t.getFrom()+":-:"+t.getTo()))) {
					j.getProduction().add(t);
				}
			}			
		}
		for (NonTerminal j : NonTerminalList){
			j.getProduction().removeIf( n -> (n.getTo().equals("e") ) );
			
		}
	}

	public static void step2removeUnitProduction(Production rule) {
		ArrayList<Production> temp =  new ArrayList<Production>();
		ArrayList<String> tempDuplicate =  new ArrayList<String>();
		
		for (NonTerminal j : NonTerminalList){
			if (j.getName().equals(rule.getTo())) {
				for (Production p: j.getProduction()) {
//					System.out.println(rule.getFrom()+"-268-"+p.getTo());
					tempDuplicate.add(p.getTo());
					temp.add(new Production(rule.getFrom(),p.getTo()));
				}
				break;
			}
		}
		for (NonTerminal j : NonTerminalList){
			if (j.getName().equals(rule.getFrom())){
				for (Production p : j.getProduction()) {
					if (!tempDuplicate.contains(p.getTo()) && (!p.getTo().equals(rule.getTo()))){
						temp.add(p);
					}
				}
				j.setProduction(temp);
				break;
			}
		}
	}
	public static void PrintCNFCFG(ArrayList<NonTerminal> NonTerminalList) {
		System.out.println("NON-TERMINAL");
		for (NonTerminal j : NonTerminalList){
			System.out.println(j.getName());
		}
		System.out.println("TERMINAL");
		for(String a : Terminal) {
			System.out.println(a);
		}
		System.out.println("RULES");
		for (NonTerminal j : NonTerminalList){
			for (Production t: j.getProduction()) {
				System.out.println(t.getFrom()+":"+t.getTo());
				
			}			
		}
		System.out.println("START");
		System.out.println(start);
		
	}
	
	public static void main(String[] args) throws IOException {
		ConvertCFGtoCNF("CFG1.txt");
		
	}
	
}


class Production{
	private String From;
	private String To;
	
	public Production(String from, String to) {
		this.From = from;
		this.To = to;
	}

	public String getFrom() {
		return From;
	}

	public void setFrom(String from) {
		From = from;
	}

	public String getTo() {
		return To;
	}

	public void setTo(String to) {
		To = to;
	}

}

class NonTerminal {
	private String name;
	private ArrayList<Production> production;
	
	public NonTerminal(String Name) {
		this.name = Name;
		this.production = new ArrayList<Production>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Production> getProduction() {
		return production;
	}
	public void setProduction(ArrayList<Production> production) {
		this.production = production;
	}
}


