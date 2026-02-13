param(
  [string]$Namespace = "kafka",
  [string]$Release = "kafka-server"
)

$ErrorActionPreference = "Stop"

$chartPath = Join-Path $PSScriptRoot "..\src\main\resources\helm"
$baseValues = Join-Path $chartPath "values.yaml"
$localValues = Join-Path $chartPath "values.portforward.yaml"

Write-Host "Installing/upgrading Helm release '$Release' in namespace '$Namespace'..."
helm upgrade --install $Release $chartPath -n $Namespace --create-namespace -f $baseValues -f $localValues
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "\nWaiting for deployments to become ready..."
kubectl -n $Namespace rollout status deploy/$Release --timeout=180s
kubectl -n $Namespace rollout status deploy/$Release-kafka-ui --timeout=180s

Write-Host "\nDone. Next, run port-forward in a separate terminal:"
Write-Host "  .\\scripts\\kafka-portforward.ps1 -Namespace $Namespace -Release $Release"
