ARG JAVA_VERSION=21
FROM amazoncorretto:${JAVA_VERSION}-al2023

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN dnf install -y \
        git \
        tar \
        gzip \
        shadow-utils \
    && dnf clean all \
    && groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home --shell /bin/bash ${USERNAME}

ARG MAVEN_VERSION=3.9.9
RUN curl -fSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
        | tar -xz -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven
ENV MAVEN_HOME=/opt/maven \
    PATH=/opt/maven/bin:${PATH}

ARG JMETER_VERSION=5.6.3
RUN curl -fSL "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" \
        | tar -xz -C /opt \
    && ln -s /opt/apache-jmeter-${JMETER_VERSION} /opt/jmeter
ENV JMETER_HOME=/opt/jmeter \
    PATH=/opt/jmeter/bin:${PATH}

USER ${USERNAME}
