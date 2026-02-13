param(
  [string]$Namespace = "kafka",
  [string]$Release = "kafka-server",
  [int]$LocalKafkaPort = 9094
)

$ErrorActionPreference = "Stop"

$kafkaService = $Release

Write-Host "Port-forward Kafka service '$kafkaService' (namespace '$Namespace') to localhost:$LocalKafkaPort ..."
Write-Host "Kafka clients / Kafka UI (running on your machine) should use: localhost:$LocalKafkaPort"
Write-Host "Press Ctrl+C to stop."

kubectl -n $Namespace port-forward svc/$kafkaService ${LocalKafkaPort}:9094
