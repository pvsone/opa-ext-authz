# Envoy with OPA on Docker Compose

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-authorization/) tutorial using Docker Compose

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

## 1. Run the App with OPA and Envoy sidecars

Run OPA, Envoy and the demo web app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

The `envoy` instances is started with the `envoy.v3.yaml` configuration file.  An example using the v2 APIs is also provided in `envoy.v2.yaml`.


## 2. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-authorization/#6-exercise-the-opa-policy
