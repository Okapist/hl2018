FROM sgrio/java-oracle

RUN apt-get update -y
RUN apt-get install -y unzip
RUN apt-get install -y libcap2-bin

RUN mkdir -p /tmp/run/
RUN mkdir -p /tmp/mydata/
RUN chmod a+rwx /tmp/mydata/

COPY lib /tmp/run/lib
COPY app.jar /tmp/run/

RUN setcap CAP_NET_BIND_SERVICE=+eip /usr/lib/jvm/java-11-oracle/bin/java

EXPOSE 80
#CMD ["bash"]
CMD ["/usr/lib/jvm/java-11-oracle//bin/java", "-Xms1200m", "-Xmx1600m", "-XX:MaxRAM=1600M", "-XX:+UseSerialGC", "-XX:+AlwaysPreTouch", "-XX:MaxRAMFraction=1", "-XX:+UseContainerSupport", "-server", "-Djava.lang.Integer.IntegerCache.high=1500000", "-jar", "/tmp/run/app.jar"]
