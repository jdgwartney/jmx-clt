package com.logicmonitor.ft.jmx.jmxman;

import com.logicmonitor.ft.jmx.jmxclient.Client;
import com.logicmonitor.ft.jmx.jmxclient.JMXPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincent on 14-10-15.
 */
public class JMXMan {

    private Client client;

    private List<JMXFullPath> jmxFullPaths;
    private List<String> uselessPaths;

    public JMXMan(String surl, String username, String password, long timeout) throws IOException {
        client = new Client(surl, username, password, timeout);
        jmxFullPaths = new ArrayList<JMXFullPath>();
        uselessPaths = new ArrayList<String>();
    }

    public void init(RunParameter runParameter) {
        discoverFullPaths(runParameter.getPaths());
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < jmxFullPaths.size(); i++) {

            try {
                values.add(getValue(jmxFullPaths.get(i).getPath()));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error happened while get values");
                break;
            }
        }

        return values;
    }

    public List<String> getValues(List<String> fullPaths) {
        List<String> values = new ArrayList<String>();
        for (String fullPath : fullPaths) {
            try {
                values.add(getValue(fullPath));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error happened while get values");
                break;
            }
        }
        return values;
    }

    public String getValue(String fullPath) throws IOException {
        return client.getValue(fullPath);
    }

    public boolean hasFullPath() {
        return !jmxFullPaths.isEmpty();
    }

    public List<String> getIndexValues(String used, String committed) {
        List<String> namesOfUsed;
        List<String> namesOfCommitted;

        try {
            namesOfUsed = client.evaluatePath(used);
            namesOfCommitted = client.evaluatePath(committed);

            if (namesOfCommitted.equals(namesOfUsed))
                return namesOfCommitted;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void discoverFullPaths(List<JMXInPath> inPaths) {

        for (int i = 0; i < inPaths.size(); i++) {
            boolean isCounter = inPaths.get(i).getPath().startsWith("counter@");
            String inpath = isCounter ? inPaths.get(i).getPath().substring("counter@".length()) : inPaths.get(i).getPath();

            if (isFullPath(inpath)) {
                JMXFullPath jmxFullPath = new JMXFullPath(inpath, isCounter);
                jmxFullPath.setOperator(inPaths.get(i).getOperator());
                jmxFullPath.setScale(inPaths.get(i).getScale());
                jmxFullPath.setUnit(inPaths.get(i).getUnit());
                if (inPaths.get(i).getAlias()!=null && !inPaths.get(i).getAlias().isEmpty())
                    jmxFullPath.setAlias(inPaths.get(i).getAlias());
                jmxFullPaths.add(jmxFullPath);
            } else {
                // TODO generate the full path of it
                List<JMXFullPath> genJMXFullPaths = null;
                if (inPaths.get(i).getAlias() != null && !inPaths.get(i).getAlias().isEmpty()) {
                    genJMXFullPaths = generateFullPaths(inpath, isCounter, inPaths.get(i).getAlias());
                } else {
                    genJMXFullPaths = generateFullPaths(inpath, isCounter);
                }

                if (genJMXFullPaths == null || genJMXFullPaths.isEmpty()) {
                    uselessPaths.add(inPaths.get(i).toString());
                } else {
                    for (int j = 0; j < genJMXFullPaths.size(); j++) {

                        (genJMXFullPaths.get(j)).setOperator(inPaths.get(i).getOperator());
                        (genJMXFullPaths.get(j)).setScale(inPaths.get(i).getScale());
                        (genJMXFullPaths.get(j)).setUnit(inPaths.get(i).getUnit());
                    }
                    jmxFullPaths.addAll(genJMXFullPaths);
                }

            }
        }
    }

    private boolean isFullPath(String path) {
        try {
            JMXPath jmxPath = JMXPath.valueOf(path);
            if (jmxPath.isWildcardDomain() || jmxPath.hasIndexProperty() || jmxPath.hasTwoIndexProperties())
                return false;
            ArrayList rsts = client.evaluatePath(path);
            if (rsts.size() == 1)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    private List<JMXFullPath> generateFullPaths(String pathStr, boolean isCounterValue, String alias) {
        List<String> generatedFullPaths = generateFullPaths(pathStr);

        if (generatedFullPaths == null || generatedFullPaths.isEmpty()) {
            return null;
        }
        List<JMXFullPath> jmxFullPaths = new ArrayList<JMXFullPath>();
        for (int i = 0; i < generatedFullPaths.size(); i++) {
            JMXFullPath jmxFullPath = new JMXFullPath(generatedFullPaths.get(i), isCounterValue, alias + "_" + i);
            jmxFullPaths.add(jmxFullPath);
        }

        return jmxFullPaths;
    }

    public List<JMXFullPath> generateFullPaths(String pathStr, boolean isCounterValue) {
        List<String> generatedFullPaths = generateFullPaths(pathStr);

        if (generatedFullPaths == null || generatedFullPaths.isEmpty()) {
            return null;
        }
        ArrayList<JMXFullPath> jmxFullPaths = new ArrayList<JMXFullPath>();
        for (String path : generatedFullPaths) {
            JMXFullPath jmxFullPath = new JMXFullPath(path, isCounterValue);
            jmxFullPaths.add(jmxFullPath);
        }

        return jmxFullPaths;
    }

    public List<String> generateFullPaths(String pathStr) {
        JMXPath path;
        try {
            path = JMXPath.valueOf(pathStr);
        } catch (Exception e) {
            return null;
        }

        List<String> generatedFullPath = new ArrayList<String>();
        // path with index properties: first fill the index properties.
        if (path.isWildcardDomain() || path.hasIndexProperty() || path.hasTwoIndexProperties()) {
            List<String> filledPaths = _fillIndexProperty(path);
            if (filledPaths == null || filledPaths.isEmpty())
                return null;
            for (String filledPath : filledPaths) {
                if (isFullPath(filledPath)) {
                    generatedFullPath.add(filledPath);
                } else {
                    ArrayList<String> comb_sel_paths = _discoverAllSelectors(JMXPath.valueOf(filledPath));
                    if (comb_sel_paths != null && !comb_sel_paths.isEmpty()) {
                        generatedFullPath.addAll(comb_sel_paths);
                    }
                }
            }
        } else {
            ArrayList<String> combinedFullPaths = _discoverAllSelectors(path);

            if (combinedFullPaths != null && !combinedFullPaths.isEmpty()) {
                generatedFullPath.addAll(combinedFullPaths);
            }
        }

        return generatedFullPath;
    }

    private List<String> _fillIndexProperty(JMXPath path) {
        List<String> indexFilled = new ArrayList<String>();
        try {
            List<Object> results = client.evaluatePath(path);
            List<Object> temp = new ArrayList<Object>();
            for (Object object: results) {
                if (!temp.contains(object)) {
                    temp.add(object);
                }
            }
            results = temp;

            if (path.isWildcardDomain()) {
                if (path.hasIndexProperty()) {
                    String tmp[] = path.getDomain().split("\\*");

                    String[] selectors = path.getSelector();
                    String combinedSelector = ":";
                    if (selectors != null && selectors.length > 0) {
                        combinedSelector = combinedSelector + selectors[0];
                        for (int i = 1; i < selectors.length; i++)
                            combinedSelector = combinedSelector + "." + selectors[i];
                    }
                    for (Object result : results) {
                        String[] indexV = (String[]) result;
                        String filled = "";
                        String domain = "";
                        // get domain
                        if (tmp.length == 0) {
                            domain = indexV[0];
                        }
                        else if (tmp.length == 1) {
                            if (path.getDomain().startsWith("*")) {
                                domain = indexV[0] + tmp[0];
                            } else {
                                domain = tmp[0] + indexV[0];
                            }
                        }
                        else if (tmp.length == 2) {
                            domain = tmp[0] + indexV[0] + tmp[1];
                        }


                        if (indexV.length == 2) {
                            filled = domain + ":" + path.getPropertyList() + "," + path.getIndexProperty() + "=" + indexV[1] + combinedSelector;
                        }
                        else if (indexV.length == 3) {
                            filled = domain + ":" + path.getIndexProperty() + "=" + indexV[1] + "," + path.getIndexProperty2() + "=" + indexV[2] + combinedSelector;
                        }

                        indexFilled.add(filled);
                    }
                } else {
                    String tmp = path.getDomain() + ":" + path.getPropertyList();
                    String[] selectors = path.getSelector();
                    if (selectors != null && selectors.length > 0) {
                        tmp = tmp + ":" + selectors[0];
                        for (int i = 1; i < selectors.length; i++)
                            tmp = tmp + "." + selectors[i];
                    }
                    String splits[] = tmp.split("\\*");

                    for (Object result : results) {
                        indexFilled.add(splits[0] + result.toString() + splits[1]);
                    }
                }
            } else if (path.hasTwoIndexProperties()) {
                String properName1 = path.getIndexProperty();
                String properName2 = path.getIndexProperty2();

                String[] selectors = path.getSelector();
                String combinedSelector = "";


                if (selectors != null && selectors.length > 0) {
                    combinedSelector = combinedSelector + ":" + selectors[0];
                    for (int i = 1; i < selectors.length; i++)
                        combinedSelector = combinedSelector + "." + selectors[i];
                }

                String properties = path.getPropertyList();
                if (properties!= null && properties.isEmpty()) {
                    properties = "";
                } else {
                    properties = properties + ",";
                }
                for (Object result : results) {
                    String[] indexV = (String[]) result;
                    String filled = path.getDomain() + ":" + properties + properName1 + "=" + indexV[0] + "," + properName2 + "=" + indexV[1] + combinedSelector;
                    indexFilled.add(filled);
                }
            } else if (path.hasIndexProperty()) {
                for (Object result : results) {
                    String filled = path.getDomain() + ":" + path.getPropertyList() + "," + path.getIndexProperty() + "=" + result.toString();
                    String[] selectors = path.getSelector();
                    if (selectors != null &&selectors.length > 0) {
                        filled = filled + ":" + selectors[0];
                        for (int i = 1; i < selectors.length; i++)
                            filled = filled + "." + selectors[i];
                    }
                    indexFilled.add(filled);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }

        return indexFilled;
    }

    private ArrayList<String> _discoverAllSelectors(JMXPath path) {

        List<String> combinedPaths = new ArrayList<String>();
        if (!path.hasSelector()) {// Without selector, discover the first selector and combine with current path with ":"
            List selectors;
            try {
                selectors = client.evaluatePath(path);
                if (selectors == null || selectors.isEmpty()) {
                    return null;
                } else {
                    for (Object selector : selectors) {
                        String combined = path.getDomain() + ":" + path.getPropertyList() + ":" + selector.toString();
                        combinedPaths.add(combined);
                    }
                }
            } catch (Exception e) {
                return null;
            }

        } else {
            ArrayList selectors;
            try {
                selectors = client.evaluatePath(path);
                if (selectors == null || selectors.isEmpty()) {
                    return null;
                } else {
                    for (Object selector : selectors) {

                        String[] cur_selectors = path.getSelector();
                        String combined = path.getDomain() + ":" + path.getPropertyList() + ":" + cur_selectors[0];
                        for (int i = 1; i < cur_selectors.length; i++) {
                            combined = combined + "." + cur_selectors[i];
                        }
                        combined = combined + "." + selector.toString();
                        combinedPaths.add(combined);
                    }
                }
            } catch (Exception e) {
                return null;
            }

        }

        ArrayList<String> combinedFullPaths = new ArrayList<String>();
        for (String combinedPath : combinedPaths) {
            if (isFullPath(combinedPath)) {
                combinedFullPaths.add(combinedPath);
            } else {
                ArrayList<String> fullpaths = _discoverAllSelectors(JMXPath.valueOf(combinedPath));
                if (fullpaths != null && !fullpaths.isEmpty()) {
                    combinedFullPaths.addAll(fullpaths);

                }
            }
        }
        return combinedFullPaths;
    }

    public List<JMXFullPath> getJmxFullPaths() {
        return jmxFullPaths;
    }
}
