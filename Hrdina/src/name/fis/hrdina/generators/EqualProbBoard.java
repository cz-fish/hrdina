package name.fis.hrdina.generators;

import name.fis.hrdina.Alphabet;

/**
 * Simple board genertor strategy. Each letter on the board is picked completely
 * at random and each letter has an equal probability of being picked, regardless
 * of its neighbourhood or letter frequency statistics.
 * @author Filip Simek <filip@fis.name>
 */
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
