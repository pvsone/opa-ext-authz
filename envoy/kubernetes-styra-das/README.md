# Envoy with OPA on Kubernetes and Styra DAS

Run an OPA demo application with [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy)
and the [OPA Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) 
on Kubernetes, and using Styra DAS as the OPA management control plane.

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

### 3. Create a Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `example-app-template.yaml` to a new file named `example-app.yaml`

**Edit** `example-app.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

## 5. Run the App with OPA and Envoy sidecars

The `example-app.yaml` includes a `Deployment` with the `example-app`, `opa` and `envoy` sidecars, as well as the `example-app` `Service`

```sh
kubectl apply -f example-app.yaml
```

### 6. Create and Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the `example-app` service IP/port.

**minikube:**
```sh
export SERVICE_PORT=$(kubectl get service example-app -o jsonpath='{.spec.ports[?(@.port==8080)].nodePort}')
export SERVICE_HOST=$(minikube ip)
export SERVICE_URL=$SERVICE_HOST:$SERVICE_PORT
echo $SERVICE_URL
```

#### Create the Ingress Policy

Within your Styra DAS Envoy system replace the contents of the `policy/ingress/rules.rego` file with the following:
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

OPA will evaluate each authorization query invoked by the Envoy proxy, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
