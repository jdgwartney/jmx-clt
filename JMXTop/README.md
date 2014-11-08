JMX Command Line Tools
======================

The JMX Command Line Tools provide light-weight Unix command-line access to communicate with remote Java processes. The benefits are as follows:

- Quickly see what's happening with an application
- Operate from a text terminal
- Leverage pluggable configuration files

The configuration files allow you to analyze only the relevant metrics, and format them to be more readable. This also enables senior administrators to pre-configure files, making junior administrators more effective during a problem scenario.

The initial release includes example files for Kafka, Tomcat and ZooKeeper. We welcome community contributions of configuration files to this GitHub repository.

The light-weight command-line interface enables you to access specific raw data. By invoking these commands from a script, you can also capture raw data and archive it for analysis or forcasting.

The JMX Command Line Tools consist of:

1. JMXTop, analagous to the Unix "top" command
2. JMXStat, analagous to the Unix "vmstat" command

The difference being they report information about the processes running on a Java Virtual Machine.

Sign up to get updates to future releases and new tools at [JMX Command Line Tools][6] on GitHub.

Background
==========

JMX stands for [Java Management Extensions][1], a Java technology that supplies tools for managing and monitoring applications, system objects, devices and service-oriented networks. Those resources are represented by objects called MBeans (for [Managed Bean][2]). The JMX Command Line Tools give you quick access to analyze these resources in a convenient format.

Installation
============

To build the JMX Command Line Tools, you need to have the following installed:

* [Git][3] (minimum version 1.8.5)
* [Gradle][4] (minimum version 2.0)
* [JDK][5] (minimum version 1.6)

The JMX Command Line Tools are available on GitHub, and can be downloaded as follows:

	$ git clone https://github.com/logicmonitor/jmx-clt

## Building Common files
Build JMXCommon as follows:

	$ cd jmx-clt/JMXCommon
	$ gradle build
	:clean
	:compileJava
	Note: Some input files use unchecked or unsafe operations.
	Note: Recompile with -Xlint:unchecked for details.
	:processResources UP-TO-DATE
	:classes
	:compileTestJava UP-TO-DATE
	:processTestResources UP-TO-DATE
	:testClasses UP-TO-DATE
	:test UP-TO-DATE
	:jar
	:copyToJMXStatLib UP-TO-DATE
	:copyToJMXTopLib UP-TO-DATE
	:build
	BUILD SUCCESSFUL
	$ cd ..

Then you can build JMXTop and JMXStat.
## Building JMXTop and JMXStat
Next build JMXTop as follows:

	$ cd JMXTop
	$ gradle build
	:clean
	:compileJava
	Note: .../jmxstat/JMXStatMain.java uses unchecked or unsafe operations.
	Note: Recompile with -Xlint:unchecked for details.
	:processResources UP-TO-DATE
	:classes
	:compileTestJava
	   :
	   :
    :jmxTopJar
	:myTar
	:myZip
	:build
	BUILD SUCCESSFUL
	$ cd ..

Also build JMXStat:

    $ cd JMXStat
	$ gradle build
	   :
	   :
	:jmxStatJar
	:myTar
	:myZip
	:build
	BUILD SUCCESSFUL
	$ cd ..

## Installing JMXTop and JMXStat
The builds from above produce the following archive files:

	$ ls */build/distributions
	JMXStat/build/distributions:
	JMXStat.tar.gz  JMXStat.zip
    
	JMXTop/build/distributions:
	JMXTop.tar.gz  JMXTop.zip

Choose whichever format is most convenient (gzipped tar or zip) to unpack and install. For example:

	# cd /usr/local
	# tar -xzf {Your build area}/jmx-clt/JMXTop/build/distributions/JMXTop.tar.gz
	# chmod +x JMXTop/JMXTop.sh
	# tar -xzf {Your build area}/jmx-clt/JMXStat/build/distributions/JMXStat.tar.gz
	# chmod +x JMXStat/JMXStat.sh

Usage
=====

Both JMXTop and JMXStat have similar command-line arguments and configuration.

To exit each tool:

- Exit from JMXStat: ctrl + c
- Exit from JMXTop: 'F4' key

To change pages when using JMXTop:

- See previous page: 'UP' key
- See next page: 'DOWN' key

Use the -h option to see usage:

	$ cd JMXTop
	$ ./jmxtop.sh -h
	usage: jmxTop jmxURL [jmx path lists] [-a] [-f <arg>] [-h] [-i <arg>] [-p
	       <arg>] [-u <arg>]
	To view statuses of jmx paths:
	 -a          Show alias names instead of jmx paths
	 -f <arg>    Path to the configure file
	 -h,--help   show this help message
	 -i <arg>    Interval between two scan tasks, unit is second
	 -p <arg>    Password for remote process
	 -u <arg>    User name for remote process
	[Use F4 to exit top console]
	[Use Key UP and Key DOWN to change page]
	@Support by Logicmonitor

