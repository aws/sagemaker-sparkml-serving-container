FROM openjdk:8u181

LABEL com.amazonaws.sagemaker.capabilities.accept-bind-to-port=true

RUN apt-get update && apt-get -y install apt-utils net-tools apt-transport-https wget curl nginx git maven

COPY / /sagemaker-sparkml-model-server
WORKDIR /sagemaker-sparkml-model-server

RUN mvn clean package

RUN cp ./target/sparkml-serving-2.3.jar /usr/local/lib/sparkml-serving-2.3.jar
RUN cp ./serve.sh /usr/local/bin/serve.sh

RUN chmod a+x /usr/local/bin/serve.sh
ENTRYPOINT ["/usr/local/bin/serve.sh"]
