FROM openjdk:8
ENV SBT_VERSION=1.0.4

RUN apt-get update && apt-get -y install apt-utils net-tools apt-transport-https wget curl nginx git scala
RUN curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt-get update && \
    apt-get install sbt

COPY / /spark-serving
WORKDIR spark-serving/sparkml-server
RUN sbt assembly
RUN cp ./target/scala-2.11/spark-serving-assembly-0.1.0-SNAPSHOT.jar /usr/local/lib/spark-serving-assembly-0.1.0-SNAPSHOT.jar
RUN cp ./serve.sh /usr/local/bin/serve.sh
RUN chmod a+x /usr/local/bin/serve.sh
ENTRYPOINT ["/usr/local/bin/serve.sh"]