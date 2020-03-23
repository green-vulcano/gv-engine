#! /bin/bash

if [ ! -d ${KARAF_HOME} ]; then

    echo "Karaf home setup: $KARAF_HOME"

    mv /home/gaia/apache-karaf-${KARAF_VERSION} /opt/karaf
    mv ${KARAF_HOME}/bin /home/gaia/
    echo "gv.app.home=/etc/gaia/default" > ${KARAF_HOME}/etc/it.greenvulcano.gvesb.cfg

    sed -i -e "s|unset|echo|g" /home/gaia/bin/inc
    sed -i -e "s|java.base=lib|java.base=${KARAF_HOME}/lib|g" /home/gaia/bin/karaf
    sed -i -e "s|java.xml=lib|java.xml=${KARAF_HOME}/lib|g" /home/gaia/bin/karaf
    sed -i -e "s|http://repo1.maven.org|https://repo1.maven.org|g" ${KARAF_HOME}/etc/org.ops4j.pax.url.mvn.cfg
    cat /home/gaia/log.cfg >> ${KARAF_HOME}/etc/org.ops4j.pax.logging.cfg

    echo "Starting GAIA setup"
    /home/gaia/setup.sh

    echo "DONE"
fi

/home/gaia/bin/karaf server
