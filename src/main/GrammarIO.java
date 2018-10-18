package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

import grammar.ExtProduction;
import grammar.Grammar;
import grammar.NonTerminal;
import grammar.Production;
import grammar.State;
import grammar.Terminal;
import parser.ACTION_TYPE;
import parser.Action;

/**
 File input/output
 Print functions
*/

public class GrammarIO {
	public static Grammar loadFromFile(String filePath) throws IOException {

		final ArrayList<String> lines = getLines(filePath);

		if(lines.isEmpty()) return null;

		// Define Non Terminals
		ArrayList<String> ntSymbols = new ArrayList<String>();
		ArrayList<NonTerminal> nonTerminalList = new ArrayList<NonTerminal>();

		for(String line:lines){
			String temp = line.split("::")[0];
			temp = removeWS(temp);
			if(! ntSymbols.contains(temp)) {
				ntSymbols.add(temp);
				NonTerminal nonTerminal = new NonTerminal(temp);
				nonTerminal.FIRST_SET = null;
				nonTerminal.FOLLOW_SET = null;
				nonTerminalList.add(nonTerminal);
			}
		}

		ArrayList<Terminal> terminalList = new ArrayList<Terminal>();
		ArrayList<Production> productionList = new ArrayList<Production>();

		ArrayList<String> nSymbols = new ArrayList<String>();

		for(String line:lines){

			String[] headNbody = line.split("::");

			String[] body = headNbody[1].trim().split(" +");

			if(body[0].compareTo("empty") == 0) {	// body is set to empty
				Production production = new Production(removeWS(headNbody[0]) , new String[0]);
				productionList.add(production);
				continue;
			}

			Production production = new Production(removeWS(headNbody[0]) , body);
			productionList.add(production);

			for(int i = 0 ; i < body.length ; i++){

				String temp = body[i];

				if (! ntSymbols.contains(temp) && ! nSymbols.contains(temp) ) {
					nSymbols.add(temp);
					Terminal terminal = new Terminal(temp);
					terminalList.add(terminal);
				}
			}
		}

		NonTerminal[] nonTerminalArray = new NonTerminal[nonTerminalList.size()];
		nonTerminalArray = nonTerminalList.toArray(nonTerminalArray);

		Terminal[] terminalArray = new Terminal[terminalList.size()];
		terminalArray = terminalList.toArray(terminalArray);

		Production[] productionArray = new Production[productionList.size()];
		productionArray = productionList.toArray(productionArray);

		String startSymbol = ntSymbols.get(0);

		return new Grammar(nonTerminalArray , terminalArray , productionArray , startSymbol);
	}

	private static String removeWS(String st){
		return st.replaceAll("\\s","");
	}

	private static ArrayList<String> getLines(String filePath) throws IOException{
		FileReader fr = null;
		BufferedReader br = null;

		ArrayList<String> lines = new ArrayList<String>();

		fr = new FileReader(filePath);
		br = new BufferedReader(fr);
		String line;
		while( (line = br.readLine()) != null ){
			lines.add(line);
		}

		try { br.close(); } catch (IOException ignore) {}
		try { fr.close(); } catch (IOException ignore) {}

		return lines;
	}

	public static ArrayList<String> loadInputFromFile(String filename) throws IOException{
		ArrayList<String> lines = getLines(filename);
		ArrayList<String> tokens = new ArrayList<String>();
		for(String line:lines){
			String[] lineTokens = line.split(" +");
			for(int i = 0 ; i < lineTokens.length ; i++) tokens.add(lineTokens[i]);
		}
		return tokens;
	}

	public static void printParseTree(final ArrayList<Action> actionList , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printParseTree(actionList , out);
		out.close();
	}

	public static void printParseTree(final ArrayList<Action> actionList , PrintWriter out){
		ListIterator<Action> iterator = actionList.listIterator(actionList.size());
		while(iterator.hasPrevious()){
			Action action = iterator.previous();
			if(action.type == ACTION_TYPE.REDUCE){
				out.println(action.reduceProduction.head +":: " +Production.bodyToString(action.reduceProduction.body));
			}	
		}
	}

	public static void printParseActions(final ArrayList<Action> actionList , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printParseActions(actionList , out);
		out.close();
	}

	public static void printParseActions(final ArrayList<Action> actionList , PrintWriter out){

		for (Action action: actionList){

			switch(action.type){
			case ACCEPT:
				out.println("ACCEPT");
				break;
			case GOTO:
				out.print("GOTO " + action.state_no);
				break;
			case REDUCE:
				out.println("REDUCE " + action.reduceProduction.head +":: " +Production.bodyToString(action.reduceProduction.body));
				break;
			case SHIFT:
				out.println("SHIFT " + action.state_no);
				break;
			}
		}


	}

	public static void printStates(Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printStates(grammar , out);
		out.close();
	}

