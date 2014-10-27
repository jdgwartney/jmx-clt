package com.logicmonitor.ft.jmx.jmxman;


import com.logicmonitor.ft.jmx.jmxclient.JMXPath;

/**
 * Created by jianyuan on 8/27/2014.
 */
public class JMXFullPath extends JMXPath {

    private boolean isCounter;
    private String alias;
    private String unit = null;
    private String operator = null;
    private int scale = 0;

    public JMXFullPath(String path) {
        super(path);
        isCounter = false;
        autoGenAlias();
    }

    public JMXFullPath(String path, boolean isCounter) {
        super(path);
        this.isCounter = isCounter;
        autoGenAlias();
    }

    public JMXFullPath(String path, boolean isCounter, String alias) {
        super(path);
        this.isCounter = isCounter;
        setAlias(alias);
    }

    static public JMXFullPath valueOf(String path) {
        return new JMXFullPath(path);
    }

    public String getSelectorStr() {
        String[] sels = getSelector();
        if (sels == null) {
            return "";
        }
        String alias = sels[0];
        for (int i = 1; i < sels.length; i++) {
            alias = alias + "." + sels[i];
        }
        return alias;
    }

    private void autoGenAlias() {
        setAlias(getSelectorStr());
    }

    public boolean isCounter() {
        return isCounter;
    }

    public void setCounter(boolean isCounter) {
        this.isCounter = isCounter;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "JMXFullPath{" +
                "isCounter=" + isCounter +
                ", alias='" + alias + '\'' +
                ", unit='" + unit + '\'' +
                ", operator='" + operator + '\'' +
                ", scale=" + scale +
                '}';
    }
}
