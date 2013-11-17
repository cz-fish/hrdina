#!/usr/bin/env python3

from Alphabet import Alphabet
from WordTree import WordTree
from GameBoard import GameBoard
from WeightedProbBoard import WeightedProbBoard
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
		print("Loaded")
		sys.stdout.flush()

	def GenerateBoard(self):
		boardGen = WeightedProbBoard(self._alpha)
		board = GameBoard()
		board.Init(self._alpha, self._tree, boardGen)
		return board

	def TestBoard(self, board):
		board.SolveBoard()
		board.Dump()
		sol = board.GetWords()
		solList = [s for s in sol]
		solList.sort()
		print("-- {} solutions ----".format(len(solList)))
		for s in solList:
			print(" {} ({} pts)".format(s, self._alpha.GetWordValue(s)))

		coordLists = board.GetCoordLists()
		print('-- {} lists ----'.format(len(coordLists)))
		brdLetters = board.GetBoard()
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

