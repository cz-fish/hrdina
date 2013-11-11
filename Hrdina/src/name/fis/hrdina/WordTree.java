package name.fis.hrdina;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Search tree containing only and all valid words.
 * @author Filip Simek <filip@fis.name>
 */
public class WordTree {

	// <editor-fold desc="Nested classes">
	/** A node of the search tree */
	private class Node {
		/** Link to a single sub-node for a single letter */
		private class Link {
			/** The letter to which the link belongs */
			public char Letter;
			/** Reference to the sub-node */
			public int NextNode;

			public Link(char letter, int nextNode) {
				Letter = letter;
				NextNode = nextNode;
			}
		}

		/** Complete word flag. If true, this node represents a complete word */
		private final boolean m_Complete;
		/** List of links to subnodes for all valid next letters */
		private final List<Link> m_Links;

		/**
		 * Creates the node
		 * @param complete Set to true if this node represents a complete word; use false otherwise
		 * @param numChildren Number of children that this node will have
		 */
		public Node(boolean complete, int numChildren) {
			m_Complete = complete;
			m_Links = new ArrayList<>(numChildren);
		}

		/**
		 * Add a child node (link) to the node
		 * @param letter The letter to which the link belongs
		 * @param dest Index of the child node
		 */
		public void AddLink(char letter, int dest) {
			m_Links.add(new Link(letter, dest));
		}

		/** @return True if this node represents a complete word. False otherwise */
		public boolean getComplete() {
			return m_Complete;
		}

		/**
		 * Returns index of the child node for the given letter
		 * @param letter The next letter of the word
		 * @return Index of the child node for the given letter
		 */
		public int getNext(char letter) {
			for (Link l : m_Links) {
				if (l.Letter == letter)
					return l.NextNode;
			}
			return -1;
		}
	}

	/** Tuple used as a result of the <code>CheckWord</code> method */
	public class WordSearchResult {
		/** If true, the string was a perspective prefix (i.e. can be prolonged
		 * to a valid word by appending some letters) */
		public boolean IsPerspectivePrefix;
		/** If true, the string was a valid word */
		public boolean IsValidWord;

		public WordSearchResult() {
			IsPerspectivePrefix = false;
			IsValidWord = false;
		}
	}
	// </editor-fold>

	// <editor-fold desc="Private members">
	/** All nodes of the tree. The root node is the 0th element */
	private List<Node> m_Nodes;
	// </editor-fold>

	public WordTree() {
		m_Nodes = null;
	}

	/**
	 * Initializes the wordtree with data from the given input stream
	 * @param wordTreeStr The input stream to read from
	 * @param alphabet Alphabet associated with the wordtree (needed to decode letters)
	 * @return true if the loading succeeded; false if it did not
	 * @throws IOException if there was a problem with input file manipulation
	 */
	public boolean Load(InputStream wordTreeStr, Alphabet alphabet)
		throws IOException
	{	
		byte[] nodeData = new byte[wordTreeStr.available()];
		wordTreeStr.read(nodeData);
		int[] nodeValues = new int[nodeData.length / 4];
		ByteBuffer.wrap(nodeData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(nodeValues);
		int numNodes = nodeValues[0] >> 8;
		m_Nodes = new ArrayList<>(numNodes);
		
		for (int i = 0; i < numNodes; i++) {
			int numChildren = nodeValues[i] & 127;
			boolean completeFlag = (nodeValues[i] & 128) == 128;
			int nodePointer = nodeValues[i] >> 8;
			Node node = new Node(completeFlag, numChildren);
			for (int j = 0; j < numChildren; j++) {
				int letterIndex = nodeValues[nodePointer + j] & 127;
				char letter = alphabet.GetLetterByIndex(letterIndex);
				int dest = nodeValues[nodePointer + j] >> 8;
				node.AddLink(letter, dest);
			}
			m_Nodes.add(node);
		}
		return true;
	}

	/**
	 * Checks, whether the given string is a valid word or prefix of a word.
	 * @param word The string to be checked
	 * @return A tuple telling, whether the string was a word or a prefix (or neither or both)
	 */
	public WordSearchResult CheckWord(String word) {
		WordSearchResult result = new WordSearchResult();

		// Trace the tree from the root
		int currentNode = 0;
		boolean wordStart = true;
		for (char c : word.toCharArray()) {
			if (currentNode == 0 && !wordStart) {
				// We mustn't visit node 0 except for the very start of the search.
				// If we do visit it here again, it means that the prefix up to the
				// current letter was a valid word, but appending character c makes
				// it an invalid word or prefix
				return result;
			}
			wordStart = false;

			int link = m_Nodes.get(currentNode).getNext(c);
			if (link == -1) {
				// No continuation for letter c from the current node. Invalid word/prefix
				return result;
			}
			currentNode = link;
		}

		// We've traced the tree for all characters of the string so it is either
		// a valid word or a valid prefix or both.
		if (currentNode == 0) {
			// If we've ended up in node 0, the string is a valid word only, because
			// there will be no way of continuing from node 0, so it can't be a prefix
			result.IsValidWord = true;
		} else if (m_Nodes.get(currentNode).getComplete()) {
			// If we've ended up in a node that is not node 0 but has the complete flag
			// then the string is indeed a complete word, but since this is not node 0,
			// there also must be some child nodes to continue to (due to the construction
			// of the tree), so it is also a valid prefix
			result.IsValidWord = true;
			result.IsPerspectivePrefix = true;
		} else {
			// Otherwise it is at least a perspective prefix
			result.IsPerspectivePrefix = true;
		}

		return result;
	}
}
