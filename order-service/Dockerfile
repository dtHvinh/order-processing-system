FROM docker.io/apache/karaf:4.4.6 AS karaf

FROM docker.io/eclipse-temurin:17.0.17_10-jdk-ubi10-minimal

ARG APP_VERSION

ENV KARAF_HOME=/opt/apache-karaf-4.4.6
WORKDIR $KARAF_HOME

COPY --from=karaf $KARAF_HOME/ $KARAF_HOME/

RUN echo "org.apache.cxf.servlet.context=/" > ${KARAF_HOME}/etc/org.apache.cxf.osgi.cfg

COPY src/main/resources/config/com.dthvinh.order_service.cfg ${KARAF_HOME}/etc/com.dthvinh.order_service.cfg

COPY target/common-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/common/${APP_VERSION}/common-${APP_VERSION}.jar
COPY target/metrics-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/metrics/${APP_VERSION}/metrics-${APP_VERSION}.jar
COPY target/messaging-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/messaging/${APP_VERSION}/messaging-${APP_VERSION}.jar
COPY target/order-service-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/order-service/${APP_VERSION}/order-service-${APP_VERSION}.jar
COPY target/feature.xml ${KARAF_HOME}/deploy/feature.xml

EXPOSE 8181

CMD ["bin/karaf", "run"]