[![N|Solid](http://www.greenvulcanotechnologies.com/wp-content/uploads/2015/11/logo-green-vulcano-technologies-colour.png)](http://www.greenvulcanotechnologies.com)
# GreenVulcano ESB 4: Quickstart guide
Latest [GreenVulcano] instances work on [apache karaf] 4.0.8.
 
## Installation
Update <karaf_home>/etc/**org.ops4j.pax.url.mvn.cfg** file and add to key **org.ops4j.pax.url.mvn.cfg** following row:
   > http://mvn.greenvulcano.com/nexus/content/groups/public@id=gv@snaphots, \

Then restart karaf.

Install feature repository and install gvegine
```sh
gvadmin@root()> feature:repo-add mvn:it.greenvulcano.gvesb/gvengine-features/4.0.0-SNAPSHOT/xml/features
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

### adapter
To use an adapter, like a **data handler** or a  **rest-call**, it is mandatory to install following component:
 ```sh
gvadmin@root()> feature:repo-add mvn:it.greenvulcano.gvesb.adapter/gvrestx/4.0.0-SNAPSHOT/xml/features
gvadmin@root()> feature:install gvrestx
 ```

For **data handler** just install following bundle, with start level 96:
 ```sh
gvadmin@root()> bundle:install -s -l 96 mvn:it.greenvulcano.gvesb.adapter/gvdatahandler/4.0.0-SNAPSHOT
 ```

To url mapping (httpInboundGateway) install also gvhttp: it is a war file and not a jar file, so don't forget to add /war suffix:
 ```sh
gvadmin@root()> bundle:install -s -l 96 mvn:it.greenvulcano.gvesb.adapter/gvhttp/4.0.0-SNAPSHOT/war
 ```

For database connections (JNDI, JDBC):
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