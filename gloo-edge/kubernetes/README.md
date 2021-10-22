# Gloo Edge with OPA on Kubernetes

An example application deployment using the [OPA-Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) with [Gloo Edge](https://docs.solo.io/gloo-edge) for external authorization in an Envoy-based gateway.

## Prerequisites

A Kubernetes cluster with Gloo Edge Gateway [installed](https://docs.solo.io/gloo-edge/latest/installation/gateway/kubernetes/)

_This tutorial has been tested with Gloo Edge 1.9.0_

## Steps

### 1. Start Minikube

[Install](https://docs.solo.io/gloo-edge/latest/installation/gateway/kubernetes/) the Gloo Edge Gateway (if not already installed)

### 2. Create OPA Deployment

Create the OPA policy in the `gloo-system` namespace
```sh
kubectl create secret generic opa-policy --from-file policy.rego -n gloo-system
```

Deploy OPA to the `gloo-system` namespace
```sh
kubectl apply -f opa.yaml
```

### 3. Create App Deployment with Gloo VirtualService

The `example-app.yaml` includes both a `Deployment` and `Service`, as well as the Gloo `VirtualService` which is configured to utilize external authorization.

```sh
kubectl apply -f example-app.yaml
```

### 4. Update Gloo Settings

Enable OPA as the external authorization server for Gloo by patching the Gloo settings
```sh
kubectl patch settings default --patch-file gloo-settings-patch.yaml --type merge -n gloo-system
```

### 5. Exercise the OPA policy

#### Check that a `GET` request to the `/get` endpoint is **Allowed** (`200 OK`).

```sh
curl -X GET $(glooctl proxy url)/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $(glooctl proxy url)/post -i
```
