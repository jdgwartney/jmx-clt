package com.logicmonitor.ft.jmx.jmxclient;

import com.logicmonitor.ft.jmx.util.StringUtil;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * {@code Client} create a MBeanServerConnection to the MBean server
 * sepecified by a url. Then the user can call {@code getObjects}
 * to retrieve values.
 */
public class Client extends com.logicmonitor.ft.jmx.jmxclient.AbstractClient {
    private JMXConnector _connector;
    private MBeanServerConnection _connection;

    // ------------------------------------------------------------------------

    /**
     * open a JMX connection using specific JMX URL and credentials
     *
     * @param surl     the JMX url string
     * @param username the username. It could be null if no username needed
     * @param password the password
     * @param timeout  timeout in milliseconds. 0 means no timeout
     * @throws java.io.IOException if url is malformat or can't connect to the jmx server
     */
    public Client(String surl, String username, String password, long timeout)
            throws IOException {
        JMXServiceURL url = new JMXServiceURL(surl);

        HashMap<String, Object> env = new HashMap<String, Object>();
        if (timeout > 0) {
            env.put("jmx.remote.x.request.waiting.timeout", Long.toString(timeout));
        }

        if (username != null && username.length() > 0) {
            String[] credentials = {username, password};
            env.put("jmx.remote.credentials", credentials);
        }

        if (timeout > 0) {
            _connectWithTimeout(url, timeout, env);
        }
        else {
            _connect(url, env);
        }

        _connection = _connector.getMBeanServerConnection();
    }

    /**
     * Connect to the JMX server with timeout in milliseconds
     *
     * @param url     the JMXServiceUrl
     * @param timeout the timeout in milliseconds
     * @param env     the environment variables
     * @throws java.io.IOException
     */
    private void _connectWithTimeout(final JMXServiceURL url,
                                     final long timeout,
                                     final HashMap<String, Object> env)
            throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor(_daemonThreadFactory);
        final BlockingQueue<Object> mailbox = new ArrayBlockingQueue<Object>(1);

        executor.submit(new Runnable() {
            public void run() {
                try {
                    JMXConnector connector = JMXConnectorFactory.connect(url, env);
                    if (!mailbox.offer(connector)) {
                        connector.close();
                    }
                }
                catch (Throwable t) {
                    mailbox.offer(t);
                }
            }
        });

        Object result;
        try {
            result = mailbox.poll(timeout, TimeUnit.MILLISECONDS);
            if (result == null) {
                if (!mailbox.offer("")) {
                    result = mailbox.take();
                }
            }
        }
        catch (InterruptedException e) {
            throw initCause(new InterruptedIOException(e.getMessage()), e);
        }
        finally {
            executor.shutdown();
        }

