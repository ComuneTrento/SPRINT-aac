FROM maven:3.5.2-jdk-8-alpine

WORKDIR /app

COPY . /app

COPY docker-configs/commoncore.properties src/main/resources/commoncore.properties
COPY docker-configs/admin.txt src/main/webapp/admin.txt

# install tomcat
RUN wget -O apache-tomcat.tgz http://it.apache.contactlab.it/tomcat/tomcat-8/v8.5.28/bin/apache-tomcat-8.5.28.tar.gz
RUN tar xzvf apache-tomcat.tgz
RUN rm apache-tomcat.tgz

EXPOSE 8080

CMD mvn clean package -Dmaven.test.skip=true \
&& cp target/aac.war apache-tomcat-8.5.28/webapps && ./apache-tomcat-8.5.28/bin/catalina.sh run

