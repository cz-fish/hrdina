package name.fis.hrdina.generators;

import name.fis.hrdina.Alphabet;

public class EqualProbBoard implements IBoardGenerator
{
	private final Alphabet m_Alphabet;
	
	public EqualProbBoard(Alphabet alphabet)
	{
		m_Alphabet = alphabet;
	}
	
	@Override
	public char[] GenerateBoard(int edgeSize) {
		int boardSize = edgeSize * edgeSize;
		char[] result = new char[boardSize];
		for (int i = 0; i < boardSize; i++)
		{
			result[i] = m_Alphabet.GetRandomLetterUniform();
		}
		return result;
	}
}
