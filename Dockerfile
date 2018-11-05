FROM openjdk:8

RUN apt-get update && apt-get -y install apt-utils net-tools apt-transport-https wget curl nginx git maven

COPY / /sagemaker-sparkml-model-server
WORKDIR /sagemaker-sparkml-model-server

RUN mvn clean test package

RUN cp ./target/sparkml-model-server-1.0-SNAPSHOT.jar /usr/local/lib/sparkml-model-server-1.0-SNAPSHOT.jar
RUN cp ./serve.sh /usr/local/bin/serve.sh

RUN chmod a+x /usr/local/bin/serve.sh
ENTRYPOINT ["/usr/local/bin/serve.sh"]