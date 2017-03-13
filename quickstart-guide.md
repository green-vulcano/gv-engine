[![N|Solid](http://www.greenvulcanotechnologies.com/wp-content/uploads/2015/11/logo-green-vulcano-technologies-colour.png)](http://www.greenvulcanotechnologies.com)
# GreenVulcano ESB 4: Quickstart guide
Latest [GreenVulcano] instances work on [apache karaf] 4.0.8.
 
## Installation
Update <karaf_home>/etc/**org.ops4j.pax.url.mvn.cfg** file and add to key **org.ops4j.pax.url.mvn.cfg** following row:
   > http://mvn.greenvulcano.com/nexus/content/groups/public@id=gv@snaphots, \

Then restart karaf.

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
````sh

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

[GreenVulcano]: https://github.com/green-vulcano/gv-engine
[apache karaf]: <http://karaf.apache.org>
[OPS4J Pax JDBC]: https://ops4j1.jira.com/wiki/display/PAXJDBC/Documentation
