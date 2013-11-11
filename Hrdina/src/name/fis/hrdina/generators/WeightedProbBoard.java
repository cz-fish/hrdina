package name.fis.hrdina.generators;

import name.fis.hrdina.Alphabet;

/**
 * Board generation strategy. Each letter on the board is picked independently on
 * other letters on the board, but the probabilities of each letter are not uniform;
 * they are determined by the relative frequencies of the respective letters.
 * @author Filip Simek <filip@fis.name>
 */
public class WeightedProbBoard implements IBoardGenerator
{
	private final Alphabet m_Alphabet;
	
	public WeightedProbBoard (Alphabet alphabet)
	{
		m_Alphabet = alphabet;
	}
	
	@Override
	public char[] GenerateBoard(int edgeSize) {
		int boardSize = edgeSize * edgeSize;
		char[] result = new char[boardSize];
		for (int i = 0; i < boardSize; i++)
		{
			result[i] = m_Alphabet.GetRandomLetterWeighted();
		}
		return result;
	}
	
}
