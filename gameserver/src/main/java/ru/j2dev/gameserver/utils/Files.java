package ru.j2dev.gameserver.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    public static void writeFile(final String path, final String string) {
        try {
            FileUtils.writeStringToFile(new File(path), string, "UTF-8");
        } catch (IOException e) {
            LOGGER.error("Error while saving file : " + path, e);
        }
    }

    public static boolean copyFile(final String srcFile, final String destFile) {
        try {
            FileUtils.copyFile(new File(srcFile), new File(destFile), false);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while copying file : " + srcFile + " to " + destFile, e);
            return false;
        }
    }
}
