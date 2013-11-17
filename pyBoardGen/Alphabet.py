import random

class Alphabet:
	NO_LETTER = ' '
	NO_LETTER_INDEX = -1

	def __init__(self):
		random.seed()
	
	def Load(self, f):
		from array import array

		arr = array('I')
		arr.fromfile(f, 1)
		version = arr[0]
		if (version > 1):
			raise "Unsupported version " + str(version)
		arr = array('I')
		arr.fromfile(f, 2)
		totLetters, distinctLetters = arr

		arr = array('u')
		arr.fromfile(f, distinctLetters)
		letters = arr
		arr = array('I')
		arr.fromfile(f, distinctLetters)
		freq = arr

		letterFreq = {}
		for i in range(distinctLetters):
			letterFreq[letters[i]] = freq[i]
		
		self.InitValues(totLetters, letterFreq, letters)

	def InitValues(self, totLetters, letterFreq, letterOrder):
		self._values = {}
		self._roulette = []
		self._indexMap = dict([(i, letterOrder[i]) for i in range(len(letterOrder))])
		
		runningLimit = 0
		for let, freq in letterFreq.items():
			relFreq = freq / float(totLetters)

			points = 1
			if relFreq < 0.06:
				points = 2
			if relFreq < 0.03:
				points = 3
			if relFreq < 0.01:
				points = 4
			if relFreq < 0.008:
				points = 5
			if relFreq < 0.004:
				points = 6
			if relFreq < 0.001:
				points = 7
			if relFreq < 0.0004:
				points = 8
			self._values[let] = points

			if points < 3:
				runningLimit += 2 * freq
			else:
				runningLimit += freq
			self._roulette += [(runningLimit, let)]

		self._selectionLimit = runningLimit
	
	def GetSize(self):
		return len(self._values)
	
	def GetLetterByIndex(self, index):
		if not index in self._indexMap:
			return NO_LETTER
		return self._indexMap[index]
	
	def GetIndexOfLetter(self, letter):
		for idx, let in self._indexMap.items():
			if let == letter:
				return idx
		return NO_LETTER_INDEX
	
	def GetRandomLetterUniform(self):
		return self.GetLetterByIndex(random.randrange(len(self._indexMap)))
	
	def GetRandomLetterWeighted(self):
		return self.GetRandomLetterByRoulette(self._roulette, self._selectionLimit)
	
	def GetRandomLetterConditional(self, probMap):
		accumulator = 0
		roulette = []
		for let, val in probMap.items():
			if val == 0:
				continue
			accumulator += val
			roulette += [(accumulator, let)]
		return self.GetRandomLetterByRoulette(roulette, accumulator)

	@staticmethod
	def GetRandomLetterByRoulette(roulette, probDivider):
		pick = random.randrange(probDivider)
		for r in roulette:
			if pick < r[0]:
				return r[1]
		# this shouldn't happen if probDivider is correct
		return roulette[-1][1]
	
	def GetLetterValue(self, let):
		if let not in self._values:
			return 0
		return self._values[let]
	
	def GetWordValue(self, word):
		return sum([self.GetLetterValue(c) for c in word])
	



