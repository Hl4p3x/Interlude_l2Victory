package ru.j2dev.gameserver.scripts;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.compiler.Compiler;
import ru.j2dev.commons.compiler.MemoryClassLoader;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.bypass.Bypass;
import ru.j2dev.gameserver.handler.bypass.BypassHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.handler.npcdialog.NpcDialogAppenderHolder;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.script.OnLoadScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.IntStream;

public class Scripts {
    public static final Map<String, ScriptClassAndMethod> onAction = new HashMap<>();
    public static final Map<String, ScriptClassAndMethod> onActionShift = new HashMap<>();
    public static final String INNERCLASS_SEPARATOR = String.valueOf('$');
    private static final Logger LOGGER = LoggerFactory.getLogger(Scripts.class);
    private static final Scripts _instance = new Scripts();
    private final ScriptListenerImpl _listeners = new ScriptListenerImpl();
    private final Compiler compiler = new Compiler();
    private final Map<String, Class<?>> _classes = new TreeMap<>();

    private Scripts() {
        load();
        loadAddons();
    }

    public static Scripts getInstance() {
        return _instance;
    }

    private void load() {
        LOGGER.info("Scripts: Loading compiled scripts...");
        final List<Class<?>> classes = new ArrayList<>();
        boolean result = false;
        final File f = new File("../lib/scripts.jar");
        if (f.exists()) {
            JarInputStream stream = null;
            final MemoryClassLoader classLoader = new MemoryClassLoader();
            try {
                stream = new JarInputStream(new FileInputStream(f));
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    //Вложенные класс
                    if (entry.getName().contains(INNERCLASS_SEPARATOR) || !entry.getName().endsWith(".class")) {
                        continue;
                    }
                    final String name = entry.getName().replace(".class", "").replace("/", ".");
                    final Class<?> clazz = classLoader.loadClass(name);
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    classes.add(clazz);
                }
                result = true;
            } catch (Exception e) {
                LOGGER.error("Fail to load scripts.jar!", e);
                classes.clear();
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        if (!result) {
            result = load(classes, "");
        }
        if (!result) {
            LOGGER.error("Scripts: Failed loading scripts!");
            Runtime.getRuntime().exit(0);
            return;
        }
        LOGGER.info("Scripts: Loaded " + classes.size() + " classes.");
        classes.forEach(clazz2 -> _classes.put(clazz2.getName(), clazz2));
    }

    private void loadAddons() {
        LOGGER.info("Addons: Loading addons...");
        final List<Class<?>> classes = new ArrayList<>();
        boolean result = false;
        final File[] listFiles = new File("../lib/").listFiles(pathname -> pathname.getName().endsWith(".add.jar"));
        for (final File extFile : Objects.requireNonNull(listFiles)) {
            if (extFile.exists()) {
                JarInputStream stream = null;
                final MemoryClassLoader classLoader = new MemoryClassLoader();
                try {
                    stream = new JarInputStream(new FileInputStream(extFile));
                    JarEntry entry;
                    while ((entry = stream.getNextJarEntry()) != null) {
                        if (!entry.getName().startsWith("java/") && !entry.getName().startsWith("ru/j2dev/authserver") && !entry.getName().startsWith("ru/j2dev/commons") && !entry.getName().startsWith("ru/j2dev/gameserver")) {
                            if (entry.getName().contains(INNERCLASS_SEPARATOR) || !entry.getName().endsWith(".class")) {
                                continue;
                            }
                            final String name = entry.getName().replace(".class", "").replace("/", ".");
                            final Class<?> clazz = classLoader.loadClass(name);
                            if (Modifier.isAbstract(clazz.getModifiers())) {
                                continue;
                            }
                            classes.add(clazz);
                        }
                    }
                    result = true;
                } catch (Exception e) {
                    LOGGER.error("Failed to load \"" + extFile + "\"!", e);
                    classes.clear();
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }
        if (!result) {
            result = load(classes, "");
        }
        LOGGER.info("Addons: Loaded " + classes.size() + " addon classes.");
        classes.forEach(clazz2 -> _classes.put(clazz2.getName(), clazz2));
    }


    /**
     * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
     */
    public void init() {
        _listeners.load();
        _classes.values().forEach(clazz -> {
            initClass(clazz);
            addHandlers(clazz);
        });
        _listeners.init();
    }

    private Object initClass(final Class<?> clazz) {
        Object o = null;
        if (OnLoadScriptListener.class.isAssignableFrom(clazz)) {
            return null;
        }

        try {
            if (OnInitScriptListener.class.isAssignableFrom(clazz)) {
                o = clazz.newInstance();
                _listeners.add((OnInitScriptListener) o);
            }
            if (INpcDialogAppender.class.isAssignableFrom(clazz)) {
                if (o == null) {
                    o = clazz.newInstance();
                }
                NpcDialogAppenderHolder.getInstance().register((INpcDialogAppender) o);
            }
            for (final Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Bypass.class)) {
                    final Bypass an = method.getAnnotation(Bypass.class);
                    if (o == null) {
                        o = clazz.newInstance();
                    }
                    final Class[] par = method.getParameterTypes();
                    if (par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class) {
                        LOGGER.error("Wrong parameters for bypass method: " + method.getName() + ", class: " + clazz.getSimpleName());
                        continue;
                    }

                    BypassHolder.getInstance().registerBypass(an.value(), o, method);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Can't initiate {} class {}", clazz.getName(), e);
            e.printStackTrace();
        }
        return o;
    }

    /**
     * Перезагрузить все скрипты в data/scripts/target
     *
     * @param target путь до класса, или каталога со скриптами
     *               <p>
     * @return true, если скрипты перезагружены успешно
     */
    public List<Class<?>> load(final File target) {
        Collection<File> scriptFiles = Collections.emptyList();

        if (target.isFile()) {
            scriptFiles = new ArrayList<>(1);
            scriptFiles.add(target);
        } else if (target.isDirectory()) {
            scriptFiles = FileUtils.listFiles(target, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
        }

        if (scriptFiles.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Class<?>> classes = new ArrayList<>();
        final Compiler compiler = new Compiler();

        if (compiler.compile(scriptFiles)) {
            final MemoryClassLoader classLoader = compiler.getClassLoader();
            classLoader.getLoadedClasses().filter(name -> !name.contains(INNERCLASS_SEPARATOR)).forEach(name -> {
                try {
                    final Class<?> clazz = classLoader.loadClass(name);
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        return;
                    }
                    classes.add(clazz);

                    initOnLoadListeners(clazz);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Scripts: Can't load script class: " + name, e);
                    classes.clear();
                }
            });
        }

        return classes;
    }

    private boolean load(final List<Class<?>> classes, final String target) {
        Collection<File> scriptFiles = Collections.emptyList();
        File file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target.replace(".", "/") + ".java");
        if (file.isFile()) {
            scriptFiles = new ArrayList<>(1);
            scriptFiles.add(file);
        } else {
            file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target);
            if (file.isDirectory()) {
                scriptFiles = FileUtils.listFiles(file, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
            }
        }
        if (scriptFiles.isEmpty()) {
            return false;
        }
        boolean success;
        if (success = compiler.compile(scriptFiles)) {
            final MemoryClassLoader classLoader = compiler.getClassLoader();
            classLoader.getLoadedClasses().filter(name -> !name.contains(INNERCLASS_SEPARATOR)).forEach(name -> {
                try {
                    final Class<?> clazz = classLoader.loadClass(name);
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        return;
                    }

                    initOnLoadListeners(clazz);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Scripts: Can't load script class: {}", name, e);
                    classes.clear();
                }
            });
            classLoader.clear();
        }
        return success;
    }

    private void initOnLoadListeners(Class<?> clazz) {
        try {
            if(OnLoadScriptListener.class.isAssignableFrom(clazz)) {
                if(OnInitScriptListener.class.isAssignableFrom(clazz)) {
                    LOGGER.warn("Scripts: Error in class: " + clazz.getName() + ". Can not use OnLoad and OnInit listeners together!");

                }

                for(Method method : clazz.getMethods()) {
                    if(method.isAnnotationPresent(Bypass.class)) {
                        LOGGER.warn("Scripts: Error in class: " + clazz.getName() + ". Can not use OnLoad listener and bypass annotation together!");
                        break;
                    }
                }

                _listeners.add((OnLoadScriptListener) clazz.newInstance());
            }
        } catch(Exception e) {
            LOGGER.error("", e);
        }
    }

    private void addHandlers(final Class<?> clazz) {
        try {
            Arrays.stream(clazz.getMethods()).forEach(method -> {
                if (method.getName().contains("OnAction_")) {
                    final String name = method.getName().substring(9);
                    onAction.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
                } else if (method.getName().contains("OnActionShift_")) {
                    final String name = method.getName().substring(14);
                    onActionShift.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
                }
            });
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public Object callScripts(final String className, final String methodName) {
        return callScripts(null, className, methodName, null, null);
    }

    public Object callScripts(final String className, final String methodName, final Object[] args) {
        return callScripts(null, className, methodName, args, null);
    }

    public Object callScripts(final String className, final String methodName, final Map<String, Object> variables) {
        return callScripts(null, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
    }

    public Object callScripts(final String className, final String methodName, final Object[] args, final Map<String, Object> variables) {
        return callScripts(null, className, methodName, args, variables);
    }

    public Object callScripts(final Player caller, final String className, final String methodName) {
        return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public Object callScripts(final Player caller, final String className, final String methodName, final Object[] args) {
        return callScripts(caller, className, methodName, args, null);
    }

    public Object callScripts(final Player caller, final String className, final String methodName, final Map<String, Object> variables) {
        return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
    }

    public Object callScripts(final Player caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables) {
        final Class<?> clazz = _classes.get(className);
        if (clazz == null) {
            LOGGER.error("Script class " + className + " not found!");
            return null;
        }
        Object o;
        try {
            o = clazz.newInstance();
        } catch (Exception e) {
            LOGGER.error("Scripts: Failed creating instance of " + clazz.getName(), e);
            return null;
        }
        if (variables != null && !variables.isEmpty()) {
            variables.forEach((key, value) -> {
                try {
                    FieldUtils.writeField(o, key, value);
                } catch (Exception e2) {
                    LOGGER.error("Scripts: Failed setting fields for " + clazz.getName(), e2);
                }
            });
        }
        if (caller != null) {
            try {
                Field field;
                if ((field = FieldUtils.getField(clazz, "self")) != null) {
                    FieldUtils.writeField(field, o, caller.getRef());
                }
            } catch (Exception e) {
                LOGGER.error("Scripts: Failed setting field for " + clazz.getName(), e);
            }
        }
        Object ret = null;
        try {
            final Class<?>[] parameterTypes = (Class<?>[]) new Class[args.length];
            IntStream.range(0, args.length).forEach(i -> parameterTypes[i] = ((args[i] != null) ? args[i].getClass() : null));
            ret = MethodUtils.invokeMethod(o, methodName, args, parameterTypes);
        } catch (NoSuchMethodException nsme) {
            LOGGER.error("Scripts: No such method " + clazz.getName() + "." + methodName + "()!");
        } catch (InvocationTargetException ite) {
            LOGGER.error("Scripts: Error while calling " + clazz.getName() + "." + methodName + "()", ite.getTargetException());
        } catch (Exception e3) {
            LOGGER.error("Scripts: Failed calling " + clazz.getName() + "." + methodName + "()", e3);
        }
        return ret;
    }

    public Map<String, Class<?>> getClasses() {
        return _classes;
    }

    public static class ScriptClassAndMethod {
        public final String className;
        public final String methodName;

        public ScriptClassAndMethod(final String className, final String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }

    public class ScriptListenerImpl extends ListenerList<Scripts> {
        void init() {
            getListeners().stream().filter(OnInitScriptListener.class::isInstance).forEach(listener -> ((OnInitScriptListener) listener).onInit());
        }

        void load() {
            getListeners().stream().filter(OnLoadScriptListener.class::isInstance).forEach(listener -> ((OnLoadScriptListener) listener).onLoad());
        }
    }
}
