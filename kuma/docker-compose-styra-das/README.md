# Kuma with OPA on Docker Compose and Styra DAS

Run an OPA demo application with [Kuma](https://kuma.io/docs/1.1.1/overview/what-is-kuma/) and the [OPA Envoy Plugin](https://github.com/open-policy-agent/opa-envoy-plugin) on Docker Compose, and using Styra DAS as the OPA management control plane.

## Prerequisites

This tutorial requires [Kuma](https://kuma.io/install/1.1.1/) and [Docker Compose](https://docs.docker.com/compose/install/).

_This tutorial has been tested with Kuma 1.1.1_

## Overview

In this example, Kuma will be run in [Universal mode](https://kuma.io/docs/1.1.1/documentation/overview/#universal-mode). The Kuma Control Plane will be run directly on the user workstation host (Mac or Linux), while the Kuma Data Plane, OPA, and Application will be run as containers via Docker Compose.

## Steps

## 1. Run the Kuma Control Plane

In order for both the user host (where the control plane will run) and the Docker container (where the data plane will run) to resolve the same hostname for the control plane URL, do the following:

A. Update `/etc/hosts` on the host machine to resolve the `host.docker.internal` hostname.  (The Docker containers will automatically resolve this within the container environment)
```
127.0.0.1 host.docker.internal
```

B. Generate a self-signed cert for the control plane using the `host.docker.internal` hostname
```
kumactl generate tls-certificate --type=server --cp-hostname=host.docker.internal
```

C. Set the Environment vars for the control plane
```
export KUMA_GENERAL_ADVERTISED_HOSTNAME=host.docker.internal 
export KUMA_GENERAL_TLS_CERT_FILE=./cert.pem
export KUMA_GENERAL_TLS_KEY_FILE=./key.pem
```

D. Run `kuma-cp`
```
kuma-cp run
```

## 2. Configure Kuma

A. Configure `kumactl` for the control plane
```
kumactl config control-planes add --name universal --address http://host.docker.internal:5681 --overwrite
```

B. Generate a token for the example app data plane (in this directory)
```
kumactl generate dataplane-token --name=example-app > token-example-app
```

C. Configure Envoy to use OPA for external authz via the Kuma `ProxyTemplate`
```
kumactl apply -f proxy-template.yaml
```

### 3. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `opa-conf-template.yaml` to a new file named `opa-conf.yaml`

**Edit** `opa-conf.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS documentation for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are two locations in the file where `<styra-system-id>` needs to be replaced

## 5. Run the App with OPA and Kuma-Dataplane sidecars

Run OPA, Kuma-Dataplane and the demo web app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

The `app` in the `docker-compose.yaml` file is based on the [`open-policy-agent/contrib/api-authz`](https://github.com/open-policy-agent/contrib/tree/master/api_authz) example.

The `kuma-dp` instance is started with the `dataplane.yaml` configuration file.

The `opa` instance is started with the `opa-conf.yaml` configuration file. It will use this configuration to communicate with Styra DAS to pull configuration and bundles, and to push decision logs.

## 6. Exercise the OPA policy

The `Ingress`, `Egress` and `Application` policies are pre-created in the Styra DAS Envoy system for the example application.

You can review the policies within the respective policy modules in the DAS UI. No modifications to the policies are necessary for this tutorial, although this tutorial will not exercise the `Egress` policy.  The tests below will demonstrate the `Ingress` and `Application` policies only.

#### Check that `alice` can see her own salary.

```
curl -i --user alice:password localhost:10080/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` can see `alice`’s salary
`bob` is `alice`’s manager, so access is allowed

```
curl -i --user bob:password localhost:10080/finance/salary/alice
```

This is **allowed** by both the `Ingress` policy and the `Application` policy.

#### Check that `bob` CANNOT see `charlie`’s salary.
`bob` is not `charlie`’s manager, so access is denied

```
curl -i --user bob:password localhost:10080/finance/salary/charlie
```

This is **allowed** by the `Ingress` policy, but **denied** by the `Application` policy.

#### Check that `bob` CANNOT view the HR dashboard.
The HR Dashboard is not allowed for any user per the default `Ingress` policy.

```
curl -i --user bob:password localhost:10080/hr/dashboard
```

This is **denied** by the `Ingress` policy.  The request is never handled by the application as Envoy rejects the request with a `403 Forbidden` response.

### 7. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
