package name.fis.hrdina;

import java.io.IOException;
import java.io.InputStream;

public class Util {
	public static int ReadLEInt(InputStream fstr) throws IOException {
		byte[] buf = new byte[4];
		fstr.read(buf);
		return (int) buf[0] + ((int) buf[1] << 8) + ((int) buf[2] << 16) + ((int) buf[3] << 24);
	}
}
