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
