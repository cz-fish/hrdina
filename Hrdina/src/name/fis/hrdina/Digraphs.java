package name.fis.hrdina;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.TreeMap;

public class Digraphs {
	// Frequencies of all digraphs in an NxN matrix (where N is the number of
	// distinct letters).
	private int[] m_DigraphMatrix;
	private Alphabet m_Alphabet;
	// Number of letters in the alphabet. Size of the digraph matrix
	private int m_AlphabetSize;
	
	public Digraphs()
	{
	}
	
	public boolean Load(InputStream str, Alphabet alphabet) throws IOException
	{
		m_Alphabet = alphabet;
		m_AlphabetSize = alphabet.GetSize();
		int triangleSize = (m_AlphabetSize * m_AlphabetSize + m_AlphabetSize)/2;
		byte[] digraphData = new byte[triangleSize * 4];
		str.read(digraphData);
		int[] digraphValues = new int[triangleSize];
		ByteBuffer.wrap(digraphData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(digraphValues);

		// We only have one triangle of the matrix. Extend it to the whole symmetric matrix
		m_DigraphMatrix = new int[m_AlphabetSize * m_AlphabetSize];
		int pos = 0;
		for (int i = 0; i < m_AlphabetSize; i++)
		{
			for (int j = 0; j <= i; j++)
			{
				m_DigraphMatrix[i * m_AlphabetSize + j] = digraphValues[pos];
				m_DigraphMatrix[j * m_AlphabetSize + i] = digraphValues[pos];
				pos++;
			}
		}

		return true;
	}
	
	public TreeMap<Character, Integer> GetDigraphsForLetter(char letter)
	{
		TreeMap<Character, Integer> result = new TreeMap<>();
		int letterIndex = m_Alphabet.GetIndexOfLetter(letter);
		if (letterIndex != Alphabet.NO_LETTER_INDEX)
		{
			int i = letterIndex * m_AlphabetSize;
			for (int j = 0; j < m_AlphabetSize; j++)
			{
				result.put(m_Alphabet.GetLetterByIndex(j), m_DigraphMatrix[i+j]);
			}
		}
		return result;
	}
}
