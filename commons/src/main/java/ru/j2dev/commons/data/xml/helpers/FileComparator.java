package ru.j2dev.commons.data.xml.helpers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * @author Java-man
 */
public class FileComparator extends Ordering<File> implements Serializable {
    private static final FileComparator INSTANCE = new FileComparator();

    public static FileComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(final File left, final File right) {
        if (left == null)
            return -1;
        if (right == null)
            return 1;

        final String leftName = left.getName();
        final String rightName = right.getName();

        final ArrayListMultimap<String, Integer> digits = ArrayListMultimap.create(2, 1);
        digits.putAll(findDigitsInString(leftName));
        digits.putAll(findDigitsInString(rightName));

        if (digits.isEmpty())
            return leftName.compareTo(rightName);

        final List<Integer> leftDigits = digits.get(leftName);
        final List<Integer> rightDigits = digits.get(rightName);

        final int digitsMaxSize = Math.max(leftDigits.size(), rightDigits.size());

        for (int i = 0; i < digitsMaxSize; i++) {
            if (i >= leftDigits.size() && i >= rightDigits.size()) {
                break;
            }
            if (i >= leftDigits.size() && i < rightDigits.size()) {
                return -1;
            }
            if (i < leftDigits.size() && i >= rightDigits.size()) {
                return 1;
            }

            final int leftDigit = leftDigits.get(i);
            final int rightDigit = rightDigits.get(i);

            final int compareResult = Integer.compare(leftDigit, rightDigit);

            if (compareResult != 0) {
                return compareResult;
            }
        }

        return stringWithoutDigits(leftName, digits.get(leftName)).compareTo(stringWithoutDigits(rightName, digits.get(rightName)));
    }

    private Multimap<String, Integer> findDigitsInString(final String str) {
        final Multimap<String, Integer> result = ArrayListMultimap.create(1, 1);

        final StringBuilder digitString = new StringBuilder(10);

        for (final char ch : str.toCharArray()) {
            if (Character.isDigit(ch)) {
                digitString.append(ch);
            } else if (digitString.length() > 0) {
                Integer value = Ints.tryParse(digitString.toString());
                if (value == null) {
                    value = Integer.MIN_VALUE;
                }

                result.put(str, value);

                digitString.setLength(0);
            }
        }

        return result;
    }

    private String stringWithoutDigits(final String str, final Iterable<Integer> digits) {
        String result = str;

        for (final int digit : digits) {
            result = result.replaceFirst(String.valueOf(digit), StringUtils.EMPTY);
        }

        return result;
    }
}