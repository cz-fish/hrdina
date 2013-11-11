package name.fis.hrdina;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.TreeMap;

/**
 * This class knows probabilities of every two letters of the alphabet being next
 * to each other in valid words. For each letter, the instance of this class is
 * able to tell, how probably every letter occurs immediately before or immediately
 * after the given letter in the words from the wordlist.
 * The statistics are loaded from a resource file.
 * @author Filip Simek <filip@fis.name>
 */
public class Digraphs {
	/** The alphabet to work with */
	private Alphabet m_Alphabet;
	/** Frequencies of all digraphs in an NxN matrix (where N is the number of
	  * distinct letters of the alphabet). */
	private int[] m_DigraphMatrix;
	/** Number of letters in the alphabet. Size of the digraph matrix */
	private int m_AlphabetSize;
	
	public Digraphs()
	{
	}
	
	/**
	 * Initializes the instance with values from the given input stream.
	 * @param str Stream to load digraph statistics from
	 * @param alphabet The alphabet to work with
	 * @return true if the loading succeeded; false if it did not
	 * @throws IOException if there is a problem with input file manipulation
	 */
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
	
	/**
	 * Returns a map that tells for each letter, how frequently it appears next
	 * to the given letter in valid words. The result contains all letters of the
	 * alphabet, including the given letter itself and letters that never appear
	 * next to the given letter (i.e. their frequency is 0).
	 * @param letter The letter
	 * @return Map of probabilities of each letter for being next to the given letter.
	 */
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
