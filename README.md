## Diagram:

```
Client
|
v
API Gateway
|
+----> Order Service (Java, Sync REST)
|
+----> Product Service (Java, Sync REST)
|
+----> Inventory Service (Rust, Sync REST)
|
v
Kafka (Async Events)
```

## Local dev quickstart

### Prereqs

- Kubernetes cluster (Docker Desktop Kubernetes or similar)
- `kubectl`
- `helm`

### Kafka + Kafka UI

- Install/upgrade:
  - `helm upgrade --install kafka-server .\kafka-server`
- What you may need to change on a different machine (IP/ports):
  - See [kafka-server/README.md](kafka-server/README.md)

### Order Service (build + docker + helm)

The `order-service` module has a Maven profile that will:

- build the Java bundle (`clean` + `package`),
- build a Docker image tagged with the Maven `${project.version}`,
- run `helm upgrade --install` while setting `image.repository` and `image.tag` to match.

From `order-processing-system/order-service`:

- `mvn -Plocal-deploy clean package`

Optional overrides:

- Custom image repo/name:
  - `mvn -Plocal-deploy -Ddocker.image.repository=order-service clean package`
- Custom namespace / release:
  - `mvn -Plocal-deploy -Dhelm.namespace=dev -Dhelm.release.name=order-service clean package`

## Cách dùng ConfigAdmin

### Bước 1: Sửa Blueprint để dùng ConfigAdmin

Thêm namespace cm:

```
xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0"
```

Và schema:

```
http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0
http://aries.apache.org/schemas/blueprint-cm/blueprint-cm-1.1.0.xsd
```

### Bước 2: Khai báo property-placeholder

```xml
<cm:property-placeholder persistent-id="com.dthvinh.publisher"/>
```

### Lưu ý:

- persistent-id phải trùng với tên file cfg.

File phải đặt tại:

KARAF_HOME/etc/com.dthvinh.publisher.cfg

### Bước 3: Inject vào bean

Thêm bean Publisher:

```xml
<bean id="publisher" class="com.dthvinh.rs.messaging.publisher.impl.PublisherImpl"
      init-method="init">
<property name="bootStrapServer" value="${kafka.bootstrap.server}"/>
</bean>
```
