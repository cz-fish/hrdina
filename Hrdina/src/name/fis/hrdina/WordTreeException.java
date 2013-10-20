package name.fis.hrdina;

public class WordTreeException extends Exception {
	private String m_Message;
	
	public WordTreeException(String message)
	{
		m_Message = message;
	}
	
	public String getMessage() {return m_Message;}
}
