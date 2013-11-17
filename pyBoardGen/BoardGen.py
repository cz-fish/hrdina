#!/usr/bin/env python3

from Alphabet import Alphabet
from WordTree import WordTree
from Digraphs import Digraphs
from GameBoard import GameBoard
from GameBoard import SIZE
from WeightedProbBoard import WeightedProbBoard
from TwoWordBoard import TwoWordBoard
import sys

ALPHA_FILE = 'data/alphabet.bin'
WORDTREE_FILE = 'data/wordtree.bin'
DIGRAPH_FILE = 'data/digraph.bin'

class BoardGen:
	def Load(self):
		print("Loading alphabet")
		sys.stdout.flush()
		alphaFile = open(ALPHA_FILE, 'rb')
		self._alpha = Alphabet()
		self._alpha.Load(alphaFile)
		alphaFile.close()

		print("Loading wordtree")
		sys.stdout.flush()
		treeFile = open(WORDTREE_FILE, 'rb')
		self._tree = WordTree()
		self._tree.Load(treeFile, self._alpha)
		treeFile.close()

		print("Loading digraphs")
		sys.stdout.flush()
		digraphFile = open(DIGRAPH_FILE, 'rb')
		self._digraph = Digraphs()
		self._digraph.Load(digraphFile, self._alpha)
		digraphFile.close()

		print("Loaded")
		sys.stdout.flush()

	def GenerateBoard(self):
		#board = WeightedProbBoard.GenerateBoard(SIZE, self._alpha, self._tree, self._digraph)
		board = TwoWordBoard.GenerateBoard(SIZE, self._alpha, self._tree, self._digraph)
		return board

	def TestBoard(self, board):
		board.Dump()
		sol = board.Words
		solList = [s for s in sol]
		solList.sort()
		print("-- {} solutions ----".format(len(solList)))
		for s in solList:
			print(" {} ({} pts)".format(s, self._alpha.GetWordValue(s)))

		coordLists = board.LetterChains
		print('-- {} lists ----'.format(len(coordLists)))
		brdLetters = board.Board
		for l in coordLists:
			word = ''
			for pos in l:
				word += brdLetters[pos]
			print('{} {}'.format(str(l), word))


if __name__ == '__main__':
	bg = BoardGen()
	bg.Load()
	board = bg.GenerateBoard()
	bg.TestBoard(board)

