# Software Delivery Shield Java Demo

This sample demonstrates security features for an end to end Java workflow. 
This samples deploys a multi-tiered application with a frontend service and 
a backend service to Google Kubernetes Engine and Cloud Run using Cloud Deploy.

## Directory contents
- `cloudbuild.yaml` - Cloud Build configuration file to build images and deploy with Cloud Deploy
- `clouddeploy.yaml` - Cloud Deploy pipeline definition
- `skaffold.yaml` - A schema file that defines Skaffold configurations ([skaffold.yaml reference](https://skaffold.dev/docs/references/yaml/)). The Skaffold files are used by Cloud Deploy releases.
- `policy.yaml` - Binary Authorization policy

- `kubernetes-manifests/` - Contains Kubernetes YAML files for the Guestbook services and deployments, including:
  - `guestbook-frontend.deployment.yaml` - deploys a pod with the frontend container image
  - `guestbook-frontend.service.yaml` - creates a load balancer and exposes the frontend service on an external IP in the cluster
  - `guestbook-backend.deployment.yaml` - deploys a pod with the backend container image
  - `guestbook-backend.service.yaml` - exposes the backend service on an internal IP in the cluster

- `cloudrun-manifests/` - Cloud Run Service YAML files
  - `guestbook-backend.dev.service.yaml`
  - `guestbook-backend.prod.service.yaml`
  - `guestbook-frontend.dev.service.yaml`
  - `guestbook-frontend.prod.service.yaml`

## Demo Instructions

* [Deploy to GKE](./instructions_gke.md)
* [Deploy to Cloud Run](./instructions_cloudrun.md)
