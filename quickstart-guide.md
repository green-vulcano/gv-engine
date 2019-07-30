[![N|Solid](http://www.greenvulcanotechnologies.com/wp-content/uploads/2017/04/logo_gv_FLAT-300x138.png)](http://www.greenvulcanotechnologies.com)
# GreenVulcano ESB 4: Quickstart guide
Latest [GreenVulcano] instances work on [Apache Karaf] 4.2.x.
+ [Download Karaf 4.2.x](http://karaf.apache.org/download.html) (Select *Binary Distribution* -> *tar.gz*)
+ Extract tar.gz into a folder in your file system. 
+ */apache-karaf-4.2.x* will be the **Karaf home**. 

## Installation

## Method 1: Use GV Karaf Installer

Make sure your JAVA_HOME is correctly set as an environment variable to avoid issues in the configuration. If it's not set, the installer will warn you. In that case, please refer to this page: https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/

Once you've done that, download **gv-karaf-conf** from [this page](https://github.com/Kylan11/gv-engine/tree/master/gv-karaf-autoconfigurator).
Open a terminal in the same folder you've downloaded gv-karaf-conf, then run (use sudo privileges if needed):
 ```sh
./gv-karaf-conf <KARAF_HOME>
```
Where KARAF_HOME is your karaf main folder. This simple program will automatically configure your Karaf instance to work with GreenVulcano ESB, and should install gv-engine automatically. Having done that, all you need to do is manually download the additional plugins you wish to use alongside your GV instance.

## Method 2: Configure Karaf manually
Before running karaf, you have to configure GreenVulcano Maven repository:
update <karaf_home>/etc/**org.ops4j.pax.url.mvn.cfg** file and add to key **org.ops4j.pax.url.mvn.repositories** following row:
   > http://mvn.greenvulcano.com/nexus/content/groups/public@id=gv@snapshots, \

Then start Karaf (Open a terminal in **#KarafHome** */bin* and write *./karaf*)

Add our feature repository and install gvegine:
```sh
gvadmin@root()> feature:repo-add mvn:it.greenvulcano.gvesb/features/4.1.0-SNAPSHOT/xml/features
gvadmin@root()> feature:install gvengine
```
At the end of this process a folder will be created at **<karaf_home>/GreenV**, which contains an empty GreenVulcano configuration. It is possibile to reference a different GreenVulcano configuration, simply by inserting the following line in the file **<karaf_home>/etc/it.greenvulcano.gvesb.cfg**

> gv.app.home=<path_to_gv_configuration>

It is necessary to restart karaf to properly load a new configuration.

## Usage
Make sure your Karaf instance is running (You can make sure of that by doing in your Karaf/bin folder and executing "./status").

To open GV Console go to http://localhost:8181/gvconsole/#/login and log with *gvadmin gvadmin*

If you want to stop Karaf and GV Console, insert *halt* into the terminal.

---

If you've installed gvengine correctly, the list of karaf features should now also include those of GreenVulcano:

```sh
gvadmin@root()> feature:list
```
To show GreenVulcano features only:

```sh
gvadmin@root()> feature:list | grep GreenVulcano
```

### Plugins and adapters
You can install specific plugins and adapters to extends GreenVulcano ESB v4
```sh

gvadmin@root()> feature:install gvvcl-rest
```

For **data handler** just install following bundle, with start level 96:
```sh
gvadmin@root()> feature:install gvdatahandler
```

To url mapping (httpInboundGateway) install also gvhttp: it is a war file and not a jar file, so don't forget to add /war suffix:
```sh
gvadmin@root()> feature:install gvhttp
```

For database connections (JNDI, JDBC), refer to [OPS4J Pax JDBC] framework i.e. :
```sh
gvadmin@root()> feature:install pax-jdbc-oracle
```   
or:
```sh
gvadmin@root()> feature:install pax-jdbc-mysql
``` 
For scheduled sevices install:
```sh
gvadmin@root()> feature:install gvscheduler
gvadmin@root()> feature:install gvscheduler-conf 
```

For jms queues intall:
```sh
gvadmin@root()> feature:install gvvcl-jms
```

To extend database compatibility to MongoDB platforms, please refer to [this tutorial](https://github.com/Kylan11/gv-adapter-mongodb).

## Deploy

Export a configuration from Developer Studio. Then choose whether to use "Developer Studio & GVConsole" or "Karaf" deploy.

- Developer Studio & GVConsole
 -- GVConsole -> Configuration
 -- upload configurazion in .zip format
 -- choose a Configuration-Id
 -- press Deploy button
- Karaf
    ```sh
    gvadmin@root()> gvesb:deploy Configuration-Id /path/to/vulcon/export.zip
     ```
 A folder named <Configuration-Id> will be created into <karaf_home>/GreenV, which contains GreenVulcano configuration.

## Logging

You can setup a fine-grained logging service configuring proprerly **log4j** in karaf.
This is a sample configuration in *etc/org.ops4j.pax.logging.cfg* to logging each service in a single file:
```
log4j2.property.MASTER_SERVICE = NOOP

# GVESB Appender to produce multiple log files - One per value of MASTER_SERVICE key in thread context map
log4j2.appender.gv.type = Routing
log4j2.appender.gv.name = RoutingGVCore
log4j2.appender.gv.routes.type = Routes
log4j2.appender.gv.routes.pattern = \$\$\\\{ctx:MASTER_SERVICE\}
log4j2.appender.gv.routes.service.type = Route
log4j2.appender.gv.routes.service.appender.type = RollingRandomAccessFile
log4j2.appender.gv.routes.service.appender.name = Rolling-\$\\\{ctx:MASTER_SERVICE\}
log4j2.appender.gv.routes.service.appender.fileName = ${karaf.data}/log/GVCore.\$\\\{ctx:MASTER_SERVICE\}.log
log4j2.appender.gv.routes.service.appender.filePattern = ${karaf.data}/log/${date:yyyy-MM}/GVCore.\$\\\{ctx:MASTER_SERVICE\}.%d{MM-dd-yyyy}.zip
log4j2.appender.gv.routes.service.appender.append = true
log4j2.appender.gv.routes.service.appender.layout.type = PatternLayout
log4j2.appender.gv.routes.service.appender.layout.pattern = [%d{ISO8601}][%-5.5p][%X{SERVICE}/%X{OPERATION}][%X{ID}][%X{bundle.id} - %X{bundle.name}][%t] - %m%n
log4j2.appender.gv.routes.service.appender.policies.type = Policies
log4j2.appender.gv.routes.service.appender.policies.time.type =TimeBasedTriggeringPolicy
log4j2.appender.gv.routes.service.appender.policies.time.interval = 1
log4j2.appender.gv.routes.service.appender.policies.time.modulate = true

# GVESB Logger mapping service name
log4j2.logger.greenvulcano.name = it.greenvulcano
log4j2.logger.greenvulcano.level = DEBUG
log4j2.logger.greenvulcano.appenderRef.gv.ref = RoutingGVCore
# Filtering specific services
#log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.type = ThreadContextMapFilter
#log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.operator = or
#log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.type = KeyValuePair
#log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.key = MASTER_SERVICE
#log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.value = (service name)
```
## Monitoring

You can access to GV ESB v4 JMX infrastructure using jconsole connected to karaf.

Use this url

```service:jmx:rmi:///jndi/rmi://<karaf host>:1099/karaf-root```

to connect jconsole on a remote instance. All MBean are under the node `GreenVulcano`

[GreenVulcano]: https://github.com/green-vulcano/gv-engine
[Apache Karaf]: <http://karaf.apache.org>
[OPS4J Pax JDBC]: https://ops4j1.jira.com/wiki/display/PAXJDBC/Documentation
