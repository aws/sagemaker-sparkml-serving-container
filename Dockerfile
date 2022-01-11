FROM openjdk:8

ENV DEBIAN_FRONTEND=noninteractive

LABEL com.amazonaws.sagemaker.capabilities.accept-bind-to-port=true

RUN apt-get update \
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
    zlib1g-dev

RUN apt -y update

ARG OPENSSL_VERSION=1.1.1l
ARG PYTHON=python3
ARG PIP=pip3
ARG PYTHON_VERSION=3.6.13

# Open-SSL
RUN wget -q -c https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz \
 && tar -xzf openssl-${OPENSSL_VERSION}.tar.gz \
 && cd openssl-${OPENSSL_VERSION} \
 && ./config && make -j $(nproc) && make install \
 && ldconfig \
 && cd .. && rm -rf openssl-* \
 && rmdir /usr/local/ssl/certs \
 && ln -s /etc/ssl/certs /usr/local/ssl/certs

# Install Python-3.6.13 from source
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

RUN cp ./target/sparkml-serving-2.4.jar /usr/local/lib/sparkml-serving-2.4.jar
RUN cp ./serve.sh /usr/local/bin/serve.sh

RUN chmod a+x /usr/local/bin/serve.sh
ENTRYPOINT ["/usr/local/bin/serve.sh"]
