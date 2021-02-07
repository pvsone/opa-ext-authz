# Istio with OPA and Styra DAS

Run an OPA demo application with [Istio](https://istio.io/latest/docs/concepts/what-is-istio/) and the [OPA Envoy Plugin](https://github.com/open-policy-agent/opa-envoy-plugin), and using Styra DAS as the OPA management control plane.

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

### 4. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 5. Create the Styra DAS configuration for OPA

**Copy** the file `example-app-template.yaml` to a new file named `example-app.yaml`

**Edit** `example-app.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

### 6. Create App Deployment with OPA sidecar

The `example-app.yaml` includes both a `Deployment` with the `example-app` and `opa` sidecar, as well as the `example-app-service` `Service`
* _The `istio-proxy` sidecar will be automatically injected_

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl -n istio-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==5000)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 7. Exercise the OPA policy

The `Ingress`, `Egress` and `Application` policies are pre-created in the Styra DAS Envoy system for the example application.

You can review the policies within the respective policy modules in the DAS UI. No modifications to the policies are necessary for this tutorial, although this tutorial will not exercise the `Egress` policy.  The tests below will demonstrate the `Ingress` and `Application` policies only.

#### Check that `alice` can see her own salary.

```
curl -i --user alice:password $SERVICE_URL/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl -i --user bob:password $SERVICE_URL/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl -i --user bob:password $SERVICE_URL/finance/salary/charlie
```

This is **allowed** by the `Ingress` policy, but **denied** by the `Application` policy.

#### Check that `bob` CANNOT view the HR dashboard.
The HR Dashboard is not allowed for any user per the default `Ingress` policy.

```
curl -i --user bob:password $SERVICE_URL/hr/dashboard
```

This is **denied** by the `Ingress` policy.  The request is never handled by the application as Envoy rejects the request with a `403 Forbidden` response.

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
