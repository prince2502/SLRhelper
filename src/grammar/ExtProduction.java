package grammar;

/**
 * class ExtProduction extends Production
 * Represents a production which has # set somewhere inside the body, to be used in State
 * */
public class ExtProduction extends Production {
	/**
	 * property hashPointer
	 * position of hash inside body
	 * */
	public int hashPointer = 0;
	
	public ExtProduction(String head , String[] body , int hashPointer){
		super(head , body);
		this.hashPointer = hashPointer;
	}
	
	/**
	 * function compareTo
	 * compare this ExtProduction to other for equality ..
	 * */
	public boolean compareTo(ExtProduction another){
		
		if(this.hashPointer != another.hashPointer) return false;
		
		if(this.head.compareTo(another.head) != 0) return false;
		
		if(this.body.length != another.body.length) return false;
		
		// this system can fail in a case. but that case can never be the case
		for(int i = 0 ; i < this.body.length ; i++){
			String thisString = this.body[i];
			String anotherString = another.body[i];
			if(thisString.compareTo(anotherString) != 0) return false;
		}
		
		return true;
	}
	
	@Override 
	public String toString(){
		return "";
	}
}
