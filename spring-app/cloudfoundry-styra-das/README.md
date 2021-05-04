# Spring Application with OPA on Cloud Foundry and Styra DAS


## Prerequisites

This tutorial requires [Cloud Foundry](https://www.cloudfoundry.org/).

## Steps

### 1. Create a Custom System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the "Custom" System type.

### 2. Download the Styra DAS configuration for OPA

Upon system creation, you will be redirected to the `System` -> `Settings` -> `Install` page.

* **Copy** the provided curl command under the heading `# Download Styra configuration for OPA` and run the command from a terminal within the same directory as this README file.

The file `opa-conf.yaml` will be downloaded from Styra DAS to the current directory

### 3. Build the `spring-opa-app`
```
> cd ../spring-opa-app
> gradle build
> ls build/libs/
spring-opa-app-0.0.1.jar

> cd ../cloudfoundry-styra-das
```

### 4. Download the OPA binary for Linux
```
curl -L -o opa https://openpolicyagent.org/downloads/latest/opa_linux_amd64
```

### 5. Add the OPA binary and DAS configuration to the app jar
```
cp ../spring-opa-app/build/libs/spring-opa-app-0.0.1.jar .
zip spring-opa-app-0.0.1.jar -u opa
zip spring-opa-app-0.0.1.jar -u opa-conf.yaml
```

### 6. Run the App with OPA sidecar
```
cf create-app spring-opa-app
cf apply-manifest -f manifest.yml
cf push spring-opa-app
```

### 7. Load the Policy and Data into Styra DAS

* **Copy** the contents of the `dataset.json` file in this directory, and paste into the `dataset` for the system in the Styra DAS UI. (Replace all existing content in the `dataset`) Click the **Publish** icon in the UI to save the change to the dataset.

* **Copy** the contents of the `rules.rego` file in this directory, and paste into the `rules/rules.rego` for the system in the Styra DAS UI.  (Replace all existing content currently in `rules.rego`) Click the **Publish** icon in the UI to save the change to the rules.

Upon publishing the dataset and the rules, Styra DAS will construct a new policy [Bundle](https://www.openpolicyagent.org/docs/latest/management/#bundles) that will be distributed to the running OPA.

### 6. Configure the Styra DAS Decision Mapping

Under the `System` -> `Settings` -> `Decision Mappings`, select the `Default` mapping.

In the "Path to decision" field, update the value to `result.allowed`.

In the Columns section add the following:
| Search key | Path to value |
| ---------- | ------------- |
| user       | input.user |
| method     | input.method |

Save the change by clicking the `Update Mapping` button.

Refer to the Styra DAS documentation for complete details on Decision Mappings configuration.

### 8. Exercise the OPA policy
```
export SERVICE_URL=spring-opa-app.<YOUR_CF_DOMAIN>
```

#### Check that `alice` can see her own salary.

```
curl --user alice:password $SERVICE_URL/finance/salary/alice
```

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl --user bob:password $SERVICE_URL/finance/salary/alice
```

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl --user bob:password $SERVICE_URL/finance/salary/charlie
```

### 9. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
