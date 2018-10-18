package grammar;
import java.util.HashMap;
import java.util.Map;

import parser.Action;

/**
 * class State
 * represents a state inside LR(0) automaton
 * */
public class State {
	
	/**
	 * property state_no
	 * */
	public int state_no;
	
	/**
	 * property kernalItems
	 * the productions which are kernal items of the state
	 * */
	public ExtProduction[] kernalItems;
	
	/**
	 * property GOTO_MAP
	 * a basic goto map for this state
	 * maps a Symbol (terminal or non terminal) to some state_no. This state will go to that state upon
	 *   encountring this symbol on stack
	 * */
	public Map<String , Integer> GOTO_MAP = new HashMap<String , Integer>();
	
	/**
	 * property SLR_TABLE
	 * an extended map, mapping a Symbol(terminal or non terminal) and an Action
	 * representing what action to take upon encountring this symbol on stack like SHIFT, REDUCE etc
	 * */
	public Map<String , Action> SLR_TABLE = null;
	
	/**
	 * function compareTo
	 * Compare this state to another for equality. (if both of them carry the same productions 
	 * 	inside kernal items)
	 * */
	public boolean compareTo(State another){
		if(this.kernalItems.length != another.kernalItems.length ) return false;
		
		for (int i = 0 ; i < this.kernalItems.length ; i++){
			if (! this.kernalItems[i].compareTo(another.kernalItems[i])){ 
				// Is this good enough test ? What if the order is different ? 
				return false;
			}
		}
		return true;
	}
}
