# Kong Mesh with OPA on Kubernetes and Styra DAS

Run an OPA demo application with [Kong Mesh](https://docs.konghq.com/mesh/) and the [OPA Envoy Plugin](https://github.com/open-policy-agent/opa-envoy-plugin) on Kubernetes, and using Styra DAS as the OPA management control plane.

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

### 3. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `opa-policy-template.yaml` to a new file named `opa-policy.yaml`

**Edit** `opa-policy.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

Apply the OPA Policy configuration
```
kubectl apply -f opa-policy.yaml
```

Update the Kong Mesh OPA configuration to override the plugin query path
```
kubectl apply -f kong-mesh-control-plane-config.yaml -n kong-mesh-system
```

Restart the Kong Mesh control plane Pod
```
kubectl -n kong-mesh-system delete pod kong-mesh-control-plane-xxxxxxxxxx-yyyyy
```

### 5. Create App Deployment

The `example-app.yaml` includes a `Deployment` for the `example-app` and the `example-app-service` `Service`
* _The `kuma-sidecar` sidecar will be automatically injected_
* A separate OPA sidecar does _not_ need to be deployed as the `kuma-sidecar` provided by Kong Mesh includes an OPA engine along with the Envoy proxy in a single sidecar instance

```
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```
export SERVICE_PORT=$(kubectl -n kmesh-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==5000)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 6. Exercise the OPA policy

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

### 7. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
