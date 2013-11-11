package name.fis.hrdina.generators;

/**
 * Interface for a board generator strategy
 * @author Filip Simek <filip@fis.name>
 */
public interface IBoardGenerator {
	/**
	 * Generate letters for a board of the given size
	 * @param size size of the edge of the (square) board
	 * @return An array of size x size letters forming the board. The 2D board
	 *         is represented as a 1D array starting in the top left corner, wrapping
	 *         at each row's end and ending in the bottom right corner.
	 */
	public char[] GenerateBoard(int size);
}
