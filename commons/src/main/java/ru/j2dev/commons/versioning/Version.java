package ru.j2dev.commons.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class Version {

    private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);
    private String _revisionNumber = "exported";
    private String _versionNumber = "-1";
    private String _buildDate = "";
    private String _buildJdk = "";
    private String _teamName = "j2dev.ru";
    private String _teamSite = "http://j2dev.ru";
    private String _licenseType = "Demo license";
    private String _coreDev = "";
    private String _dataDev = "";
    private String _jarSignature = "";

    public Version(final Class<?> c) {
        File jarName = null;
        try {
            //TODO: fix close resourse
            jarName = Locator.getClassSource(c);
            final JarFile jarFile = new JarFile(jarName);

            final Attributes attrs = jarFile.getManifest().getMainAttributes();

            setBuildJdk(attrs);

            setBuildDate(attrs);

            setRevisionNumber(attrs);

            setTeamName(attrs);

            setTeamSite(attrs);

            setLicenseType(attrs);

            setCoreDev(attrs);

            setDataDev(attrs);

            setJarSignature(attrs);

            setVersionNumber(attrs);
        } catch (final IOException e) {
            LOGGER.error("Unable to get soft information\nFile name '{}' isn't a valid jar", jarName.getAbsolutePath(), e);
        }

    }

    public static String[] getCpuInfo() {
        return new String[]{
                "Avaible CPU(s): " + Runtime.getRuntime().availableProcessors(), "CPU: " + System.getenv("PROCESSOR_IDENTIFIER")
        };
    }

    public static String getOSInfo() {
        return "Operating System: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version") + ", arch: " + System.getProperty("os.arch");
    }

    public static String getJavaInfo() {
        return "JMV: " + System.getProperty("java.vm.name") + " Build: " + System.getProperty("java.runtime.version");
    }

    public static String[] getMemoryInfo() {
        double max = Runtime.getRuntime().maxMemory() / 1024L;
        double allocated = Runtime.getRuntime().totalMemory() / 1024L;
        double nonAllocated = max - allocated;
        double cached = Runtime.getRuntime().freeMemory() / 1024L;
        double used = allocated - cached;
        double useable = max - used;
        DecimalFormat df = new DecimalFormat(" (0.0000'%')");
        DecimalFormat df2 = new DecimalFormat(" # 'KB'");
        return new String[]{"+----", "| Global Memory Informations at " + getRealTime() + ":", "|    |", "| Allowed Memory:" + df2.format(max), "|    |= Allocated Memory:" + df2.format(allocated) + df.format(allocated / max * 100.0D), "|    |= Non-Allocated Memory:" + df2.format(nonAllocated) + df.format(nonAllocated / max * 100.0D), "| Allocated Memory:" + df2.format(allocated), "|    |= Used Memory:" + df2.format(used) + df.format(used / max * 100.0D), "|    |= Unused (cached) Memory:" + df2.format(cached) + df.format(cached / max * 100.0D), "| Useable Memory:" + df2.format(useable) + df.format(useable / max * 100.0D), "+----"};
    }

    public static String[] getCPUInfo() {
        return new String[]{"Available CPU(s): " + Runtime.getRuntime().availableProcessors(), "Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"), "..................................................", ".................................................."};
    }

    public static String[] getOSArrayInfo() {
        return new String[]{"OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"), "OS Arch: " + System.getProperty("os.arch"), "..................................................", ".................................................."};
    }

    public static String[] getJREInfo() {
        return new String[]{"Java Platform Information", "Java Runtime  Name: " + System.getProperty("java.runtime.name"), "Java Version: " + System.getProperty("java.version"), "Java Class Version: " + System.getProperty("java.class.version"), "..................................................", ".................................................."};
    }

    public static String[] getJVMInfo() {
        return new String[]{"Virtual Machine Information (JVM)", "JVM Name: " + System.getProperty("java.vm.name"), "JVM installation directory: " + System.getProperty("java.home"), "JVM version: " + System.getProperty("java.vm.version"), "JVM Vendor: " + System.getProperty("java.vm.vendor"), "JVM Info: " + System.getProperty("java.vm.info"), "..................................................", ".................................................."};
    }

    public static String getRealTime() {
        SimpleDateFormat String = new SimpleDateFormat("H:mm:ss");
        return String.format(new Date());
    }

    public static void printMemoryInfo() {
        for (String line : getMemoryInfo()) {
            LOGGER.info(line);
        }
    }

    public static void printCPUInfo() {
        for (String line : getCPUInfo()) {
            LOGGER.info(line);
        }
    }

    public static void printOSInfo() {
        for (String line : getOSArrayInfo()) {
            LOGGER.info(line);
        }
    }

    public static void printJREInfo() {
        for (String line : getJREInfo()) {
            LOGGER.info(line);
        }
    }

    public static void printJVMInfo() {
        for (String line : getJVMInfo()) {
            LOGGER.info(line);
        }
    }

    public static void printRealTime() {
        LOGGER.info(getRealTime());
    }

    public static void printAllInfos() {
        printOSInfo();
        printCPUInfo();
        printJREInfo();
        printJVMInfo();
        printMemoryInfo();
    }

    public String getRevisionNumber() {
        return _revisionNumber;
    }

    /**
     * @param attrs
     */
    private void setRevisionNumber(final Attributes attrs) {
        final String revisionNumber = attrs.getValue("Revision");
        if (revisionNumber != null) {
            _revisionNumber = revisionNumber;
        } else {
            _revisionNumber = "-1";
        }
    }

    public String getVersionNumber() {
        return _versionNumber;
    }

    /**
     * @param attrs
     */
    private void setVersionNumber(final Attributes attrs) {
        final String versionNumber = attrs.getValue("Chronicle");
        if (versionNumber != null) {
            _versionNumber = versionNumber;
        } else {
            _versionNumber = "-1";
        }
    }

    public String getBuildDate() {
        return _buildDate;
    }

    /**
     * @param attrs
     */
    private void setBuildDate(final Attributes attrs) {
        final String buildDate = attrs.getValue("Build-Date");
        if (buildDate != null) {
            _buildDate = buildDate;
        } else {
            _buildDate = "-1";
        }
    }

    public String getBuildJdk() {
        return _buildJdk;
    }

    /**
     * @param attrs
     */
    private void setBuildJdk(final Attributes attrs) {
        String buildJdk = attrs.getValue("Build-Jdk");
        if (buildJdk != null) {
            _buildJdk = buildJdk;
        } else {
            buildJdk = attrs.getValue("Created-By");
            if (buildJdk != null) {
                _buildJdk = buildJdk;
            } else {
                _buildJdk = "-1";
            }
        }
    }

    public String getTeamName() {
        return _teamName;
    }

    private void setTeamName(final Attributes attrs) {
        final String teamName = attrs.getValue("Team-Name");
        if (teamName != null) {
            _teamName = teamName;
        } else {
            _teamName = "-1";
        }
    }

    public String getTeamSite() {
        return _teamSite;
    }

    private void setTeamSite(final Attributes attrs) {
        final String teamSite = attrs.getValue("Team-Site");
        if (teamSite != null) {
            _teamSite = teamSite;
        } else {
            _teamSite = "-1";
        }
    }

    public String getLicenseType() {
        return _licenseType;
    }

    private void setLicenseType(final Attributes attrs) {
        final String licenseType = attrs.getValue("License-Type");
        if (licenseType != null) {
            _licenseType = licenseType;
        } else {
            _licenseType = "";
        }
    }

    public String getCoreDev() {
        return _coreDev;
    }

    private void setCoreDev(final Attributes attrs) {
        final String coreDev = attrs.getValue("Core-Dev");
        if (coreDev != null) {
            _coreDev = coreDev;
        } else {
            _coreDev = "";
        }
    }

    public String getDataDev() {
        return _dataDev;
    }

    private void setDataDev(final Attributes attrs) {
        final String dataDev = attrs.getValue("Data-Dev");
        if (dataDev != null) {
            _dataDev = dataDev;
        } else {
            _dataDev = "";
        }
    }

    public String getJarSignature() {
        return _jarSignature;
    }

    private void setJarSignature(final Attributes attrs) {
        final String dataDev = attrs.getValue("JAR-Signature");
        if (dataDev != null) {
            _jarSignature = dataDev;
        } else {
            _jarSignature = "";
        }
    }
}
