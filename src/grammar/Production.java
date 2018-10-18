package grammar;

/**
 * class Production
 * represents a production inside grammar. i.e. stmt -> if ( cond ) { stmt }
 * */
public class Production {
	
	/**
	 * property head
	 * head of production, stmt in above example
	 * */
	public String head;
	
	/**
	 * property body
	 * list of body elements of production. if , ( , cond ... inside above example
	 * */
	public String[] body;
	
	
	public Production(String head , String[] body){
		this.head = head;
		this.body = body;
	}
	
	/**
	 * function bodyToString
	 * join all parts of body and return one string
	 * */
	public static String bodyToString(String[] arr){
		StringBuilder value = new StringBuilder("");
		for(int i = 0 ; i < arr.length ; i++) {
			value.append(arr[i]).append(" ");
		}
		return value.toString();
	}
	
	@Override
	public String toString(){
		return this.head + " :: " + bodyToString(this.body);
	}
}
