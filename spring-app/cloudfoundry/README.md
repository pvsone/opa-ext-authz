# Spring Application with Cloud Foundry


## Prerequisites

This tutorial requires [Cloud Foundry](https://www.cloudfoundry.org/).

## Steps

### 1. Build the `spring-opa-app`
```
> cd ../spring-opa-app
> gradle build
> ls build/libs/
spring-opa-app-0.0.1.jar

> cd ../cloudfoundry
```

### 2. Download the OPA binary for Linux
```
curl -L -o opa https://openpolicyagent.org/downloads/latest/opa_linux_amd64
```

### 3. Add the OPA binary and policy to the app jar
```
cp ../spring-opa-app/build/libs/spring-opa-app-0.0.1.jar .
zip spring-opa-app-0.0.1.jar -u opa
zip spring-opa-app-0.0.1.jar -u policy.rego
```

### 4. Run the App with OPA sidecar
```
cf create-app spring-opa-app
cf apply-manifest -f manifest.yml
cf push spring-opa-app
```

### 5. Exercise the OPA policy
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
