package ru.j2dev.commons.data.xml;


import ru.j2dev.commons.logging.LoggerObject;

/**
 * Author: VISTALL
 * Date:  18:34/30.11.2010
 */
public abstract class AbstractHolder extends LoggerObject {
    private long parseStartTime, parseEndTime;

    private static String formatOut(final String st) {
        final char[] chars = st.toCharArray();
        final StringBuilder buf = new StringBuilder(chars.length);

        for (final char ch : chars) {
            if (Character.isUpperCase(ch)) {
                buf.append(' ');
            }

            buf.append(Character.toLowerCase(ch));
        }

        return buf.toString();
    }

    public void log() {
        info(String.format("loaded %d%s(s) count.", size(), formatOut(getClass().getSimpleName().replace("Holder", "")).toLowerCase()));
    }

    protected void process() {
    }

    /**
     * Вызывается непосредственно перед загрузкой
     */
    public void beforeParsing() {
        parseStartTime = System.nanoTime();
    }

    /**
     * Вызывается после того, как были загружены все элементы.
     */
    public void afterParsing() {
        parseEndTime = System.nanoTime();
    }

    public int size() {
        throw new UnsupportedOperationException("override log or size methods.");
    }

    public abstract void clear();
}