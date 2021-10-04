# Kong Mesh with OPA on Kubernetes

An example application deployment using the [OPA-Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) with [Kong Mesh](https://konghq.com/kong-mesh/) for external authorization in an Envoy-based service mesh.

## Prerequisites

A Kubernetes cluster with Kong Mesh [installed](https://docs.konghq.com/mesh/1.4.x/installation/kubernetes/)

_This tutorial has been tested with Kong Mesh 1.4.0_

## Steps

### 1. Start Minikube

[Install](https://docs.konghq.com/mesh/1.4.x/installation/kubernetes/) the Kong Mesh Control Plane (if not already installed)

### 2. Create the `kmesh-opa-demo` namespace with kuma sidecar-injection enabled

```sh
kubectl apply -f namespace.yaml
```

### 3. Define the OPA policy

```sh
kubectl apply -f opa-policy.yaml
```

### 4. Create App Deployment

The `example-app.yaml` includes a `Deployment` for the `example-app` and the `example-app-service` `Service`
* _The `kuma-sidecar` sidecar will be automatically injected_
* A separate OPA sidecar does _not_ need to be deployed as the `kuma-sidecar` provided by Kong Mesh includes an OPA engine along with the Envoy proxy in a single sidecar instance

```sh
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl -n kmesh-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 6. Exercise the OPA policy

#### Check that a `GET` request to the `/get` endpoint is **Allowed** (`200 OK`).

```sh
curl -X GET $SERVICE_URL/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $SERVICE_URL/post -i
```
