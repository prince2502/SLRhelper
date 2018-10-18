package grammar;

/**
 * class Symbol
 * Represents smallest unit inside grammar.
 * For example a terminal or a Non terminal
 * */

public class Symbol {
	/**
	 * Property name
	 * the value of the symbol. i.e.  stmt , $ , E
	 * */
	public String name;
	
	public Symbol(String name){
		this.name = name;
	}
}
