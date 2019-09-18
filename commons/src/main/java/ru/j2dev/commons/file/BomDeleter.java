package ru.j2dev.commons.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by JunkyFunky
 * on 10.02.2018 17:35
 * group j2dev
 */
public class BomDeleter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BomDeleter.class);

    public static boolean deleteBOM(final File file) throws IOException {
        // -17, -69, -65
        byte[] byteArray = FileUtils.readFileToByteArray(file);
        if (byteArray.length > 3) {
            if (byteArray[0] == -17 && byteArray[1] == -69 && byteArray[2] == -65) {
                byte[] temp = new byte[byteArray.length - 3];
                System.arraycopy(byteArray, 3, temp, 0, temp.length);
                FileUtils.writeByteArrayToFile(file, temp);
                return true;
            }
        }

        return false;
    }

    /**
     * @param dirs
     */
    public static void deleteBomFromFiles(String... dirs) {
        Arrays.stream(dirs).forEach(directory -> {
            File dir = new File(directory);

            try {
                int delete = 0;
                int normal = 0;

                Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".html"), FileFilterUtils.directoryFileFilter());
                for (File file : files) {
                    if (deleteBOM(file)) {
                        delete++;
                    } else {
                        normal++;
                    }
                }

                files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".htm"), FileFilterUtils.directoryFileFilter());
                for (File file : files) {
                    if (deleteBOM(file)) {
                        delete++;
                    } else {
                        normal++;
                    }
                }

                LOGGER.info("Normal: " + normal + ", Delete: " + delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
