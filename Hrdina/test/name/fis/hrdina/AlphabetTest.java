/*
 * Copyright (C) 2013 Filip Simek
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package name.fis.hrdina;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Filip Simek
 */
public class AlphabetTest {
	
	private Alphabet instance;
	private boolean loadResult;
	
	public AlphabetTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp()
	{
		byte[] bytes = new byte[] {
			// File version
			1, 0, 0, 0,
			// total letters = 1024
			0, 4, 0, 0,
			// distinct letters
			3, 0, 0, 0,
			// letter a
			0x61, 0, 0, 0,
			// letter b
			0x62, 0, 0, 0,
			// letter c
			0x63, 0, 0, 0,
			// frequency of a = 865
			0x61, 3, 0, 0,
			// frequency of b = 32
			0x20, 0, 0, 0,
			// frequency of c = 127
			0x7f, 0, 0, 0
		};

		byte[] random = new byte[] {
			0, 2, 4, 14, 20 
		};

		InputStream alphabetStr = new ByteArrayInputStream(bytes);
		instance = new Alphabet(new RandomMock(random));
		try
		{
			loadResult = instance.Load(alphabetStr);
		}
		catch (IOException | WordTreeException e)
		{
			loadResult = false;
		}
	}

	/**
	 * Test of Load method, of class Alphabet.
	 */
	@Test
	public void testLoad() {
		System.out.println("Load");
		
		boolean expResult = true;
		assertEquals(expResult, loadResult);
		assertEquals(3, instance.GetSize());
		assertEquals('a', instance.GetLetterByIndex(0));
		// The other tests will check the rest
	}

	/**
	 * Test of GetSize method, of class Alphabet.
	 */
	@Test
	public void testGetSize() {
		System.out.println("GetSize");
		int expResult = 3;
		int result = instance.GetSize();
		assertEquals(expResult, result);
	}

	/**
	 * Test of GetLetterByIndex method, of class Alphabet.
	 */
	@Test
	public void testGetLetterByIndex() {
		System.out.println("GetLetterByIndex");
		assertEquals('a', instance.GetLetterByIndex(0));
		assertEquals('b', instance.GetLetterByIndex(1));
		assertEquals('c', instance.GetLetterByIndex(2));
		assertEquals(Alphabet.NO_LETTER, instance.GetLetterByIndex(3));
	}

	/**
	 * Test of GetIndexOfLetter method, of class Alphabet.
	 */
	@Test
	public void testGetIndexOfLetter() {
		System.out.println("GetIndexOfLetter");
		assertEquals(0, instance.GetIndexOfLetter('a'));
		assertEquals(1, instance.GetIndexOfLetter('b'));
		assertEquals(2, instance.GetIndexOfLetter('c'));
		assertEquals(Alphabet.NO_LETTER_INDEX, instance.GetIndexOfLetter('d'));
	}

	/**
	 * Test of GetRandomLetterUniform method, of class Alphabet.
	 */
	@Test
	public void testGetRandomLetterUniform() {
		System.out.println("GetRandomLetterUniform");
		assertEquals('a', instance.GetRandomLetterUniform());
		assertEquals('c', instance.GetRandomLetterUniform());
		assertEquals('b', instance.GetRandomLetterUniform());
	}

	/**
	 * Test of GetRandomLetterWeighted method, of class Alphabet.
	 */
	@Test
	public void testGetRandomLetterWeighted() {
		System.out.println("GetRandomLetterWeighted");
		assertEquals('a', instance.GetRandomLetterWeighted());
		assertEquals('a', instance.GetRandomLetterWeighted());
		assertEquals('a', instance.GetRandomLetterWeighted());
	}

	/**
	 * Test of GetRandomLetterConditional method, of class Alphabet.
	 */
	@Test
	public void testGetRandomLetterConditional() {
		System.out.println("GetRandomLetterConditional");
		TreeMap<Character, Integer> map = new TreeMap<>();
		map.put('a', 8);
		map.put('b', 7);
		map.put('c', 2);
		assertEquals('a', instance.GetRandomLetterConditional(map));
		assertEquals('a', instance.GetRandomLetterConditional(map));
		assertEquals('a', instance.GetRandomLetterConditional(map));
		assertEquals('b', instance.GetRandomLetterConditional(map));
		assertEquals('a', instance.GetRandomLetterConditional(map));
	}

	/**
	 * Test of GetLetterValue method, of class Alphabet.
	 */
	@Test
	public void testGetLetterValue() {
		System.out.println("GetLetterValue");
		assertEquals(1, instance.GetLetterValue('a'));
		assertEquals(2, instance.GetLetterValue('b'));
		assertEquals(1, instance.GetLetterValue('c'));
	}

	/**
	 * Test of GetWordValue method, of class Alphabet.
	 */
	@Test
	public void testGetWordValue() {
		System.out.println("GetWordValue");
		assertEquals(6, instance.GetWordValue("baba"));
		assertEquals(6, instance.GetWordValue("abba"));
		assertEquals(3, instance.GetWordValue("aaa"));
		assertEquals(4, instance.GetWordValue("bac"));
	}
}
