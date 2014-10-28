package com.logicmonitor.ft.jmxstat;

import com.logicmonitor.ft.jmx.jmxman.JMXInPath;
import com.logicmonitor.ft.jmx.jmxman.RunParameter;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class TestJMXStatMain {
    public final static String TEST_FILE_PATH = "/test.json";

    @Test
    public void testGetPathsFromFile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = JMXStatMain.class.getDeclaredMethod("getPathsFromFile", String.class);
        method.setAccessible(true);
        String filePath = this.getClass().getResource(TEST_FILE_PATH).getPath();
        List<JMXInPath> paths = (List<JMXInPath>) method.invoke(null, filePath);
        assertEquals(paths.size(), 3);
        JMXInPath firstPath = paths.get(0);
        assertEquals(firstPath.getPath(), "java.lang:type=MemoryPool,name=Code Cache:Usage");
        assertEquals(firstPath.getAlias(), "test1");
        assertEquals(firstPath.getOperator(), "/");
        assertEquals(firstPath.getScale(), 10000);
        assertEquals(firstPath.getUnit(), "KB");
    }

    @Test
    public void testARGSAnalyser() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Method method = JMXStatMain.class.getDeclaredMethod("ARGSAnalyser", String[].class);
        method.setAccessible(true);
        String url = "service:jmx:";
        String[] args = new String[]{url, "-u", "user", "-p", "password", "-t", "10", "-i", "20", "-a", "pathA"};
        RunParameter runParameter = (RunParameter) method.invoke(null, (Object)args);

        // valid args
        assertEquals(runParameter.isValid(), true);
        assertEquals(runParameter.getSurl(), url);
        assertEquals(runParameter.getUsername(), "user");
        assertEquals(runParameter.getPassword(), "password");
        assertEquals(runParameter.getTimes(), 10);
        assertEquals(runParameter.getInterval(), 20);
        assertEquals(runParameter.isShowAliasTitle(), true);
        assertEquals(runParameter.getPaths().size(), 1);
        assertEquals(runParameter.getPaths().get(0).getPath(), "pathA");

        // test load configure file
        String filePath = this.getClass().getResource(TEST_FILE_PATH).getPath();
        args = new String[]{url, "-f", filePath};
        runParameter = (RunParameter) method.invoke(null, (Object)args);
        assertNotSame(runParameter.getPaths().size(), 0);

        // no url
        args = new String[]{""};
        runParameter = (RunParameter) method.invoke(null, (Object)args);
        assertEquals(runParameter.isValid(), false);

        // no path
        args = new String[]{url};
        runParameter = (RunParameter) method.invoke(null, (Object)args);
        assertEquals(runParameter.isValid(), false);
    }
}
