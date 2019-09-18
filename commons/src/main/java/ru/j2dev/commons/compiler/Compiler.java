package ru.j2dev.commons.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.Diagnostic.Kind;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Класс компиляции внешних Java файлов<br> *
 * @author G1ta0
 * @editor JunkyFunky
 */
public class Compiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);
    private static final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private final DiagnosticListener<JavaFileObject> listener = new DefaultDiagnosticListener();
    private final DiagnosticCollector<JavaFileObject> diacol = new DiagnosticCollector<>();
    private final StandardJavaFileManager fileManager = javac.getStandardFileManager(diacol, Locale.getDefault(), StandardCharsets.UTF_8);
    private final MemoryClassLoader memClassLoader = AccessController.doPrivileged((PrivilegedAction<MemoryClassLoader>) MemoryClassLoader::new);
    private final MemoryJavaFileManager memFileManager = new MemoryJavaFileManager(fileManager, memClassLoader);

    public boolean compile(Iterable<? extends File> files) {
        if (javac == null) {
            throw new RuntimeException("Error: server started by JRE instead JDK! Please start server with Java Development Kit.");
        }
        // javac options
        final List<String> options = new ArrayList<>();
        options.add("-Xlint:all");
        options.add("-g");

        final Writer writer = new StringWriter();
        return javac.getTask(writer, memFileManager, listener, options, null, fileManager.getJavaFileObjectsFromFiles(files)).call();

    }

    public MemoryClassLoader getClassLoader() {
        return memClassLoader;
    }

    private static class DefaultDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        @Override
        public void report(final Diagnostic<? extends JavaFileObject> diagnostic) {
            final Kind kind = diagnostic.getKind();
            if (kind == Kind.ERROR) {
                LOGGER.error(diagnostic.getSource().getName() + ((diagnostic.getPosition() == -1L) ? "" : (":" + diagnostic.getLineNumber() + "," + diagnostic.getColumnNumber())) + ": " + diagnostic.getMessage(Locale.getDefault()));
            } else {
                LOGGER.debug(diagnostic.getSource().getName() + (diagnostic.getPosition() == Diagnostic.NOPOS ? "" : ":" + diagnostic.getLineNumber() + ',' + diagnostic.getColumnNumber()) + ": " + diagnostic.getMessage(Locale.getDefault()));
            }
        }
    }
}
