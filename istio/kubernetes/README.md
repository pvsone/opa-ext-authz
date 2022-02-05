# Istio with OPA

_This tutorial has been deprecated (although it is still maintained).  Please see the [Istio Tutorial](https://www.openpolicyagent.org/docs/latest/envoy-tutorial-istio/) within the official OPA documentation for a more complete example._

## Prerequisites

A Kubernetes cluster with Istio [installed](https://istio.io/latest/docs/setup/getting-started/#install)

_This tutorial has been tested with Istio 1.12_

## Steps

### 1. Start Minikube

[Install](https://istio.io/latest/docs/setup/getting-started/#install) Istio (if not already installed)

### 2. Create the `istio-opa-demo` namespace with istio-injection enabled

```sh
kubectl apply -f namespace.yaml
```

### 3. Create the Istio external authorizer

The `AuthorizationPolicy` defines a CUSTOM action to invoke an `opa-sidecar` for authorization for the `example-app` app (the app will be deployed in a subsequent step).

```sh
kubectl apply -f opa-authz-policy.yaml
```

Define the external authorizer for the mesh
```sh
kubectl edit configmap istio -n istio-system
```

```yaml
data:
  mesh: |-
    # Add the following content to define the external authorizer.
    extensionProviders:
    - name: "opa-sidecar"
      envoyExtAuthzGrpc:
        service: "opa-grpc.local"
        port: "9191"
```

Rollout the changes
```sh
kubectl rollout restart deployment/istiod -n istio-system
```

### 4. Define the OPA policy

```sh
kubectl create secret generic opa-policy --from-file policy.rego -n istio-opa-demo
```

### 5. Create App Deployment with OPA sidecar

The `example-app.yaml` includes a `Deployment` with the `example-app` and `opa` sidecar, the `example-app` `Service`, and the Istio `ServiceEntry` for the `opa` sidecar.
* _The `istio-proxy` sidecar will be automatically injected_

```sh
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl -n istio-opa-demo get service example-app -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
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
