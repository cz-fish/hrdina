class Node:
	def __init__(self, complete):
		self.Complete = complete
		self.Links = []
	
	def AddLink(self, let, node):
		self.Links += [(let, node)]

	def GetNext(self, letter):
		for l in self.Links:
			if l[0] == letter:
				return l[1]
		return -1

class WordTree:
	def __init__(self):
		pass
	
	def Load(self, f, alphabet):
		from array import array
		arr = array('I')
		arr.fromfile(f, 2)
		counters = arr
		arr = array('I')
		arr.fromfile(f, counters[0])
		rawPointers = arr
		arr = array('I')
		arr.fromfile(f, counters[1])
		rawNodes = arr
		self._nodes = []

		for i in range(counters[0]):
			numChildren = rawPointers[i] & 127
			completeFlag = rawPointers[i] & 128 == 128
			nodePointer = rawPointers[i] >> 8
			node = Node(completeFlag)
			for j in range(numChildren):
				childData = rawNodes[nodePointer - counters[0] + j]
				letterIndex = childData & 127
				letter = alphabet.GetLetterByIndex(letterIndex)
				dest = childData >> 8
				node.AddLink(letter, dest)
			self._nodes += [node]
	
	def CheckWord(self, word):
		"""Returns tuple: (perspective prefix, valid word)"""
		currentNode = 0
		wordStart = True
		for c in word:
			if currentNode == 0 and not wordStart:
				return (False, False)
			wordStart = False

			link = self._nodes[currentNode].GetNext(c)
			if link == -1:
				return (False, False)
			currentNode = link

		if currentNode == 0:
			return (False, True)
		elif self._nodes[currentNode].Complete:
			return (True, True)
		else:
			return (True, False)


