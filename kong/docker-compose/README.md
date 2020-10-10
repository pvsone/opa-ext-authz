# Kong

Run an OPA demo application with [Kong Gateway](https://konghq.com/kong/) and the [OPA Kong Plugin](https://github.com/open-policy-agent/contrib/tree/master/kong_api_authz) on Docker Compose.

## Build a Docker image with Kong and the OPA Kong Plugin

Download or clone the [open-policy-agent/contrib](https://github.com/open-policy-agent/contrib) repo.

Build the Docker image
```
# within the contrib directory
cd kong_api_authz
docker build . -t kong-opa:2.0
```

## Run the App with Kong and OPA

```
# within this repo's directory
docker-compose up
```

The `app` in the `docker-compose.yaml` file uses the same image (`openpolicyagent/demo-test-server:v1`) used in the [Envoy Authorization with OPA](https://www.openpolicyagent.org/docs/latest/envoy-authorization/) tutorial.

The `opa` instance is started with the `policy.rego` file. This file has been modified slightly from the version in the [Envoy Authorization with OPA](https://www.openpolicyagent.org/docs/latest/envoy-authorization/#3-define-a-opa-policy) tutorial.
* The `token` value is used as is, since the OPA Kong plugin decodes the token and provides the decoded payload as part of the `input` to the OPA authorization query.
* In the `action_allowed` rule for the `POST` method, the expression which checked the `input.parsed_body` was removed.  The OPA Kong plugin does not pass the request body from Kong to OPA. The `input` fields that are currently provided are `token`, `method`, and `path`.
* The package was renamed to `kong.authz`.

Verify Kong is running
```
curl -i http://localhost:8001/
```

## Configure the Kong Service, Route and Plugin
```
# Configure the Service
curl -i -X POST \
  http://localhost:8001/services \
  --data name=demo-app \
  --data url='http://app:8080'

# Configure the Route
curl -i -X POST \
  http://localhost:8001/services/demo-app/routes \
  --data 'paths[]=/'

# Configure the Plugin
curl -i -X POST \
  http://localhost:8001/plugins \
  --data name=opa \
  --data config.server.host=opa \
  --data config.policy.decision=kong/authz/allow
```

## Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-authorization/#6-exercise-the-opa-policy

*Note*: The check "that Bob cannot create an employee with the same firstname as himself", will not result in a 403 forbidden as in the original tutorial. This is due to the policy change described above - where the expression that relied on `input.parse_body` was removed.
