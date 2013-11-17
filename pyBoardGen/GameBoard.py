from Alphabet import Alphabet

# board edge size
SIZE = 4

class GameBoard:
	def __init__(self, boardLetters, alphabet, wordTree):
		self.Board = boardLetters
		self._alphabet = alphabet
		self._wordTree = wordTree

		self.SolveBoard()
	
	def SolveBoard(self):
		self.Words = set()
		self.LetterChains = []
		for i in range(SIZE * SIZE):
			visited = [False] * SIZE * SIZE
			visited[i] = True
			self.ContinueSolvingFrom(i, self.Board[i], visited, [i])

		self.Utilization = [0] * SIZE * SIZE
		for chain in self.LetterChains:
			for pos in chain:
				self.Utilization[pos] += 1
	
	def ContinueSolvingFrom(self, position, wordSoFar, visited, coordChain):
		result = set()
		isPrefix, isWord = self._wordTree.CheckWord(wordSoFar)
		if isWord:
			# only include the word the first time it is found
			if wordSoFar not in self.Words:
				self.Words.add(wordSoFar)
				self.LetterChains += [coordChain]

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
				if (not self.IsNeighborValid(position, i)) or visited[i] or self.Board[i] == Alphabet.NO_LETTER:
					continue
				visited[i] = True
				self.ContinueSolvingFrom(i, wordSoFar + self.Board[i], visited, coordChain + [i])
				visited[i] = False
	
	def IsNeighborValid(self, position, neighbor):
		if neighbor < 0 or neighbor >= SIZE * SIZE:
			return False
		px = position % SIZE
		py = position // SIZE
		nx = neighbor % SIZE
		ny = neighbor // SIZE
		if abs(px-nx) > 1 or abs(py-ny) > 1:
			return False

		return True

	##
	def Dump(self):
		for i in range(0, SIZE * SIZE, SIZE):
			for j in range(SIZE):
				print('{} {}; '.format(
					self.Board[i+j],
					self._alphabet.GetLetterValue(self.Board[i+j])
					), end='')
			print('')

		print ("-------")
		for i in range(0, SIZE * SIZE, SIZE):
			for j in range(SIZE):
				print('{:2} '.format(self.Utilization[i+j]), end='')
			print('')

