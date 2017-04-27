[![N|Solid](http://www.greenvulcanotechnologies.com/wp-content/uploads/2015/11/logo-green-vulcano-technologies-colour.png)](http://www.greenvulcanotechnologies.com)
# GreenVulcano ESB 4: Quickstart guide
Latest [GreenVulcano] instances work on [Apache Karaf] 4.1.1.
 
## Installation
Before to run karaf, you have to configure GreenVulcano Maven repository:
update <karaf_home>/etc/**org.ops4j.pax.url.mvn.cfg** file and add to key **org.ops4j.pax.url.mvn.repositories** following row:
   > http://mvn.greenvulcano.com/nexus/content/groups/public@id=gv@snaphots, \

Then start karaf.

Install feature repository and install gvegine
```sh
gvadmin@root()> feature:repo-add mvn:it.greenvulcano.gvesb/features/4.0.0-SNAPSHOT/xml/features
gvadmin@root()> feature:install gvengine
```
The list of karaf features, now also include those of GreenVulcano:

```sh
gvadmin@root()> feature:list
```
To show GreenVulcano features only:

```sh
gvadmin@root()> feature:list | grep Green
```

At the end of this process a folder it will be created at **<karaf_home>/Greenv**, which contains an empty GreenVulcano configuration. It is possibile to reference a different GreenVulcano configuration, simply by inserting the following line in the file **<karaf_home>/etc/it.greenvulcano.gvesb.cfg**

> gv.app.home=<path_to_gv_configuration>

It is necessary to restart karaf to properly load a new configuration.

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

## Deploy

Export a configuration from Developer studio. Then choose whether to use "Developer Studio & GVConsole" or "Karaf" deploy.

- Developer Studio & GVConsole
 -- GVConsole -> Configuration
 -- upload configurazion in .zip format
 -- choos a Configuration-Id
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
# GVESB Appender to produce multiple log files - One per value of MASTER_SERVICE key in thread context map
log4j2.appender.gv.type = Routing
log4j2.appender.gv.name = RoutingGVCore
log4j2.appender.gv.routes.type = Routes
log4j2.appender.gv.routes.pattern = \$\$\\\{ctx:MASTER_SERVICE\}
log4j2.appender.gv.routes.service.type = Route
log4j2.appender.gv.routes.service.appender.type = RollingRandomAccessFile
log4j2.appender.gv.routes.service.appender.name = Rolling-\$\\\{ctx:MASTER_SERVICE\}
log4j2.appender.gv.routes.service.appender.fileName = ${karaf.data}/log/GVCore.\$\\\{ctx:MASTER_SERVICE\}.log
log4j2.appender.gv.routes.service.appender.filePattern = ${karaf.data}/log/${date:yyyy-MM}/GVCore.\$\\\{ctx:MASTER_SERVICE\}.%d{MM-dd-yyyy}.%i
log4j2.appender.gv.routes.service.appender.append = true
log4j2.appender.gv.routes.service.appender.layout.type = PatternLayout
log4j2.appender.gv.routes.service.appender.layout.pattern = [%d{ISO8601}][%-5.5p][%X{SERVICE}/%X{OPERATION}][%X{bundle.id} - %X{bundle.name}][%t] - %m%n
log4j2.appender.gv.routes.service.appender.policies.type = Policies
log4j2.appender.gv.routes.service.appender.policies.size.type =SizeBasedTriggeringPolicy
log4j2.appender.gv.routes.service.appender.policies.size.size = 32MB

# GVESB Logger mapping service name
log4j2.logger.greenvulcano.name = it.greenvulcano
log4j2.logger.greenvulcano.level = DEBUG
log4j2.logger.greenvulcano.appenderRef.gv.ref = RoutingGVCore
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.type = ThreadContextMapFilter
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.operator = or
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.type = KeyValuePair
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.key = MASTER_SERVICE
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.fleet.value = (service name)
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.vehicle.type = KeyValuePair
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.vehicle.key = MASTER_SERVICE
log4j2.logger.greenvulcano.appenderRef.gv.filter.mdc.vehicle.value = (service name)
```
[GreenVulcano]: https://github.com/green-vulcano/gv-engine
[Apache Karaf]: <http://karaf.apache.org>
[OPS4J Pax JDBC]: https://ops4j1.jira.com/wiki/display/PAXJDBC/Documentation
