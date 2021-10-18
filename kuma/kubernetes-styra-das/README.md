# Kuma with OPA on Kubernetes and Styra DAS

_This tutorial has been deprecated (although it is still maintained).  Please see the [Kuma Tutorial](https://docs.styra.com/v1/docs/tutorials/kuma/) within the official Styra DAS documentation for a more complete example._

## Prerequisites

A Kubernetes cluster with Kuma [installed](https://kuma.io/docs/1.3.0/installation/kubernetes/)

_This tutorial has been tested with Kuma 1.3.0_

## Steps

### 1. Start Minikube

[Install](https://kuma.io/docs/1.3.0/installation/kubernetes/) the Kuma Control Plane (if not already installed)

### 2. Create the `kuma-opa-demo` namespace with kuma sidecar-injection enabled

```sh
kubectl apply -f namespace.yaml
```

### 3. Create the Kuma `ProxyTemplate`

The `ProxyTemplate` configuration defines an external authorization filter `envoy.ext_authz` for a gRPC authorization server provided by the OPA sidecar.

```sh
kubectl apply -f proxy-template.yaml
```

### 4. Create a Kuma System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Kuma` System type.

### 5. Create the Styra DAS configuration for OPA

**Copy** the file `example-app-template.yaml` to a new file named `example-app.yaml`

**Edit** `example-app.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

### 6. Create App Deployment with OPA sidecar

The `example-app.yaml` includes both a `Deployment` with the `example-app` and `opa` sidecar, as well as the `example-app-service` `Service`
* _The `kuma-dp` sidecar will be automatically injected_
* In order to add an `httpFilter` via the [`ProxyTemplate`](https://kuma.io/docs/1.3.0/policies/proxy-template/#http-filter) (_via **Step 3.** above_), the `Service` must be annotated with `<port>.service.kuma.io/protocol: http` as described in [Protocol support in Kuma](https://kuma.io/docs/1.3.0/policies/protocol-support-in-kuma/)

```sh
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl -n kuma-opa-demo get service example-app-service -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 7. Create and Exercise the OPA policy

#### Create the Ingress Policy

Within your Styra DAS Kuma system replace the contents of the `policy/ingress/rules.rego` file with the following:
```rego
package policy.ingress

import input.attributes.request.http as http_request

default allow = false

allow {
  http_request.method == "GET"
  input.parsed_path = ["get"]
}
```

**Publish** the policy to save and distribute the policy to the OPA instance.

#### Check that a `GET` request to the `/get` endpoint is **Allowed** (`200 OK`).

```sh
curl -X GET $SERVICE_URL/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $SERVICE_URL/post -i
```

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query invoked by the Kuma dataplane proxy, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.

