FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD smppsim.jar smppsim.jar
COPY conf/ /conf/
COPY www.zip /www.zip
RUN sh -c 'touch /smppsim.jar'
RUN sh -c 'unzip /www.zip'
ENTRYPOINT [ "sh", "-c", "java -Djava.net.preferIPv4Stack=true -Djava.util.logging.config.file=/conf/logging.properties -jar /smppsim.jar /conf/smppsim.props" ]
