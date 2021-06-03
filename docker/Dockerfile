FROM openjdk:11.0.10-jdk-slim 

ENV GAIA_VERSION 4.1.0
ENV GAIA_FEATURES "gvengine, gvvcl-rest, gvdatahandler, gvhttp"
ENV KARAF_VERSION 4.2.7
ENV KARAF_HOME /opt/karaf/apache-karaf-${KARAF_VERSION}
ENV KARAF_ETC /etc/karaf
ENV KARAF_LOG /var/log/karaf
ENV MAVEN_REPOSITORY /opt/karaf/m2/repository

ENV JAVA_OPTS="-XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40"
ENV EXTRA_JAVA_OPTS="-XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XshowSettings:vm -Dkaraf.log.console=INFO"

#******** Download and install karaf
ADD http://archive.apache.org/dist/karaf/4.2.7/apache-karaf-4.2.7.tar.gz /tmp

RUN mkdir -p /home/gaia \
 && tar xzf /tmp/apache-karaf-${KARAF_VERSION}.tar.gz -C /home/gaia \
 && rm /tmp/apache-karaf-${KARAF_VERSION}.tar.gz \
 && mkdir -p ${MAVEN_REPOSITORY} \
 && mkdir -p ${KARAF_ETC} \
 && mkdir -p ${KARAF_LOG} \
 && mkdir /etc/gaia

COPY ./log.cfg /home/gaia
COPY ./setup.sh /home/gaia
COPY ./start.sh /home/gaia

RUN useradd -u 1431 -r -g 0 -c "GreenVulcano user" gaia \
 && chown -R 1431:0 /opt/karaf \
 && chown -R 1431:0 /etc/gaia \
 && chown -R 1431:0 /etc/karaf \
 && chown -R 1431:0 /var/log/karaf \
 && chown -R 1431:0 /home/gaia \
 && chmod 740 /home/gaia/start.sh \
 && chmod 740 /home/gaia/setup.sh \
 && chmod -R g+rwX /home/gaia \
 && chmod -R g+rwX /etc/gaia \
 && chmod -R g+rwX /var/log/karaf \
 && chmod -R g+rwX /opt/karaf

USER 1431
VOLUME /etc/gaia
VOLUME /etc/karaf
VOLUME /opt/karaf
VOLUME /var/log/karaf

EXPOSE 8181
EXPOSE 8101

WORKDIR /home/gaia
CMD ["/home/gaia/start.sh"]
