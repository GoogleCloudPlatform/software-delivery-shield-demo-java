# Software Delivery Shield Demo for Java

This sample demonstrates security features for an end to end Java workflow. 
This samples deploys a multi-tiered application with a frontend service and 
a backend service to Google Kubernetes Engine or Cloud Run using Cloud Deploy.

> **🧪 Preview:** This product or feature is covered by the
[Pre-GA Offerings Terms](https://cloud.devsite.corp.google.com/terms/service-terms#1) of the Google Cloud
Terms of Service. Pre-GA products and features might have limited support, and changes to
pre-GA products and features might not be compatible with other pre-GA versions.
For more information, see the [launch stage descriptions](https://cloud.devsite.corp.google.com/products#product-launch-stages).

Some features are not available for public access. To get access to these features, 
see the [access request page](https://docs.google.com/forms/d/e/1FAIpQLSeBUSpLmXsvGhnKfYx7g-Cmd-oth9yXbUTZNFIL87cdGIu2RQ/viewform?resourcekey=0-tR1FN8wQtdR43sJixQL3jw).

## Demo Instructions

[Deploy to GKE](./instructions_gke.md)  
[Deploy to Cloud Run](./instructions_cloudrun.md)

### Pre-Reqs
The permissions needed for these tutorials can be fulfilled by the [Owner or Editor roles](https://cloud.google.com/iam/docs/understanding-roles#basic-definitions).

> **Note:** [Organization](https://cloud.google.com/resource-manager/docs/organization-policy/overview) or [Binary Authorization](https://cloud.google.com/binary-authorization/docs/key-concepts#policies) policies may already be set for your organization and may cause deployment errors.

You will need:
* To create or select a [Google Cloud project](https://cloud.google.com/resource-manager/docs/creating-managing-projects). 

  > **Note:** If you don't plan to keep the resources that you create in this procedure, create a project instead of selecting an existing project. After you finish these steps, you can delete the project, removing all resources associated with the project.

* To make sure that billing is enabled for your Cloud project. Learn how to [check if billing is enabled on a project](https://cloud.google.com/billing/docs/how-to/verify-billing-enabled).

* To [Install and initialize the gcloud CLI](https://cloud.google.com/sdk/docs/install) and authenticate via: `gcloud auth login`.

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