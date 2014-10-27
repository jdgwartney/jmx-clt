package com.logicmonitor.ft.jmx.jmxman;

import java.util.ArrayList;

/**
 * Created by jianyuan on 8/28/2014.
 */
public class RunParameter {

    private boolean isValid;
    private String validInfo;

    private String surl;
    private String username;
    private String password;
    private int timeout;

    private boolean showAliasTitle;
    private int times;
    private int interval;   //seconds

    private ArrayList<JMXInPath> paths;

    public RunParameter() {
        this.validInfo = "";
        this.showAliasTitle = false;
        this.isValid = false;

        this.timeout = -1;
        this.username = null;
        this.password = null;

        this.times = -1;
        this.interval = 3;
        this.paths = new ArrayList<JMXInPath>();
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getValidInfo() {
        return validInfo;
    }

    public void setValidInfo(String validInfo) {
        this.validInfo = validInfo;
    }

    public String getSurl() {
        return surl;
    }

    public void setSurl(String surl) {
        this.surl = surl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public ArrayList<JMXInPath> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<JMXInPath> paths) {
        this.paths = paths;
    }

    public boolean isShowAliasTitle() {
        return showAliasTitle;
    }

    public void setShowAliasTitle(boolean showAliasTitle) {
        this.showAliasTitle = showAliasTitle;
    }

    @Override
    public String toString() {
        return "RunParameter{" +
                "isValid=" + isValid +
                ", validInfo='" + validInfo + '\'' +
                ", surl='" + surl + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", timeout=" + timeout +
                ", times=" + times +
                ", interval=" + interval +
                ", paths=" + paths.toString() +
                '}';
    }
}
