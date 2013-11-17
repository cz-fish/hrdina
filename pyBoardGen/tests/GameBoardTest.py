#!/usr/bin/env python3

import unittest
import sys
sys.path.append('../')
from GameBoard import GameBoard
from GameBoard import SIZE

class GameBoardTest(unittest.TestCase):
	def test_IsNeighborValid(self):
		brd = GameBoard()
		self.assertTrue(brd.IsNeighborValid(0, 1))
		self.assertTrue(brd.IsNeighborValid(1, 0))
		self.assertTrue(brd.IsNeighborValid(0, SIZE))
		self.assertTrue(brd.IsNeighborValid(SIZE, 0))
		self.assertTrue(brd.IsNeighborValid(0, SIZE + 1))
		self.assertFalse(brd.IsNeighborValid(0, -1))
		self.assertFalse(brd.IsNeighborValid(SIZE - 1, SIZE))
		self.assertTrue(brd.IsNeighborValid(SIZE - 1, 2 * SIZE - 2))
		self.assertFalse(brd.IsNeighborValid(0, 2))
		self.assertFalse(brd.IsNeighborValid(SIZE*SIZE, SIZE*SIZE-1))
		self.assertTrue(brd.IsNeighborValid(SIZE*SIZE-1, SIZE*SIZE-2))

if __name__=='__main__':
	unittest.main()

