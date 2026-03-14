FROM docker.io/apache/karaf:4.4.6 AS karaf

FROM docker.io/eclipse-temurin:17.0.17_10-jre-ubi10-minimal

ARG APP_VERSION

ENV KARAF_HOME=/opt/apache-karaf-4.4.6
WORKDIR $KARAF_HOME

COPY --from=karaf $KARAF_HOME/ $KARAF_HOME/

RUN echo "org.apache.cxf.servlet.context=/" > ${KARAF_HOME}/etc/org.apache.cxf.osgi.cfg

COPY src/main/resources/config/org.ops4j.datasource-product.cfg ${KARAF_HOME}/etc/org.ops4j.datasource-product.cfg

COPY target/common-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/common/${APP_VERSION}/common-${APP_VERSION}.jar
COPY target/metrics-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/metrics/${APP_VERSION}/metrics-${APP_VERSION}.jar
COPY target/product-service-${APP_VERSION}.jar ${KARAF_HOME}/system/com/dthvinh/product-service/${APP_VERSION}/product-service-${APP_VERSION}.jar
COPY target/feature.xml ${KARAF_HOME}/deploy/feature.xml

EXPOSE 8181

CMD ["bin/karaf", "run"]