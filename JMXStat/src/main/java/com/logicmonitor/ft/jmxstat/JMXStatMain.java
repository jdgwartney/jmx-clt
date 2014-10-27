package com.logicmonitor.ft.jmxstat;

import com.logicmonitor.ft.jmx.jmxman.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.cli.*;
import org.fusesource.jansi.AnsiConsole;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by vincent on 14-10-16.
 */
public class JMXStatMain {

    public static void main(String[] args){
        RunParameter runParameter = ARGSAnalyser(args);
        if (!runParameter.isValid()) {
            System.out.println(runParameter.getValidInfo());
            System.out.println("<Please check your arguments>");
            exit(-1);
        }
        try {
            JMXMan jmxman = new JMXMan(runParameter.getSurl(),runParameter.getUsername(),runParameter.getPassword(),runParameter.getTimeout());
            jmxman.init(runParameter);
            statJMXValues(jmxman,runParameter);
        } catch (Exception e) {
            System.out.println("Fail to connect to process, check the surl,username and password");
            e.printStackTrace();
        }
    }

    private static void statJMXValues(JMXMan jmxMan, RunParameter runParameter) {
        AnsiConsole.systemInstall();

        if (!jmxMan.hasFullPath()){
            System.err.print("Can not discover any full path for inputted paths, please check the path format!");
            exit(-1);
        }
        List<String> lastValues;
        List<String> values ;
        lastValues = jmxMan.getValues();
        List<JMXFullPath> jmxFullPaths = jmxMan.getJmxFullPaths();
        int columnLen[] = new int[jmxFullPaths.size()];

        _showCompanyInfo();
        if (runParameter.isShowAliasTitle()) {
            for (int i = 0; i < jmxFullPaths.size(); i++) {
                int tmp = jmxFullPaths.get(i).getDomain().length() > jmxFullPaths.get(i).getPropertyList().length() ? jmxFullPaths.get(i).getDomain().length() : jmxFullPaths.get(i).getPropertyList().length();
                int tmp2 = jmxFullPaths.get(i).getSelectorStr().length() > jmxFullPaths.get(i).getAlias().length() ? jmxFullPaths.get(i).getSelectorStr().length() : jmxFullPaths.get(i).getAlias().length();

                columnLen[i] = tmp > tmp2 ? tmp : tmp2;
            }
            _showTitles(jmxFullPaths,columnLen);
        } else {
            for (int i = 0; i < jmxFullPaths.size(); i++) {
                columnLen[i] = jmxFullPaths.get(i).getPath().length();
            }
            _showPathTitles(jmxFullPaths);
        }
        int times = runParameter.getTimes();
        int interval = runParameter.getInterval();

        if (times > 0) {

            while (times > 0) {
                values = jmxMan.getValues();
                _showValues(jmxFullPaths, values, lastValues ,columnLen);
                times--;
                lastValues = values;
                try {
                    Thread.sleep(interval * 1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            while (true) {
                values = jmxMan.getValues();
                _showValues(jmxFullPaths, values, lastValues ,columnLen);
                lastValues = values;
                try {
                    Thread.sleep(interval * 1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        AnsiConsole.systemUninstall();
    }

    private static void _showCompanyInfo() {
        System.out.println(" JMXstat was created by LogicMonitor under the BSD3 License.");
        System.out.println("");
        System.out.println(" To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.");
        System.out.println(" For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmxtools.");
        System.out.println("");
    }

    private static void _showPathTitles(List<JMXFullPath> jmxFullPaths) {
        for (int i = 0; i < jmxFullPaths.size(); i++) {
            System.out.printf(String.format(" %s ", jmxFullPaths.get(i).getPath()));
        }
        System.out.printf("\n");
    }

    private static void _showTitles(List<JMXFullPath> jmxFullPaths, int columnLen[]) {

        for (int i = 0; i < jmxFullPaths.size(); i++) {
            System.out.printf(String.format(" %s ", fixLengthStr(jmxFullPaths.get(i).getDomain(),columnLen[i])));
        }
        System.out.println();

        for (int i = 0; i < jmxFullPaths.size(); i++) {
            System.out.printf(String.format(" %s ", fixLengthStr(jmxFullPaths.get(i).getPropertyList(),columnLen[i])));
        }
        System.out.println();

        for (int i = 0; i < jmxFullPaths.size(); i++) {
            System.out.printf(String.format(" %s ", fixLengthStr(jmxFullPaths.get(i).getSelectorStr(),columnLen[i])));
        }
        System.out.println();

        for (int i = 0; i < jmxFullPaths.size(); i++) {
            System.out.printf(String.format(" %s ", fixLengthStr(jmxFullPaths.get(i).getAlias(),columnLen[i])));
        }

        System.out.println();
    }

    private static void _showValues(List<JMXFullPath> jmxFullPaths,List<String> values, List<String> lastValues, int columnLen[]){
        String combined_all = "";
        for (int i = 0; i < jmxFullPaths.size(); i++) {

            String value ;
            if (jmxFullPaths.get(i).isCounter()) {
                long rs = Long.valueOf(values.get(i)) - Long.valueOf(lastValues.get(i));
                value = String.valueOf(rs);
            }
            else {
                value = values.get(i);
            }

            if (jmxFullPaths.get(i).getOperator() != null && jmxFullPaths.get(i).getScale() != 0) {

                String opt = jmxFullPaths.get(i).getOperator();
                int scale = jmxFullPaths.get(i).getScale();

                long v = Long.valueOf(value);

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
                else {}
                value = String.valueOf(v);
            }

            if (jmxFullPaths.get(i).getUnit() != null)
                value = value + jmxFullPaths.get(i).getUnit();

            combined_all = combined_all + " " + fixLengthStr(value, columnLen[i]) + " ";
        }

        System.out.printf("%s\n", combined_all);
    }

    private static String fixLengthStr(String inStr, int length) {

        //TODO: The source String is longer than dest-length, just return the origin String currently
        if (inStr.length() >= length)
            return inStr;

        int spc = length - inStr.length();
        int left_spaces_count = spc / 2;
        int right_spaces_count = spc - left_spaces_count;

        String left_spaces = "";
        for (int i = 0; i < left_spaces_count; i++)
            left_spaces += " ";

        String right_spaces = "";
        for (int i = 0; i < right_spaces_count; i++)
            right_spaces += " ";

        return left_spaces + inStr + right_spaces;
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
                .addOption(new Option("t",true,"Exit after scanning jmx-paths n times"))
                .addOption(new Option("i",true,"Interval between two scan tasks"))
                .addOption(new Option("A",false,"Show alias names instead of jmx paths"));

        CommandLineParser parser = new BasicParser();
        RunParameter runParameter = new RunParameter();
        ArrayList<JMXInPath> inputtedPaths = new ArrayList<JMXInPath>();
        try {
            CommandLine cli = parser.parse(options,args);
            if (args.length == 0 || cli.hasOption('h')){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("jmxstat jmxURL [argument] [path lists]","To view statuses of jmx paths:",options,"@Support by Logicmonitor",true);
                exit(0);
            }

            runParameter.setValid(true);
            if (cli.hasOption('A')) {
                runParameter.setShowAliasTitle(true);
            }
            if (cli.hasOption('f')){
                List<JMXInPath> paths_from_file = getPathsFromFile( cli.getOptionValue('f'));
                inputtedPaths.addAll(paths_from_file);
            }
            if (cli.hasOption('t')){
                try {
                    int times = Integer.valueOf(cli.getOptionValue('t'));
                    if (times < 0)
                        System.out.println("The argument after <-t> is useless here since it's a negative number.");
                    else
                        runParameter.setTimes(times);
                } catch (Exception e) {
                    runParameter.setValid(false);
                    runParameter.setValidInfo(runParameter.getValidInfo()+ "Argument after <-t> should be an integer\n");
                }
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
                if (other.toLowerCase().startsWith("service:jmx")) {
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
                runParameter.setValidInfo(runParameter.getValidInfo()+"No jmxurl found. The jmxurl should start with \"service:jmx\" \n");
            }
        } catch (ParseException e) {
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
