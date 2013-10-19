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


def dump_tree(tree, frequencies):
	"""Prints the results to the output in python dictionary/list format.
	Nothing clever here."""
	print(str(frequencies))
	print(str(tree))


def main():
	if len(sys.argv) < 2:
		print("Usage: {} <input_file>".format(sys.argv[0]), file=sys.stderr)
		return
	
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
	print("\rWords processed:", counter, file=sys.stderr)
	print("Tree nodes:", len(tree), file=sys.stderr)
	print("Now sorting", file=sys.stderr)
	tree = tidy_tree(tree)
	print("Tree nodes:", len(tree), file=sys.stderr)
	dump_tree(tree, frequencies)


if __name__=='__main__':
	main()

