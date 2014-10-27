package com.logicmonitor.ft.jxmtop;

import com.logicmonitor.ft.jmx.jmxman.JMXMan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincent on 14-10-16.
 */
public class JVMGeneral {
    private final String uptime = "java.lang:type=Runtime:Uptime";

    private final String threadsLive = "java.lang:type=Threading:ThreadCount";  //LIVE
    private final String threadsDaemon = "java.lang:type=Threading:DaemonThreadCount";   //DAEMON
    private final String threadsPeak = "java.lang:type=Threading:PeakThreadCount";       //PEAK
    private final String threadsTotal = "java.lang:type=Threading:TotalStartedThreadCount";       //Total


    private final String cpuUsage = "java.lang:type=OperatingSystem:ProcessCpuTime";
    private final String cpuTotal = "java.lang:type=OperatingSystem:AvailableProcessors";

    private final String heapMemUsed = "java.lang:type=Memory:HeapMemoryUsage.used";
    private final String heapMemCommitted = "java.lang:type=Memory:HeapMemoryUsage.committed";

    private final String nonHeapMemUsed  = "java.lang:type=Memory:NonHeapMemoryUsage.used";
    private final String nonHeapMemCommitted = "java.lang:type=Memory:NonHeapMemoryUsage.committed";
    // Memory pool paths
    private final String memoryPoolUsed = "java.lang:type=MemoryPool,name=*:Usage.used";
    private final String memoryPoolCommitted = "java.lang:type=MemoryPool,name=*:Usage.committed";

    public List<String> getJVMGeneralPaths(){
        List<String> jvmGeneralPaths = new ArrayList<String>();
        jvmGeneralPaths.add(uptime);
        jvmGeneralPaths.add(threadsLive);
        jvmGeneralPaths.add(threadsPeak);
        jvmGeneralPaths.add(threadsDaemon);
        jvmGeneralPaths.add(threadsTotal);

        jvmGeneralPaths.add(cpuUsage);
        jvmGeneralPaths.add(cpuTotal);

        jvmGeneralPaths.add(heapMemUsed);
        jvmGeneralPaths.add(heapMemCommitted);
        jvmGeneralPaths.add(nonHeapMemUsed);
        jvmGeneralPaths.add(nonHeapMemCommitted);


        return jvmGeneralPaths;
    }

    public List<String> getAllMemoryPoolUsed(JMXMan jmxMan){
        return jmxMan.generateFullPaths(getMemoryPoolUsed());
    }

    public List<String> getAllMemoryPoolCommitted(JMXMan jmxMan){
        return jmxMan.generateFullPaths(getMemoryPoolCommitted());
    }

    public String getUptime() {
        return uptime;
    }

    public String getThreadsLive() {
        return threadsLive;
    }

    public String getThreadsDaemon() {
        return threadsDaemon;
    }

    public String getThreadsPeak() {
        return threadsPeak;
    }

    public String getThreadsTotal() {
        return threadsTotal;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public String getCpuTotal() {
        return cpuTotal;
    }

    public String getHeapMemUsed() {
        return heapMemUsed;
    }

    public String getHeapMemCommitted() {
        return heapMemCommitted;
    }

    public String getNonHeapMemUsed() {
        return nonHeapMemUsed;
    }

    public String getNonHeapMemCommitted() {
        return nonHeapMemCommitted;
    }

    public String getMemoryPoolUsed() {
        return memoryPoolUsed;
    }

    public String getMemoryPoolCommitted() {
        return memoryPoolCommitted;
    }
}
