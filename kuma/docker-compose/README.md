# Kuma with OPA on Docker Compose

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-authorization/) tutorial using Docker Compose

## Prerequisites

This tutorial requires [Kuma](https://kuma.io/install/1.0.3/) and [Docker Compose](https://docs.docker.com/compose/install/).

_This tutorial has been tested with Kuma 1.0.3, Docker 20.10.0, Docker Compose 1,27.4 on a Mac_

## Overview

In this example, Kuma will be run in [Universal mode](https://kuma.io/docs/1.0.3/documentation/overview/#universal-mode). The Kuma Control Plane will be run directly on the user workstation host (Mac or Linux), while the Kuma Data Plane, OPA, and Application will be run as containers via Docker Compose.

## Steps

## 1. Run the Kuma Control Plane

In order for both the user host (where the control plane will run) and the Docker container (where the data plane will run) to resolve the same hostname for the control plane URL, do the following:

A. Update `/etc/hosts` on the host machine to resolve the `host.docker.internal` hostname.  (The Docker containers will automatically resolve this within the container environment)
```
127.0.0.1 host.docker.internal
```

B. Generate a self-signed cert for the control plane using the `host.docker.internal` hostname
```
kumactl generate tls-certificate --type=server --cp-hostname=host.docker.internal
```

C. Set the Environment vars for the control plane
```
export KUMA_GENERAL_ADVERTISED_HOSTNAME=host.docker.internal 
export KUMA_GENERAL_TLS_CERT_FILE=./cert.pem
export KUMA_GENERAL_TLS_KEY_FILE=./key.pem
```

D. Run `kuma-cp`
```
kuma-cp run
```

## 2. Configure Kuma

A. Configure `kumactl` for the control plane
```
kumactl config control-planes add --name universal --address http://host.docker.internal:5681 --overwrite
```

B. Generate a token for the example app data plane (in this directory)
```
kumactl generate dataplane-token --name=example-app > token-example-app
```

C. Configure Envoy to use OPA for external authz via the Kuma `ProxyTemplate`
```
kumactl apply -f proxytemplate.yaml
```

## 3. Run the App with OPA and Kuma-Dataplane sidecars

Run OPA, Kuma-Dataplane and the demo web app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

## 4. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```
export SERVICE_URL=localhost:10080
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-authorization/#6-exercise-the-opa-policy
