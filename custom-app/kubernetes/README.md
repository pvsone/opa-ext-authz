# Custom Application with Kubernetes

A variant of the [OPA HTTP API Authorization](https://www.openpolicyagent.org/docs/latest/http-api-authorization/) tutorial using Kubernetes

## Goals

It is recommended that you read through and run the [OPA HTTP API Authorization](https://www.openpolicyagent.org/docs/latest/http-api-authorization/) tutorial as documented (using Docker Compose) prior to running this version on Kubernetes.

This tutorial is an abridged version of the official tutorial, with a focus on deploying the application and policy to a Kubernetes environment. The original tutorial provides greater detail and description of the application and policy implementation and behavior.

## Prerequisites

This tutorial requires a Kubernetes cluster.  To run the tutorial locally, we recommend using [minikube](https://minikube.sigs.k8s.io/docs/start/).

## Steps

### 1. Start Minikube

```bash
minikube start
```
*  Note: On a mac, `minikube start --driver=hyperkit` is recommended for ease of use of the NodePort service type.


### 2. Define the OPA policy

```
kubectl create configmap opa-policy --from-file example.rego
```

### 3. Create App Deployment with OPA sidecar

The `example-app.yaml` includes both a `Deployment` with the `example-app` and `opa` sidecar, as well as the `example-app-service` `Service`

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl get service example-app-service -o jsonpath='{.spec.ports[?(@.port==5000)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

* Note: If the `SERVICE_URL` value is not generated properly, or doesn't allow access to the app during testing below, consult minikube's [Accessing apps](https://minikube.sigs.k8s.io/docs/handbook/accessing/) documentation for guidance on accessing the application for your minikube configuration.

### 4. Exercise the OPA policy

#### Check that `alice` can see her own salary.

```
curl --user alice:password $SERVICE_URL/finance/salary/alice
```

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl --user bob:password $SERVICE_URL/finance/salary/alice
```

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl --user bob:password $SERVICE_URL/finance/salary/charlie
```
