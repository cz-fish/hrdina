package name.fis.hrdina.generators;

import name.fis.hrdina.Alphabet;

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
