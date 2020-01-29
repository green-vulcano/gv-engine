FROM adoptopenjdk/openjdk11:x86_64-debian-jdk-11.0.6_10

ENV GAIA_VERSION 4.1.0
ENV GAIA_FEATURES "gvvcl-rest, gvdatahandler, gvhttp"
ENV KARAF_VERSION 4.2.7

ENV KARAF_HOME /home/gaia/karaf
ENV MAVEN_REPOSITORY ${KARAF_HOME}/m2/repository

ENV EXTRA_JAVA_OPTS="-XshowSettings:vm -XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -Dkaraf.log.console=INFO"

ADD http://archive.apache.org/dist/karaf/4.2.7/apache-karaf-4.2.7.tar.gz /tmp/

#******** Download and install karaf
RUN mkdir -p /home/gaia \
 && tar xzf /tmp/apache-karaf-${KARAF_VERSION}.tar.gz -C /home/gaia \
 && mv /home/gaia/apache-karaf-${KARAF_VERSION} ${KARAF_HOME} \
 && rm /tmp/apache-karaf-${KARAF_VERSION}.tar.gz \
 && mkdir -p ${MAVEN_REPOSITORY}

COPY ./log.cfg /home/gaia
COPY ./setup.sh /home/gaia
COPY ./start.sh /home/gaia

RUN groupadd -r gaia -g 433 \
 && useradd -u 431 -r -g gaia -c "GreenVulcano user" gaia \
 && chown -R gaia:gaia /home/gaia \
 && chmod 740 /home/gaia/start.sh \
 && chmod 740 /home/gaia/setup.sh

USER gaia
VOLUME $KARAF_HOME

EXPOSE 8181
EXPOSE 8101

WORKDIR /home/gaia
CMD ["/home/gaia/start.sh"]
