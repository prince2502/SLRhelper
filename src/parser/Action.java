package parser;

import grammar.Production;

/**
 * class Action
 * tells the parser what to do upon seeing a symbol on stack
 * */
public class Action {
	
	/**
	 * property type
	 * what kind of option is this
	 * If this is SHIFT or GOTO, state_no tell which state to
	 * If this is REDUCE, reduceProduction tells using which production
	 * */
	public ACTION_TYPE type = ACTION_TYPE.ACCEPT;
	
	public int state_no;		// for shift and goto
	
	public Production reduceProduction;
	
	public Action(ACTION_TYPE type){
		this.type = type;
	}
}
