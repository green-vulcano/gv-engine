## How to configure a persist scheduler

### Step 1
Copy datasource configuration file [org.ops4j.datasource-quartz.cfg](https://github.com/green-vulcano/gv-engine/raw/master/gvengine/gvscheduler/org.ops4j.datasource-quartz.cfg) into `<karaf_home>/etc`

### Step 2
Copy schema creation script [tables_h2.sql](https://github.com/green-vulcano/gv-engine/raw/master/gvengine/gvscheduler/tables_h2.sql) anywhere in the same machine where karaf is running

### Step 3
Run this command in the karaf shell to execute the schema creation script `jdbc:execute quartz-ds "runscript from '<path to>/tables_h2.sql'"`

### Step 4
 Run this command in the karaf shell to install gvscheduler `feature:install gvscheduler`

### Step 5
Copy gvscheduler config [it.greenvulcano.gvesb.quartz.cfg](https://github.com/green-vulcano/gv-engine/raw/master/gvengine/gvscheduler/it.greenvulcano.gvesb.quartz.cfg) into `<karaf_home>/etc`

### Final step
Restart karaf and enjoy!