For example, to run JMXTop for the following:

- JMX Address is 192.168.151.1:9004
- JMX Username is demo
- Password is demo123
- Display memory and CPU statistics

The command would be:

	$ cd JMXTop
	$ ./jmxtop.sh service:jmx:rmi:///jndi/rmi://192.168.151.1:9004/jmxrmi \
	  -u demo -p demo123
	 
	 JMXtop was created by LogicMonitor under the BSD3 License.
	 
	 To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.
	 For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmx-clt.
	 
	 JMXtop - 2014-10-28 17:50:57 up 150536h 36m 38s
	 Thread: 85 live, 89 peak, 67 daemon, 195 total
	 CPU: 1399.910s, 8.00% usage
	 Heap               :     146177KB used,     506816KB committed, 28.84% usage
	 NonHeap            :      22834KB used,      25024KB committed, 91.25% usage
	 CMS Old Gen        :      27424KB used,     349568KB committed, 7.85% usage
	 Par Eden Space     :     105191KB used,     139776KB committed, 75.26% usage
	 Code Cache         :       3652KB used,       3776KB committed, 96.72% usage
	 CMS Perm Gen       :      19181KB used,      21248KB committed, 90.27% usage
	 Par Survivor Space :      13561KB used,      17472KB committed, 77.62% usage

By default, JMXTop always shows the above statistics. If you want to view additional resources, you can either specify those JMX paths on the command line, or in a configuration file.  If you supply both, the effect is additive.

Next we have a JMXStat example to look at Usage at JMX address 127.0.0.1:9003, on the local host. The command would be:

	$ ./jmxstat.sh service:jmx:rmi:///jndi/rmi://127.0.0.1:9003/jmxrmi \
	"java.lang:type=MemoryPool,name=Code Cache:Usage" \
    -u demo -p demo123 -a
	
	 JMXStat was created by LogicMonitor under the BSD3 License.
    
	 To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.
	 For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmx-clt.
    
	            java.lang                        java.lang                        java.lang                        java.lang            
	 type=MemoryPool,name=Code Cache  type=MemoryPool,name=Code Cache  type=MemoryPool,name=Code Cache  type=MemoryPool,name=Code Cache 
	         Usage.committed                    Usage.init                        Usage.max                       Usage.used            
	         Usage.committed                    Usage.init                        Usage.max                       Usage.used            
	            26673152                          2555904                         50331648                         26409216             
	            26673152                          2555904                         50331648                         26409216

Notice that for Usage, JMXStat displays the four entries (committed, init, max and used) that belong to Usage. To fit better within the screen width, we used the -a flag to display alias names for the column headers.

## Rules of JMX paths

In the above example, displaying the memory usage required a JMX path in the command arguments:

	"java.lang:type=MemoryPool,name=Code Cache:Usage"

To interpret this path, "java.lang" is the domain, "type=MemoryPool,name=Code Cache" specifies properties, and "Usage" is the selector.

The JMX tools support wild-card domain and index properties in the JMX path using '*' as follows:

	Pattern:                            Example:
	Wild-domain without index property  jav*g:type=MemoryPool,name=Code Cache:Usage.committed
	Wild-domain with an index property  jav*g:type=MemoryPool,name=*:Usage.committed
	An index property	                java.lang:type=*,name=Code Cache:Usage.committed
	Two index properties	            java.lang:type=*,name=*:Usage.committed

You can also have the following optional path parameter:

	The value is a counter              counter@jav*ng:type=*,name=Code Cache:Usage.committed

To display a current value correctly, the tools need to know if the value returned is a counter (a count of things as they occur, implying a rate) or if it reports a value directly (like a guage).

# Leveraging the Convenience of Configuration files

The JMX tools use modifiable, pluggable configuration files to provide convenient display of only what's relevant for you to examine in the JVM.  For example, you might want to look at General statistics, Heap usage, and CPU to see what might be causing a performance issue on your server. Or you may want to examine common applications, like Kafka, Tomcat, or ZooKeeper. 

## Configuration file Syntax

The configure file should be written in JSON style. Each JSON object describing a path can contain the following:

	path	  required
	alias	  optional
	operate	  optional
	scale	  optional
	unit	  optional

## Basic General statistics example

	{
	    "version": "0.0.2",
	    "paths": [
	        {
	            "path": "java.lang:type=MemoryPool,name=Code Cache:Usage",
	            "alias": "test",
	            "operate": "/",
	            "scale": 1000,
	            "unit": "KB"
	        },
	        {
	            "path": "java.lang:type=MemoryPool,name=Code Cache:Usage",
	            "alias": "test",             
	            "unit": "B"
	        },
	        {
	            "path": "counter@jav*ng:type=*,name=Code Cache:Usage.committed",
	            "alias": "test2",
	            "unit": "B"
	        }
	    ]
	}