        if (result == null) {
            throw new SocketTimeoutException("Connect timed out: " + url);
        }
        if (result instanceof JMXConnector) {
            _connector = (JMXConnector) result;
        }
        else {
            try {
                throw (Throwable) result;
            }
            catch (IOException e) {
                throw e;
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Error e) {
                throw e;
            }
            catch (Throwable e) {
                throw new IOException(e.toString(), e);
            }
        }
    }

    private static <T extends Throwable> T initCause(T wrapper, Throwable wrapped) {
        wrapper.initCause(wrapped);
        return wrapper;
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }

    private static final ThreadFactory _daemonThreadFactory = new DaemonThreadFactory();

    /**
     * Connect to the JMX server without timeout
     *
     * @param url the JMXServiceUrl
     * @param env the environment variables
     * @throws java.io.IOException
     */
    private void _connect(JMXServiceURL url, HashMap<String, Object> env)
            throws IOException {
        try {
            _connector = JMXConnectorFactory.connect(url, env);
        }
        catch (SecurityException e) {
            throw new IOException(e.toString(), e);
        }
    }

    /**
     * Close JMXConnector and MBeanServerConnection
     */
    public void close() {
        if (_connector != null) {
            try {
                _connector.close();
            }
            catch (Exception e) {
            }
        }
    }

    /**
     * Given a JMX path withn an index property, returns the list of values. For example, given the path
     * Catalina:type=Cache,host=localhost,path=*, this method will returns ["/", "/admin", "/manager", ...]
     *
     * @param path the JMX path with an index property
     * @return An array of values of the index property.
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException if path is invalid
     */
    protected ArrayList<String> _listValuesOfIndexProperty(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException, MalformedObjectNameException {
        String strObjName = path.getObjectName();
        /*Logger.info("Client._listValuesOfIndexProperty", String.format(
                "ObjectName=%s", strObjName
        ));*/

        ArrayList<String> results = new ArrayList<String>();
        for (ObjectName name : _connection.queryNames(new ObjectName(strObjName), null)) {
            String key = name.getKeyProperty(path.getIndexProperty());
            if (key == null || key.length() == 0) {
                //Logger.info("Client._listValuesOfIndexProperty", "Ignore object name - " + name);
            }
            else {
                results.add(key);
            }
        }

        return results;
    }


    protected ArrayList<String> _listDomains(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException, MalformedObjectNameException {
        ArrayList<String> values = new ArrayList<String>();

        String[] domains = _connection.getDomains();
        for (String domain : domains) {
            String capture = StringUtil.matchGlobAndCapture(path.getDomain(), domain);
            if (capture == null) {
                continue;
            }

            try {
                MBeanInfo mbeanInfo = _connection.getMBeanInfo(new ObjectName(path.getObjectName(domain)));
                values.add(capture);
            }
            catch (InstanceNotFoundException e) {
                continue;
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }

        return values;
    }


    protected ArrayList<String[]> _listDomainsAndProperties(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException, MalformedObjectNameException {
        ArrayList<String[]> values = new ArrayList<String[]>();

        String[] domains = _connection.getDomains();
        for (String domain : domains) {
            String capture = StringUtil.matchGlobAndCapture(path.getDomain(), domain);
            if (capture == null) {
                continue;
            }

            com.logicmonitor.ft.jmx.jmxclient.JMXPath newPath = new com.logicmonitor.ft.jmx.jmxclient.JMXPath(path.buildPathWithDomain(domain));
            if (newPath.hasTwoIndexProperties()) {
                for (String prop[] : this._listValuesOfIndexProperty2(newPath)) {
                    String[] tuple = new String[]{capture, prop[0], prop[1]};
                    values.add(tuple);
                }

            } else {
                for (String prop : this._listValuesOfIndexProperty(newPath)) {
                    String[] tuple = new String[]{capture, prop};
                    values.add(tuple);
                }
            }
        }

        return values;
    }


    /**
     * Given a JMX path withn two index properties, returns the list of values. For example, given the path
     * Catalina:type=Cache,host=*,path=*, this method will returns [["localhost", "/"], ["localhost", "/admin"], ...]
     *
     * @param path the JMX path with 2 index properties
     * @return An array of tuples
     * @throws java.io.IOException
     * @throws javax.management.MalformedObjectNameException if path is invalid
     */
    protected ArrayList<String[]> _listValuesOfIndexProperty2(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException, MalformedObjectNameException {
        String strObjName = path.getObjectName();
        /*Logger.info("Client._listValuesOfIndexProperty2", String.format(
                "ObjectName=%s", strObjName
        ));*/

        ArrayList<String[]> results = new ArrayList<String[]>();
        for (ObjectName name : _connection.queryNames(new ObjectName(strObjName), null)) {
            String key = name.getKeyProperty(path.getIndexProperty());
            if (key == null || key.length() == 0) {
                //Logger.info("Client._listValuesOfIndexProperty2", "Ignore object name - " + name);
            }
            else {
                String key2 = name.getKeyProperty(path.getIndexProperty2());
                if (key2 == null || key2.length() == 0) {
                    //Logger.info("Client._listValuesOfIndexProperty2", "Ignore object name - " + name);
                }
                else {
                    String[] tokens = new String[]{key, key2};
                    results.add(tokens);
                }
            }
        }

        return results;
    }


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
    protected ArrayList<String> _listAttributeNames(com.logicmonitor.ft.jmx.jmxclient.JMXPath path)
            throws IOException, MalformedObjectNameException {
        try {
            ArrayList<String> attrNames = new ArrayList<String>();
            MBeanInfo mbeanInfo = _connection.getMBeanInfo(new ObjectName(path.getObjectName()));
            for (MBeanAttributeInfo attrInfo : mbeanInfo.getAttributes()) {
                attrNames.add(attrInfo.getName());
            }

            return attrNames;
        }
        catch (JMException e) {
            throw new IOException(e);
        }
    }


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
    protected ArrayList<String> _listSubNodes(JMXPath path)
            throws IOException, MalformedObjectNameException {
        try {
            Object attrValue = _connection.getAttribute(new ObjectName(path.getObjectName()), path.getJMXAttribute());
            return path.listSubNodes(attrValue);
        }
        catch (JMException e) {
            throw new IOException(e);
        }
    }

    // -------------------------------------------------------------------------
    // UT tool
    public static void main(String []args) throws Exception {
        if(args.length < 3) {
            System.out.println("Usage: JMXClient <url> <path> <op> [<user> <pass> <timeout>]");
            System.exit(-1);
        }

        String url = args[0];
        String path = args[1];
        String op = args[2];

        String user = "";
        String pass = "";
        int timeout = 0;

        if(args.length > 3) {
            user = args[3];
        }

        if(args.length > 4) {
            pass = args[4];
        }

        if(args.length > 5) {
            timeout = Integer.valueOf(args[5]);
        }

        Client c = new Client(url, user, pass, timeout * 1000);

        try {
            if(op.equalsIgnoreCase("get")) {
                String value = c.getValue(path);
                System.out.println(String.format(
                                       "Path %s has value %s", path, value
                                       ));
            }
            else if(op.equalsIgnoreCase("eval")) {
                ArrayList values = c.evaluatePath(path);
                System.out.println(String.format(
                                       "Has %d evaluation results for path %s",
                                       values == null ? -1 : values.size(), path
                                       ));

                if(values != null) {
                    for(Object o : values) {
                        System.out.println(o);
                    }
                }
            }
            else {
                System.out.println("Invalid op. Only GET or EVAL accepted");
            }
        }
        finally {
            c.close();
        }
    }
}
