class WeightedProbBoard:
	def __init__(self, alphabet):
		self._alphabet = alphabet
	
	def GenerateBoard(self, edgeSize):
		boardSize = edgeSize * edgeSize
		result = []
		for i in range(boardSize):
			result += [self._alphabet.GetRandomLetterWeighted()]
		return result

