#!/usr/bin/env python3
# Dumps the digraph value matrix as a CSV file for visual check of the values

import sys
sys.path.append('../')
from Alphabet import Alphabet
from Digraphs import Digraphs

ALPHA_FILE = '../data/alphabet.bin'
DIGRAPH_FILE = '../data/digraph.bin'

if __name__=='__main__':
	print("Loading alphabet", file=sys.stderr)
	sys.stderr.flush()
	alphaFile = open(ALPHA_FILE, 'rb')
	alpha = Alphabet()
	alpha.Load(alphaFile)
	alphaFile.close()

	print("Loading digraphs", file=sys.stderr)
	sys.stderr.flush()
	digraphFile = open(DIGRAPH_FILE, 'rb')
	digraph = Digraphs()
	digraph.Load(digraphFile, alpha)
	digraphFile.close()

	letters = [alpha.GetLetterByIndex(i) for i in range(alpha.GetSize())]
	print(','.join([''] + letters), file=sys.stdout)
	for l in letters:
		line = [l]
		dig = digraph.GetDigraphsForLetter(l)
		for o in letters:
			line += [str(dig[o])]
		print(','.join(line), file=sys.stdout)

	

