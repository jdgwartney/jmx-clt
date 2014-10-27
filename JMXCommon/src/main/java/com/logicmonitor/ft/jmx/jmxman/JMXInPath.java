package com.logicmonitor.ft.jmx.jmxman;

/**
 * Created by jianyuan on 8/28/2014.
 */
public class JMXInPath {
    private String path;
    private String alias;

    private String unit = null;
    private String operator = null;
    private int scale = 0;


    public JMXInPath(String path) {
        this.path = path;
        this.alias = null;
    }

    public JMXInPath(String path, String alias) {
        this.path = path;
        this.alias = alias;
    }

    public static JMXInPath valueOf(String path) {
        return new JMXInPath(path);
    }

    public String getPath() {
        return path;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "JMXInPath{" +
                "path='" + path + '\'' +
                ", alias='" + alias + '\'' +
                ", unit='" + unit + '\'' +
                ", operator='" + operator + '\'' +
                ", scale=" + scale +
                '}';
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
}
