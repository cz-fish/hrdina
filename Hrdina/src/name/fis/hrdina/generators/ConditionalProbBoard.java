package name.fis.hrdina.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import name.fis.hrdina.Alphabet;
import name.fis.hrdina.Digraphs;

public class ConditionalProbBoard implements IBoardGenerator
{
	private final Alphabet m_Alphabet;
	private final Digraphs m_Digraphs;
	
	public ConditionalProbBoard(Alphabet alphabet, Digraphs digraphs)
	{
		m_Alphabet = alphabet;
		m_Digraphs = digraphs;
	}
	
	@Override
	public char[] GenerateBoard(int edgeSize) {
		int boardSize = edgeSize * edgeSize;
		char[] result = new char[boardSize];
		for (int i = 0; i < boardSize; i++)
			result[i] = Alphabet.NO_LETTER;

		List<Integer> coordOrder = GenerateSpiral(edgeSize);
		boolean first = true;
		
		for (int coord: coordOrder)
		{
			if (first)
			{
				// Pick the first letter at random (based on unconditional letter probabilities)
				first = false;
				result[coord] = m_Alphabet.GetRandomLetterWeighted();
			}
			else
			{
				// Compute conditional letter probabilities based on the neighbourhood
				List<Integer> neighborhood = GetNeighborhood(coord, edgeSize);
				TreeMap<Character, Integer> probMap = new TreeMap<>();
				for (int neighbor: neighborhood)
				{
					if (result[neighbor] != Alphabet.NO_LETTER)
					{
						TreeMap<Character, Integer> neighborProb = m_Digraphs.GetDigraphsForLetter(result[neighbor]);
						// TODO: combine probability maps differently. Plain addition would discriminate neighbors that are less frequent letters
						for (Entry<Character, Integer> p: neighborProb.entrySet())
						{
							Character key = p.getKey();
							Integer oldValue = probMap.get(key);
							probMap.put(key, oldValue == null? p.getValue(): oldValue + p.getValue());
						}
					}
				}
				// TODO: make sure every letter has nonzero probability in the probMap

				result[coord] = m_Alphabet.GetRandomLetterConditional(probMap);
			}
		}
		
		return result;
	}
	
	/// @return a list of board coordinates going in a spiral from the center to the edge
	private List<Integer> GenerateSpiral(int size)
	{
		List<Integer> spiral = new ArrayList<>(size * size);
		int pos = -1;
		int inc = 1;
		int segment = size;
		boolean horiz = true;
		while (segment > 0)
		{
			for (int i = 0; i < segment; i++)
			{
				pos += inc;
				spiral.add(pos);
			}
			
			horiz = !horiz;
			if (horiz)
			{
				inc = (inc > 0)? -1: 1;
			}
			else
			{
				segment -= 1;
				inc = inc * size;
			}
		}
		Collections.reverse(spiral);
		return spiral;
	}
	
	private List<Integer> GetNeighborhood(int center, int edgeSize)
	{
		List<Integer> result = new ArrayList<>();
		int x = center % edgeSize;
		int y = center / edgeSize;
		
		if (x > 0)
		{
			if (y > 0)
				result.add(center - edgeSize - 1);
			result.add(center - 1);
			if (y < edgeSize - 1)
				result.add(center + edgeSize - 1);
		}
		if (x < edgeSize - 1)
		{
			if (y > 0)
				result.add(center - edgeSize + 1);
			result.add(center + 1);
			if (y < edgeSize - 1)
				result.add(center + edgeSize + 1);
		}
		if (y > 0)
			result.add(center - edgeSize);
		if (y < edgeSize - 1)
			result.add(center + edgeSize);
		
		return result;
	}
}
