FROM maven:3.5.2-jdk-8-alpine

WORKDIR /app

COPY . /app

COPY docker-configs/commoncore.properties src/main/resources/commoncore.properties
COPY docker-configs/admin.txt src/main/webapp/admin.txt
RUN wget apache.panu.it/tomcat/tomcat-8/v8.5.27/bin/apache-tomcat-8.5.27.tar.gz
RUN tar xzvf apache-tomcat-8.5.27.tar.gz
RUN rm apache-tomcat-8.5.27.tar.gz

EXPOSE 8080

CMD mvn clean package -Dmaven.test.skip=true \
&& cp target/aac.war apache-tomcat-8.5.27/webapps && ./apache-tomcat-8.5.27/bin/catalina.sh run

