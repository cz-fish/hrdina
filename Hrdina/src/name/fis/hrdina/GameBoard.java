package name.fis.hrdina;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GameBoard {
	private final int SIZE = 4;
	private char[] m_Board;
	private WordTree m_Tree;
	private Alphabet m_Alphabet;
	
	public GameBoard()
	{
		m_Board = new char[SIZE * SIZE];
	}
	
	public void Init(WordTree tree)
	{
		m_Tree = tree;
		m_Alphabet = tree.getAlphabet();
		// Pick size * size random letters
		for (int i = 0; i < SIZE * SIZE; i++)
		{
			m_Board[i] = m_Alphabet.GetRandomLetter();
		}
	}
	
	public List<String> SolveBoard()
	{
		HashSet<String> hs = new HashSet<>();
		for (int i = 0; i < SIZE * SIZE; i++)
		{
			boolean[] visited = new boolean[SIZE*SIZE];
			for (int j = 0; j < SIZE*SIZE; j++)
				visited[j] = (i == j);
			
			hs.addAll(ContinueSolvingFrom(i, String.valueOf(m_Board[i]), visited));
		}
		
		List<String> result = new ArrayList<>(hs.size());
		result.addAll(hs);
		return result;
	}
	
	private HashSet<String> ContinueSolvingFrom(int position, String wordSoFar, boolean []visited)
	{
		HashSet<String> result = new HashSet<>();
		WordTree.WordSearchResult perspective = m_Tree.CheckWord(wordSoFar);
		
		if (perspective.IsValidWord)
		{
			result.add(wordSoFar);
		}
		
		if (perspective.IsPerspectivePrefix)
		{
			int[] neighbors = new int[] {
				position - SIZE - 1,
				position - SIZE,
				position - SIZE + 1,
				position - 1,
				position + 1,
				position + SIZE - 1,
				position + SIZE,
				position + SIZE + 1
			};
			
			for (int i: neighbors)
			{
				if (!IsNeighborValid(position, i) || visited[i])
					continue;
				visited[i] = true;
				result.addAll(ContinueSolvingFrom(i, wordSoFar + m_Board[i], visited));
				visited[i] = false;
			}
		}
		
		return result;
	}
	
	private boolean IsNeighborValid(int position, int neighbor)
	{
		if (neighbor < 0 || neighbor >= SIZE * SIZE)
			return false;
		int px = position % SIZE;
		int py = position / SIZE;
		int nx = neighbor % SIZE;
		int ny = neighbor / SIZE;
		if ((Math.abs(px-nx) > 1) || (Math.abs(py-ny) > 1))
			return false;
		return true;
	}

	/** For testing only */
	public void DumpBoard()
	{
		for (int i = 0; i < 16; i+=4)
		{
			System.out.println(String.format("%c %d; %c %d; %c %d; %c %d",
				m_Board[i], m_Alphabet.GetLetterValue(m_Board[i]),
				m_Board[i+1], m_Alphabet.GetLetterValue(m_Board[i+1]),
				m_Board[i+2], m_Alphabet.GetLetterValue(m_Board[i+2]),
				m_Board[i+3], m_Alphabet.GetLetterValue(m_Board[i+3])
			));
		}
	}
}
