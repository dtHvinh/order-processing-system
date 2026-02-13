# Kafka (Helm chart) + Kafka UI

This folder is a Helm chart that deploys:

- `kafka-server` (Apache Kafka in **KRaft** single-node mode)
- `kafka-ui` (Provectus Kafka UI) as a subchart

## Prereqs

- Kubernetes cluster (Docker Desktop Kubernetes, minikube, kind, etc.)
- `kubectl`
- `helm`

## Install

From the repo root (or anywhere, using a correct chart path):

- Install/upgrade:
  - `helm upgrade --install kafka-server .\kafka-server\src\main\resources\helm`

## One command: deploy (NodePort) for external clients

If you want Kafka to be reachable for services/clients outside the cluster via:

- `192.168.65.3:30092`

and Kafka UI to connect in-cluster automatically, run:

- `mvn -f .\kafka-server\pom.xml -P k8s-nodeport-up validate`

Defaults:

- Namespace: `kafka`
- Release: `kafka-server`
- Kafka external host: `192.168.65.3`
- Kafka external NodePort: `30092`
- Kafka UI NodePort: `30090`

Override if needed:

- `mvn -f .\kafka-server\pom.xml -P k8s-nodeport-up -Dkafka.externalAdvertisedHost=192.168.65.3 -Dkafka.externalNodePort=30092 validate`

## Local profile (port-forward Kafka for a local UI)

If you run Kafka UI (or any Kafka client) **on your machine** (outside the cluster), Kafka must advertise `localhost` and a port that you forward.

This chart includes a local override profile:

- `values.portforward.yaml`

Run:

### Option A) Maven profile (recommended)

From the repo root:

- Install/upgrade Kafka + Kafka UI into namespace `kafka`:
  - `mvn -f .\kafka-server\pom.xml -P k8s-local exec:exec@helm-install`
- Wait for Kafka to be ready:
  - `mvn -f .\kafka-server\pom.xml -P k8s-local exec:exec@rollout-wait`
- Port-forward Kafka so local clients can connect (this blocks the terminal):
  - `mvn -f .\kafka-server\pom.xml -P k8s-local exec:exec@port-forward-kafka`

Kafka clients / Kafka UI running on your machine should use:

- `localhost:9094`

### Option B) PowerShell scripts

- Install (creates namespace `kafka` by default):
  - `powershell -File .\kafka-server\scripts\kafka-helm-local.ps1`
- Port-forward Kafka EXTERNAL listener to your machine:
  - `powershell -File .\kafka-server\scripts\kafka-portforward.ps1`

Then configure your locally-running Kafka UI / client to use:

- `localhost:9094`

Useful checks:

- `kubectl get pods`
- `kubectl get svc kafka-server kafka-server-kafka-ui`

## What to change on another machine

### 1) External advertised host (most common)

File: `kafka-server/values.yaml`

- `kafka.externalAdvertisedHost`

This value is used to build `KAFKA_ADVERTISED_LISTENERS` (for the **EXTERNAL** listener). If you run on a different machine/cluster, you must set it to an IP/DNS name that your _external Kafka clients_ can reach.

Examples:

- Docker Desktop Kubernetes:
  - Often a VM/host IP like `192.168.65.3`, but it can differ per machine.
- minikube:
  - Use `minikube ip`
- kind:
  - Usually you’ll use port-forwarding or a LoadBalancer implementation; NodePort may not be ideal.

If you want to fully control the advertised listeners, set:

- `kafka.advertisedListeners` (takes precedence over generated value)

### 2) NodePorts / ports

File: `kafka-server/values.yaml`

- In-cluster ports (should normally stay default):
  - `service.ports.internal` (default `9092`)
  - `service.ports.controller` (default `9093`)
- External listener port exposed by the Service:
  - `service.ports.external` (default `30092`)
  - `service.nodePorts.external` (default `30092`)

Notes:

- If `service.type` is `NodePort`, Kubernetes may assign a random nodePort if you don’t pin it.
- If you change `service.ports.external` / `service.nodePorts.external`, also ensure your clients use that port.

### 3) Kafka UI: which broker to connect to

By default Kafka UI will connect to the in-cluster service name for the current release.

File: `kafka-server/charts/kafka-ui/values.yaml`

- If you need to override bootstrap servers explicitly:
  - `kafka.bootstrapServers: "kafka-server:9092"`
- Or override only the service name/port:
  - `kafka.serviceName: "kafka-server"`
  - `kafka.bootstrapPort: 9092`

This is useful when:

- You install the chart with a different release name
- Kafka is in a different namespace
- You point Kafka UI to an external Kafka cluster

### 4) Release name / namespace

If you install into a different namespace, remember to include `-n <ns> --create-namespace`:

- `helm upgrade --install kafka-server .\kafka-server -n ms --create-namespace`

Then use:

- `kubectl get svc -n ms`

If Kafka UI cannot resolve the broker service name, check DNS:

- `kubectl run dnscheck --rm -i --restart=Never --image=busybox:1.36 -- sh -c "nslookup kafka-server.default.svc.cluster.local"`

## Verify Kafka UI is online

Inside the cluster:

- `kubectl run kafka-ui-check --rm -i --restart=Never --image=curlimages/curl:8.6.0 -- sh -c "curl -sS http://kafka-server-kafka-ui:8080/api/clusters"`

You should see `"status":"online"`.

## Notes (KRaft single-node)

This chart defaults the KRaft controller quorum voters to `localhost:9093` for single-node startup reliability.

If you move to multi-node KRaft, you’ll need to explicitly set `kafka.controllerQuorumVoters` to the correct `<nodeId>@<host>:<controllerPort>` set for all controllers.