	public static void printStates(Grammar grammar , PrintWriter out){
		ArrayList<State> states = grammar.getExtStates();

		for(State state:states){
			out.println("------------------- State: " + state.state_no + " --------------------");
			ExtProduction[] productions = state.kernalItems;
			for(int i = 0 ; i < productions.length ; i++) {
				String[] body = productions[i].body;

				String temp = "";

				for(int j = 0 ; j < body.length ; j++){
					if(productions[i].hashPointer == j) temp += "#";
					temp += " " + body[j];
				}

				if(productions[i].hashPointer == body.length) temp += "#";

				out.println(productions[i].head + ":: " + temp);
			}	 
		}
		out.println("");
	}
	
	public static void printStatesAsDOT(Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printStatesAsDOT(grammar , out);
		out.close();
	}

	public static void printStatesAsDOT(Grammar grammar , PrintWriter out){
		out.println("digraph tl12Ast {");
		out.println("ordering=out;");
		out.println("node [shape = box, style = filled];");

		ArrayList<State> states = grammar.getExtStates();

		// print nodes
		for(State state:states){
			out.print("state" + state.state_no);
			out.println("[label=");
			out.print("\"");
			out.println("State " + state.state_no);
			
			ExtProduction[] productions = state.kernalItems;
			for(int i = 0 ; i < productions.length ; i++) {
				String[] body = productions[i].body;

				String temp = "";

				for(int j = 0 ; j < body.length ; j++){
					if(productions[i].hashPointer == j) temp += "#";
					temp += " " + body[j];
				}

				if(productions[i].hashPointer == body.length) temp += "#";

				out.println(productions[i].head + ":: " + temp);
			}

			out.println("\"");
			out.println(",shape=box]");

		}

		// print edges
		for(State state:states){
			
			for(Map.Entry<String,Integer> pair : state.GOTO_MAP.entrySet()){
				String symbol = pair.getKey();
				Integer state_no = pair.getValue();
				
				out.println("state" + state.state_no + " -> " + "state" 
								+ state_no + "[label=\"" + symbol + "\"]");
			}
			
		}

		out.println("}");
	}

	public static void printNonTerminals (Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printNonTerminals(grammar , out);
		out.close();
	}

	public static void printNonTerminals (Grammar grammar , PrintWriter out){
		NonTerminal[] nTerminals = grammar.getNonTerminals();
		for(int i = 0 ; i < nTerminals.length ; i++){
			out.println(nTerminals[i].name);
		}
	}

	public static void printFIRSTSets(Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printFIRSTSets(grammar , out);
		out.close();
	}

	public static void printFIRSTSets(Grammar grammar , PrintWriter out){
		NonTerminal[] nTerminals = grammar.getNonTerminals();
		for(int i = 0 ; i < nTerminals.length ; i++){
			out.print("First (" +nTerminals[i].name + "): ");
			Terminal[] set = nTerminals[i].FIRST_SET;
			for(int j = 0 ; j < set.length ; j ++) out.print(set[j].name+" ");
			// we can test nullability and add empty to First Set as well
			out.println("");
		}
	}

	public static void printFOLLOWSets(Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printFOLLOWSets(grammar , out);
		out.close();
	}

	public static void printFOLLOWSets(Grammar grammar , PrintWriter out){
		NonTerminal[] nTerminals = grammar.getNonTerminals();
		for(int i = 0 ; i < nTerminals.length ; i++){
			out.print("Follow (" +nTerminals[i].name + "): ");
			Terminal[] set = nTerminals[i].FOLLOW_SET;
			for(int j = 0 ; j < set.length ; j ++) out.print(set[j].name+" ");
			out.println("");
		}
	}

	public static void printTerminals (Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printTerminals(grammar , out);
		out.close();
	}

	public static void printTerminals (Grammar grammar , PrintWriter out){
		Terminal[] terminals = grammar.getTerminals();
		for(int i = 0 ; i < terminals.length ; i++){
			out.println(terminals[i].name);
		}
	}

	public static void printSLRTable(Grammar grammar , String filename) throws IOException{
		File file = new File(filename);
		file.createNewFile();
		PrintWriter out = new PrintWriter (file);
		printSLRTable(grammar , out);
		out.close();
	}

	public static void printSLRTable(Grammar grammar , PrintWriter out){
		ArrayList<State> states = grammar.getExtStates();

		for(State state:states){
			out.println("------------------- State: " + state.state_no + " --------------------");

			for(Map.Entry<String, Action> pair : state.SLR_TABLE.entrySet()){
				Action action = pair.getValue();
				String actionString = "";
				switch(action.type){
				case SHIFT:
					actionString = "shift " + action.state_no;
					break;
				case REDUCE:
					actionString = "reduce (" +action.reduceProduction.head+ "::" 
							+ Production.bodyToString(action.reduceProduction.body) + ")";
					break;
				case GOTO:
					actionString = "goto " + action.state_no;
					break;
				case ACCEPT:
					actionString = "accept";
				}
				out.println(pair.getKey() + " " + actionString);
			}
		}
		out.println("");
	}
}
