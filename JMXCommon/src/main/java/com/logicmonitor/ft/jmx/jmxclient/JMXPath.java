package com.logicmonitor.ft.jmx.jmxclient;

import com.logicmonitor.ft.jmx.util.StringUtil;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.*;

/**
 * {@code JMXPath} is used to represent a JMX path that is used to retrieve data from
 * a JMX server.
 * <p/>
 * A path consists of three parts - domain, propertyList, and selector (optional) in the
 * following format:
 * <p/>
 * <domain>:<propertyList>[:<selector>]
 * <p/>
 * For example,
 * <p/>
 * java.lang:type=MemoryPool,name=*
 * Catalina:type=ThreadPool,path=/localhost:Count
 * LogicMonitor:type=rrdfs:MapOfMapStats.demo.key1
 * java.lang:type=*,name=*
 * <p/>
 * Usually, the caller use JMXPath to retrieve three types of data
 * <p/>
 * 1. get a metrics. "Catalina:type=ThreadPool,path=/localhost:Count" returns
 * TOMCAT threadpool size for the context "/localhost".
 * <p/>
 * 2. list all values of the index property. For example,
 * "java.lang:type=MemoryPool,name=*" ('name' is the index property) will returns
 * all memorypool names.
 * <p/>
 * 3. list all sub-nodes of the selector (actually, the case 1 is the special case of
 * this).
 */
public class JMXPath {
    /**
     * The original path string
     */
    private final String _path;

    /**
     * The domain part of JMX ObjectName such as java.lang, Catalina, etc.
     */
    private final String _domain;

    /**
     * The property list part of JMX ObjectName such as "type=MemoryPool,path=/". If the
     * value of a property is "*" - this property is called the index property. "path" is
     * the index property of "type=MemoryPool,path=*", for example.
     * <p/>
     * A JMXPath with an index property ISN'T allow to contain a selector part.
     * <p/>
     * If there is the index property, _propertyList just contains the non-index properties.
     * <p/>
     * Also, we jsut allow at most one index property that must be the last property in the list.
     */
    private final String _propertyList;

    /**
     * The index property if there is one in the path
     */
    private String _indexProperty = null;
    private String _indexProperty2 = null;

    /**
     * The selector part of a JMX path. A selector consists of multiple stages. The first
     * stage must be one of attribute exported by the MBean pointed by the ObjectName (_domain:_propertyList).
     * <p/>
     * Then we treat the value of a MBean's attribute as a tree. The 2nd stage to the last stage is
     * a path to traverse the tree.
     */
    private final String[] _selector;

    // ------------------------------------------------------------------------

    /**
     * Parse a JMX path string into a JMXPath instance
     *
     * @param path The JMX path string
     * @return JMXPath  The object representing the parsed JMX path
     */
    static public JMXPath valueOf(String path) {
        return new JMXPath(path);
    }

