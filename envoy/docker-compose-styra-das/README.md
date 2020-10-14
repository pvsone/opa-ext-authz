# Envoy with OPA on Docker Compose and Styra DAS

Run an OPA demo application with [Envoy](https://www.envoyproxy.io/docs/envoy/v1.15.0/intro/what_is_envoy) and the [OPA Envoy Plugin](https://github.com/open-policy-agent/opa-envoy-plugin) on Docker Compose, and using Styra DAS as the OPA management control plane.

## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

### 1. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the "Envoy" System type.

### 2. Create the Styra DAS configuration for OPA

**Copy** the file `opa-conf-template.yaml` to a new file named `opa-conf.yaml`

**Edit** `opa-conf.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

## 3. Run the App with OPA and Envoy sidecars

Run OPA, Envoy and the demo web app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

The `app` in the `docker-compose.yaml` file is based on the [`open-policy-agent/contrib/api-authz`](https://github.com/open-policy-agent/contrib/tree/master/api_authz) example.

The `envoy` instances is started with the `envoy.v3.yaml` configuration file.  An example using the v2 APIs is also provided in `envoy.v2.yaml`.

The `opa` instance is started with the `opa-conf.yaml` configuration file. It will use this configuration to communicate with Styra DAS to pull configuration and bundles, and to push decision logs.

## 4. Exercise the OPA policy

The `Ingress`, `Egress` and `Application` policies are pre-created in the Styra DAS Envoy system for the example application.

You can review the policies within the respective policy modules in the DAS UI. No modifications to the policies are necessary for this tutorial, although this tutorial will not exercise the `Egress` policy.  The tests below will demonstrate the `Ingress` and `Application` policies only.

#### Check that `alice` can see her own salary.

```
curl -i --user alice:password localhost:8000/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl -i --user bob:password localhost:8000/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl -i --user bob:password localhost:8000/finance/salary/charlie
```

This is **allowed** by the `Ingress` policy, but **denied** by the `Application` policy.

#### Check that `bob` CANNOT view the HR dashboard.
The HR Dashboard is not allowed for any user per the default `Ingress` policy.

```
curl -i --user bob:password localhost:8000/hr/dashboard
```

This is **denied** by the `Ingress` policy.  The request is never handled by the application as Envoy rejects the request with a `403 Forbidden` response.

### 5. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
