package name.fis.hrdina;

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
	// Number of letters in all words in the word tree in total
	// (Used to calculate relative frequency of each letter)
	private int m_SelectionLimit;
	private Random m_Rand;

	public Alphabet(int totalLetters, Map<Character, Integer> letterFrequency)
	{
		m_Rand = new Random();
		InitValues(totalLetters, letterFrequency);
	}

	public char GetRandomLetter()
	{
		int pick = m_Rand.nextInt(m_SelectionLimit);
		//double pick = Math.random() * m_SelectionLimit;
		Entry<Integer, Character> pickedEntry = m_Roulette.higherEntry(pick);
		if (pickedEntry == null)
		{
			// We shouldn't get here. This is a safety fallback
			return m_Roulette.lastEntry().getValue();
		}
		return pickedEntry.getValue();
	}

	public int GetLetterValue(char letter)
	{
		if (!m_Values.containsKey(letter))
			return 0;
		return m_Values.get(letter);
	}

	private void InitValues(int totalLetters, Map<Character, Integer> letterFrequency)
	{
		m_Values = new TreeMap<>();
		m_Roulette = new TreeMap<>();
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
	
	/* For testing only */
	public void Dump()
	{
		for (char c: m_Values.keySet())
		{
			System.out.println(String.format("%c: %d", c, m_Values.get(c)));
		}
	}
}
