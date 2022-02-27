FROM harbor.trssllc.com/trss-base-image/trss-ubuntu-20.04-java8:stable

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS=""

WORKDIR /home/trss

USER root
ADD entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown trss:trss entrypoint.sh
USER trss

ADD *.war app.war

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 9999 5701/udp
