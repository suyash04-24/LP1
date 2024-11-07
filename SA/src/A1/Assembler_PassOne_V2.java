package A1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import A1.LitTuple;
import A1.SymTuple;
import A1.Tuple;

class Tuple {
	//m_class specifies class of the mnemonic such as IS, DL, or AD
	String mnemonic, m_class, opcode;
	int length;
	
	Tuple() {}
	
	Tuple(String s1, String s2, String s3, String s4) {
		mnemonic = s1;
		m_class = s2; 
		opcode = s3;
		length = Integer.parseInt(s4);
		
	}
}

class SymTuple {
	String symbol, address;
	int length;
	
	SymTuple(String s1, String s2, int i1) {
		symbol = s1;
		address = s2;
		length = i1;
	}
}

class LitTuple {
	String literal, address;
	int length;
	
	LitTuple() {}
	
	LitTuple(String s1,  String s2, int i1) {
		literal = s1;
		address = s2;
		length = i1;		
	}
}

public class Assembler_PassOne_V2 {

	static int lc,iSymTabPtr=0, iLitTabPtr=0, iPoolTabPtr=0;
	static int poolTable[] = new int[10];
	static Map<String,Tuple> MOT;
	static Map<String,SymTuple> symtable;
	static ArrayList<LitTuple> littable;
	static Map<String, String> regAddressTable;
	static PrintWriter out_pass2;
	static PrintWriter out_pass1;
	static int line_no;
	
	
	public static void main(String[] args) throws Exception{
		initializeTables();
		System.out.println("====== PASS 1 OUTPUT ======\n");
		pass1();

	}
	
