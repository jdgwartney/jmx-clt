package com.logicmonitor.ft.jmx.jmxclient;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dengxianzhi on 14-3-25.
 */
public abstract class AbstractClient {

    /**
     * There are four types of JMXPath. Client will use different
     * algorithms to deal with them.
     * <p/>
     * 1. A JMXPath with an index property such as Catalina:type=Cache,host=localhost,path=*
     * <p/>
     * Client will returns an array of values of the property 'path' such as [/, /admin, /core, ...]
     * <p/>
     * 2. A JMXPath with 2 index properties such as Catalina:type=Cache,host=*,path=*
     * <p/>
     * Client will returns an array of String[2] such as [[localhost, /], [localhost, /admin], ...]
     * <p/>
     * 3. A JMXPath with a complete ObjectName but selector such as Catalina:type=Cache,host=localhost,path=/admin
     * <p/>
     * Client will returns an array of names of all attribute of the MBean such as [accessCount, cacheMaxSize,
     * hitsCount, maxAllocateInterations, spareNotFoundEntries, cacheSize, desiedEntryAccessRatio, modelerType]
     * <p/>
     * 4. A JMXPath with a complete ObjectName and selector
     * such as Catalina:type=Cache,host=localhost,path=/admin:accessCount
     * <p/>
     * 5. A JMXPath with a wildcard * in domain.
     * <p/>
     * Client will returns an array of names matching the domain.
     * <p/>
     * Client will treat the value of a MBean attribute as a tree with the name of the attribute as
     * the root. Client will returns all sub-nodes of the node refered by the selector.
     *
     * @throws java.io.IOException
     */
    public ArrayList evaluatePath(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException {
        try {
            if (path.isWildcardDomain() && !path.hasIndexProperty()) {
                return _listDomains(path);
            }
            else if (path.isWildcardDomain() && path.hasIndexProperty()) {
                return _listDomainsAndProperties(path);
            }
            else if (path.hasTwoIndexProperties()) {
                return _listValuesOfIndexProperty2(path);
            }
            else if (path.hasIndexProperty()) {
                return _listValuesOfIndexProperty(path);
            }
            else if (!path.hasSelector()) {
                return _listAttributeNames(path);
            }
            else {
                return _listSubNodes(path);
            }
        }
        catch (MalformedObjectNameException e) {
            throw new IOException(e);
        }
    }
    public ArrayList evaluatePath(String path) throws IOException {
        return evaluatePath(com.logicmonitor.ft.jmx.jmxclient.JMXPath.valueOf(path));
    }

    /**
     * * Given a JMX path without properties, and the domain has wildcard , return the list of values. For example, given the path
     *  java.lang.*:type=Cache, this method will returns [["localhost", "/"], ["localhost", "/admin"], ...]
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException
     */
    protected abstract ArrayList<String> _listDomains(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)throws IOException, MalformedObjectNameException;

    /**
     * Given a JMX path with a index properties, and the domain has wildcard , return the list of values. For example, given the path
     *  java.lang.*:type=Cache,path=*, this method will returns [["localhost", "/"], ["localhost", "/admin"], ...]
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException
     */
    protected abstract ArrayList<String[]> _listDomainsAndProperties(com.logicmonitor.ft.jmx.jmxclient.JMXPath path) throws IOException, MalformedObjectNameException;
    /**
     * Given a JMX path with two index properties, returns the list of values. For example, given the path
     * Catalina:type=Cache,host=*,path=*, this method will returns [["localhost", "/"], ["localhost", "/admin"], ...]
     *
     * @param path the JMX path with 2 index properties
     * @return An array of tuples
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException if path is invalid
     */
    protected abstract ArrayList<String[]> _listValuesOfIndexProperty2(com.logicmonitor.ft.jmx.jmxclient.JMXPath path) throws IOException, MalformedObjectNameException;
    /**
     * Given a JMX path withn an index property, returns the list of values. For example, given the path
     * Catalina:type=Cache,host=localhost,path=*, this method will returns ["/", "/admin", "/manager", ...]
     *
     * @param path the JMX path with an index property
     * @return An array of values of the index property.
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException if path is invalid
     */
    protected abstract ArrayList<String> _listValuesOfIndexProperty(com.logicmonitor.ft.jmx.jmxclient.JMXPath path) throws IOException, MalformedObjectNameException;
    /**
     * Returns an array of attribute name exposed by the MBean. For example, given the
     * JMX path "Catalian:type=Cache,host=localhost,path=/", the moethod will return
     * [modelerType, accessCount, cacheMaxSize, hitsCount, maxAllocateIterations,
     * spareNotFoundEntries, ...]
     *
     * @param path The JMX path with a complete ObjectName but the selector
     * @return An array of attribute names exposed by the MBean
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException if path is invalid
     */
    protected abstract ArrayList<String> _listAttributeNames(com.logicmonitor.ft.jmx.jmxclient.JMXPath path) throws IOException, MalformedObjectNameException;
    /**
     * Returns the sub-nodes of the node refered by the selector in the path. We
     * trreat the value of a MBean attribute as a tree with the attribute name as the
     * root. The selector acts like a file system path, which guides us to traverse
     * the tree and returns all sub-nodes.
     *
     * @param path the JMX path
     * @return an array of sub-nodes of the node
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException
     */
    protected abstract ArrayList<String> _listSubNodes(com.logicmonitor.ft.jmx.jmxclient.JMXPath path) throws IOException, MalformedObjectNameException;

    /**
     * return the string value from the given jmx path
     * @param spath
     * @return
     * @throws java.io.IOException
     */
    public String getValue(String spath) throws IOException {
        com.logicmonitor.ft.jmx.jmxclient.JMXPath path = com.logicmonitor.ft.jmx.jmxclient.JMXPath.valueOf(spath);

        ArrayList<?> rs = evaluatePath(path);
        if (rs == null || rs.size() == 0) {
            return null;
        }

        Object r = rs.get(0);
        return r.toString();
    }

    /**
     * get the array value from the given jmx path
     * @param spath
     * @return
     * @throws java.io.IOException
     */
    public Object[] getChildren(String spath) throws IOException {
        com.logicmonitor.ft.jmx.jmxclient.JMXPath path = com.logicmonitor.ft.jmx.jmxclient.JMXPath.valueOf(spath);

        ArrayList rs = evaluatePath(path);
        if (rs == null) {
            return new Object[0];
        }

        return rs.toArray();
    }

    public void close() {
    }


}
