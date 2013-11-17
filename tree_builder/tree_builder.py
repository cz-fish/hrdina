#!/usr/bin/env python3

import sys
from array import array

LETTER = 0
NEXT = 1

COMPLETE = 1
NOT_COMPLETE = 0

#### Word tree ###########################
def add_word(tree, frequencies, word):
	"""Adds a single word to the tree and increments letter counters in the
	frequencies dictionary. The last node of the word will have the complete
	flag set to COMPLETE. The complete flag is the first item of each node's list
	of successors (that are represented as (letter, next node index) tuples)."""

	node = 0
	for letter in word:
		if letter in frequencies:
			frequencies[letter] += 1
		else:
			frequencies[letter] = 1

		# Starting index 1: skips the complete flag, which is the first item
		for link in tree[node][1:]:
			if link[LETTER] == letter:
				# Move to the next node according to the current letter
				node = link[NEXT]
				break
		else:
			# The current letter is new in the current context. Add a new node,
			# point to it from the current context, move to the new node and
			# mark it as not complete
			tree[node] += [(letter, len(tree))]
			node = len(tree)
			tree += [[NOT_COMPLETE]]
	# Mark the last visited node as complete word
	tree[node][0] = COMPLETE


def tidy_tree(tree):
	"""Creates a new tree from the one provided as parameter. The new tree will
	have all letters (tuples) in each node sorted alphabetically. Also, for the
	sake of compactness, there will be no empty nodes that just represent complete
	words. Instead, the links to empty nodes will be replaced by links to node 0.
	Therefore, link to 0 is a special case, not a loop.
	Note that the complete flags in nodes are still necessary, because one word
	could be a prefix of another, so not all complete words will point to node 0,
	some will still point to different nodes with the complete flag set."""
	
	# Make sure the tree is non-empty
	if len(tree) <= 1:
		return tree

	next_node = 1
	# Maps old node numbers to new node numbers. The 0th node will always remain
	map_forward = {0: 0}

	# Go through all nodes for the first time and assign new numbers to nodes, skipping
	# the empty ones
	for node in tree:
		if len(node) == 1:
			# This is an empty node (it only contains complete flag)
			continue
		for link in node[1:]:
			# Visit the link to the next node. If it is an empty node, remap it to 0.
			# Otherwise remap it ot the next free node index.
			if (len(tree[link[NEXT]]) == 1):
				map_forward[link[NEXT]] = 0
			else:
				map_forward[link[NEXT]] = next_node
				next_node += 1
	
	# Now we know how big the result tree will be
	new_tree = [[]] * next_node

	# Go through the tree once again
	for i in range(len(tree)):
		if i > 0 and map_forward[i] == 0:
			# This is an empty node
			continue
		# Copy letters from the i-th node of the old tree, but remap the links to new values
		successors = [(link[LETTER], map_forward[link[NEXT]]) for link in tree[i][1:]]
		successors.sort()
		new_tree[map_forward[i]] = [tree[i][0]] + successors
	
	return new_tree


### Digraphs #################################
def add_digraph(digraphs, c1, c2):
	if c1 not in digraphs:
		digraphs[c1] = {c2: 1}
	else:
		if c2 not in digraphs[c1]:
			digraphs[c1][c2] = 1
		else:
			digraphs[c1][c2] += 1


### Text Output ##############################
def dump_text_alphabet(total_letters, frequencies, f):
	print(str(total_letters), file=f)
	print(str(frequencies), file=f)

def dump_text_tree(tree, f):
	print(str(tree), file=f)

def dump_text_digraphs(digraphs, f):
	print(str(digraphs), file=f)


### Binary Output ############################
def dump_binary_alphabet(total_letters, frequencies, f):
	distinct_letters = len(frequencies)
	if distinct_letters > 128:
		raise ValueError("Too many distinct letters ({}). At most 128 are supported".format(distinct_letters))

	# File version and statistics
	array('I', [1]).tofile(f)
	array('I', [total_letters, distinct_letters]).tofile(f)

	# Alphabet and letter frequencies
	array('u', list(frequencies.keys())).tofile(f)
	array('I', list(frequencies.values())).tofile(f)
	letter_map = {}
	i = 0
	for letter in frequencies.keys():
		letter_map[letter] = i
		i += 1
	
	return letter_map

