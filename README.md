# Java Guestbook Demo

The Guestbook sample demonstrates how to deploy a 2 service application with a frontend service 
and a backend service to GKE and Cloud Run using Cloud Deploy.  

## Directory contents
- `cloudbuild.yaml` - build images and deploy with Cloud Deploy
- `clouddeploy.yaml` - Cloud Deploy pipeline definition
  - Cloud Deploy pipeline has targets (clusters)
  - A Cloud Deploy release has a Skaffold file which points to both backend and frontend Skaffold files
- `skaffold.yaml` - A schema file that defines skaffold configurations ([skaffold.yaml reference](https://skaffold.dev/docs/references/yaml/))
  - Found in both parent and subdirectories
  - Skaffold defines how to build and points to k8s manifests. This demo manifests and Skaffold have our image placeholder value.
- `policy.yaml` - binary authorization policy

- `kubernetes-manifests/` - Contains Kubernetes YAML files for the Guestbook services and deployments, including:
  - `guestbook-frontend.deployment.yaml` - deploys a pod with the frontend container image
  - `guestbook-frontend.service.yaml` - creates a load balancer and exposes the frontend service on an external IP in the cluster
  - `guestbook-backend.deployment.yaml` - deploys a pod with the backend container image
  - `guestbook-backend.service.yaml` - exposes the backend service on an internal IP in the cluster

- `cloudrun-manifests/` - Cloud Run Service YAML files
  - `guesbook-backend.dev.service.yaml`
  - `guesbook-backend.prod.service.yaml`
  - `guesbook-frontend.dev.service.yaml`
  - `guesbook-frontend.prod.service.yaml`
