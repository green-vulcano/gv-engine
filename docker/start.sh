#! /bin/bash

if [ ! -d ${KARAF_HOME}/data/cache ]; then

    echo "gv.app.home=${GAIA_CONFIG}/default" > ${KARAF_HOME}/etc/it.greenvulcano.gvesb.cfg

    sed -i -e "s|http://repo1.maven.org|https://repo1.maven.org|g" ${KARAF_HOME}/etc/org.ops4j.pax.url.mvn.cfg
    cat /opt/gaia/log.cfg >> ${KARAF_HOME}/etc/org.ops4j.pax.logging.cfg

    echo "Starting GAIA setup"
    /home/gaia/setup.sh

fi

/home/gaia/karaf/bin/karaf server
