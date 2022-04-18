# Envoy with OPA on Kubernetes

Run an OPA demo application with [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy)
and the [OPA Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) 
on Kubernetes.

## Steps

### 1. Start Minikube

```sh
minikube start
```

### 2. Create the Envoy `ConfigMap`

The Envoy configuration defines an external authorization filter `envoy.ext_authz` for a gRPC authorization server provided by the OPA sidecar.

```sh
kubectl create configmap proxy-config --from-file envoy.yaml
```

### 3. Define the OPA policy

```sh
kubectl create secret generic opa-policy --from-file policy.rego
```

## 4. Run the App with OPA and Envoy sidecars

The `example-app.yaml` includes a `Deployment` with the `example-app`, `opa` and `envoy` sidecars, as well as the `example-app` `Service`

```sh
kubectl apply -f example-app.yaml
```

### 5. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the `example-app` service IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl get service example-app -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

#### Check that a `GET` request to the `/get` endpoint is **Allowed** (`200 OK`).

```sh
curl -X GET $SERVICE_URL/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $SERVICE_URL/post -i
```
