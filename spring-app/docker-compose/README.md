# Spring Application with OPA on Docker Compose

A variant of the [OPA HTTP API Authorization](https://www.openpolicyagent.org/docs/latest/http-api-authorization/) tutorial using a Spring application.

## Goals

The core goals are the same as those detailed in the official tutorial [Goals](https://www.openpolicyagent.org/docs/latest/http-api-authorization/#goals)

Rather than the Python application used in the original tutorial we will deploy a Spring application that utilizes the [Spring AccessDecisionVoter for OPA](https://github.com/open-policy-agent/contrib/blob/master/spring_authz/README.md) to integrate OPA for external authorization.


## Prerequisites

This tutorial requires [Docker Compose](https://docs.docker.com/compose/install/).

## Steps

### 1. Build the `spring-opa-app`
```
> cd ../spring-opa-app
> gradle jibDockerBuild
> docker images spring-opa-app
REPOSITORY       TAG       IMAGE ID       CREATED        SIZE
spring-opa-app   0.0.1     1fc17a004dce   51 years ago   217MB

> cd ../docker-compose
```

### 2. Run the App with OPA sidecar

Run OPA and the spring app using the `docker-compose.yaml` file provided in this directory.

```
docker-compose up
```

### 3. Exercise the OPA policy

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
