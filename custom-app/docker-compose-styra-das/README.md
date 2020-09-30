# Custom Application with Docker Compose and Styra DAS

A variant of the [OPA HTTP API Authorization](https://www.openpolicyagent.org/docs/latest/http-api-authorization/) tutorial using Styra DAS for OPA configuration, policy distribution and decision logging.

## Goals

The core goals are the same as those detailed in the official tutorial [Goals](https://www.openpolicyagent.org/docs/latest/http-api-authorization/#goals)

Additionally, we will use Styra DAS to configure the OPA instance, publish and distribute policies to the OPA, and receive and display the OPA decision logs.

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

### 1. Create a Custom System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the "Custom" System type.

### 2. Download the Styra DAS configuration for OPA

Upon system creation, you will be redirected to the `System` -> `Settings` -> `Install` page.

* **Copy** the provided curl command under the heading `# Download Styra configuration for OPA` and run the command from a terminal within the same directory as this README file.

### 3. Bootstrap the tutorial environment using Docker Compose.

Run OPA and the demo web app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

The following modifications were made to the `docker-compose.yaml` file as compared to the version in original tutorial:
1. A `volume` has been configured to mount the `opa-conf.yaml` file in the `opa` container
2. The `command` for the `opa` container now uses the `--config-file` option to run OPA with the `opa-conf.yaml`
3. The `POLICY_PATH` `environment` variable has been updated to a new path (which will be fully explained in the following sections) 

### 4. Load the Policy and Data into Styra DAS

* **Copy** the contents of the `dataset.json` file in this directory, and paste into the `dataset` for the system in the Styra DAS UI. Click the **Publish** icon in the UI to save the change to the dataset.

* **Copy** the contents of the `rules.rego` file in this directory, and paste into the `rules/rules.rego` for the system in the Styra DAS UI.  Click the **Publish** icon in the UI to save the change to the rules.

The following modifications were made to the `rules.rego` policy file as compared to the `example.rego` file in original tutorial:
1. The package was renamed from `httpapi.authz` to `rules`.  While Styra DAS supports any user-defined package names and policy file structure, the default package for a custom system is `rules`, so for simplicity we have renamed the package to fit the default. The package name change is the reason we modified the `POLICY_PATH` variable above to the new path `/v1/data/rules`
2. The `subordinates` data was moved into the file `dataset.json` to separate the external data from the policy.  An statement was added to `import data.dataset` into the rules, and the reference within the file to the `subordinates` data is now qualified under `dataset` namespace, e.g. `dataset.subordinates[...` 

Upon publishing the dataset and the rules, Styra DAS will construct a new policy [Bundle](https://www.openpolicyagent.org/docs/latest/management/#bundles) that will be distributed to the running OPA.

### 5. Configure the Styra DAS Decision Mapping

Under the `System` -> `Settings` -> `Decision Mappings`, select the `Default` mapping.

In the "Path to decision" field, update the value to `result.allow`.

Save the change by clicking the `Update Mapping` button.

### 6. Exercise the OPA policy

#### Check that `alice` can see her own salary.

```
curl --user alice:password localhost:5000/finance/salary/alice
```

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl --user bob:password localhost:5000/finance/salary/alice
```

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl --user bob:password localhost:5000/finance/salary/charlie
```

### 7. Review the Decisions in Styra DAS

After OPA evaluates the query made via the demo web app, the OPA will send a log of the decision to Styra DAS.  You can view them under the `System` -> `Decisions` tab.
