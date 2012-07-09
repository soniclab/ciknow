package ciknow;
/**
 * 
 * @author gyao
 *
 */
public class UnsupportedOperationException extends Exception{
	private static final long serialVersionUID = 1L;

	public UnsupportedOperationException(){
		super();
	}
	
	public UnsupportedOperationException(String message){
		super(message);
	}
	
	public UnsupportedOperationException(String message, Throwable t){
		super(message, t);
	}
	
	public UnsupportedOperationException(Throwable t){
		super(t);
	}
}
