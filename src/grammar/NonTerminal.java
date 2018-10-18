package grammar;


/**
 * class NonTerminal.
 * Represents a non terminal(Variable) inside grammar.
 * */
public class NonTerminal extends Symbol {
		
	/**
	 * property isNullable
	 * ture if this non terminal can produce an empty string
	 * */	
	public boolean isNullable = false;
	
	/**
	 * property nullableFlag
	 * internal property to specify if nullability of this non terminal has been calculated yet or not 
	 * */
	public boolean nullableFlag = false;
	
	/**
	 * property FIRST_SET
	 * A list of terminals which are in FIRST SET of this non terminal
	 * */
	public Terminal[] FIRST_SET = null;
	
	/**
	 * property FOLLOW_SET
	 * A list of terminals which are in FOLLOW SET of this non terminal
	 * */
	public Terminal[] FOLLOW_SET = null;
		
	public NonTerminal(String name){
		super(name);
	}
}