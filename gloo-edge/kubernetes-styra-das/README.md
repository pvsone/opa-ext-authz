# Gloo Edge with OPA on Kubernetes and Styra DAS

An example application deployment using the [OPA-Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) with [Gloo Edge](https://docs.solo.io/gloo-edge) for external authorization in an Envoy-based gateway,
using Styra DAS as the OPA management control plane.

## Prerequisites

A Kubernetes cluster with Gloo Edge Gateway [installed](https://docs.solo.io/gloo-edge/latest/installation/gateway/kubernetes/)

_This tutorial has been tested with Gloo Edge 1.11.12_

## Steps

### 1. Start Minikube

[Install](https://docs.solo.io/gloo-edge/latest/installation/gateway/kubernetes/) the Gloo Edge Gateway (if not already installed)

### 2. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 3. Create the Styra DAS configuration for OPA

**Copy** the file `opa-template.yaml` to a new file named `opa.yaml`

**Edit** `opa.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS [documentation](https://docs.styra.com/v1/docs/operations/create-api-token/) for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are three locations in the file where `<styra-system-id>` needs to be replaced

### 4. Create OPA Deployment

The `opa.yaml` includes the OPA configuration (as a `Secret`), a `Deployment`, and `Service`.

All resources will be created in the `gloo-system` namespace.

```sh
kubectl apply -f opa.yaml
```

### 5. Create App Deployment with Gloo VirtualService

The `example-app.yaml` includes both a `Deployment` and `Service`, as well as the Gloo `VirtualService` which is configured to utilize external authorization.

```sh
kubectl apply -f example-app.yaml
```

### 6. Update Gloo Settings

Enable OPA as the external authorization server for Gloo by patching the Gloo settings
```sh
kubectl patch settings default --patch-file gloo-settings-patch.yaml --type merge -n gloo-system
```

### 7. Create and Exercise the OPA policy

#### Create the Ingress Policy

Within your Styra DAS system replace the contents of the `policy/ingress/rules.rego` file with the following:
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
curl -X GET $(glooctl proxy url --local-cluster)/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $(glooctl proxy url --local-cluster)/post -i
```

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query invoked by the Gloo Edge gateway, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.