def dump_binary_tree(tree, letter_map, f):
	if len(tree) > 2**24:
		raise ValueError("Too many tree nodes ({}). At most 2^24 are supported".format(len(tree)))

	# Create lists of nodes. First list contains node header, size and offset.
	# Second list contains all links of all nodes in a consecutive order.
	rel_pointers = []
	nodes = []
	next_node = 0
	for node in tree:
		rel_pointers += [(len(node)-1, node[0], next_node)]
		for link in node[1:]:
			nodes += [letter_map[link[LETTER]], link[NEXT] & 255, (link[NEXT] >> 8) & 255, (link[NEXT] >> 16) & 255]
			next_node += 1
	offset = len(rel_pointers)
	pointers = []
	for p in rel_pointers:
		abs_address = p[2] + offset
		pointers += [p[0] | (p[1] << 7), abs_address & 255, (abs_address >> 8) & 255, (abs_address >> 16) & 255]

	array('I', [len(pointers)>>2, len(nodes)>>2]).tofile(f)
	array('B', pointers).tofile(f)
	array('B', nodes).tofile(f)

def dump_binary_digraphs(digraphs, letter_map, f):
	# The frequencies of digraphs form a symmetric matrix NxN (where N is the number
	# of distinct letters). It is sufficient to store just lower triangle.
	inverse_lettermap = [(p[1],p[0]) for p in letter_map.items()]
	inverse_lettermap.sort()
	triangle = []
	for first in inverse_lettermap:
		for second in inverse_lettermap:
			if second[0] > first[0]:
				break
			if second[1] not in digraphs[first[1]]:
				triangle += [0]
			else:
				triangle += [digraphs[first[1]][second[1]]]
	array('I', triangle).tofile(f)
	

##############################################
def open_file(name, text_output):
	if name == '-':
		return sys.stdout
	print("Creating {}".format(name), end='', file=sys.stderr)
	if text_output:
		return open(name, 'wt')
	else:
		return open(name, 'wb')

def close_file(name, f):
	if name == '-':
		return
	print(" ... done", file=sys.stderr)
	f.close()

def main():
	if len(sys.argv) < 5:
		print("Usage: {} [-T] <dict_file> <alphabet_file> <wordtree_file> <digraph_file>".format(sys.argv[0]), file=sys.stderr)
		print("   -T [optional]          Produce text output instead of binary", file=sys.stderr)
		print("   <dict_file> [in]       Dictionary of words (one per line) to be processed", file=sys.stderr)
		print("   <alphabet_file> [out]  Output file for alphabet statistics", file=sys.stderr)
		print("   <wordtree_file> [out]  Output file for wordtree", file=sys.stderr)
		print("   <digraph_file> [out]   Output file for digraph statistics", file=sys.stderr)
		return
	
	text_output = False
	if (sys.argv[1] == '-T'):
		text_output = True
		file_args = sys.argv[2:6]
	else:
		file_args = sys.argv[1:5]

	input_file, alphabet_file, wordtree_file, digraph_file = file_args
	
	source = open(input_file)
	tree = [[NOT_COMPLETE]]
	frequencies = {}
	digraphs = {}
	counter = 0
	for line in source:
		if counter % 1000 == 0:
			print("\rWords processed:", counter, file=sys.stderr, end='')
		counter += 1
		# sanitize line
		line = line.strip()
		if len(line) < 3:
			continue

		# add word to word tree
		add_word(tree, frequencies, line)

		# add word to digraph counter
		for i in range(len(line) - 1):
			c1 = line[i]
			c2 = line[i+1]
			add_digraph(digraphs, c1, c2)
			add_digraph(digraphs, c2, c1)

	total_letters = sum(frequencies.values())
	print("\rWords processed:", counter, file=sys.stderr)
	print("Tree nodes:", len(tree), file=sys.stderr)
	
	print("Now sorting", file=sys.stderr)
	tree = tidy_tree(tree)
	print("Tree nodes:", len(tree), file=sys.stderr)

	if text_output:
		f = open_file(alphabet_file, text_output)
		dump_text_alphabet(total_letters, frequencies, f)
		close_file(alphabet_file, f)
		
		f = open_file(wordtree_file, text_output)
		dump_text_tree(tree, f)
		close_file(wordtree_file, f)
		
		f = open_file(digraph_file, text_output)
		dump_text_digraphs(digraphs, f)
		close_file(digraph_file, f)
	else:
		f = open_file(alphabet_file, text_output)
		letter_map = dump_binary_alphabet(total_letters, frequencies, f)
		close_file(alphabet_file, f)

		f = open_file(wordtree_file, text_output)
		dump_binary_tree(tree, letter_map, f)
		close_file(wordtree_file, f)

		f = open_file(digraph_file, text_output)
		dump_binary_digraphs(digraphs, letter_map, f)
		close_file(digraph_file, f)


if __name__=='__main__':
	main()

