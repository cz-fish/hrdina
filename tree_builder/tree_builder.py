#!/usr/bin/env python3

import sys

LETTER = 0
NEXT = 1

COMPLETE = 1
NOT_COMPLETE = 0

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


def dump_tree_python(tree, frequencies, total_letters, f):
	"""Prints the results to the output file in python dictionary/list format.
	Nothing clever here."""
	print(str(total_letters), file=f)
	print(str(frequencies), file=f)
	print(str(tree), file=f)


def dump_tree_binary(tree, frequencies, total_letters, f):
	"""Stores the tree in a compact binary format"""
	from array import array

	distinct_letters = len(frequencies)
	if distinct_letters > 128:
		raise ValueError("Too many distinct letters ({}). At most 128 are supported".format(distinct_letters))
	if len(tree) > 2**24:
		raise ValueError("Too many tree nodes ({}). At most 2^24 are supported".format(len(tree)))

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

	array('B', pointers).tofile(f)
	array('B', nodes).tofile(f)


def main():
	if len(sys.argv) < 2:
		print("Usage: {} <input_file> [<text_output_file> [<binary_output_file>]]".format(sys.argv[0]), file=sys.stderr)
		return
	
	input_file = sys.argv[1]
	text_output_file = None
	binary_output_file = None
	if len(sys.argv) > 2:
		text_output_file = sys.argv[2]
	if len(sys.argv) > 3:
		binary_output_file = sys.argv[3]
	
	source = open(sys.argv[1])
	tree = [[NOT_COMPLETE]]
	frequencies = {}
	counter = 0
	for line in source:
		if counter % 1000 == 0:
			print("\rWords processed:", counter, file=sys.stderr, end='')
		counter += 1
		line = line.strip()
		if len(line) < 3:
			continue
		add_word(tree, frequencies, line)
	total_letters = sum(frequencies.values())
	print("\rWords processed:", counter, file=sys.stderr)
	print("Tree nodes:", len(tree), file=sys.stderr)
	
	print("Now sorting", file=sys.stderr)
	tree = tidy_tree(tree)
	print("Tree nodes:", len(tree), file=sys.stderr)

	if text_output_file == None or text_output_file == '-':
		if binary_output_file != None:
			print("Writing data to standard output")
			dump_tree_python(tree, frequencies, total_letters, sys.stdout)
		else:
			print("Skipping text output")
	else:
		print("Writing data to text file", text_output_file)
		f = open(text_output_file, 'wt')
		dump_tree_python(tree, frequencies, total_letters, f)
		f.close()
	
	if binary_output_file != None:
		print("Writing data to binary file", binary_output_file)
		f = open(binary_output_file, 'wb')
		dump_tree_binary(tree, frequencies, total_letters, f)
		f.close()


if __name__=='__main__':
	main()

