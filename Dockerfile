FROM gradle:8.5-jdk17

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download and install Android SDK
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

RUN mkdir -p ${ANDROID_HOME} && \
    cd ${ANDROID_HOME} && \
    curl -o sdk.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip sdk.zip && \
    rm sdk.zip

# Accept licenses and install required SDK components
RUN mkdir -p ${ANDROID_HOME}/licenses && \
    echo "8933bad161af4178b1185d1a37fbf41ea5269c55" > ${ANDROID_HOME}/licenses/android-sdk-license && \
    echo "d56f5187479451eabf01fb78af6dfcb131a6481e" >> ${ANDROID_HOME}/licenses/android-sdk-license

RUN ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager --update && \
    ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager "platforms;android-30" "build-tools;30.0.3"

WORKDIR /app
COPY . .

# Set Gradle configurations
ENV GRADLE_USER_HOME=/gradle
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"