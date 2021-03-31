FROM 861417252882.dkr.ecr.eu-west-1.amazonaws.com/es/olimpo/images/docker/base/openjdk:1.0.0
VOLUME /tmp
RUN mkdir /app
COPY target/*.jar /app/jv-batch-cloud-application.jar
ENTRYPOINT ["java","-Dfile.encoding=utf-8","-Djava.security.egd=file:/dev/./urandom","-XX:+ExitOnOutOfMemoryError","-Xms512m","-Xmx2048m", "-XshowSettings:vm","-jar","/app/jv-batch-cloud-application.jar"]
