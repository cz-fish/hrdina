package name.fis.hrdina;

/**
 * Custom exception type to distinguish a problem with the WordTree from other exceptions
 * @author Filip Simek <filip@fis.name>
 */
public class WordTreeException extends Exception {
	private final String m_Message;
	
	public WordTreeException(String message)
	{
		m_Message = message;
	}
	
	@Override
	public String getMessage() {return m_Message;}
}
