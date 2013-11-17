from GameBoard import GameBoard
from WeightedProbBoard import WeightedProbBoard
import random

class TwoWordBoard:
	@staticmethod
	def GenerateBoard(edgeSize, alphabet, wordTree, digraphs):
		boardSize = edgeSize * edgeSize

		board = WeightedProbBoard.GenerateBoard(edgeSize, alphabet, wordTree, digraphs)
		utilization = board.Utilization
		letterChains = board.LetterChains
		
		letters = [alphabet.GetLetterByIndex(i) for i in range(alphabet.GetSize())]

		# For each letter that doesn't belong to at least two words, try to replace it with
		# another letter and see if the board improves.
		# This definitely isn't a safe algorithm. It doesn't guarantee that each letter will
		# in the end belong to two words. Changing the letter may also break (at most one) word
		# that the letter was already participating in and therefore the change may make some
		# other letter to not meet the two word condition because of the broken letter.
		# Well, let's try and see what we get...
		for i in range(boardSize):
			if utilization[i] >= 2:
				continue

			# randomize the available alphabet so that the letters from the beginning are not favorized
			random.shuffle(letters)
			for l in letters:
				if l == board.Board[i]:
					continue
				newBoardMatrix = board.Board
				newBoardMatrix[i] = l
				newBoard = GameBoard(newBoardMatrix, alphabet, wordTree)

				if newBoard.Utilization[i] >= 2 and len(newBoard.Words) > len(board.Words):
					board = newBoard
					break

		return board