In this example, the value that the JVM returns from MemoryPool for Usage may be in a hard-to-read unit, such as bytes. We have specified a divide operator '/' with a scale of 1000 to convert the output to display in KB.

## Configuration files included

The initial release of JMX Monitor Suite includes example files for Kafka, Tomcat and ZooKeeper as follows:

    kafka_demo.json
	tomcat_demo.json
	zookeeper_demo.json

These can be found under the JMXStat/conf and JMXTop/conf directories.

## Tomcat example

Now let's examine a Tomcat application as follows:

- JMX Address is 192.168.151.1:9004
- JMX Username is demo
- Password is demo123
- Display statistics for Tomcat

The command would be:

	$ cd JMXTop
	$ ./jmxtop.sh service:jmx:rmi:///jndi/rmi://192.168.151.1:9004/jmxrmi \
	  -f conf/tomcat_demo.json -u demo -p demo123
	 
	 JMXtop was created by LogicMonitor under the BSD3 License.
	 
	 To learn more about LogicMonitor and its automated IT Infrastructure Performance Monitoring Platform, visit www.logicmonitor.com.
	 For the latest updates, versions and configuration files, please visit our page on GitHub at https://github.com/logicmonitor/jmx-clt.
	 
	 JMXtop - 2014-10-28 17:50:57 up 150536h 36m 38s
	 Thread: 85 live, 89 peak, 67 daemon, 195 total
	 CPU: 1399.910s, 8.00% usage
	 Heap               :     146177KB used,     506816KB committed, 28.84% usage
	 NonHeap            :      22834KB used,      25024KB committed, 91.25% usage
	 CMS Old Gen        :      27424KB used,     349568KB committed, 7.85% usage
	 Par Eden Space     :     105191KB used,     139776KB committed, 75.26% usage
	 Code Cache         :       3652KB used,       3776KB committed, 96.72% usage
	 CMS Perm Gen       :      19181KB used,      21248KB committed, 90.27% usage
	 Par Survivor Space :      13561KB used,      17472KB committed, 77.62% usage
	 Catalina:type=Cache,host=localhost,path=/cached:accessCount                9
	 Catalina:type=Cache,host=localhost,path=/cached:hitsCount                  1
	 Catalina:type=Manager,host=localhost,path=/cached:activeSessions           0
	 Catalina:type=Manager,host=localhost,path=/cached:rejectedSessions         0
	 Catalina:type=Manager,host=localhost,path=/cached:sessionAverageAliveTime  0
	 Catalina:type=Manager,host=localhost,path=/cached:sessionCounter           0
	 Catalina:type=GlobalRequestProcessor,name=jk-8209:requestCount             0
	 Catalina:type=GlobalRequestProcessor,name=http-8448:requestCount           0
	 Catalina:type=GlobalRequestProcessor,name=http-8088:requestCount           0
	 Catalina:type=GlobalRequestProcessor,name=jk-8209:processingTime           0
	 Catalina:type=GlobalRequestProcessor,name=http-8448:processingTime         0
	 Catalina:type=GlobalRequestProcessor,name=http-8088:processingTime         0
	 Catalina:type=GlobalRequestProcessor,name=jk-8209:errorCount               0
	 Catalina:type=GlobalRequestProcessor,name=http-8448:errorCount             0
	 Catalina:type=GlobalRequestProcessor,name=http-8088:errorCount             0
	 Catalina:type=GlobalRequestProcessor,name=jk-8209:bytesSent                0
	 Catalina:type=GlobalRequestProcessor,name=http-8448:bytesSent              0
	 Catalina:type=GlobalRequestProcessor,name=http-8088:bytesSent              0
	 Catalina:type=GlobalRequestProcessor,name=jk-8209:bytesReceived            0
	 Catalina:type=GlobalRequestProcessor,name=http-8448:bytesReceived          0
	 Catalina:type=GlobalRequestProcessor,name=http-8088:bytesReceived          0

The resources accessCount, histCount, activeSessions, and so on are pre-defined in the configuration file (conf/tomcat_demo.json). JMXTop displays these in addition to the default memory and CPU statistics.

[1]: http://www.oracle.com/technetwork/java/javamail/javamanagement-140525.html
[2]: http://docs.oracle.com/javaee/7/tutorial/doc/jsf-develop001.htm
[3]: http://git-scm.com/downloads
[4]: http://www.gradle.org/installation
[5]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[6]: http://github.com/logicmonitor/jmx-clt
