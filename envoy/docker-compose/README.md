# Envoy with OPA on Docker Compose

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/) tutorial using Docker Compose.

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

## 1. Run the App with OPA and Envoy sidecars

Run OPA, Envoy and the demo web app using the `docker-compose.yaml` file provided in this directory.

```sh
docker-compose up
```

## 2. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```sh
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/#7-exercise-the-opa-policy.
