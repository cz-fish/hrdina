package name.fis.hrdina;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WordTree {

	// <editor-fold desc="Nested classes">
	private class Node {

		private class Link {

			public char Letter;
			public int NextNode;

			public Link(char letter, int nextNode) {
				Letter = letter;
				NextNode = nextNode;
			}
		}

		private final boolean m_Complete;
		private final List<Link> m_Links;

		public Node(boolean complete, int numChildren) {
			m_Complete = complete;
			m_Links = new ArrayList<>(numChildren);
		}

		public void AddLink(char letter, int dest) {
			m_Links.add(new Link(letter, dest));
		}

		public boolean getComplete() {
			return m_Complete;
		}

		public int getNext(char letter) {
			for (Link l : m_Links) {
				if (l.Letter == letter)
					return l.NextNode;
			}
			return -1;
		}
	}

	public class WordSearchResult {

		public boolean IsPerspectivePrefix;
		public boolean IsValidWord;

		public WordSearchResult() {
			IsPerspectivePrefix = false;
			IsValidWord = false;
		}
	}
	
	// </editor-fold>

	// <editor-fold desc="Private members">
	// All nodes of the tree. The root node is the 0th element
	private List<Node> m_Nodes;
	// </editor-fold>

	public WordTree() {
		m_Nodes = null;
	}
	
	public boolean Load(InputStream wordTreeStr, Alphabet alphabet)
		throws IOException, WordTreeException
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

	public WordSearchResult CheckWord(String word) {
		WordSearchResult result = new WordSearchResult();

		// Trace the tree from the root
		int currentNode = 0;
		boolean wordStart = true;
		for (char c : word.toCharArray()) {
			if (currentNode == 0 && !wordStart) {
				// We mustn't visit node 0 except for the very start of the search.
				// If we do visit it here again it means that the prefix up to the
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
