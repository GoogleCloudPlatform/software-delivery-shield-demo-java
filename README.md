# Java Guestbook Demo

The Guestbook sample demonstrates how to deploy a Kubernetes application with a front end service and a back end service using Cloud Deploy.  


## Kubernetes architecture
![Kubernetes Architecture Diagram](./img/diagram.png)

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

- `cloudrun-manifests/`
  - `guesbook-backend.dev.service.yaml`
  - `guesbook-backend.prod.service.yaml`
  - `guesbook-frontend.dev.service.yaml`
  - `guesbook-frontend.prod.service.yaml`

- `backend/`
  - `cloudrun.clouddeploy.yaml`
  - `clouddeploy.yaml`

## GKE Instructions

### Before you begin
  1. Enable APIs  
      * Artifact Registry, Container Analysis, and Binary Authorization APIs.

  2. Download **Cloud Code Source Protect plugin**

  3. Set permissions
      * TBD

  4. Set **Binary Authorization** Policy (need to run a build first for Cloud Build attestor to be present)
      ```
      gcloud container binauthz policy import policy.yaml
      ```

  5. Set config for gcloud
      ```
      gcloud config set deploy/region us-central1
      gcloud config set project <PROJECT_ID>
      ``` 

### Setup

#### Artifact Registry repos
  1. Create an **Artifact Registry Docker Repository**
      ```
      gcloud artifacts repositories create containers \
          --repository-format=docker \
          --description="Docker repository"
      ```

  1. Create an **Artifact Registry Remote Repository** ([https://cloud.google.com/artifact-registry/docs/repositories/remote-repo](https://cloud.google.com/artifact-registry/docs/repositories/remote-repo) )
      ```
      gcloud artifacts repositories create guestbook-remote-repo \
          --repository-format=maven \
          --location=us-central1 \
          --description="My remote repo" \
          --mode=remote-repository \
          --remote-repo-config-desc="Maven Central" \
          --remote-mvn-repo=MAVEN-CENTRAL
      ```

  2. Download dependencies

      ```
      mvn compile
      ```

  3. View cached dependencies
      ```
      gcloud artifacts files list \
          --repository=guestbook-remote-repo \
          --location=us-central1
      ```

#### GKE Cluster

  4. Create clusters with **Binary Authorization** enabled
      ```
      gcloud container clusters create-auto dev-cluster --region=us-central1 --binauthz-evaluation-mode=PROJECT_SINGLETON_POLICY_ENFORCE
      && \
      gcloud container clusters create-auto prod-cluster --region=us-central1 --binauthz-evaluation-mode=PROJECT_SINGLETON_POLICY_ENFORCE
      ```

### Retrieve the code
  1. Clone repo
      ```
      git clone
      ```

  2. Change directory
      ```
      cd guestbook-demo
      ```
  
### **Cloud Code Source Protect Plugin** to view dependencies
  Instructions TBD

### Deploy
  1. Create your Cloud Deploy delivery pipeline and targets:
      * Edit clouddeploy.yaml with your PROJECT_ID
      * Register pipeline
        ```
        gcloud deploy apply --file clouddeploy.yaml
        ```

  2. Run Cloud Build to do the following:
      * Build and push containers to Artifact Registry with provenance
      * Builds and deploys Java artifacts in AR ([https://cloud.google.com/artifact-registry/docs/java/manage-packages](https://cloud.google.com/artifact-registry/docs/java/manage-packages) ) with provenance
      * Automatically signs with attestor “built-with-cloud-build”
      * Create a release via **Cloud Deploy**
        ```
        gcloud builds submit --config cloudbuild.yaml --substitution SHORT_SHA=1234
        ```
  3. Promote the release via the Cloud Deploy UI

### View Software Delivery Shield features
  1. Open **Cloud Build SDS panel** (Cloud Build UI > Build Artifacts > View Security Insights)
      * View SLSA level, provenance, and **Java packages/SBOM**
      * Click "Artifacts Scanned" to view vulnerabilities in Artifact Registry
      * Or Use gcloud (UI and gcloud seem to have different info!)
      * (Optional) Verify provenance: [https://cloud.google.com/build/docs/securing-builds/view-build-provenance](https://cloud.google.com/build/docs/securing-builds/view-build-provenance) 

  2. View **GKE Security Postures** UI

## Cloud Run Instructions

  ```
  gcloud deploy apply --file=cloudrun.clouddeploy.yaml

  gcloud builds submit --config cloudrun.cloudbuild.yaml
  ```