# Contour with OPA on Kubernetes and Styra DAS

Run a demo application with [Contour](https://projectcontour.io) and the [OPA Envoy Plugin](https://github.com/open-policy-agent/opa-envoy-plugin) on Kubernetes, using Styra DAS as the OPA management control plane.

## Prerequisites

A Kubernetes cluster with Contour [installed](https://projectcontour.io/getting-started/)

_This tutorial has been tested with Contour 1.18.0_

## Steps

### 1. Start Minikube

[Install](https://projectcontour.io/getting-started/) Contour (if not already installed)

### 2. Install cert-manager

Install [cert-manager](https://cert-manager.io/docs/installation/) to allow for self-signed TLS certificate provisioning.

Contour requires TLS/HTTPS on virtual hosts when configuring [External Authorization](https://projectcontour.io/guides/external-authorization/)

Create a `selfSigned` `ClusterIssuer`
```
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: selfsigned
spec:
  selfSigned: {}
EOF
```

### 3. Create an Envoy System in Styra DAS

Refer to the Styra DAS documentation for detailed instructions on how to create a new System within your DAS environment, using the `Envoy` System type.

### 4. Create the Styra DAS configuration for OPA

**Copy** the file `opa-template.yaml` to a new file named `opa.yaml`

**Edit** `opa.yaml` to replace the following placeholder values:
* `<styra-organization-id>` with your tenant value, e.g. `myorg.styra.com`
* `<styra-token>` with a valid Styra DAS API token. Refer to the Styra DAS [documentation](https://docs.styra.com/v1/docs/operations/create-api-token/) for detailed instructions on how to create a new API token.
* `<styra-system-id>` with the `System ID` value from the `System` -> `Settings` -> `General` page.
    * Note: there are three locations in the file where `<styra-system-id>` needs to be replaced

### 5. Deploy OPA

The `opa.yaml` includes the OPA configuration (as a `Secret`), a `Deployment`, `Service`, and the `projectcontour.io/v1alpha1.`[ExtensionService](https://projectcontour.io/docs/v1.18.0/config/api/#projectcontour.io/v1alpha1.ExtensionService) needed for Contour External Authorization.

All resources will be created in the `contour-opa-demo` namespace.

```
kubectl apply -f opa.yaml
```

### 5. Create App Deployment with Contour HTTPProxy and OPA AuthZ

The `example-app.yaml` includes a `Deployment`, `Service`, `Certificate` (for TLS), and the `projectcontour.io/v1.`[HTTPProxy](https://projectcontour.io/docs/v1.18.0/config/api/#projectcontour.io/v1.HTTPProxy) needed to configure the virtual host and route for the `example-app`, integrated with `opa` for authorization.

In order to support the `fqdn` for the `HTTPProxy` and `dnsNames` for the `Certificate` [nip.io](http://nip.io) is used for DNS.  You can substitute with another DNS implementation if desired.

```
export CONTOUR_IP=$(minikube ip)

envsubst < example-app.yaml | kubectl apply -f -
```

If you do not have the `envsubst` command available, you can manually replace the `${CONTOUR_IP}` values in the `example-app.yaml` file with the IP address returned by the `minikube ip` command.

### 6. Update the OPA Policy

Copy the contents of `ingress-rules.rego` into the `Ingress` > `Rules` module in Styra DAS.  Click Publish to save the policy and distribute to the deployed OPA.

Only the `Ingress` policy is utilized by Contour, you can ignore the `Egress` and `Application` policy modules.

### 7. Exercise the OPA policy

The ingress rules validate and decode JWTs as part of the policy implementation. For convenience in testing, store the following test tokens in environment variables.

```
export ALICE_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiZ3Vlc3QiLCJzdWIiOiJZV3hwWTJVPSIsIm5iZiI6MTUxNDg1MTEzOSwiZXhwIjoxNjQxMDgxNTM5fQ.K5DnnbbIOspRbpCr2IKXE9cPVatGOCBrBQobQmBmaeU"
export BOB_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYWRtaW4iLCJzdWIiOiJZbTlpIiwibmJmIjoxNTE0ODUxMTM5LCJleHAiOjE2NDEwODE1Mzl9.WCxNAveAVAdRCmkpIObOTaSd0AJRECY2Ch2Qdic3kU8"
```

#### Check that `alice` can execute a `GET` request.

Alice's token contains the `guest` role claim.
```
curl -k -i "https://${CONTOUR_IP}.nip.io/get" -H "Authorization: Bearer ${ALICE_TOKEN}"
```

This is **allowed** by the `Ingress` policy and returns HTTP status `200`

#### Check that `alice` can not execute a `POST` request.

Alice's token does not contain the `admin` role claim.
```
curl -k -i "https://${CONTOUR_IP}.nip.io/post" -X POST -H "Authorization: Bearer ${ALICE_TOKEN}"
```

This is **denied** by the `Ingress` policy and returns HTTP status `403`

#### Check that `bob` can execute a `POST` request.

Bob's token contains the `admin` role claim.

```
curl -k -i "https://${CONTOUR_IP}.nip.io/post" -X POST -H "Authorization: Bearer ${BOB_TOKEN}"
```

This is **allowed** by the `Ingress` policy and returns HTTP status `200`

### 8. Review the Decisions in Styra DAS

OPA will evaluate each authorization query from the demo web app, and return to it the result. Based on the Styra DAS configuration, the OPA will also send a log of the decision to Styra DAS. You can view each log entry under the `System` -> `Decisions` tab.
