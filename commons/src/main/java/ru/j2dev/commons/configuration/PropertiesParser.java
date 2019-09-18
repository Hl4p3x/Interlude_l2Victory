package ru.j2dev.commons.configuration;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author G1ta0
 */
public class PropertiesParser extends Properties {

    public static final String defaultDelimiter = "[\\s,;]+";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesParser.class);

    public static PropertiesParser parse(final String fileName) {
        return parse(new File(fileName));
    }

    public static PropertiesParser parse(File file) {
        PropertiesParser result = new PropertiesParser();

        try {
            result.load(file);
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        return result;
    }

    public static boolean parseBoolean(final String s) {
        return Boolean.parseBoolean(s.toLowerCase());
    }

    public void load(final String fileName) throws IOException {
        load(new File(fileName));
    }

    public void load(final File file) throws IOException {
        InputStream is = null;
        try {
            load(is = new FileInputStream(file));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public boolean getProperty(final String name, final boolean defaultValue) {
        boolean val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            val = parseBoolean(value);
        }
        return val;
    }

    public int getProperty(final String name, final int defaultValue) {
        int val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            val = Integer.parseInt(value);
        }
        return val;
    }

    public long getProperty(final String name, final long defaultValue) {
        long val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            val = Long.parseLong(value);
        }
        return val;
    }

    public double getProperty(final String name, final double defaultValue) {
        double val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            val = Double.parseDouble(value);
        }
        return val;
    }

    public String[] getProperty(final String name, final String[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public String[] getProperty(final String name, final String[] defaultValue, final String delimiter) {
        String[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            val = value.split(delimiter);
        }
        return val;
    }

    public boolean[] getProperty(final String name, final boolean[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public boolean[] getProperty(final String name, final boolean[] defaultValue, final String delimiter) {
        boolean[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            final String[] values = value.split(delimiter);
            val = new boolean[values.length];
            for (int i = 0; i < val.length; ++i) {
                val[i] = parseBoolean(values[i]);
            }
        }
        return val;
    }

    public int[] getProperty(final String name, final int[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public int[] getProperty(final String name, final int[] defaultValue, final String delimiter) {
        int[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            if (!value.isEmpty()) {
                final String[] values = value.split(delimiter);
                val = new int[values.length];
                Arrays.setAll(val, i -> Integer.parseInt(values[i]));
            } else {
                val = new int[0];
            }
        }
        return val;
    }

    public long[] getProperty(final String name, final long[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public long[] getProperty(final String name, final long[] defaultValue, final String delimiter) {
        long[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            if (!value.isEmpty()) {
                final String[] values = value.split(delimiter);
                val = new long[values.length];
                Arrays.setAll(val, i -> Long.parseLong(values[i]));
            } else {
                val = new long[0];
            }
        }
        return val;
    }

    public double[] getProperty(final String name, final double[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public double[] getProperty(final String name, final double[] defaultValue, final String delimiter) {
        double[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            if (!value.isEmpty()) {
                final String[] values = value.split(delimiter);
                val = new double[values.length];
                Arrays.setAll(val, i -> Double.parseDouble(values[i]));
            } else {
                val = new double[0];
            }
        }
        return val;
    }

    public float[] getProperty(final String name, final float[] defaultValue) {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public float[] getProperty(final String name, final float[] defaultValue, final String delimiter) {
        float[] val = defaultValue;
        final String value;
        if ((value = super.getProperty(name, null)) != null) {
            if (!value.isEmpty()) {
                final String[] values = value.split(delimiter);
                val = new float[values.length];
                for (int i = 0; i < val.length; ++i) {
                    val[i] = Float.parseFloat(values[i]);
                }
            } else {
                val = new float[0];
            }
        }
        return val;
    }
}
