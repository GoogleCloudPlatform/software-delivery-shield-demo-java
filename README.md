# Java Guestbook

The Guestbook sample demonstrates how to deploy a Kubernetes application with a front end service and a back end service using the Cloud Code IDE extension.  



### Kubernetes architecture
![Kubernetes Architecture Diagram](./img/diagram.png)

### Directory contents

- `skaffold.yaml` - A schema file that defines skaffold configurations ([skaffold.yaml reference](https://skaffold.dev/docs/references/yaml/))
- `kubernetes-manifests/` - Contains Kubernetes YAML files for the Guestbook services and deployments, including:

  - `guestbook-frontend.deployment.yaml` - deploys a pod with the frontend container image
  - `guestbook-frontend.service.yaml` - creates a load balancer and exposes the frontend service on an external IP in the cluster
  - `guestbook-backend.deployment.yaml` - deploys a pod with the backend container image
  - `guestbook-backend.service.yaml` - exposes the backend service on an internal IP in the cluster

---