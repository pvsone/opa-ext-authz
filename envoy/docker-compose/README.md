# Envoy with OPA on Docker Compose

Run an OPA demo application with [Envoy](https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy)
and the [OPA Envoy Plugin](https://www.openpolicyagent.org/docs/latest/envoy-introduction/) 
on Docker Compose.

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

#### Check that a `GET` request to the `/get` endpoint is **Allowed** (`200 OK`).

```sh
curl -X GET $SERVICE_URL/get -i
```

#### Check that a `POST` request to the `/post` endpoint is **Denied** (`403 Forbidden`).

```sh
curl -X POST $SERVICE_URL/post -i
```
