package parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import grammar.Grammar;
import grammar.Production;
import grammar.State;

/**
 * class SLRParser
 * Represents SLR parser as a machine
 * */
public class SLRParser {
	
		
	private static Map<Integer , Integer> state_indices = new HashMap<Integer , Integer>();
	
	private static ArrayList<State> stateList;
	
	public static ArrayList<Action> dead;
	
	public static ArrayList<Action> parse(Grammar grammar , ArrayList<String> string) throws ParseException{
		
		stateList = grammar.getStates();
		setIndices();
		int input_index = 0;
		ArrayList<Action> actionList = new ArrayList<Action>();
		Stack<Integer> state_stack = new Stack<Integer>();
		state_stack.push(0);		//put state 0 to the top of stack
		try {
			PARSER_MACHINE:
				for(;;){ // forever
					// get action for current state on top of stack and next symbol
					Action action = getAction(state_stack.peek() , string.get(input_index) );
					trace(action , state_stack , string , input_index);
					switch(action.type){
					case SHIFT:	// if action is shift, push the state for that action to stack
						state_stack.push(action.state_no);
						input_index++;
						break;
					case REDUCE:
						/** if reduce, pop as many states from stack as many symbols in body inside 
						 * 	production associated with that reduce , and then find GOTO for state NOW 
						 * 	on top of stack and head of that production. Push the result to stack
						 * */
						int noOfSymbolsInBody = action.reduceProduction.body.length;
						for(int i = 1 ; i <= noOfSymbolsInBody ; i++){
							state_stack.pop();
						}
						
						Action inAction = getAction(state_stack.peek() , action.reduceProduction.head);
						if(inAction.type == ACTION_TYPE.GOTO){
							state_stack.push(inAction.state_no);
						} else break PARSER_MACHINE; // shuold never be the case
						break;
					case ACCEPT:
						actionList.add(action);
						break PARSER_MACHINE;
					case GOTO:
						// never the case
					}
					actionList.add(action);
				}
		} catch (Exception e){
			dead = actionList;
			throw new ParseException("Error in parsing string" , input_index + 1);
		}
		return actionList;
		
	}
	
	private static void trace(Action action, Stack<Integer> state_stack , ArrayList<String> input , int index){
		
		String act = "";
		switch(action.type){
		case ACCEPT:
			act = "ACCEPT";
			break;
		case GOTO:
			act = "GOTO " + action.state_no;
			break;
		case REDUCE:
			act = "REDUCE " +  action.reduceProduction.head +":: " +Production.bodyToString(action.reduceProduction.body);
			break;
		case SHIFT:
			act = "SHIFT";
			break;
		}
		
		/**
		 * remaing input
		 * */
		StringBuilder remInput = new StringBuilder();
		
		for(int i = index ; i < input.size() ; i++){
			remInput = remInput.append(input.get(i)).append(' ');
		}
		
		/**
		 * stack trace
		 * */
		StringBuilder stackTrace = new StringBuilder();
		
		for (Integer stateNo : state_stack){
			stackTrace.append(stateNo).append(' ');
		}
		
		System.out.println(  stackTrace.toString() + "    " + remInput.toString() +  "    " + act);
	}
	
	/**
	 * Given a state number and a symbol, get Action 
	 * */
	private static Action getAction(int state_no , String symbol){
		State state = getState(state_no);
		return state.SLR_TABLE.get(symbol);
	}
	
	/**
	 * Set Indices to make search in global list faster
	 * */
	private static void setIndices(){
		int index = 0;
		for(State state:stateList){
			state_indices.put(state.state_no, index++);
		}
	}
	
	/**
	 * Search for State in global 'stateList'
	 * */
	private static State getState(int state_no){
		return stateList.get(state_indices.get(state_no));
	}
	
}
