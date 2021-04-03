# Istio with OPA

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) tutorial using [Istio](https://istio.io/)

## Prerequisites

A Kubernetes cluster with Istio [installed](https://istio.io/latest/docs/setup/getting-started/#install)

_This tutorial has been tested with Istio 1.8_

## Steps

### 1. Start Minikube

[Install](https://istio.io/latest/docs/setup/getting-started/#install) Istio (if not already installed)

### 2. Create the `istio-opa-demo` namespace with istio-injection enabled

```
kubectl apply -f namespace.yaml
```

### 3. Create the Istio `EnvoyFilter`

The `EnvoyFilter` configuration defines an external authorization filter `envoy.ext_authz` for a gRPC authorization server provided by the OPA sidecar.

```
kubectl apply -f envoy-filter.yaml
```

### 4. Define the OPA policy

```
kubectl create secret generic opa-policy --from-file policy.rego -n istio-opa-demo
```

### 5. Create App Deployment with OPA sidecar

The `example-app.yaml` includes both a `Deployment` with the `example-app` and `opa` sidecar, as well as the `example-app-service` `Service`
* _The `istio-proxy` sidecar will be automatically injected_

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl -n istio-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 6. Exercise the OPA policy

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/#6-exercise-the-opa-policy