    /**
     * Parse a give path string.
     *
     * @param path the path in string
     * @throws NullPointerException     if path is NULL
     * @throws IllegalArgumentException if "path" is not a valid path
     */
    public JMXPath(String path) {
        _path = path;

        if (path == null) {
            throw new NullPointerException();
        }

        String[] tokens = StringUtil.split(path, ":", 3);
        if (tokens.length != 2 && tokens.length != 3) {
            throw new IllegalArgumentException(String.format(
                    "'%s' is not a valid path", path
            ));
        }

        _domain = tokens[0];

        String[] props = StringUtil.split(tokens[1], ",");
        StringBuilder propList = new StringBuilder();
        int idx = 0;
        for (String prop : props) {
            String[] tmp = StringUtil.split(prop, "=");
            if (tmp.length != 2) {
                throw new IllegalArgumentException(path + " isn't a valid JMX path");
            }
            if (tmp[1].equals("*")) { // this is an index property
                if (idx == 0) {
                    _indexProperty = tmp[0];
                    idx++;
                }
                else if (idx == 1) {
                    _indexProperty2 = tmp[0];
                    idx++;
                }
                else {
                    throw new IllegalArgumentException("The path contains more than 2 index properties");
                }
            }
            else {
                if (propList.length() > 0) {
                    propList.append(',');
                }
                propList.append(prop);
            }
        }

        _propertyList = propList.toString();

        // parse the selector
        if (tokens.length == 3) {
            _selector = StringUtil.split(tokens[2], ".");
        }
        else {
            _selector = null;
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Check if a given path is a valid and full JMX path. A full JMX path
     * consists of domain, property list, and the selector
     */
    static public boolean isFullJMXPath(String path) {
        // why 7? a minimum full JMX path will be a:b=c:d
        if (path == null || path.length() <= 7) {
            return false;
        }

        String[] tokens = StringUtil.split(path, ":", 3);
        if (tokens.length != 3) {
            return false;
        }

        if (tokens[1].indexOf("=") < 0) {
            return false;
        }

        return true;
    }

    // ------------------------------------------------------------------------

    public String toString() {
        return _path;
    }

    // ------------------------------------------------------------------------

    public String getPath() {
        return _path;
    }

    public String getDomain() {
        return _domain;
    }

    public String getPropertyList() {
        return _propertyList;
    }

    public String getIndexProperty() {
        return _indexProperty;
    }

    public String getIndexProperty2() {
        return _indexProperty2;
    }

    public String[] getSelector() {
        return _selector;
    }

    // ------------------------------------------------------------------------

    public boolean isWildcardDomain() {
        return _domain.indexOf('*') >= 0;
    }

    /**
     * Returns the MBean ObjectName
     */
    public String getObjectName() {
        return getObjectName(_domain);
    }

    public String buildPathWithDomain(String domain) {
        String[] tokens = StringUtil.split(_path, ":", 2);
        if (tokens.length == 1) {
            return domain;
        }
        else {
            return String.format("%s:%s", domain, tokens[1]);
        }
    }

    public String getObjectName(String domain) {
        if (_indexProperty == null) {
            return String.format("%s:%s", domain, _propertyList);
        }

        if (_propertyList.length() == 0) {
            return String.format("%s:*", domain);
        }
        else {
            return String.format("%s:%s,*", domain, _propertyList);
        }
    }

    public boolean hasIndexProperty() {
        return _indexProperty != null;
    }

    public boolean hasTwoIndexProperties() {
        return _indexProperty2 != null;
    }

    /**
     * Return true if an SBA attribute selector is give, false if not
     */
    public boolean hasSelector() {
        return _selector != null;
    }

    /**
     * Return the attribute name of the JMX Mbean instance to be selected
     */
    public String getJMXAttribute() {
        return _selector != null ? _selector[0] : null;
    }

    // ------------------------------------------------------------------------

    /**
     * Travers the value tree to return an array of child nodes
     *
     * @param attrValue the value of the attribute
     * @return an array of child nodes
     * @throws IllegalArgumentException if the travers fails
     */
    public ArrayList<String> listSubNodes(Object attrValue) {
        int curIndex = 0;
        Object curValue = attrValue;

        while (_selector.length > curIndex + 1) {
            curValue = _getChild(curValue, _selector[curIndex + 1]);
            curIndex++;
        }

        return _toArray(curValue);
    }

    private Object _getChild(Object o, String childName)
            throws IllegalArgumentException {
        if (o instanceof TabularData) {
            // childName has the format key1[,key2...]
            String[] key = StringUtil.split(childName, ",", true);
            return ((TabularData) o).get(key);
        }
        else if (o instanceof CompositeData) {
            CompositeData cds = (CompositeData) o;
            for (String key : cds.getCompositeType().keySet()) {

                if (key.equalsIgnoreCase(childName)) {
                    return cds.get(key);
                }
            }
        }
        else if (o instanceof Set) {
            int i = Integer.valueOf(childName);
            Set s = (Set) o;
            int p = 0;
            for (Object k : s) {
                if (p == i) {
                    return k;
                }
                p++;
            }
        }
        else if (o instanceof List) {
            int i = Integer.valueOf(childName);
            List l = (List) o;
            return l.get(i);
        }
        else if (o.getClass().isArray()) {
            int i = Integer.valueOf(childName);
            Object[] a = (Object[]) o;
            return a[i];
        }
        else if (o instanceof Map) {
            Map m = (Map) o;
            for (Object key : m.keySet()) {
                if (key.toString().equalsIgnoreCase(childName)) {
                    return m.get(key);
                }
            }
        }

        throw new IllegalArgumentException("No such child");
    }

    private ArrayList<String> _toArray(Object o) {
        ArrayList<String> r = new ArrayList<String>();

        if (o instanceof TabularData) {
            TabularData td = (TabularData) o;
            for (CompositeData v : (Collection<CompositeData>) td.values()) {
                Object[] key = td.calculateIndex(v);
                StringBuilder buf = new StringBuilder();
                for (Object k : key) {
                    if (buf.length() > 0) {
                        buf.append(",");
                    }
                    buf.append(k.toString());
                }

                r.add(buf.toString());
            }
        }
        else if (o instanceof CompositeData) {
            CompositeData cds = (CompositeData) o;
            for (String key : cds.getCompositeType().keySet()) {
                r.add(key);
            }
        }
        else if (o instanceof Set) {
            Set s = (Set) o;
            for (Object k : s) {
                r.add(k.toString());
            }
        }
        else if (o instanceof List) {
            List l = (List) o;
            for (Object k : l) {
                r.add(k.toString());
            }
        }
        else if (o instanceof Map) {
            Map m = (Map) o;
            for (Object key : m.keySet()) {
                r.add(key.toString());
            }
        }
        else if (o.getClass().isArray()) {
            Object[] a = (Object[]) o;
            for (Object k : a) {
                r.add(k.toString());
            }
        }
        else {
            r.add(o.toString());
        }

        return r;
    }
}

