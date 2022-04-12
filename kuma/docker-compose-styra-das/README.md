# Kuma with OPA on Docker Compose and Styra DAS

Run an OPA demo application with [Kuma](https://kuma.io/docs/1.6.x/introduction/what-is-kuma/) 
and the [OPA Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) 
on Docker Compose, and using Styra DAS as the OPA management control plane.

## Prerequisites

This tutorial requires [Kuma](https://kuma.io/install/1.6.x/) and [Docker Compose](https://docs.docker.com/compose/install/).

_This tutorial has been tested with Kuma 1.6.0_

## Overview

In this example, Kuma will be run in Universal mode. The Kuma Control Plane will be run directly on the user workstation host (Mac or Linux), while the Kuma Data Plane, OPA, and Application will be run as containers via Docker Compose.

## Steps

## 1. Run the Kuma Control Plane

In order for both the user host (where the control plane will run) and the Docker container (where the data plane will run) to resolve the same hostname for the control plane URL, do the following:

A. Update `/etc/hosts` on the host machine to resolve the `host.docker.internal` hostname.  (The Docker containers will automatically resolve this within the container environment)
```
127.0.0.1 host.docker.internal
```

B. Generate a self-signed cert for the control plane using the `host.docker.internal` hostname
```sh
kumactl generate tls-certificate --type=server --hostname=host.docker.internal
```

C. Set the Environment vars for the control plane
```sh
export KUMA_GENERAL_ADVERTISED_HOSTNAME=host.docker.internal 
export KUMA_GENERAL_TLS_CERT_FILE=./cert.pem
export KUMA_GENERAL_TLS_KEY_FILE=./key.pem
```

D. Run `kuma-cp`
```sh
kuma-cp run
```

## 2. Configure Kuma

A. Configure `kumactl` for the control plane
```sh
kumactl config control-planes add --name universal --address http://host.docker.internal:5681 --overwrite
```

B. Generate a token for the example app data plane (in this directory)
```sh
kumactl generate dataplane-token --name=example-app > token-example-app
```

C. Configure Envoy to use OPA for external authz via the Kuma `ProxyTemplate`
```sh
kumactl apply -f proxy-template.yaml
```

### 3. Create a Kuma System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Kuma` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `opa-conf-template.yaml` to a new file named `opa-conf.yaml`

**Edit** `opa-conf.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

## 5. Run the App with OPA and Kuma-Dataplane sidecars

Run OPA, Kuma-Dataplane and the demo web app using the `docker-compose.yaml` file provided in this directory.

```sh
docker-compose up
```

## 6. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```sh
export SERVICE_URL=localhost:10080
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

### 7. Review the Decisions in Styra DAS

OPA will evaluate each authorization query invoked by the Kuma proxy, and return to it the result. The OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
