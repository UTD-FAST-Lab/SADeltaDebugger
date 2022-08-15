FROM ubuntu:20.04 as setup

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y && apt-get upgrade -y
RUN apt-get -y install git openjdk-8-jdk git maven wget
ENV DELTA_DEBUGGER_HOME="/app/SADeltaDebugger"


WORKDIR /app  
RUN git clone --branch test_branch https://github.com/Pancax/SADeltaDebugger
WORKDIR /app/SADeltaDebugger
ADD "https://api.github.com/repos/Pancax/SADeltaDebugger/commits/test_branch" latest_commits
RUN git pull
WORKDIR /app/SADeltaDebugger/ProjectLineCounter
RUN mvn install
WORKDIR /app/SADeltaDebugger/ViolationDeltaDebugger
RUN mvn compile package -DskipTests
ENTRYPOINT ["mvn","test"]
