# Envoy with OPA on Docker Compose and Styra DAS

Run an OPA demo application with [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy)
and the [OPA Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) 
on Docker Compose, and using Styra DAS as the OPA management control plane.

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

### 1. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 2. Create the Styra DAS configuration for OPA

**Copy** the file `opa-conf-template.yaml` to a new file named `opa-conf.yaml`

**Edit** `opa-conf.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are three locations in the file where `<styra-system-id>` needs to be replaced

## 3. Run the App with OPA and Envoy sidecars

Run OPA, Envoy and the demo web app using the `docker-compose.yaml` file provided in this directory.

```sh
docker-compose up
```

## 4. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```sh
export SERVICE_URL=localhost:8000
```

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

### 5. Review the Decisions in Styra DAS

OPA will evaluate each authorization query invoked by the Envoy proxy, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
