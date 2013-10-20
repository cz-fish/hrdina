package name.fis.hrdina;
import java.io.*;
import java.util.List;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		WordTree tree = new WordTree();
		FileInputStream fstr = null;
		try
		{
			fstr = new FileInputStream("wordlist.bin");
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Wordtree file not found");
			return;
		}
		
		try
		{
			if (!tree.LoadTree(fstr))
			{
				System.out.println("Failed to load word tree");
				return;
			}
		}
		catch (WordTreeException e)
		{
			System.out.println(String.format("Error loading tree: %s", e.getMessage()));
			return;
		}
		
		// Test
/*		TestWord(tree, "srp");
		TestWord(tree, "srpen");
		TestWord(tree, "padrť");
		TestWord(tree, "lžíce");
		TestWord(tree, "oj");
		TestWord(tree, "křpch"); */
		
		GameBoard board = new GameBoard();
		board.Init(tree);
		board.DumpBoard();
		List<String> solutions = board.SolveBoard();
		System.out.println("--------");
		System.out.println(String.format("Found %d solutions", solutions.size()));
		for (String s: solutions)
		{
			System.out.println(s);
		}
		
		if (fstr != null)
		{
			fstr.close();
		}
	}
	
	private static void TestWord(WordTree tree, String word)
	{
		WordTree.WordSearchResult r;
		r = tree.CheckWord(word);
		System.out.println(String.format("%s: is word? %s, is prefix? %s", word, r.IsValidWord? "YES":"NO", r.IsPerspectivePrefix? "YES":"NO"));
	}
}
