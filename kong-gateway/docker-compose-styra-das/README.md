# Kong Gateway (OSS) with OPA on Docker Compose and Styra DAS

Run an OPA demo application with [Kong Gateway (OSS)](https://docs.konghq.com/gateway-oss/) and the [OPA Kong Plugin](https://github.com/open-policy-agent/contrib/tree/master/kong_api_authz) on Docker Compose, and using Styra DAS as the OPA management control plane.

## Steps 

### 1. Create a Custom System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the "Custom" System type.

### 2. Download the Styra DAS configuration for OPA

Upon system creation, you will be redirected to the `System` -> `Settings` -> `Install` page.

* **Copy** the provided curl command under the heading `# Download Styra configuration for OPA` and run the command from a terminal within the same directory as this README file.

The file `opa-conf.yaml` will be downloaded from Styra DAS to the current directory

### 3. Build a Docker image with Kong and the OPA Kong Plugin

Download or clone the [open-policy-agent/contrib](https://github.com/open-policy-agent/contrib) repo.

Build the Docker image
```sh
cd contrib/kong_api_authz

# Update the `Dockerfile` with your preferred `kong` image version
# e.g. `FROM kong:2.6.0`
sed -i '' -e 's/kong:2.0/kong:2.6.0/' Dockerfile

docker build . -t kong-opa:2.6.0
```

### 4. Run the App with Kong and OPA
```sh
docker-compose up
```

The `app` in the `docker-compose.yaml` file uses the same image (`openpolicyagent/demo-test-server:v1`) used in the [OPA Envoy](https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/) tutorial.

The `opa` instance is started with the `opa-conf.yaml` configuration file. It will use this configuration to communicate with Styra DAS to pull configuration and bundles, and to push decision logs.

Open a second terminal and verify Kong is running
```sh
curl -i http://localhost:8001
```

### 5. Load the Policy and Data into Styra DAS

* **Copy** the contents of the `rules.rego` file in this directory, and paste into the `rules/rules.rego` for the system in the Styra DAS UI.  (Replace all existing content currently in `rules.rego`) Click the **Publish** icon in the UI to save the change to the rules.

The following modifications were made to the `rules.rego` policy file as compared to the `policy.rego` file in original tutorial:
1. The package was renamed from `envoy.authz` to `rules`.  While Styra DAS supports any user-defined package names and policy file structure, the default package for a custom system is `rules`, so for simplicity we have renamed the package to fit the default.
2. The `token` value is used as is, since the OPA Kong plugin decodes the token and provides the decoded payload as part of the `input` to the OPA authorization query.
3. In the `action_allowed` rule for the `POST` method, the expression which checked the `input.parsed_body` was removed.  The OPA Kong plugin does not pass the request body from Kong to OPA. The `input` fields that are currently provided are `token`, `method`, and `path`.

Upon publishing the dataset and the rules, Styra DAS will construct a new policy [Bundle](https://www.openpolicyagent.org/docs/latest/management/#bundles) that will be distributed to the running OPA.

### 6. Configure the Styra DAS Decision Mapping

Under the `System` -> `Settings` -> `Decision Mappings`, select the `Default` mapping.

In the "Path to decision" field, update the value to `result`.

In the Columns section add the following:
| Search key | Path to value |
| ---------- | ------------- |
| role       | input.token.payload.role |
| method     | input.method |
| path       | input.path |

Save the change by clicking the `Update Mapping` button.

Refer to the Styra DAS documentation for complete details on Decision Mappings configuration.

### 7. Exercise the OPA policy

Set the `SERVICE_URL` environment variable to the service’s IP/port.

```sh
export SERVICE_URL=localhost:8000
```

Follow the instructions provided at https://www.openpolicyagent.org/docs/latest/envoy-tutorial-standalone-envoy/#7-exercise-the-opa-policy

_**Note**_: The check "_that Bob cannot create an employee with the same firstname as himself_", will **not** result in a '403 Forbidden' as in the original tutorial. This is due to the policy change described above - where the expression that relied on `input.parsed_body` was removed.

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
