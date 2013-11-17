from Alphabet import Alphabet

class Digraphs:
	def __init__(self):
		pass
	
	def Load(self, f, alphabet):
		from array import array
		self._alphabet = alphabet
		self._alphabetSize = alphabet.GetSize()
		triangleSize = (self._alphabetSize * self._alphabetSize + self._alphabetSize) // 2
		arr = array('I')
		arr.fromfile(f, triangleSize)
		digraphValues = arr

		self._digraphMatrix = [0] * self._alphabetSize * self._alphabetSize
		pos = 0
		for i in range(self._alphabetSize):
			for j in range(i+1):
				self._digraphMatrix[i * self._alphabetSize + j] = digraphValues[pos]
				self._digraphMatrix[j * self._alphabetSize + i] = digraphValues[pos]
				pos += 1
	
	def GetDigraphsForLetter(self, letter):
		result = {}
		letterIndex = self._alphabet.GetIndexOfLetter(letter)
		if letterIndex != Alphabet.NO_LETTER_INDEX:
			i = letterIndex * self._alphabetSize
			for j in range(self._alphabetSize):
				result[self._alphabet.GetLetterByIndex(j)] = self._digraphMatrix[i+j]
		return result

