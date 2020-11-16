

FROM ubuntu:latest as nlsbuilder

WORKDIR /home/maven

RUN apt-get update && DEBIAN_FRONTEND="noninteractive" apt-get install -y maven git openjdk-8-jdk scala && \
	update-java-alternatives --set /usr/lib/jvm/java-1.8.0-openjdk-amd64

RUN git clone https://github.com/Hotzkow/webtest.git

# # coppy config file then compile
# COPY application.conf /home/maven/webtest/language-analysis-app/src/main/resources/
RUN cd webtest && mvn install

FROM ubuntu:latest

RUN apt-get update && DEBIAN_FRONTEND="noninteractive" apt-get install -y openjdk-8-jre wget curl



COPY --from=nlsbuilder /home/maven/webtest/language-analysis-app/target/language-analysis-app-1.0-SNAPSHOT-standalone.jar /home/nls-service.jar

EXPOSE 1234

WORKDIR /home

# fetch the word2vec model
RUN mkdir /home/TestData && \
	wget https://s3.amazonaws.com/dl4j-distribution/GoogleNews-vectors-negative300.bin.gz -O /home/TestData/GoogleNews-vectors-negative300.bin.gz

# need to start the jar with -Xmx4g due to huge word2vec model

CMD ["java", "-jar","-Xmx4g","/home/nls-service.jar"]
