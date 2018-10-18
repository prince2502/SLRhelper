package main;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import grammar.Grammar;
import parser.Action;
import parser.SLRParser;

public class Main {

	public static void main(String[] args) {
		
		// here we go .. 

		if(args[0] == null) {
			System.err.println("Grammar file missing.");
			System.exit(0);
		}
		
		Grammar grammar = null;
		
		try {
			grammar = GrammarIO.loadFromFile(args[0]);
		} catch (IOException e) {
			System.err.println("Unable to read grammar file.");
			System.exit(0);
		}
		
		if(grammar != null){
			
			try {
				GrammarIO.printTerminals(grammar, "terminals.txt");
				GrammarIO.printNonTerminals(grammar, "non terminals.txt");
				GrammarIO.printFIRSTSets(grammar, "first set.txt");
				GrammarIO.printFOLLOWSets(grammar, "follow set.txt");
				GrammarIO.printStates(grammar, "states.txt");
				GrammarIO.printStatesAsDOT(grammar, "fsm.dot");
				GrammarIO.printSLRTable(grammar, "SLR Table.txt");
			} catch (IOException e) {
				System.err.println("Error writing output to file.");
				System.exit(0);
			}
			
			if(args[1] == null) {
				System.err.println("No input file to parse.");
				System.exit(0);
			}
			
			ArrayList<String> tokens = null;
			
			//System.exit(0);
			
			try {
				tokens = GrammarIO.loadInputFromFile(args[1]);
			} catch (IOException e) {
				System.err.println("Unable to read input file.");
				System.exit(0);
			}
			
			if(tokens != null) {
				
				try {
					ArrayList<Action> actions = SLRParser.parse(grammar, tokens);
					GrammarIO.printParseTree(actions, "parse tree.txt");
					GrammarIO.printParseActions(actions, "parse actions.txt");
				} catch (ParseException e) {
					try {
						GrammarIO.printParseActions(SLRParser.dead, "dead parse actions.txt");
					} catch (IOException e1) {}
					
					System.err.println(e.getMessage() + " at" + e.getErrorOffset());
				} catch (IOException e) {
					
				}
				
			}
		}
		
	}

}
