# Kong Mesh with OPA on Kubernetes

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) tutorial using [Kong Mesh](https://konghq.com/kong-mesh/)

## Prerequisites

A Kubernetes cluster with Kong Mesh [installed](https://docs.konghq.com/mesh/1.2.x/installation/kubernetes/)

_This tutorial has been tested with Kong Mesh 1.2.1_

## Steps

### 1. Start Minikube

[Install](https://docs.konghq.com/mesh/1.2.x/installation/kubernetes/) the Kong Mesh Control Plane (if not already installed)

### 2. Create the `kmesh-opa-demo` namespace with kuma sidecar-injection enabled

```
kubectl apply -f namespace.yaml
```

### 3. Define the OPA policy

```
kubectl apply -f opa-policy.yaml
```

### 4. Create App Deployment

The `example-app.yaml` includes a `Deployment` for the `example-app` and the `example-app-service` `Service`
* _The `kuma-sidecar` sidecar will be automatically injected_
* A separate OPA sidecar does _not_ need to be deployed as the `kuma-sidecar` provided by Kong Mesh includes an OPA engine along with the Envoy proxy in a single sidecar instance

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl -n kmesh-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 5. Exercise the OPA policy

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/#6-exercise-the-opa-policy
