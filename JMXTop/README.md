JMXtop  was created by LogicMonitor under the BSD3 License.

To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.
For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmxtools.

Deployment
----------
        1. Build JMXCommon with command 'gradle build'.
        2. Build JMXTop with command 'gradle build'.
        3. Get the JMXTop.tar.gz(for linux system) and JMXTop.zip(for windows system) file in its build/distributions folder.
Usage
---------
        usage: jmxtop jmxURL [jmx path lists] [-a] [-f <arg>] [-h] [-i <arg>] [-p <arg>] [-u <arg>]
        To view statuses of jmx paths:
         -a          Show alias names instead of jmx paths
         -f <arg>    Path to the configure file
         -h,--help   show this help message
         -i <arg>    Interval between two scan tasks, unit is second
         -p <arg>    Password for remote process
         -u <arg>    User name for remote process

        example:
        jmxtop.sh "service:jmx:rmi:///jndi/rmi://localhost:jmx_port/jmxrmi" "java.lang:type=MemoryPool,name=Code Cache:Usage.committed" -u jmx_username -p jmx_password
        jmxtop.bat "service:jmx:rmi:///jndi/rmi://localhost:jmx_port/jmxrmi" -f conf/tomcat_demo.json -u jmx_username -p jmx_password
