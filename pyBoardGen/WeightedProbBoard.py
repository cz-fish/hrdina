from GameBoard import GameBoard

class WeightedProbBoard:
	@staticmethod
	def GenerateBoard(edgeSize, alphabet, wordTree, digraphs):
		boardSize = edgeSize * edgeSize
		result = []
		lettersUsed = {}
		for i in range(boardSize):
			letter = alphabet.GetRandomLetterWeighted()

			# prevent any letter from being used more than twice on a board to improve diversity
			while letter in lettersUsed and lettersUsed[letter] >= 2:
				letter = alphabet.GetRandomLetterWeighted()

			if letter in lettersUsed:
				lettersUsed[letter] += 1
			else:
				lettersUsed[letter] = 1
			result += [letter]

		return GameBoard(result, alphabet, wordTree)

