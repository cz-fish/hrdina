/*
 * Copyright (C) 2013 Filip Simek <filip@fis.name>
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

import java.util.Random;

/**
 * Subclasses Random to return deterministic values instead of random
 * @author Filip Simek <filip@fis.name>
 */
public class RandomMock extends Random {
	private final byte [] m_Retvals;
	private int m_Position;
	public RandomMock(byte[] retVals)
	{
		m_Retvals = retVals;
		m_Position = 0;
	}
	
	@Override
	protected int next(int nbits)
	{
		byte ret = m_Retvals[m_Position];
		m_Position++;
		if (m_Position == m_Retvals.length)
			m_Position = 0;
		return ret;
	}
}
