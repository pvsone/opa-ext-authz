# Spring Application with Docker Compose and Styra DAS

A variant of the [OPA HTTP API Authorization](https://www.openpolicyagent.org/docs/latest/http-api-authorization/) tutorial using a Spring application and Styra DAS as the OPA management control plane.

## Goals

The core goals are the same as those detailed in the official tutorial [Goals](https://www.openpolicyagent.org/docs/latest/http-api-authorization/#goals)

Rather than the Python application used in the original tutorial we will deploy a Spring application that utilizes the [Spring AccessDecisionVoter for OPA](https://github.com/open-policy-agent/contrib/blob/master/spring_authz/README.md) to integrate OPA for external authorization.

Additionally, we will use Styra DAS to configure the OPA instance, publish and distribute policies to the OPA, and receive and display the OPA decision logs.

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

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
> gradle jibDockerBuild
> docker images spring-opa-app
REPOSITORY       TAG       IMAGE ID       CREATED        SIZE
spring-opa-app   0.0.1     1fc17a004dce   51 years ago   217MB

> cd ../docker-compose
```

### 4. Run the App with OPA sidecar.

Run OPA and the spring app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

### 5. Load the Policy and Data into Styra DAS

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

### 7. Exercise the OPA policy

#### Check that `alice` can see her own salary.

```
curl --user alice:password localhost:8080/finance/salary/alice
```

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl --user bob:password localhost:8080/finance/salary/alice
```

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl --user bob:password localhost:8080/finance/salary/charlie
```

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
