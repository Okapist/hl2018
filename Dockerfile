FROM sgrio/java-oracle

RUN apt-get update -y
RUN apt-get install -y unzip
RUN apt-get install -y libcap2-bin

RUN mkdir -p /tmp/run/
RUN mkdir -p /tmp/run/lib/
RUN mkdir -p /tmp/mydata/
RUN chmod a+rwx /tmp/mydata/
RUN chmod a+rwx /tmp/run/

COPY lib /tmp/run/lib
COPY app.jar /tmp/run/

RUN chmod a+rwx /tmp/run -R

RUN setcap CAP_NET_BIND_SERVICE=+eip /usr/lib/jvm/java-11-oracle/bin/java

RUN ulimit -n 1000000

EXPOSE 80
CMD ["/usr/lib/jvm/java-11-oracle//bin/java", "-Xms1500m", "-Xmx1600m", "-XX:MaxRAM=1600M", "-XX:+UseSerialGC", "-XX:+AlwaysPreTouch", "-XX:MaxRAMFraction=1", "-XX:+UseContainerSupport", "-server", "-Djava.lang.Integer.IntegerCache.high=1500000", "-jar", "/tmp/run/app.jar"]
