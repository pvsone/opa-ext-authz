# Kuma

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-authorization/) tutorial using [Kuma](https://kuma.io/)

## Prerequisites

A Kubernetes cluster with Kuma [installed](https://kuma.io/docs/0.7.1/installation/kubernetes/)

## Steps

### 1. Start Minikube

### 2. Create the `kuma-opa-demo` namespace with kuma sidecar-injection enabled

```
kubectl apply -f namespace.yaml
```

### 3. Define the OPA policy

```
kubectl create secret generic opa-policy --from-file policy.rego -n kuma-opa-demo
```

### 4. Create App Deployment with OPA sidecar

The `example-app.yaml` includes both a `Deployment` with the `example-app` and `opa` sidecar, as well as the `example-app-service` `Service`
* _The `kuma-dp` sidecar will be automatically injected_
* In order to add an `httpFilter` via the [`ProxyTemplate`](https://kuma.io/docs/0.7.1/policies/proxy-template/#http-filter) (_in **Step 5.** below_), the `Service` must be annotated with `<port>.service.kuma.io/protocol: http` as described in [Protocol support in Kuma](https://kuma.io/docs/0.7.1/policies/protocol-support-in-kuma/)

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl get service example-app-service -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 5. Create the Kuma `ProxyTemplate`

The `ProxyTemplate` configuration defines an external authorization filter `envoy.ext_authz` for a gRPC authorization server provided by the OPA sidecar.

```
kubectl apply -f proxy-template.yaml
```

### 6. Exercise the OPA policy

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-authorization/#6-exercise-the-opa-policy
