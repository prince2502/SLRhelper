package grammar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import parser.ACTION_TYPE;
import parser.Action;


/**
 * class Grammar
 * Represent one context free grammar
 * */
public class Grammar {
	/**
	 * property nonTerminals
	 * all of the non terminal symbols used in this grammar
	 * */
	private NonTerminal[] nonTerminals;
	
	/**
	 * property terminals
	 * List of terminal symbols
	 * */
	final private Terminal[] terminals;
	
	/**
	 * property NULL_TERMINAL
	 * special terminal representing epsilon
	 * */
	final private Terminal NULL_TERMINAL;
	
	/**
	 * String representation of start symbol for this grammar
	 * */
	private String startSymbol;
	
	/**
	 * List of productions of grammar. i.e. E-> T + F etc
	 * */
	private Production[] ruleList;
	
	/**
	 * An index for mapping name of Non Terminals to their position in this.nonTerminals array
	 * */
	private Map<String, Integer> nonTermIndex;
	
	/**
	 * An index for mapping name of Terminals to their position in this.terminals array
	 * */
	private Map<String, Integer> termIndex;
	
	/**
	 * All the states in LR(0) automaton made from this grammar
	 * */
	private ArrayList<State> states;
	
	private HashSet<String> callStack;
	
	public Grammar(NonTerminal[] nonTerminals , Terminal[] terminals , Production[] ruleList
			, String startSymbol){
		this.nonTerminals = nonTerminals;
		this.terminals = terminals;
		this.ruleList = ruleList;
		this.startSymbol = startSymbol;
		
		this.states = new ArrayList<State>();
		
		Terminal nterminal = new Terminal(null);
		this.NULL_TERMINAL = nterminal;
		
		createIndices(nonTerminals , terminals);
		
		// calculate and set all the properties of this grammar
		this.setNULLABLE();
		this.setFIRST();
		this.setFOLLOW();
		this.setGOTO();
		this.setSLR_TABLE();
	}
	
	public ArrayList<State> getStates(){
		return this.states;
	}
	
	/**
	 * Get all states with their kernal items set to kernal + non kernal items
	 * for printing purposes only
	 * */
	public ArrayList<State> getExtStates(){
		
		ArrayList<State> states = new ArrayList<State>();
		states = this.states;
		
		for(State state:states){
			state.kernalItems = CLOSURE(state.kernalItems);
		}
		return states;
	}
	
	public Terminal[] getTerminals(){
		return this.terminals;
	}
	
	public NonTerminal[] getNonTerminals(){
		return this.nonTerminals;
	}
	
