package name.fis.hrdina;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class Alphabet {
	// Maps a limit value to a character. It is used in random letter selection.
	// If the random number falls between two adjacent limits in this map then
	// the respective letter is returned.
	private TreeMap<Integer, Character> m_Roulette;
	// Point value of each letter, based on the frequency
	private Map<Character, Integer> m_Values;
	// Maps letter index as used in the data files to the actual character
	private TreeMap<Integer, Character> m_IndexMap;
	// Number of letters in all words in the word tree in total
	// (Used to calculate relative frequency of each letter)
	private int m_SelectionLimit;
	private final Random m_Rand;
	
	public final static int NO_LETTER_INDEX = -1;
	public final static char NO_LETTER = ' ';

	public Alphabet()
	{
		m_Rand = new Random();
	}

	public boolean Load(InputStream alphabetStr) throws IOException, WordTreeException
	{
		// Check header
		int version = Util.ReadLEInt(alphabetStr);
		if (version > 1) {
			throw new WordTreeException(String.format("Unsupported wordtree version %d", version));
		}

		// Unpack alphabet / letter frequencies
		Map<Character, Integer> letterFreq = new TreeMap<>();
		int distinctLetters;
		int totalLetters = Util.ReadLEInt(alphabetStr);
		distinctLetters = Util.ReadLEInt(alphabetStr);

		byte[] letterData = new byte[distinctLetters * 4];
		byte[] counterData = new byte[distinctLetters * 4];
		alphabetStr.read(letterData);
		alphabetStr.read(counterData);

		Charset charset = Charset.forName("UTF-32LE");
		char[] letters = charset.decode(ByteBuffer.wrap(letterData)).array();
		int[] frequencies = new int[distinctLetters];
		ByteBuffer.wrap(counterData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(frequencies);

		for (int i = 0; i < distinctLetters; i++) {
			letterFreq.put(letters[i], frequencies[i]);
		}
		
		InitValues(totalLetters, letterFreq, letters);

		return true;
	}

	private void InitValues(int totalLetters, Map<Character, Integer> letterFrequency, char[] letterOrder)
	{
		m_Values = new TreeMap<>();
		m_Roulette = new TreeMap<>();
		m_IndexMap = new TreeMap<>();
		
		int letterIndex = 0;
		for (char c: letterOrder)
		{
			m_IndexMap.put(letterIndex, c);
			letterIndex++;
		}
		
		int runningLimit = 0;
		
		for (char c: letterFrequency.keySet())
		{
			int absFreq = letterFrequency.get(c);
			
			// The less frequent the letter, the more points for it
			float relFreq = absFreq / (float)totalLetters;
			int points = 1;
			if (relFreq < 0.06)
				points = 2;
			if (relFreq < 0.03)
				points = 3;
			if (relFreq < 0.01)
				points = 4;
			if (relFreq < 0.008)
				points = 5;
			if (relFreq < 0.004)
				points = 6;
			if (relFreq < 0.001)
				points = 7;
			if (relFreq < 0.0004)
				points = 8;
			m_Values.put(c, points);
			
			// Make more frequent letters more popular
			if (points < 3)
				runningLimit += 2 * absFreq;
			else
				runningLimit += absFreq;
			m_Roulette.put(runningLimit, c);
		}
		m_SelectionLimit = runningLimit;
	}

	public int GetSize()
	{
		return m_IndexMap.size();
	}

	// <editor-fold desc="Letter getters">
	public char GetLetterByIndex(int index)
	{
		return m_IndexMap.get(index);
	}
	
	public int GetIndexOfLetter(char letter)
	{
		for (Entry<Integer, Character> entry: m_IndexMap.entrySet())
		{
			if (entry.getValue() == letter)
				return entry.getKey();
		}
		return NO_LETTER_INDEX;
	}
	
	/// @return one letter with uniform distribution of all letters
	public char GetRandomLetterUniform()
	{
		int pick = m_Rand.nextInt(m_IndexMap.size());
		return GetLetterByIndex(pick);
	}
	
	/// @return one letter with probabilities weighted by relative letter frequencies
	public char GetRandomLetterWeighted()
	{
		return GetRandomLetterByRoulette(m_Roulette, m_SelectionLimit);
	}
	
	public char GetRandomLetterConditional(TreeMap<Character, Integer> probMap)
	{
		TreeMap<Integer, Character> roulette = new TreeMap<>();
		int accumulator = 0;
		for (Entry<Character, Integer> e: probMap.entrySet())
		{
			int value = e.getValue();
			if (value == 0)
				continue;
			/**/
			if (m_Values.get(e.getKey()) < 3)
			{
				value *= 2;
			}
			/**/
			accumulator += value;
			roulette.put(accumulator, e.getKey());
		}
		return GetRandomLetterByRoulette(roulette, accumulator);
	}
	
	private char GetRandomLetterByRoulette(TreeMap<Integer, Character> roulette, int probDivider)
	{
		int pick = m_Rand.nextInt(probDivider);
		Entry<Integer, Character> pickedEntry = roulette.higherEntry(pick);
		if (pickedEntry == null)
		{
			// We shouldn't get here. This is a safety fallback
			return roulette.lastEntry().getValue();
		}
		return pickedEntry.getValue();
	}
	// </editor-fold>

	/// @return point value of the given letter
	public int GetLetterValue(char letter)
	{
		if (!m_Values.containsKey(letter))
			return 0;
		return m_Values.get(letter);
	}
	
	public int GetWordValue(String word)
	{
		int value = 0;
		for (char c: word.toCharArray())
		{
			value += GetLetterValue(c);
		}
		return value;
	}
	
	/* For testing only */
	public void Dump()
	{
		for (char c: m_Values.keySet())
		{
			System.out.println(String.format("%c: %d", c, m_Values.get(c)));
		}
	}
}
