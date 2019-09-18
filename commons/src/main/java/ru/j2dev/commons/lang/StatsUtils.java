package ru.j2dev.commons.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.SystemUtil;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class StatsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsUtils.class);
    private static final MemoryMXBean memMXbean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXbean = ManagementFactory.getThreadMXBean();

    public static long getUsedMem() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    }

    public static long getTotalMem() {
        return Runtime.getRuntime().maxMemory() / 1048576;
    }

    public static long getMemUsed() {
        return memMXbean.getHeapMemoryUsage().getUsed();
    }

    public static String getMemUsedMb() {
        return getMemUsed() / 0x100000 + " Mb";
    }

    public static long getMemMax() {
        return memMXbean.getHeapMemoryUsage().getMax();
    }

    public static String getMemMaxMb() {
        return getMemMax() / 0x100000 + " Mb";
    }

    public static long getMemFree() {
        final MemoryUsage heapMemoryUsage = memMXbean.getHeapMemoryUsage();
        return heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
    }

    public static String getMemFreeMb() {
        return getMemFree() / 0x100000 + " Mb";
    }

    public static CharSequence getMemUsage() {
        final double maxMem = memMXbean.getHeapMemoryUsage().getMax() / 1024.;
        final double allocatedMem = memMXbean.getHeapMemoryUsage().getCommitted() / 1024.;
        final double usedMem = memMXbean.getHeapMemoryUsage().getUsed() / 1024.;
        final double nonAllocatedMem = maxMem - allocatedMem;
        final double cachedMem = allocatedMem - usedMem;
        final double useableMem = maxMem - usedMem;

        final StringBuilder list = new StringBuilder();

        list.append("AllowedMemory: ........... ").append((int) maxMem).append(" KB").append('\n');
        list.append("     Allocated: .......... ").append((int) allocatedMem).append(" KB (").append(((double) Math.round(allocatedMem / maxMem * 1000000) / 10000)).append("%)").append('\n');
        list.append("     Non-Allocated: ...... ").append((int) nonAllocatedMem).append(" KB (").append((double) Math.round(nonAllocatedMem / maxMem * 1000000) / 10000).append("%)").append('\n');
        list.append("AllocatedMemory: ......... ").append((int) allocatedMem).append(" KB").append('\n');
        list.append("     Used: ............... ").append((int) usedMem).append(" KB (").append((double) Math.round(usedMem / maxMem * 1000000) / 10000).append("%)").append('\n');
        list.append("     Unused (cached): .... ").append((int) cachedMem).append(" KB (").append((double) Math.round(cachedMem / maxMem * 1000000) / 10000).append("%)").append('\n');
        list.append("UseableMemory: ........... ").append((int) useableMem).append(" KB (").append((double) Math.round(useableMem / maxMem * 1000000) / 10000).append("%)").append('\n');

        return list;
    }

    public static CharSequence getThreadStats() {
        final StringBuilder list = new StringBuilder();

        final int threadCount = threadMXbean.getThreadCount();
        final int daemonCount = threadMXbean.getThreadCount();
        final int nonDaemonCount = threadCount - daemonCount;
        final int peakCount = threadMXbean.getPeakThreadCount();
        final long totalCount = threadMXbean.getTotalStartedThreadCount();

        list.append("Live: .................... ").append(threadCount).append(" threads").append('\n');
        list.append("     Non-Daemon: ......... ").append(nonDaemonCount).append(" threads").append('\n');
        list.append("     Daemon: ............. ").append(daemonCount).append(" threads").append('\n');
        list.append("Peak: .................... ").append(peakCount).append(" threads").append('\n');
        list.append("Total started: ........... ").append(totalCount).append(" threads").append('\n');
        list.append("=================================================").append('\n');

        return list;
    }

    public static CharSequence getThreadStats(final boolean lockedMonitors, final boolean lockedSynchronizers, final boolean stackTrace) {
        final StringBuilder list = new StringBuilder();

        for (final ThreadInfo info : threadMXbean.dumpAllThreads(lockedMonitors, lockedSynchronizers)) {
            list.append("Thread #").append(info.getThreadId()).append(" (").append(info.getThreadName()).append(')').append('\n');
            list.append("=================================================\n");
            list.append("\tgetThreadState: ...... ").append(info.getThreadState()).append('\n');
            for (final MonitorInfo monitorInfo : info.getLockedMonitors()) {
                list.append("\tLocked monitor: ....... ").append(monitorInfo).append('\n');
                list.append("\t\t[").append(monitorInfo.getLockedStackDepth()).append(".]: at ").append(monitorInfo.getLockedStackFrame()).append('\n');
            }

            for (final LockInfo lockInfo : info.getLockedSynchronizers()) {
                list.append("\tLocked synchronizer: ...").append(lockInfo).append('\n');
            }

            if (stackTrace) {
                list.append("\tgetStackTace: ..........\n");
                for (final StackTraceElement trace : info.getStackTrace()) {
                    list.append("\t\tat ").append(trace).append('\n');
                }
            }
            list.append("=================================================\n");
        }

        return list;
    }

    public static CharSequence getGCStats() {
        final StringBuilder list = new StringBuilder();

        for (final GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            list.append("GarbageCollector (").append(gcBean.getName()).append(")\n");
            list.append("=================================================\n");
            list.append("getCollectionCount: ..... ").append(gcBean.getCollectionCount()).append('\n');
            list.append("getCollectionTime: ...... ").append(gcBean.getCollectionTime()).append(" ms").append('\n');
            list.append("=================================================\n");
        }

        return list;
    }

    public static void printLogo() {
        LOGGER.info("|                                                ");
        LOGGER.info("|          ######...####...#####...#####..##..## ");
        LOGGER.info("|              ##..##..##..##..##..##.....##..## ");
        LOGGER.info("|              ##.....##...##..##..####...##..## ");
        LOGGER.info("|          ##  ##...##.....##..##..##......####  ");
        LOGGER.info("|           ####...######..#####...#####....##   ");
        LOGGER.info("|                                                ");
    }

    public static String[] getMemoryInfo() {
        double max = Runtime.getRuntime().maxMemory() / 1024; // maxMemory is the upper limit the jvm can use
        double allocated = Runtime.getRuntime().totalMemory() / 1024; // totalMemory the size of the current allocation pool
        double nonAllocated = max - allocated; // non allocated memory till jvm limit
        double cached = Runtime.getRuntime().freeMemory() / 1024; // freeMemory the unused memory in the allocation pool
        double used = allocated - cached; // really used memory
        double useable = max - used; // allocated, but non-used and non-allocated memory
        DecimalFormat df = new DecimalFormat(" (0.0000'%')");
        DecimalFormat df2 = new DecimalFormat(" # 'KB'");

        return new String[]{
                "| Global Memory Informations at " + getRealTime(),
                "| Allowed Memory:" + df2.format(max),
                "| Allocated Memory:" + df2.format(allocated) + df.format(allocated / max * 100),
                "| Non-Allocated Memory:" + df2.format(nonAllocated) + df.format(nonAllocated / max * 100),
                "| Allocated Memory:" + df2.format(allocated),
                "| Used Memory:" + df2.format(used) + df.format(used / max * 100),
                "| Unused (cached) Memory:" + df2.format(cached) + df.format(cached / max * 100),
                "| Useable Memory:" + df2.format(useable) + df.format(useable / max * 100),
        };
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

    public static void printCpuInfo() {
        LOGGER.info("| Avaible CPU(s): {}", Runtime.getRuntime().availableProcessors());
        LOGGER.info("| Processor(s) Identifier: {}", System.getenv("PROCESSOR_IDENTIFIER"));
    }

    public static void printOSInfo() {
        LOGGER.info("| OS: {} Build: {}", System.getProperty("os.name"), System.getProperty("os.version"));
        LOGGER.info("| OS Arch: {}", System.getProperty("os.arch"));
    }

    public static void printPid() {
        LOGGER.info("| Process ID: {}", SystemUtil.getPid());
    }
}
