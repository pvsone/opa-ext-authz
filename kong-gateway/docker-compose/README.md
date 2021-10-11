# Kong Gateway (OSS) with OPA on Docker Compose

Run an OPA demo application with [Kong Gateway (OSS)](https://docs.konghq.com/gateway-oss/) and the [OPA Kong Plugin](https://github.com/open-policy-agent/contrib/tree/master/kong_api_authz) on Docker Compose.

## Steps 

### 1. Build a Docker image with Kong and the OPA Kong Plugin

Download or clone the [open-policy-agent/contrib](https://github.com/open-policy-agent/contrib) repo.

Build the Docker image
```sh
cd contrib/kong_api_authz

# Update the `Dockerfile` with your preferred `kong` image version
# e.g. `FROM kong:2.6.0`
sed -i '' -e 's/kong:2.0/kong:2.6.0/' Dockerfile

docker build . -t kong-opa:2.6.0
```

### 2. Run the App with Kong and OPA
```sh
docker-compose up
```

The `app` in the `docker-compose.yaml` file uses the same image (`openpolicyagent/demo-test-server:v1`) used in the [OPA Envoy](https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/) tutorial.

The `opa` instance is started with the `policy.rego` file. This file has been modified slightly from the version in the tutorial, specifically:
* The `token` value is used as is, since the OPA Kong plugin decodes the token and provides the decoded payload as part of the `input` to the OPA authorization query.
* In the `action_allowed` rule for the `POST` method, the expression which checked the `input.parsed_body` was removed.  The OPA Kong plugin does not pass the request body from Kong to OPA. The `input` fields that are currently provided are `token`, `method`, and `path`.
* The package was renamed to `kong.authz`.

Open a second terminal and verify Kong is running
```sh
curl -i http://localhost:8001
```

### 3. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```sh
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/#7-exercise-the-opa-policy

_**Note**_: The check "_that Bob cannot create an employee with the same firstname as himself_", will **not** result in a '403 Forbidden' as in the original tutorial. This is due to the policy change described above - where the expression that relied on `input.parsed_body` was removed.