	static void pass1() throws Exception {
		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("src/A1/input.txt")));
		out_pass1 = new PrintWriter(new FileWriter("src/A1/output_pass1.txt"), true);
		PrintWriter out_symtable = new PrintWriter(new FileWriter("src/A1/symtable.txt"), true);
		PrintWriter out_littable = new PrintWriter(new FileWriter("src/A1/littable.txt"), true);
		String s;
		//Read from input file one line at a time
		lc=0;
		while((s = input.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(s, " ", false);
			//For each line, separate out the tokens
			String s_arr[] = new String[st.countTokens()];
			for(int i=0 ; i < s_arr.length ; i++) {
				s_arr[i] = st.nextToken();
			}
			if(s_arr.length == 0){
				continue;
			}
			int curIndex = 0;
			//Contains a value in the label field
			if(s_arr.length == 3){
				String label = s_arr[0];
				insertIntoSymTab(label,lc+"");
				curIndex = 1;
			}
			
			String curToken = s_arr[curIndex];
			//Get current tuple from opcode Table
			Tuple curTuple = MOT.get(curToken);
			
			String intermediateStr="";
			//Analyze current token to check class of token (IS, DL, AD)
			if(curTuple.m_class.equalsIgnoreCase("IS")){
				intermediateStr += lc + " (" +  curTuple.m_class + "," + curTuple.opcode + ")  ";
				lc += curTuple.length;
				intermediateStr += processOperands(s_arr[curIndex+1]);
			}
			else if(curTuple.m_class.equalsIgnoreCase("AD")){
				if(curTuple.mnemonic.equalsIgnoreCase("START")){
					intermediateStr += lc + " (" +  curTuple.m_class + "," + curTuple.opcode + ")  ";
					lc = Integer.parseInt(s_arr[curIndex+1]);
					intermediateStr += "(C," + (s_arr[curIndex+1]) + ") ";
				}
				else if(curTuple.mnemonic.equalsIgnoreCase("LTORG")){
					intermediateStr +=processLTORG();
				}
				else if(curTuple.mnemonic.equalsIgnoreCase("END")){
					intermediateStr += lc + " (" +  curTuple.m_class + "," + curTuple.opcode + ")  \n";
					intermediateStr +=processLTORG();
					//break;
				}
			}
			else if(curTuple.m_class.equalsIgnoreCase("DL")){
				intermediateStr += lc + " (" +  curTuple.m_class + "," + curTuple.opcode + ")  ";
				if(curTuple.mnemonic.equalsIgnoreCase("DS")){
					lc += Integer.parseInt(s_arr[curIndex+1]);
				}
				else if(curTuple.mnemonic.equalsIgnoreCase("DC")){
					lc += curTuple.length;
				}
				intermediateStr += "(C," + s_arr[curIndex+1] + ") ";
			}
			//Print the instruction in the intermediate file
			System.out.println(intermediateStr);
			out_pass1.println(intermediateStr);
			//Add the length of the instruction in the location counter
		}
		//Close intermediate file
		out_pass1.flush();
		out_pass1.close();
		//Print symbol table
		System.out.println("====== Symbol Table ======");
		SymTuple tuple;
		Iterator<SymTuple> it = symtable.values().iterator();
		String tableEntry;
		while(it.hasNext()){
			tuple = it.next();
			tableEntry = tuple.symbol + "\t" + tuple.address ;
			out_symtable.println(tableEntry);
			System.out.println(tableEntry);
		}
		out_symtable.flush();
		out_symtable.close();
		
		//Print literal table
		System.out.println("====== Literal Table ======");
		LitTuple litTuple;
		//Iterator<LitTuple> iterator = littable.values().iterator();
		tableEntry = "";
		for(int i=0; i<littable.size(); i++){
			litTuple = littable.get(i);
			tableEntry = litTuple.literal + "\t" + litTuple.address ;
			out_littable.println(tableEntry);
			System.out.println(tableEntry);
		}
		out_littable.flush();
		out_littable.close();
	}
	
	static String processLTORG(){
		//Process literal table and assign addresses to every literal in the table
		LitTuple litTuple;
		String intermediateStr = "";
		for(int i=poolTable[iPoolTabPtr-1]; i<littable.size(); i++){
			litTuple = littable.get(i);
			litTuple.address = lc+"";
			intermediateStr += lc + " (DL,02)  (C," + litTuple.literal + ") \n";
			lc++;
		}
		//Make a new entry in pool table;
		poolTable[iPoolTabPtr] = iLitTabPtr;
		iPoolTabPtr++;
		return intermediateStr;
	}
	static String processOperands(String operands){
		StringTokenizer st = new StringTokenizer(operands, ",", false);
		//Separate out the tokens separated by comma
		String s_arr[] = new String[st.countTokens()];
		for(int i=0 ; i < s_arr.length ; i++) {
			s_arr[i] = st.nextToken();
		}
		String intermediateStr = "", curToken;
		for(int i=0; i <s_arr.length; i++){
			curToken = s_arr[i];
			if(curToken.startsWith("=")){
				//Operand is a literal	
				//Extract literal from the string
				StringTokenizer str = new StringTokenizer(curToken, "'", false);
				//Separate out the tokens separated by comma
				String tokens[] = new String[str.countTokens()];
				for(int j=0 ; j < tokens.length ; j++) {
					tokens[j] = str.nextToken();
				}
				String literal = tokens[1];
				insertIntoLitTab(literal,"");
				intermediateStr += "(L," + (iLitTabPtr -1) + ")";
			}
			else if(regAddressTable.containsKey(curToken)){
				//Operand is a register name
				intermediateStr += "(RG," + regAddressTable.get(curToken) + ") ";
			}
			else{
				//Operand is a symbol
				insertIntoSymTab(curToken,"");
				intermediateStr += "(S," + (iSymTabPtr -1) + ")";
			}
		}
		return intermediateStr;
		
	}
	static void insertIntoSymTab(String symbol, String address){
		//Check if the symbol is already present in the symbol table
		if(symtable.containsKey(symbol)== true){
			//Extract entry from symbol table
			SymTuple s = symtable.get(symbol);
			//Update its address field
			s.address = address;
		}
		else{
			//If symbol is not present in the symbol table, create a new entry
			symtable.put(symbol, new SymTuple(symbol, address, 1));
		}
		iSymTabPtr++;
	}
	
	static void insertIntoLitTab(String literal, String address){
		//If label is not present in the literal table, create a new entry
		littable.add(iLitTabPtr, new LitTuple(literal, address, 1));
		iLitTabPtr++;
	}
	
	static void initializeTables() throws Exception {
		symtable = new LinkedHashMap<>();
		littable = new ArrayList<>();
		regAddressTable = new HashMap<>();
		MOT = new HashMap<>();
		String s,mnemonic;
		BufferedReader br;
		br = new BufferedReader(new InputStreamReader(new FileInputStream("src/A1/mot.txt")));
		while((s = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(s, " ", false);
			mnemonic = st.nextToken();
			MOT.put(mnemonic, (new Tuple(mnemonic, st.nextToken(), st.nextToken(), st.nextToken())));
		}
		br.close();
		//Initiallize register address table
		regAddressTable.put("AREG", "1");
		regAddressTable.put("BREG", "2");
		regAddressTable.put("CREG", "3");
		regAddressTable.put("DREG", "4");
		
		//Initiallize pool table
		poolTable[iPoolTabPtr] = iLitTabPtr;
		iPoolTabPtr++;
	}
}


