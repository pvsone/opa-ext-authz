# Istio with OPA and Styra DAS

_This tutorial has been deprecated (although it is still maintained).  Please see the [Istio Tutorial](https://docs.styra.com/v1/docs/tutorials/istio/) within the official Styra DAS documentation for a more complete example._

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

### 4. Create an Istio System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Istio` System type.

### 5. Create the Styra DAS configuration for OPA

**Copy** the file `example-app-template.yaml` to a new file named `example-app.yaml`

**Edit** `example-app.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

### 6. Create App Deployment with OPA sidecar

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

### 7. Create and Exercise the OPA policy

#### Create the Ingress Policy

Within your Styra DAS Istio system replace the contents of the `policy/ingress/rules.rego` file with the following:
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

OPA will evaluate each authorization query invoked by the Istio proxy, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.

