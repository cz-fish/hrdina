package name.fis.hrdina;

import java.io.IOException;
import java.io.InputStream;

/**
 * Miscellaneous utilities
 * @author Filip Simek <filip@fis.name>
 */
public class Util {
	/**
	 * Reads a little-endian encoded 32bit integer from a stream
	 * @param fstr Stream to read from
	 * @return Integer decoded from the stream
	 * @throws IOException In case there are not enough bytes in the input stream
	 */
	public static int ReadLEInt(InputStream fstr) throws IOException {
		byte[] buf = new byte[4];
		fstr.read(buf);
		return (int) buf[0] + ((int) buf[1] << 8) + ((int) buf[2] << 16) + ((int) buf[3] << 24);
	}
}
