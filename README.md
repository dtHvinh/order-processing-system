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
