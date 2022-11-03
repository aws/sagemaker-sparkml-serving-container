FROM openjdk:8

ENV DEBIAN_FRONTEND=noninteractive

LABEL com.amazonaws.sagemaker.capabilities.accept-bind-to-port=true

RUN apt-get update \
 && apt-get -y upgrade \
 && apt-get -y install apt-utils \
    net-tools \
    apt-transport-https \
    wget \
    curl \
    nginx \
    git \
    maven \
    make \
    gcc \
    # openssl \
    zlib1g-dev

ARG OPENSSL_VERSION=1.1.1q
ARG PYTHON=python3
ARG PIP=pip3
ARG PYTHON_VERSION=3.10.7

# Open-SSL
RUN wget -q -c https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz \
 && tar -xzf openssl-${OPENSSL_VERSION}.tar.gz \
 && cd openssl-${OPENSSL_VERSION} \
 && ./config && make -j $(nproc) && make install \
 && ldconfig \
 && cd .. && rm -rf openssl-* \
 && rmdir /usr/local/ssl/certs \
 && ln -s /etc/ssl/certs /usr/local/ssl/certs

# Install Python-3.10.7 from source
RUN wget -q https://www.python.org/ftp/python/$PYTHON_VERSION/Python-$PYTHON_VERSION.tgz \
 && tar -xzf Python-$PYTHON_VERSION.tgz \
 && cd Python-$PYTHON_VERSION \
 && ./configure \
 && make -j $(nproc) && make install \
 && cd .. && rm -rf ../Python-$PYTHON_VERSION* \
 && ln -s /usr/local/bin/pip3 /usr/bin/pip \
 && ln -s /usr/local/bin/$PYTHON /usr/local/bin/python \
 && ${PIP} --no-cache-dir install --upgrade pip

# Remove other Python installations.
RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*

COPY / /sagemaker-sparkml-model-server
WORKDIR /sagemaker-sparkml-model-server

RUN mvn clean package

RUN cp ./target/sparkml-serving-3.3.jar /usr/local/lib/sparkml-serving-3.3.jar \
 && cp ./serve.sh /usr/local/bin/serve.sh \
 && chmod a+x /usr/local/bin/serve.sh

# remove the maven-shared-utils packages - older versions create vulnerabilities
RUN find / -depth -name maven-shared-utils -type d -exec rm -r "{}" \; \
 # remove the surefire packages - contains versions of maven-shared-utils that create vulnerabilities
 && find / -depth -name surefire -type d -exec rm -r "{}" \; \
 # remove maven-shared-utils jar file with vulnerabilities
 # comment out if need to use maven utilities
 && rm /usr/share/java/maven-shared-utils.jar \
 # remove wagon-http-shaded jar file with vulnerabilities associated with org.jsoup:jsoup
 && rm /usr/share/java/wagon-http-shaded-3.3.4.jar \
 # remove plexus-utils directory because plexus-utils has vulnerabilities
 # comment out if need to use maven utilities
 && find / -depth -name plexus-utils -type d -exec rm -r "{}" \; \
 # remove old version of commons-compress with vulnerability
 && find / -depth -name commons-compress -type d -exec rm -r "{}/1.20" \; \
 # remove jar files from common-io v2.5 and 2.6 both have vulnerabilities
 && find / -name commons-io*2.5.jar -type f -exec rm "{}" \; \
 && find / -name commons-io*2.6.jar -type f -exec rm "{}" \; \
 # remove jackson-databind
 && find / -name jackson-databind -type d -exec rm -r "{}/2.13.3" \; \
 # remove junit-4.12.jar
 && find / -name junit-4.12.jar -type f -exec rm "{}" \; \
 # remove maven-compiler-plugin jar from maven repo
 && find / -name maven-compiler-plugin*.jar -type f -exec rm "{}" \; \
 # remove guava jar files
 && rm /usr/share/java/guava.jar && rm /root/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar \
 # remove commons-codec jar
 && find / -name commons-codec-1.11.jar -type f -exec rm "{}" \;

ENTRYPOINT ["/usr/local/bin/serve.sh"]