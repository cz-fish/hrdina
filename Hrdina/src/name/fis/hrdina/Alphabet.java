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

/**
 * Manages the alphabet used by the game. The alphabet is loaded from a resource
 * file. This class is mostly responsible for generating random letters to form
 * a game board and for scoring.
 * @author Filip Simek <filip@fis.name>
 */
public class Alphabet {
	/** Maps a limit value to a character. It is used in random letter selection.
	  * If the random number falls between two adjacent limits in this map then
	  * the respective letter is returned. */
	private TreeMap<Integer, Character> m_Roulette;
	/** Point value of each letter, based on the frequency */
	private Map<Character, Integer> m_Values;
	/** Maps letter index as used in the data files to the actual character */
	private TreeMap<Integer, Character> m_IndexMap;
	/** Number of letters in all words in the word tree in total
	  * (Used to calculate relative frequency of each letter) */
	private int m_SelectionLimit;
	private final Random m_Rand;
	
	/** Value returned by GetLetterIndex for a nonexistant letter */
	public final static int NO_LETTER_INDEX = -1;
	/** Placeholder for a nonexistant letter */
	public final static char NO_LETTER = ' ';

	/**
	 * Default constructor, using the Random class as random number generator
	 */
	public Alphabet()
	{
		m_Rand = new Random();
	}

	/**
	 * This overload lets the caller to specify the random number generator
	 * @param rand Random number generator to use
	 */
	public Alphabet(Random rand)
	{
		m_Rand = rand;
	}

	/**
	 * Initializes the alphabet instance with data from the given input stream
	 * @param alphabetStr Stream to load alphabet data from
	 * @return true if loading succeeded; false if it did not
	 * @throws IOException Problems manipulating the input file
	 * @throws WordTreeException Data in the input file is invalid
	 */
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

	/**
	 * Calculates private members from the data loaded from the input stream
	 * @param totalLetters Total number of all letters in all words
	 * @param letterFrequency Number of occurencies of each letter
	 * @param letterOrder Maps letter index to the actual letter
	 */
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

	/**
	 * @return Number of distinct letters in the alphabet
	 */
	public int GetSize()
	{
		return m_IndexMap.size();
	}

	// <editor-fold desc="Letter getters">
	/**
	 * @param index Letter index from the input stream
	 * @return Letter assigned to the given index
	 */
	public char GetLetterByIndex(int index)
	{
		if (!m_IndexMap.containsKey(index))
			return NO_LETTER;
		return m_IndexMap.get(index);
	}
	
	/**
	 * Maps a letter back to its index used in the input stream.
	 * Not optimized, performs a linear scan of the alphabet.
	 * @param letter The letter to convert to its index
	 * @return Index of the given letter or <code>NO_LETTER_INDEX</code> if not found
	 */
	public int GetIndexOfLetter(char letter)
	{
		for (Entry<Integer, Character> entry: m_IndexMap.entrySet())
		{
			if (entry.getValue() == letter)
				return entry.getKey();
		}
		return NO_LETTER_INDEX;
	}
	
	/** @return one letter with uniform distribution of all letters */
	public char GetRandomLetterUniform()
	{
		int pick = m_Rand.nextInt(m_IndexMap.size());
		return GetLetterByIndex(pick);
	}
	
	/** @return one letter with probabilities weighted by relative letter frequencies */
	public char GetRandomLetterWeighted()
	{
		return GetRandomLetterByRoulette(m_Roulette, m_SelectionLimit);
	}
	
	/**
	 * Returns a random letter. Each letter has a probability of being picked
	 * calculated as the ratio between its frequency specified by the <code>probMap</code>
	 * parameter and the sum of frequencies of all letters. It means that a letter
	 * that is not in the <code>probMap</code> at all can't be picked.
	 * @param probMap Letter probability map
	 * @return one letter with probabilities specified by the given probability map
	 */
	public char GetRandomLetterConditional(TreeMap<Character, Integer> probMap)
	{
		TreeMap<Integer, Character> roulette = new TreeMap<>();
		int accumulator = 0;
		for (Entry<Character, Integer> e: probMap.entrySet())
		{
			int value = e.getValue();
			if (value == 0)
				continue;
			accumulator += value;
			roulette.put(accumulator, e.getKey());
		}
		return GetRandomLetterByRoulette(roulette, accumulator);
	}
	
	/**
	 * Picks a random letter based on letter probabilities using roulette algorithm.
	 * Used by <code>GetRandomLetterWeighted</code> and <code>GetRandomLetterConditional</code>.
	 * A random number between 0 and <code>probDivider</code> is generated. The <code>roulette</code>
	 * parameter contains keys of an increasing sequence and maps (not necessarilly equally large)
	 * number intervals to letters.
	 * @param roulette Letter interval boundaries
	 * @param probDivider Roulette size
	 * @return A random letter picked by the roulette
	 */
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

	/**
	 * @param letter A single letter
	 * @return point value of the given letter */
	public int GetLetterValue(char letter)
	{
		if (!m_Values.containsKey(letter))
			return 0;
		return m_Values.get(letter);
	}
	
	/**
	 * Calculates value of a word as a sum of values of all its letters
	 * @param word The word
	 * @return The word's point value
	 */
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
