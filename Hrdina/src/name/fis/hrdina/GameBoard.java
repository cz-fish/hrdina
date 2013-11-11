package name.fis.hrdina;

import name.fis.hrdina.generators.IBoardGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The game board. It is currently always assumed to be square.
 * Consists of NxN letters. It is able to solve itself (find all valid words
 * that it contains) for reference.
 * @author Filip Simek <filip@fis.name>
 */
public class GameBoard {
	/** Size of the edge of the board */
	private final int SIZE = 4;
	/** Board contents, i.e. SIZExSIZE letters */
	private char[] m_Board;
	/** WordTree used for word validation */
	private WordTree m_Tree;
	/** Alphabet that generates the letters for the board */
	private Alphabet m_Alphabet;
	
	/**
	 * Creates a new, uninitialized board
	 */
	public GameBoard()
	{
		m_Board = new char[SIZE * SIZE];
	}
	
	/**
	 * Generates a random board
	 * @param alphabet The alphabet providing random letters
	 * @param wordTree WordTree used for checking valid words
	 * @param boardGenerator Board generation strategy
	 */
	public void Init(Alphabet alphabet, WordTree wordTree, IBoardGenerator boardGenerator)
	{
		m_Alphabet = alphabet;
		m_Tree = wordTree;
		m_Board = boardGenerator.GenerateBoard(SIZE);
	}
	
	/**
	 * Solves the board, i.e. finds all valid words that the board contains
	 * @return List of all unique valid words on the board
	 */
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
	
	/**
	 * Recursive step of the valid word search
	 * @param position Position of the next letter to be visited
	 * @param wordSoFar The prefix of the word built so far
	 * @param visited For each letter of the board, true signals that that letter
	 *        was already used and can't be used again
	 * @return Set of all valid words that were found from the starting point
	 *         determined by the parameters.
	 */
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
	
	/**
	 * For the given board position, determines whether the other position is its neighbor or not
	 * @param position First position
	 * @param neighbor Second position; proposed neighbor
	 * @return true if the two positions are neighbors on the board; false if they are not
	 */
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