	/**
	 * Calculates SLR Table for this grammar
	 * Follow Sets of all the Non Terminals should be set before calling this method
	 * */
	private void setSLR_TABLE(){
		for (State state : this.states){ // for each state
			
			state.SLR_TABLE = new HashMap<String , Action>();
			
			/**
			 * if this state(A) goes to state(B) on terminal a: Add SHIFT (a , B) to SLR Table(line) of state A
			 * if this state(A) goes to state(B) on non terminal a: Add GOTO (a , B) to SLR Table(line) of state A 
			 * */
			for(Map.Entry<String,Integer> pair : state.GOTO_MAP.entrySet()){
				String symbol = pair.getKey();
				Integer state_no = pair.getValue();

				NonTerminal matchedNTerminal = this.getNonTerminalByName(symbol);				
				if(matchedNTerminal != null) {
					Action action = new Action(ACTION_TYPE.GOTO);
					action.state_no = state_no;
					state.SLR_TABLE.put(symbol, action);
				} else {	// its a terminal
					Action action = new Action(ACTION_TYPE.SHIFT);
					action.state_no = state_no;
					state.SLR_TABLE.put(symbol, action);
				}
			}
			
			/**
			 * If # is at the end of any production(P) in this state, find head(H) of that production
			 * 		if H is start symbol, Add ACCEPT($) to SLR table
			 * 		else for each terminal (a) which is in follow of non terminal H, add REDUCE (P) to SLR Table
			 * */
			ExtProduction[] allItems = this.CLOSURE(state.kernalItems);
			for (int i = 0 ; i < allItems.length ; i++){
				ExtProduction production = allItems[i];
				
				// if # is at the end of body
				if(production.body.length == production.hashPointer){
					String headSymbol = production.head;
					
					if(headSymbol == this.startSymbol){
						Action action = new Action(ACTION_TYPE.ACCEPT);
						state.SLR_TABLE.put("$", action);
						continue;
					}
					
					NonTerminal matchedNTerminal = this.getNonTerminalByName(headSymbol);
					if(matchedNTerminal != null){
						for (int j = 0 ; j < matchedNTerminal.FOLLOW_SET.length ; j++){
							Terminal terminal = matchedNTerminal.FOLLOW_SET[j];
							Action action = new Action(ACTION_TYPE.REDUCE);
							action.reduceProduction = production;
							state.SLR_TABLE.put(terminal.name, action);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Calculate LR(0) automaton states for this grammar
	 * */
	private void setGOTO(){
		
		//Add new start symbol and prodcution to Grammar like E' -> E before calculating its LR(0) automaton
		 
		Production newProduction =  addNewDummyProduction();
		
		// Add state zero 
		State state = new State();
		ExtProduction firstStatePro = new ExtProduction(newProduction.head , newProduction.body , 0);
		ExtProduction[] firstItemKer = new ExtProduction[1];
		firstItemKer[0] = firstStatePro;
		state.kernalItems = firstItemKer;
		state.state_no = 0;
		
		this.states.add(state);
		
		// for each state find its behavior for each symbol, add a new state if it wants to GOTO a state that doesn't exist
		for(int i = 0 ; i< this.states.size() ; i++){
			setGOTO(this.states.get(i));
		}
	}
	
	/**
	 * Add new start symbol and prodcution to Grammar i.e. E' -> E
	 * */
	private Production addNewDummyProduction(){
		
		final String newStartSymbol = this.startSymbol + "'";
		NonTerminal oldStart = this.getNonTerminalByName(this.startSymbol);
		
		// create a new Terminal using newStartSymbol
		NonTerminal newStart = new NonTerminal(newStartSymbol);
		newStart.FIRST_SET = oldStart.FIRST_SET;
		newStart.isNullable = oldStart.isNullable;
		newStart.FOLLOW_SET = new Terminal[0];
		
		ArrayList<NonTerminal> nonTermList = new ArrayList<NonTerminal>(Arrays.asList(this.nonTerminals));
		nonTermList.add(newStart);
		NonTerminal[] newNonTerminals = new NonTerminal[nonTermList.size()];
		newNonTerminals = nonTermList.toArray(newNonTerminals);
		this.nonTerminals = newNonTerminals;
		
		// create a new Production for newStartSymbol
		String[] tempbody = new String[1];
		tempbody[0] = oldStart.name;
		
		Production newProduction = new Production(newStartSymbol , tempbody);
		
		ArrayList<Production> newRuleListArr = new ArrayList<Production>(Arrays.asList(this.ruleList));
		newRuleListArr.add(newProduction);
		Production[] newRuleList = new Production[newRuleListArr.size()];
		newRuleList = newRuleListArr.toArray(newRuleList);
		this.ruleList = newRuleList;
		
		this.startSymbol = newStartSymbol;
		return newProduction;
	}
	
	/**
	 * Set GOTO_MAP for a state.
	 * Create a new state if there exists no state with kernal items similar to the state this might go on a particular symbol
	 * */
	private void setGOTO (State state) {
		ExtProduction[] closure = CLOSURE(state.kernalItems);
		
		//for each terminal, find to which state this state will GOTO, add that to goto map 
		for (int i = 0 ; i < this.terminals.length ; i++){
			String terminalName = this.terminals[i].name;
			int nextState = this.getNextState(closure, terminalName);
			if (nextState > 0 ) state.GOTO_MAP.put(terminalName, nextState);
		}
		
		//for each non terminal, find to which state this state will GOTO, add that to goto map
		for (int i = 0 ; i < this.nonTerminals.length ; i++){
			String terminalNName = this.nonTerminals[i].name;
			int nextState = this.getNextState(closure, terminalNName);
			if (nextState > 0 ) state.GOTO_MAP.put(terminalNName, nextState);
		}
	}
	
	/**
	 * Given set of productions of a State, and a Symbol(terminal or non terminal) 
	 * 		find to which state that state will goto
	 * */
	private int getNextState(ExtProduction[] closure , String symbol){
		ArrayList<ExtProduction> kernalItems = new ArrayList<ExtProduction>();
		
		/**
		 * Look for all the productions where # is in front of 'symbol', and for each create a new item with hash 
		 * 	 after it, add it to kernalItems 
		 * */
		for(int j = 0 ; j < closure.length ; j++){
			// if # is at the end of Body of production. nothing needed be done
			if(closure[j].hashPointer > closure[j].body.length-1) continue;
			String nextSymbol = closure[j].body[ closure[j].hashPointer ];
			if(nextSymbol.compareTo(symbol) == 0) {
				ExtProduction temp = new ExtProduction(closure[j].head , closure[j].body , closure[j].hashPointer+1);
				kernalItems.add(temp);
			}
		}
		
		/**if kernalItems is empty, this state will not GOTO anywhere for this symbol */
		if(kernalItems.size() == 0) return -1;
		
		/**
		 * otherwise create a state with those kernal items , if that kind of state already exists return
		 *  	number of that state, if not create a new one and return number of that state
		 * */
		ExtProduction[] kernalItemsArr = new ExtProduction[kernalItems.size()];
		kernalItemsArr = kernalItems.toArray(kernalItemsArr);
		// create a new item
		State tentativeNewState = new State();
		tentativeNewState.kernalItems = kernalItemsArr;
		
		// see it this state already exists ....
		boolean stateAlreadyExists = false;
		int existingStateNo = 0;
		for (State listState:this.states){
			if(listState.compareTo(tentativeNewState)) {
				stateAlreadyExists = true;
				existingStateNo = listState.state_no;
				break;
			}
		}
		
		if (! stateAlreadyExists) { // create a new item
			tentativeNewState.state_no = this.states.size();
			this.states.add(tentativeNewState);
			existingStateNo = tentativeNewState.state_no;
		}
		
		return existingStateNo;
	}
	
	/**
	 * Find CLOSURE of a set of productions
	 * */
	private ExtProduction[] CLOSURE (ExtProduction[] productions){
		List<ExtProduction> list = new ArrayList<ExtProduction>();
		
		// copy productions array to array list, because we will extend it here
		for( int i = 0 ; i < productions.length ; i++){
			list.add(productions[i]);
		}
		
		/**
		 * In given SET of productions, find the ones in which hash is in front of a Non Terminal(NT)
		 * 		for each of them add all new productions to this SET whose head is NT (productions of that Non terminal), 
		 * 		keep doing this until the SET converges
		 * */
		for(int i = 0 ; i < list.size() ; i++){
			ExtProduction extProduction = list.get(i);
			// if # is at the end of Body of production. nothing needed be done
			if(extProduction.hashPointer > extProduction.body.length-1) continue;
						
			String currSymbol = extProduction.body[ extProduction.hashPointer ];
			
			NonTerminal matchedNTerminal = this.getNonTerminalByName(currSymbol);
			
			if(matchedNTerminal != null) {	// # is in front of this non Terminal
				
				for(int j = 0 ; j < this.ruleList.length ; j++){
					String head = this.ruleList[j].head;
					
					if(head.compareTo(matchedNTerminal.name) == 0){
						ExtProduction newExtProduction = new ExtProduction(this.ruleList[j].head , this.ruleList[j].body , 0);
						// add new branch to state if it is already not there
						if(! listSearch(list , newExtProduction)) list.add(newExtProduction);
					}
				}
			}
		}
		
		ExtProduction[] finalList = new ExtProduction[list.size()];
		finalList = list.toArray(finalList);
		return finalList;
	}
	
	/**
	 * Find if given production(element) exists in given Set(list)
	 * */
	private boolean listSearch(List<ExtProduction> list , ExtProduction element){
		for(int i = 0 ; i < list.size() ; i ++){
			if(list.get(i).compareTo(element)) return true;
		}
		return false;
	}
	
	/**
	 * Calculate FOLLOW SETs for all non Terminals
	 * */
	private void setFOLLOW(){
		
		/**
		 * Add end marker($) to FOLLOW SET of start symbol
		 * */
		NonTerminal start = this.getNonTerminalByName(this.startSymbol);
		Terminal terminal = new Terminal("$");
		HashSet<Terminal> temp = new HashSet<Terminal>();
		temp.add(terminal);
		this.mergeFOLLOWSet(start, temp);
		
		for(int i = 0 ; i < this.nonTerminals.length ; i++){
			this.callStack = new HashSet<String>();
			setFOLLOW(this.nonTerminals[i]);
		}
	}
	
	/**
	 * Calculate FOLLOW SET for a variable
	 * */
	private void setFOLLOW(NonTerminal var){
		HashSet<Terminal> followSet = new HashSet<Terminal>();
		
		for(int q = 0 ; q < this.ruleList.length ; q++){		// for each production of this grammar
			String[] body = this.ruleList[q].body;
			
			/**
			 * In body of this production, see if this variable(V) exists, if yes for the rest of the body:
			 * 		find first(F) of the first Symbol after it(terminal itself for terminal , FIRST_SET for a non terminal)
			 * 		add all to FOLLOW of this var
			 * 		Now, if V is nullable, do whole procedure for next Symbol in rest of the body
			 * 	If every symbol is Rest of the body is nullable, add FOLLOW of head of this production to FOLLOW of this var
			 * 		if follow of Head doesn't exist, find that first
			 * */
			BODYLOOP:
			for(int i = 0 ; i < body.length ; i++){
				if(body[i].compareTo(var.name) == 0){ // found it
					
					for (int j = i+1 ; j < body.length ; j++){		// for the rest of the body
						String currSymbol = body[j];
						Terminal matchedTerminal = this.getTerminalByName(currSymbol);
						if(matchedTerminal != null){ // its a terminal
							followSet.add(matchedTerminal);
							break BODYLOOP;
						}
						
						NonTerminal matchedNTerminal = this.getNonTerminalByName(currSymbol);
						if(matchedNTerminal != null) {		// its a non terminal
							// Add first set of Following symbol to its follow
							for(int k = 0 ; k < matchedNTerminal.FIRST_SET.length ; k++){
								followSet.add(matchedNTerminal.FIRST_SET[k]);
							}
							
							if(! matchedNTerminal.isNullable) break BODYLOOP;
						}
					}
					
					// if you are here, means whole string after THIS VAR(in rest of body) is nullable
					String head = this.ruleList[q].head;
					NonTerminal headNTerminal = this.getNonTerminalByName(head);
					
					if(headNTerminal.FOLLOW_SET == null && ! this.callStack.contains(var.name)) {	// follow of head doesn't exist
						this.callStack.add(var.name);
						setFOLLOW(headNTerminal);
						this.callStack.remove(var.name);
					}
					
					// copy follow of head to itself
					if(headNTerminal.FOLLOW_SET != null)		// if itsn't still null
					for(int k = 0 ; k < headNTerminal.FOLLOW_SET.length ; k++){
						followSet.add(headNTerminal.FOLLOW_SET[k]);
					}
					
					break;
				}
			}
		}
		
		this.mergeFOLLOWSet(var, followSet);
	}
	
	/**
	 * Add new elements to FOLLOW SET of var
	 * */
	private void mergeFOLLOWSet(NonTerminal var , HashSet<Terminal> partialFollowSet){
		if (var.FOLLOW_SET != null && var.FOLLOW_SET.length > 0){
			for (int i = 0 ; i < var.FOLLOW_SET.length ; i++){
				partialFollowSet.add(var.FOLLOW_SET[i]);
			}
		}
		Terminal[] finalArray = new Terminal[partialFollowSet.size()];
		finalArray = partialFollowSet.toArray(finalArray);
		var.FOLLOW_SET = finalArray;
	}
	
	/**
	 * For all non terminals, compute if they are nullable
	 * */
	private void setNULLABLE(){
		for (int i = 0 ; i < this.nonTerminals.length ; i++){
			this.callStack = new HashSet<String>();
			setNULLABLE(this.nonTerminals[i]);
		}
	}
	
	/**
	 * For a Non Terminal, compute if it is nullable
	 * */
	private void setNULLABLE(NonTerminal var){
		
		if(var.nullableFlag) return;
		/**
		 * For each production headed by this var, if all the variables inside body of that production are nullable
		 * 	this var is nullable
		 * */
		for(int q = 0 ; q < this.ruleList.length ; q++){
			String head = this.ruleList[q].head;
			if(head.compareTo(var.name) == 0){		// production who's head is this variable
				String[] body = this.ruleList[q].body;
				// the master step ....
				if(this.callStack.contains(Production.bodyToString(body))) continue;
				int i = 0;
				/**This loops breaks every time it finds a Symbol inside the production which is not nullable*/
				for( ; i < body.length ; i++){
					String currSymbol = body[i];
					
					Terminal matchedTerminal = this.getTerminalByName(currSymbol);
					if(matchedTerminal != null) break;
					
					NonTerminal matchedNTerminal = this.getNonTerminalByName(currSymbol);
					if(matchedNTerminal != null ){
						if(matchedNTerminal.nullableFlag){
							if(matchedNTerminal.isNullable) continue;
							else break;
						}
						else {
							this.callStack.add(Production.bodyToString(body));
							
							setNULLABLE(matchedNTerminal);
							if(matchedNTerminal.nullableFlag){
								if(matchedNTerminal.isNullable) continue;
								else break;
							} // else there is some problem with grammar
							
							this.callStack.remove(Production.bodyToString(body));
						}
					}
				}
				/** if i = body lenght, means upper loop was not broken forcibly, means there is no symbol inside the body
				 * 		which is not nullable , set var nullable and return 
				 * */
				if(i == body.length) {
					var.isNullable = true;
					var.nullableFlag = true;
					return;
				}
			}
		}
		
		// var is not nullable
		var.isNullable = false;
		var.nullableFlag = true;
		return;
	}
	
	/**
	 * Calculate FIRST Sets of all non terminals
	 * */
	private void setFIRST(){
		for (int i = 0 ; i < this.nonTerminals.length ; i++){
			this.callStack = new HashSet<String>();
			if(this.nonTerminals[i].FIRST_SET == null)
				setFIRST(this.nonTerminals[i]);
		}
	}
	
	/**
	 * Calculate FIRST SET of this variable
	 * */
	private void setFIRST(NonTerminal var){
		
		HashSet<Terminal> firstSet = new HashSet<Terminal>();
		
		/**
		 * in all productions of this grammar, find the ones heaaded by this var
		 * 	for their bodies, add the FIRST SET of first symbols to FIRST SET of this var
		 * 	if that first symbols is nullable add the FIRST SET of Second symbols to FIRST SET of this var and so on
		 * */
		for(int q = 0 ; q < this.ruleList.length ; q++){		// for each production of grammar
			String head = this.ruleList[q].head;
			if(head.compareTo(var.name) == 0){		// a production headed by this var
				String[] body = this.ruleList[q].body;
				// the master step ....
				if(this.callStack.contains(Production.bodyToString(body))) continue;
				// one production
				for(int i = 0 ; i < body.length ; i++){
					String currSymbol = body[i];
					
					Terminal matchedTerminal = this.getTerminalByName(currSymbol);
					if(matchedTerminal != null) { // current symbol is a terminal
						firstSet.add(matchedTerminal);
						break;
					}
					
					NonTerminal matchedNTerminal = this.getNonTerminalByName(currSymbol);
					if(matchedNTerminal != null ){
						// if first set of this Non terminal is not discovered yet
						if(matchedNTerminal.FIRST_SET == null) {
							this.callStack.add(Production.bodyToString(body));
							setFIRST(matchedNTerminal);
							this.callStack.remove(Production.bodyToString(body));
						} 
						// add all elemets to FIRST set of this Non Terminal
						for(int j = 0 ; j < matchedNTerminal.FIRST_SET.length ; j++){
							firstSet.add( matchedNTerminal.FIRST_SET[j] );
						}
						
						if(matchedNTerminal.isNullable) continue;
						else {
							break;
						}
					}
				}
				if(var.isNullable){
					firstSet.add(NULL_TERMINAL);
				}
			}
		}
		
		this.mergeFIRSTSet(var, firstSet);
	}
	
	/**
	 * Add new terminals to FIRST set of this var
	 * */
	private void mergeFIRSTSet(NonTerminal var , HashSet<Terminal> partialFirstSet){
		
		if (var.FIRST_SET != null && var.FIRST_SET.length > 0){
			for (int i = 0 ; i < var.FIRST_SET.length ; i++){
				partialFirstSet.add(var.FIRST_SET[i]);
			}
		}
		Terminal[] finalArray = new Terminal[partialFirstSet.size()];
		finalArray = partialFirstSet.toArray(finalArray);
		var.FIRST_SET = finalArray;
	}
	
	/**
	 * Create Indices for global 'terminals' and 'nonTerminals' to the search Faster
	 * */
	private void createIndices(NonTerminal[] nonTerminals , Terminal[] terminals){
		this.nonTermIndex = new HashMap<String,Integer>();
		for (int i = 0 ; i < nonTerminals.length ; i++){
			this.nonTermIndex.put(nonTerminals[i].name, i);
		}
		
		this.termIndex = new HashMap<String, Integer>();
		for (int i = 0 ; i < terminals.length ; i++){
			this.termIndex.put(terminals[i].name, i);
		}
	}
	
	/**
	 * Search a Terminal in global 'terminals' list
	 * */
	private Terminal getTerminalByName(String name){
		Integer index = termIndex.get(name);
		if(index == null) return null;
		else return this.terminals[index];
	}
	
	/**
	 * Search a non terminal in global 'nonTerminals' list
	 * */
	private NonTerminal getNonTerminalByName(String name){
		Integer index = this.nonTermIndex.get(name);
		if(index == null) return null;
		else return this.nonTerminals[index];
	}
	
}
