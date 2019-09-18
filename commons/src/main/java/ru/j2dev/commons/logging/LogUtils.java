package ru.j2dev.commons.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {
    public static String dumpStack() {
        return dumpStack(new Throwable());
    }

    public static String dumpStack(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
