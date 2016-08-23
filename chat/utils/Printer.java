package chat.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Printer {
	public static String getStackTrace(Throwable th) {
		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);
		th.printStackTrace(pw);

		return out.toString();
	}
}
