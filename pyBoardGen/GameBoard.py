from Alphabet import Alphabet

# board edge size
SIZE = 4

class GameBoard:
	def __init__(self):
		self._board = [Alphabet.NO_LETTER] * SIZE * SIZE
	
	def Init(self, alphabet, wordTree, boardGenerator):
		self._alphabet = alphabet
		self._wordTree = wordTree
		self._board = boardGenerator.GenerateBoard(SIZE)
	
	def SolveBoard(self):
		words = set()
		for i in range(SIZE * SIZE):
			visited = [False] * SIZE * SIZE
			visited[i] = True
			words |= self.ContinueSolvingFrom(i, self._board[i], visited)
		return words
	
	def ContinueSolvingFrom(self, position, wordSoFar, visited):
		result = set()
		isPrefix, isWord = self._wordTree.CheckWord(wordSoFar)
		if isWord:
			result.add(wordSoFar)

		if isPrefix:
			neighbors = [
				position - SIZE - 1,
				position - SIZE,
				position - SIZE + 1,
				position - 1,
				position + 1,
				position + SIZE - 1,
				position + SIZE,
				position + SIZE + 1
			]

			for i in neighbors:
				if (not self.IsNeighborValid(position, i)) or visited[i]:
					continue
				visited[i] = True
				result |= self.ContinueSolvingFrom(i, wordSoFar + self._board[i], visited)
				visited[i] = False

		return result
	
	def IsNeighborValid(self, position, neighbor):
		if neighbor < 0 or neighbor >= SIZE * SIZE:
			return False
		px = position % SIZE
		py = position / SIZE
		nx = neighbor % SIZE
		ny = neighbor / SIZE
		if abs(px-nx) > 1 or abs(py-ny) > 1:
			return False

		return True

	##
	def Dump(self):
		for i in range(0, SIZE * SIZE, SIZE):
			for j in range(SIZE):
				print('{} {}; '.format(
					self._board[i+j],
					self._alphabet.GetLetterValue(self._board[i+j])
					), end='')
			print('')
