package ru.j2dev.dataparser.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 24.08.12 16:47
 */
public final class ParserUtil {
    static final Pattern mayBeNotNullPattern = Pattern.compile("[\\w\\d]+");

    public static int getArrayCounts(StringBuilder buffer, int arrayLevel) {
        int arrayCounts = 0;
        int bracesCount = 0;
        for (int pos = 0; pos < buffer.length(); pos++) {
            char ch = buffer.charAt(pos);
            if (ch == '{') {
                bracesCount++;
            } else if (ch == '}') {
                if (arrayLevel == bracesCount)
                    arrayCounts++;
                bracesCount--;
            }
        }
        return arrayCounts;
    }

    public static StringBuilder removeBracetsAndCopy(StringBuilder buffer) {
        StringBuilder tempBuffer = new StringBuilder(buffer);
        int pos = tempBuffer.indexOf("{");
        tempBuffer = tempBuffer.replace(pos, pos + 1, "");
        pos = tempBuffer.lastIndexOf("}");
        tempBuffer = tempBuffer.replace(pos, pos + 1, "");
        return tempBuffer;
    }

    @SuppressWarnings("unchecked")
    public static void appendValueToField(Field field, Object object, Object value) throws IllegalAccessException {
        Class<?> clazz = field.getType();
        if (!List.class.isAssignableFrom(clazz)) {
            field.set(object, value);
            return;
        }
        // Добавление значения в список
        List list = (List) field.get(object);
        if (list == null)
            list = new ArrayList();
        list.add(value);
        field.set(object, list);
    }

    /**
     * Выделяет список заключенный в {...} из строки
     *
     * @param buffer - входная строка
     * @return - {...} список верхнего уровня, либо null, если список из строки
     * выделить не удалось
     */
    public static StringBuilder getArray(int from, StringBuilder buffer) {
        int bracesCount = 0;
        boolean startFlag = false;
        int firstBracketPos = -1, lastBracketPos = -1;
        for (int pos = from; pos < buffer.length(); pos++) {
            char ch = buffer.charAt(pos);
            if (ch == '{') {
                bracesCount++;
                startFlag = true;
                if (firstBracketPos < 0)
                    firstBracketPos = pos;
            } else if (ch == '}') {
                bracesCount--;
            }
            if (startFlag && bracesCount == 0) {
                if (lastBracketPos < 0)
                    lastBracketPos = pos;
                break;
            }
        }
        if (startFlag && bracesCount == 0) {
            return new StringBuilder(buffer.substring(firstBracketPos, lastBracketPos + 1));
        }
        return null;
    }

    public static boolean mayBeNullObject(CharSequence charSequence) {
        Matcher matcher = mayBeNotNullPattern.matcher(charSequence);
        return !matcher.find();
    }

    public static StringBuilder deleteFromBuffer(StringBuilder buffer, List<int[]> replacements) {
        // Часто встречающиеся замены
        if (replacements.size() == 0)
            return buffer;
        else if (replacements.size() == 1)
            return buffer.replace(replacements.get(0)[0], replacements.get(0)[1], "");
        StringBuilder stringBuilder = new StringBuilder(buffer.length());
        int currentPos = 0;
        int replacementsSize = replacements.size();
        int[] nextDelete = replacements.get(currentPos++);
        for (int i = 0; i < buffer.length(); i++) {
            if (i >= nextDelete[0] && i < nextDelete[1]) {
                if (i + 1 >= nextDelete[1]) {
                    if (currentPos < replacementsSize) {
                        nextDelete = replacements.get(currentPos++);
                    } else {
                        return stringBuilder.append(buffer.substring(i + 1, buffer.length()));
                    }
                }
            } else
                stringBuilder.append(buffer.charAt(i));
        }
        return stringBuilder;
    }
}
