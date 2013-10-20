package name.fis.hrdina;

public class WordTreeException extends Exception {
	private final String m_Message;
	
	public WordTreeException(String message)
	{
		m_Message = message;
	}
	
	@Override
	public String getMessage() {return m_Message;}
}
