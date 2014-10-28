package com.logicmonitor.ft.jxmtop;

import com.logicmonitor.ft.jmx.jmxman.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jcurses.system.InputChar;
import jcurses.widgets.*;

import static java.lang.System.exit;

/**
 * Created by vincent on 14-10-16.
 */
public class JMXTopMain {


    public static void main(String[] args) {

        RunParameter runParameter = ARGSAnalyser(args);
        if (!runParameter.isValid()) {
            System.out.println(runParameter.getValidInfo());
            System.out.println("<Please check your arguments>");
            exit(-1);
        }

        try {
            JMXMan jmxman = new JMXMan(runParameter.getSurl(),runParameter.getUsername(),runParameter.getPassword(),runParameter.getTimeout());
            jmxman.init(runParameter);
            topJMXValues(jmxman,runParameter);
        }
        catch (IOException e) {
            System.out.println("Fail to connect to process, check the surl,username and password");
            e.printStackTrace();
            exit(-1);
        }

    }

    private static void topJMXValues(JMXMan jmxMan, RunParameter runParameter){

        JVMGeneral jvmGeneral = new JVMGeneral();
        List<String> generalPaths = jvmGeneral.getJVMGeneralPaths();
        List<String> memoryPoolUsed = jvmGeneral.getAllMemoryPoolUsed(jmxMan);
        List<String> memmoryPoolCommitted = jvmGeneral.getAllMemoryPoolCommitted(jmxMan);

        List<JMXFullPath> fullPaths = jmxMan.getJmxFullPaths();
        List<String> memoryPoolNames = jmxMan.getIndexValues(jvmGeneral.getMemoryPoolUsed(),jvmGeneral.getMemoryPoolCommitted());

        int generalInfoCounts = 5 + memoryPoolNames.size() ;

        int maxMemoryPoolNameLength = 0;
        for (String memoryPoolName : memoryPoolNames) {
            maxMemoryPoolNameLength = maxMemoryPoolNameLength > memoryPoolName.length() ? maxMemoryPoolNameLength : memoryPoolName.length();
        }

        int maxPathLength = 0;
        if (jmxMan.hasFullPath()) {
            int pathsNumb = fullPaths.size();
            for (JMXFullPath fullPath : fullPaths) {
                if (runParameter.isShowAliasTitle()) {
                    maxPathLength = fullPath.getAlias().length() > maxPathLength ? fullPath.getAlias().length() : maxPathLength;
                } else {
                    maxPathLength = fullPath.getPath().length() > maxPathLength ? fullPath.getPath().length() : maxPathLength;
                }
            }
        }

        Window window = new Window(1,1,1000,100,false,"");
        window.setShadow(false);
        window.moveToTheTop();
        DefaultLayoutManager mgr = new DefaultLayoutManager();
        mgr.bindToContainer(window.getRootPanel());

        String[] companyInfo = new String[]{" JMXtop was created by LogicMonitor under the BSD3 License.",
                "",
                " To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.",
                " For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmxtools."};
        Label companyInfoLabels[] = new Label[companyInfo.length];
        Label jvmGenLabels[] = new Label[generalInfoCounts];
        Label valuesLabels[] = null;

        try {

            // company info
            for (int i = 0; i < companyInfo.length; i++) {
                companyInfoLabels[i] = new Label("initTop");
                mgr.addWidget(companyInfoLabels[i],
                        0, i, 40, 20,
                        WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
            }

            // general jvm info
            String lastCPUTime = jmxMan.getValue(jvmGeneral.getCpuUsage());
            List<String> lastValues = jmxMan.getValues();
            long lastSystemTime = System.currentTimeMillis();

            for (int i = 0; i < generalInfoCounts; i++) {
                jvmGenLabels[i] = new Label("initTop");
                mgr.addWidget(jvmGenLabels[i],
                        0, i + companyInfo.length, 40, 20,
                        WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
            }

            // user path
            int pathsNumb = fullPaths.size();
            valuesLabels = new Label[pathsNumb];
            for (int i = 0; i < pathsNumb; i++) {
                valuesLabels[i] = new Label("initTop");
                mgr.addWidget(valuesLabels[i],
                        0, i + generalInfoCounts + 1 + companyInfo.length, 40, 20,
                        WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
            }

            int duration = runParameter.getInterval() * 1000;
            boolean trigger = false;
            int times = runParameter.getTimes();

            while (true) {

                for (int i = 0; i < companyInfo.length; i++) {
                    mgr.removeWidget(companyInfoLabels[i]);
                }

                int maxGeneralInfoLength = 0;
                for (int i = 0; i < generalInfoCounts; i++) {
                    mgr.removeWidget(jvmGenLabels[i]);

                }
                if (!fullPaths.isEmpty()) {
                    for (int i=0;i<fullPaths.size();i++){
                        mgr.removeWidget(valuesLabels[i]);
                    }
                }

                if (window.isClosed()) {
                    System.out.println("Window is closed!");
                    exit(-1);
                }

                mgr.unbindFromContainer();
                Window tmpWin = window;
                window = new Window(0,0,1000, 100, false, "");
                window.setShadow(false);
                window.setClosingChar(new InputChar(InputChar.KEY_F4));

                mgr.bindToContainer(window.getRootPanel());

                int startLine = 0;
                for (int i = 0; i < companyInfo.length; i++) {
                    companyInfoLabels[i] = new Label(companyInfo[i]);
                    mgr.addWidget(companyInfoLabels[i],
                            0, startLine, companyInfo[i].length(), 20,
                            WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
                    startLine++;
                }


                List<String> generalValues = jmxMan.getValues(generalPaths);
                List<String> memoryPoolUsedValues = jmxMan.getValues(memoryPoolUsed);
                List<String> memoryPoolCommittedValues = jmxMan.getValues(memmoryPoolCommitted);
                long cur_Time = System.currentTimeMillis();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Long upTime = Long.valueOf(generalValues.get(0));
                int nHours = (int) (upTime / 3600);
                int nMinis = (int) (upTime - nHours * 3600) / 60;
                int nSeconds = (int) (upTime - nHours * 3600 - nMinis * 60);

                String upTimeString = "" + nSeconds + "s";
                if (nHours != 0) {
                    upTimeString = "" + nHours + "h " + nMinis + "m " + upTimeString;
                }
                else if (nMinis != 0) {
                    upTimeString = "" + nMinis + "m " + upTimeString;
                }
                String topString = " JMXtop - " + df.format(new Date()) + " UP " + upTime + " da " + nHours;
                maxGeneralInfoLength = topString.length()>maxGeneralInfoLength ? topString.length() : maxGeneralInfoLength;
                String threadsInfo = " Thread: " + generalValues.get(1) + " live, " + generalValues.get(2) + " peak, " + generalValues.get(3) + " daemon, " + generalValues.get(4) + " total";
                maxGeneralInfoLength = threadsInfo.length() > maxGeneralInfoLength ? threadsInfo.length() : maxGeneralInfoLength;

                long cpuProcessTime = Long.valueOf(generalValues.get(5));
                float cpuUsage = (cpuProcessTime - Long.valueOf(lastCPUTime)) / (1000 * (cur_Time - lastSystemTime) * Integer.valueOf(generalValues.get(6)));
                String cpuInfo = String.format(" CPU: %.3fs, %.2f%% usage", (float) cpuProcessTime / 1000000000, cpuUsage);
                maxGeneralInfoLength = cpuInfo.length() > maxGeneralInfoLength ? cpuInfo.length() : maxGeneralInfoLength;

                long heapUsed = Long.valueOf(generalValues.get(7)) / 1024;
                long heapCommitted = Long.valueOf(generalValues.get(8)) / 1024;

                Long nonHeapUsed = Long.valueOf(generalValues.get(9)) / 1024;
                Long nonHeapCommitted = Long.valueOf(generalValues.get(10)) / 1024;
                String heapMemInfo = String.format(" %s : %10dKB used, %10dKB committed, %.2f%% usage",alignString("Heap",maxMemoryPoolNameLength), heapUsed, heapCommitted, (float) heapUsed*100 / heapCommitted);
                String nonHeapMemInfo = String.format(" %s : %10dKB used, %10dKB committed, %.2f%% usage",alignString("NonHeap",maxMemoryPoolNameLength), nonHeapUsed, nonHeapCommitted, (float) nonHeapUsed*100 / nonHeapCommitted);
                maxGeneralInfoLength = heapMemInfo.length() > maxGeneralInfoLength ? heapMemInfo.length() : maxGeneralInfoLength;
                maxGeneralInfoLength = nonHeapMemInfo.length() > maxGeneralInfoLength ? nonHeapMemInfo.length() : maxGeneralInfoLength;


                jvmGenLabels[0] = new Label(" JMXtop - " + df.format(new Date()) + " up " + upTimeString);
                jvmGenLabels[1] = new Label(threadsInfo);
                jvmGenLabels[2] = new Label(cpuInfo);
                jvmGenLabels[3] = new Label(heapMemInfo);
                jvmGenLabels[4] = new Label(nonHeapMemInfo);
//                memoryPoolUsedValues
                for (int i=0;i<memoryPoolNames.size();i++){
                    Long used = Long.valueOf(memoryPoolUsedValues.get(i))/1024;
                    Long commited = Long.valueOf(memoryPoolCommittedValues.get(i))/1024;
                    jvmGenLabels[i+5] = new Label(String.format(" %s : %10dKB used, %10dKB committed, %.2f%% usage", alignString(memoryPoolNames.get(i).toString(),maxMemoryPoolNameLength), used, commited, (float) used*100 / commited));
                }

                startLine++;
                for (int i=0;i< generalInfoCounts;i++) {
                    mgr.addWidget(jvmGenLabels[i],
                            0, startLine, maxGeneralInfoLength, 20,
                            WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
                    startLine++;
                }

                startLine++;
                if (!fullPaths.isEmpty()){
                    List<String> curValuesOfFullPaths = jmxMan.getValues();
                    for (int i = 0; i < curValuesOfFullPaths.size(); i++) {
                        String curValue;
                        if (fullPaths.get(i).isCounter()){
                            long rst = Long.valueOf(curValuesOfFullPaths.get(i)) - Long.valueOf(lastValues.get(i));
                            curValue = "" + rst;
                        } else {
                            curValue = curValuesOfFullPaths.get(i);
                        }
                        if (fullPaths.get(i).getOperator() != null && fullPaths.get(i).getScale() != 0) {
                            String opt = fullPaths.get(i).getOperator();
                            int scale = fullPaths.get(i).getScale();

                            long v = Long.valueOf(curValue);

                            if (opt.equals("+")) {
                                v += scale;
                            }
                            else if (opt.equals("-")) {
                                v -= scale;
                            }
                            else if (opt.equals("*")) {
                                v *= scale;
                            }
                            else if (opt.equals("/")) {
                                v /= scale;
                            }
                            else {

                            }

                            curValue = String.valueOf(v);
                        }
                        if (fullPaths.get(i).getUnit() != null)
                            curValue = curValue + fullPaths.get(i).getUnit();

                        String tmp = "";
                        if (runParameter.isShowAliasTitle()) {
                            tmp = String.format(" %s\t\t%s", alignString(fullPaths.get(i).getAlias(), maxPathLength), curValue);
                        } else {
                            tmp = String.format(" %s\t\t%s", alignString(fullPaths.get(i).getPath(), maxPathLength), curValue);
                        }
                        valuesLabels[i] = new Label(tmp);
                        mgr.addWidget(valuesLabels[i],
                                0, startLine, tmp.length(), 20,
                                WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_LEFT);
                        startLine++;
                    }
                    lastValues = curValuesOfFullPaths;
                }
                window.show();
                if (trigger) {
                    tmpWin.close();
                } else
                    trigger = true;

                try {
                    Thread.sleep(duration);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String alignString(String inString, int length) {
        if (inString == null || inString.isEmpty())
            inString = "";

        if (inString.length() > length)
            return inString;

        int spaceNeededNum = length - inString.length();

        for (int i = 0; i < spaceNeededNum; i++) {
            inString += " ";
        }

        assert (inString.length() == length);

        return inString;
    }

    /**
     * @param args : arguments array
     * @return Run Parameter
     */
    private static RunParameter ARGSAnalyser(String[] args) {

        Options options = new Options();
        options.addOption(new Option("h","help",false,"show this help message"))
                .addOption(new Option("u",true,"User name for remote process"))
                .addOption(new Option("p",true,"Password for remote process"))
                .addOption(new Option("f",true,"Path to the configure file"))
                .addOption(new Option("i",true,"Interval between two scan tasks, unit is second"))
                .addOption(new Option("a",false,"Show alias names instead of jmx paths"));

        CommandLineParser parser = new BasicParser();
        RunParameter runParameter = new RunParameter();
        ArrayList<JMXInPath> inputtedPaths = new ArrayList<JMXInPath>();
        try {
            CommandLine cli = parser.parse(options,args);
            if (args.length == 0 || cli.hasOption('h')){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("jmxTop jmxURL [jmx path lists]","To view statuses of jmx paths:",options,"[Use F4 to exit top console]\n@Support by Logicmonitor", true);
                exit(0);
            }

            runParameter.setValid(true);
            if (cli.hasOption('a')) {
                runParameter.setShowAliasTitle(true);
            }
            if (cli.hasOption('f')){
                List<JMXInPath> paths_from_file = getPathsFromFile( cli.getOptionValue('f'));
                inputtedPaths.addAll(paths_from_file);
            }

            if (cli.hasOption('u')){
                runParameter.setUsername(cli.getOptionValue('u'));
            }
            if (cli.hasOption('p')) {
                runParameter.setPassword(cli.getOptionValue('p'));
            }
            if (cli.hasOption('i')){
                try{
                    int interval = Integer.valueOf(cli.getOptionValue('i'));
                    if (interval < 0)
                        System.err.println("The interval value is negative! Using default set!");
                    else {
                        runParameter.setInterval(interval);
                    }
                } catch (Exception e){
                    runParameter.setValid(false);
                    runParameter.setValidInfo(runParameter.getValidInfo() + "Argument after <-i> should be an integer\n");
                }
            }
            List<String> others = cli.getArgList();
            boolean jmxurl_found = false;
            for (String other:others){
                if (other.toLowerCase().startsWith("service:jmx:")) {
                    if (jmxurl_found) {
                        runParameter.setValid(false);
                        runParameter.setValidInfo(runParameter.getValidInfo()+"multiple jmxurl found\n");
                        return runParameter;
                    } else {
                        jmxurl_found = true;
                        runParameter.setSurl(other.toLowerCase());
                    }
                } else {
                    inputtedPaths.add(new JMXInPath(other));
                }
            }
            if (!jmxurl_found) {
                runParameter.setValid(false);
                runParameter.setValidInfo(runParameter.getValidInfo()+"No jmxurl found. The jmxurl should start with \"service:jmx:\" \n");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            runParameter.setValid(false);
            runParameter.setValidInfo(runParameter.getValidInfo()+"Exception caught while parse arguments!\n");
        }

        if (inputtedPaths.isEmpty()) {
            runParameter.setValid(false);
            runParameter.setValidInfo(runParameter.getValidInfo()+"No jmx paths inputted");
        } else {
            runParameter.setPaths(inputtedPaths);
        }
        return runParameter;
    }

    /**
     * Get the paths from the configure file
     * @param filePath path to the configure file
     * @return JMX in paths
     */
    private static List<JMXInPath> getPathsFromFile(String filePath) {
        ArrayList<JMXInPath> pathsFromFile = new ArrayList<JMXInPath>();
        String strFromFile = FileHelper.loadFileIntoString(filePath);

        if (strFromFile == null || strFromFile.isEmpty())
            return pathsFromFile;

        JSONObject confJSON = JSONObject.fromObject(strFromFile);
        // TODO: the usage of version #
        String confVersion = confJSON.optString("version");
        JSONArray pathArrays = confJSON.optJSONArray("paths");
        if (pathArrays == null || pathArrays.isEmpty())
            return pathsFromFile;

        for (Object ob : pathArrays) {

            JSONObject pathStrJSON = JSONObject.fromObject(ob);
            JMXInPath inPath = new JMXInPath(pathStrJSON.optString("path", null), pathStrJSON.optString("alias", null));
            String opt = pathStrJSON.optString("operate", null);

            int scale = pathStrJSON.optInt("scale", 0);
            String unit = pathStrJSON.optString("unit", null);

            if (unit != null)
                inPath.setUnit(unit);

            if (opt != null && !opt.isEmpty() && scale != 0) {
                inPath.setOperator(opt);
                inPath.setScale(scale);
            }
            pathsFromFile.add(inPath);

        }
        return pathsFromFile;

    }

}
