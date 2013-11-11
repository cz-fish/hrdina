package name.fis.hrdina.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import name.fis.hrdina.Alphabet;
import name.fis.hrdina.Digraphs;

/**
 * Board generation strategy. Each letter (except for the first one) is generated
 * with probabilities based on its direct neighborhood. Letter pairs that are more
 * frequent as digraphs should be generated next to each other more often.
 * @author Filip Simek <filip@fis.name>
 */
public class ConditionalProbBoard implements IBoardGenerator
{
	/** Alphabet to work with */
	private final Alphabet m_Alphabet;
	/** Digraph statistics */
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
				TreeMap<Character, Integer> probMap = GetProbMapFromNeighbors(coord, edgeSize, result);
				result[coord] = m_Alphabet.GetRandomLetterConditional(probMap);
			}
		}
		
		return result;
	}
	
	/**
	 * @param size Board edge size
	 * @return a list of board coordinates going in a spiral from the center to the edge
	 */
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
	
	/**
	 * @param center Position on the board, whose neighborhood should be generated
	 * @param edgeSize Board edge size
	 * @return list of coordinates of neighbors of the given position on the board
	 */
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

	/**
	 * Calculates conditional probabilities for the letter on the given coordinates
	 * based on letters in its neighborhood
	 * @param coord Coordinates, where to place the new letter; center of the neighborhood
	 * @param edgeSize Board edge size
	 * @param boardSoFar Contents of the board. Positions that were not yet populated
	 *        with letters contain the value NO_LETTER.
	 * @return Probability of each letter of being selected for the given position on the board
	 */
	private TreeMap<Character, Integer> GetProbMapFromNeighbors(int coord, int edgeSize, char[] boardSoFar)
	{
		// Compute conditional letter probabilities based on the neighbourhood
		List<Integer> neighborhood = GetNeighborhood(coord, edgeSize);
		TreeMap<Character, Integer> probMap = new TreeMap<>();
		for (int neighbor: neighborhood)
		{
			if (boardSoFar[neighbor] != Alphabet.NO_LETTER)
			{
				TreeMap<Character, Integer> neighborProb = m_Digraphs.GetDigraphsForLetter(boardSoFar[neighbor]);
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
		return probMap;
	}
}
