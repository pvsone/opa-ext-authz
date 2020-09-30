# Envoy

Run the [OPA Envoy Authorization](https://www.openpolicyagent.org/docs/latest/envoy-authorization/) tutorial using Docker Compose

## Run the App with OPA and Envoy sidecars

```
docker-compose up
```

The default `docker-compose.yaml` file uses `envoy.v3.yaml` for the Envoy configuration.  An example using the v2 APIs is also provided in `envoy.v2.yaml`.


## Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-authorization/#6-exercise-the-opa-policy
