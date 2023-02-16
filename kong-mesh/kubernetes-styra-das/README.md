# Kong Mesh with OPA on Kubernetes and Styra DAS

_This tutorial has been deprecated (although it is still maintained).  Please see the [Kong Mesh Tutorial](https://docs.styra.com/v1/docs/tutorials/kong-mesh/) within the official Styra DAS documentation for a more complete example._

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

### 3. Create a Kong Mesh System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Kong Mesh` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `opa-policy-template.yaml` to a new file named `opa-policy.yaml`

**Edit** `opa-policy.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

Apply the OPA Policy configuration
```sh
kubectl apply -f opa-policy.yaml
```

Update the Kong Mesh OPA configuration to override the plugin query path
```sh
kubectl apply -f kong-mesh-control-plane-config.yaml -n kong-mesh-system
```

Restart the Kong Mesh control plane Pod
```sh
kubectl -n kong-mesh-system delete pod kong-mesh-control-plane-xxxxxxxxxx-yyyyy
```

### 5. Create App Deployment

The `example-app.yaml` includes a `Deployment` for the `example-app` and the `example-app-service` `Service`
* _The `kuma-sidecar` sidecar will be automatically injected_
* A separate OPA sidecar does _not_ need to be deployed as the `kuma-sidecar` provided by Kong Mesh includes an OPA engine along with the Envoy proxy in a single sidecar instance

```sh
kubectl apply -f example-app.yaml
```

Set the `SERVICE_URL` environment variable to the service’s IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl -n kmesh-opa-demo get service example-app -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

### 6. Create and Exercise the OPA policy

#### Create the Ingress Policy

Within your Styra DAS Kong Mesh system replace the contents of the `policy/ingress/rules.rego` file with the following:
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

### 7. Review the Decisions in Styra DAS

OPA will evaluate each authorization query invoked by the Kong Mesh sidecar, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.

