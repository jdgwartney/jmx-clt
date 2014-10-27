JMXTop
=====
Deployment
------
        1. Build JMXCommon with command 'gradle build'.
        2. Build JMXTop with command 'gradle build'. Get the tar(for linux system) and zip(for windows system) file in its build/distributions folder.
Usage
------
        usage: jmxTop jmxURL [argument] [path lists] [-A] [-f <arg>] [-h] [-i <arg>] [-p <arg>] [-u <arg>]
        To view statuses of jmx paths:
         -A          Show alias names instead of jmx paths
         -f <arg>    Path to the configure file
         -h,--help   show this help message
         -i <arg>    Interval between two scan tasks
         -p <arg>    Password for remote process
         -u <arg>    User name for remote process
        [Use F4 to exit top console]

        example:
        jmxTop.sh "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi" "java.lang:type=MemoryPool,name=Code Cache:Usage.committed"
        jmxTop.bat "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi" -f conf/tomcat_demo.json -A

