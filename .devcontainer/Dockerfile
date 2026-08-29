ARG JAVA_VERSION=21
FROM amazoncorretto:${JAVA_VERSION}-al2023

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN dnf install -y \
        git \
        tar \
        gzip \
        xz \
        shadow-utils \
        unzip \
        findutils \
    && dnf clean all \
    && groupadd --gid ${USER_GID} ${USERNAME} \
    && useradd --uid ${USER_UID} --gid ${USER_GID} --create-home --shell /bin/bash ${USERNAME}

ARG GRADLE_VERSION=9.7.1
RUN curl -fSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
        -o /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && mv /opt/gradle-${GRADLE_VERSION} /opt/gradle \
    && rm /tmp/gradle.zip
ENV GRADLE_HOME=/opt/gradle \
    PATH=/opt/gradle/bin:${PATH}

ARG JMETER_VERSION=5.6.3
RUN curl -fSL "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" \
        | tar -xz -C /opt \
    && ln -s /opt/apache-jmeter-${JMETER_VERSION} /opt/jmeter
ENV JMETER_HOME=/opt/jmeter \
    PATH=/opt/jmeter/bin:${PATH}

ARG NODE_VERSION=22.23.2
ENV NODE_HOME=/opt/node \
    PATH=/opt/node/bin:${PATH}
RUN curl -fSL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.xz" \
        | tar -xJ -C /opt \
    && ln -s /opt/node-v${NODE_VERSION}-linux-x64 /opt/node \
    && npm install -g @fission-ai/openspec@latest

ARG SONAR_SCANNER_VERSION=6.2.1.4610
RUN curl -fSL "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux-x64.zip" \
        -o /tmp/sonar-scanner.zip \
    && unzip /tmp/sonar-scanner.zip -d /opt \
    && mv /opt/sonar-scanner-${SONAR_SCANNER_VERSION}-linux-x64 /opt/sonar-scanner \
    && rm /tmp/sonar-scanner.zip \
    && dnf clean all
ENV SONAR_SCANNER_HOME=/opt/sonar-scanner \
    PATH=/opt/sonar-scanner/bin:${PATH}

USER ${USERNAME}
