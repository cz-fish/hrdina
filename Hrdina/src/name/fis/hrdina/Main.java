package name.fis.hrdina;
import java.io.*;
import java.util.List;
import name.fis.hrdina.generators.ConditionalProbBoard;
import name.fis.hrdina.generators.EqualProbBoard;
import name.fis.hrdina.generators.IBoardGenerator;
import name.fis.hrdina.generators.WeightedProbBoard;

public class Main
{
	private static final String ALPHABET_FILE_NAME = "alphabet.bin";
	private static final String WORDTREE_FILE_NAME = "wordtree.bin";
	private static final String DIGRAPH_FILE_NAME = "digraph.bin";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		Alphabet alphabet = new Alphabet();
		WordTree tree = new WordTree();
		Digraphs digraphs = new Digraphs();
		
		InputStream alphabetStr, wordtreeStr, digraphStr;
		alphabetStr = Main.class.getResourceAsStream("/name/fis/hrdina/data/" + ALPHABET_FILE_NAME);
		wordtreeStr = Main.class.getResourceAsStream("/name/fis/hrdina/data/" + WORDTREE_FILE_NAME);
		digraphStr = Main.class.getResourceAsStream("/name/fis/hrdina/data/" + DIGRAPH_FILE_NAME);
		
		try
		{
			if (!alphabet.Load(alphabetStr))
			{
				System.err.println("Failed to load alphabet");
				return;
			}
			alphabetStr.close();
			
			if (!tree.Load(wordtreeStr, alphabet))
			{
				System.err.println("Failed to load word tree");
				return;
			}
			wordtreeStr.close();
			
			if (!digraphs.Load(digraphStr, alphabet))
			{
				System.err.println("Failed to load digraphs");
				return;
			}
			digraphStr.close();
		}
		catch (WordTreeException e)
		{
			System.err.println(String.format("Error loading data files: %s", e.getMessage()));
			return;
		}


		// Test
/*		TestWord(tree, "srp");
		TestWord(tree, "srpen");
		TestWord(tree, "padrť");
		TestWord(tree, "lžíce");
		TestWord(tree, "oj");
		TestWord(tree, "křpch"); */
		
//		tree.getAlphabet().Dump();
		
		GameBoard board = new GameBoard();
		
		IBoardGenerator generator = new EqualProbBoard(alphabet);
		board.Init(alphabet, tree, generator);
		TestBoard(board, "EqualProb");

		generator = new WeightedProbBoard(alphabet);
		board.Init(alphabet, tree, generator);
		TestBoard(board, "WeightedProb");

		generator = new ConditionalProbBoard(alphabet, digraphs);
		board.Init(alphabet, tree, generator);
		TestBoard(board, "ConditionalProb");
	}
	
	private static void TestWord(WordTree tree, String word)
	{
		WordTree.WordSearchResult r;
		r = tree.CheckWord(word);
		System.out.println(String.format("%s: is word? %s, is prefix? %s", word, r.IsValidWord? "YES":"NO", r.IsPerspectivePrefix? "YES":"NO"));
	}
	
	private static void TestBoard(GameBoard board, String type)
	{
		System.out.println("*********************");
		System.out.println(String.format("Board type %s", type));
		board.DumpBoard();
		List<String> solutions = board.SolveBoard();
		System.out.println("--------");
		System.out.println(String.format("Found %d solutions", solutions.size()));
		for (String s: solutions)
		{
			System.out.println(s);
		}
	}
}
