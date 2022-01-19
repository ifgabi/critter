FROM x11docker/xfce
ARG JAR_FILE=target/critter-bot.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

RUN apt-get update
RUN apt-get install xvfb --assume-yes
RUN apt-get install openjdk-17-jre --assume-yes
RUN apt-get install krita --assume-yes

RUN mkdir /usr/critter
COPY src/main/resources/critter/ /usr/critter/
#there is no /usr/share/krita without the apt-get krita
COPY src/main/resources/critter/scripts/ /usr/share/krita/pykrita/
