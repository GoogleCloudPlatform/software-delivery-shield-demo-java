## Deploy on Cloud Run

### Before you begin

1. Download **Cloud Code source protect (Preview)**

   [Cloud Code](https://cloud.google.com/code/docs) source protect is not available
   for public access. To get access to this feature, see the
   [access request page][access].

1. You have two options to get things ready:


    - [Use Terraform to get things setup automatically](#Use-Terraform-to-get-things-setup-automatically)

    __or__

    - [Setup the environment manually](#setup-environment-manually)

### Use Terraform to get things partially setup automatically

1. Setup account info

    -  __(If you don't have an existing project you want to deploy this demo into, skip to the next step)__ If you have an __existing__ project that you want to deploy this demo into, provide it in `deploy-for-cloud-run.tfvars` by un-commenting `existing_project_id`, and replace "sds-java-demo" with the project ID you would like to use.

    __or__

    -  If you want Terraform to __create__ a project for you within your organization's billing account, provide it in `deploy-for-cloud-run.tfvars` by un-commenting `google_billing_account`, and replace "00000-000000-0000" with the billing account number

1. _cd_ into the `tf` folder` and run:
    ```
    terraform plan -var-file="deploy-for-cloud-run.tfvars"
    ```

1. Terraform will then show you a listing of everything that will be deployed. If it all looks good, go ahead and run:
    ```
    terraform apply -var-file="deploy-for-cloud-run.tfvars"
    ```

1. Replace PROJECT_ID placeholder with your Project Id:
    * MacOS
        ```sh
        sed -i '.bak' "s/PROJECT_ID/$PROJECT_ID/g" **/*clouddeploy.yaml policy.yaml pom.xml
        ```
    * Linux
        ```sh
        sed -i "s/PROJECT_ID/$PROJECT_ID/g" **/*clouddeploy.yaml policy.yaml pom.xml
        ```

1. Deploy placeholder services:

    1. Deploy placeholder services for the private backend:

        ```sh
        cd backend
        gcloud run deploy guestbook-backend-dev \
            --source . \
            --no-allow-unauthenticated

        gcloud run deploy guestbook-backend-prod \
            --source . \
            --no-allow-unauthenticated
        cd ..
        ```

    1. Create a service account for the frontend service to invoke the private backend service:

        ```sh
        gcloud iam service-accounts create frontend-dev-identity
        gcloud iam service-accounts create frontend-prod-identity

        gcloud run services add-iam-policy-binding guestbook-backend-dev \
            --member serviceAccount:frontend-dev-identity@$PROJECT_ID.iam.gserviceaccount.com \
            --role roles/run.invoker

        gcloud run services add-iam-policy-binding guestbook-backend-prod \
            --member serviceAccount:frontend-prod-identity@$PROJECT_ID.iam.gserviceaccount.com \
            --role roles/run.invoker
        ```

    1. Deploy placeholder services for the public frontend:

        ```sh
        cd frontend
        gcloud run deploy guestbook-frontend-dev \
            --source . \
            --allow-unauthenticated \
            --service-account frontend-dev-identity@$PROJECT_ID.iam.gserviceaccount.com

        gcloud run deploy guestbook-frontend-prod \
            --source . \
            --allow-unauthenticated \
            --service-account frontend-prod-identity@$PROJECT_ID.iam.gserviceaccount.com
        cd ..
        ```

        **⚠️ Note:** If your organization doesn’t allow public services, the frontend services may error during deployment. Change flag `--allow-unauthenticated` to `--no-allow-unauthenticated`. However, this means your frontend service will need an authentication header to access.

1. Set a **[Binary Authorization](https://cloud.google.com/binary-authorization/docs/deploy-cloud-build)** policy:

    ```sh
    gcloud container binauthz policy import policy.yaml
    ```

    **⚠️ Note:** For your project to have the built by Cloud Build attestor, you need to run a build first (see above).

1. Enable **Binary Authorization** on the Cloud Run services:

    ```sh
    gcloud run services update guestbook-backend-dev --binary-authorization=default
    gcloud run services update guestbook-frontend-dev --binary-authorization=default
    gcloud run services update guestbook-backend-prod --binary-authorization=default
    gcloud run services update guestbook-frontend-prod --binary-authorization=default
    ```
1. Create an **Artifact Registry [remote repository](https://cloud.google.com/artifact-registry/docs/repositories/remote-repo) (Preview)**: 
    This feature is not available for public access. To get access to this feature, 
    see the [access request page][access].

    With access uncomment `<repositories>...</repositories>` in [`pom.xml`](./pom.xml).

    ```sh
    gcloud artifacts repositories create guestbook-remote-repo \
        --repository-format=maven \
        --location=us-central1 \
        --description="My remote repo" \
        --mode=remote-repository \
        --remote-repo-config-desc="Maven Central" \
        --remote-mvn-repo=MAVEN-CENTRAL
    ```

1. Create your **Cloud Deploy** delivery pipeline and targets:

    ```sh
    gcloud deploy apply --file ./frontend/cloudrun.clouddeploy.yaml
    gcloud deploy apply --file ./backend/cloudrun.clouddeploy.yaml
    ```
   **⚠️ Note:** Ensure clouddeploy.yaml has the correct values. “PROJECT_ID” should have been already replaced with your Project Id
   
1. You're ready for the [Demo](#demo)!

### Setup the environment manually

1. Set config for `gcloud`:
    ```sh
    export PROJECT_ID=<YOUR_PROJECT_ID>
    ```

    ```sh
    gcloud config set deploy/region us-central1
    gcloud config set artifacts/location us-central1
    gcloud config set project $PROJECT_ID
    ```

1. Enable APIs: 

    ```sh
    gcloud services enable \
        artifactregistry.googleapis.com \
        cloudbuild.googleapis.com \
        clouddeploy.googleapis.com \
        run.googleapis.com \
        binaryauthorization.googleapis.com \
        containeranalysis.googleapis.com \
        containerscanning.googleapis.com \
        containersecurity.googleapis.com
    ```

1. Make sure the default Compute Engine service account and Cloud Build service account have sufficient permissions.

    > **Note:** The service account might already have the necessary permissions. These steps are included for projects that disable automatic role grants for default service accounts.

    * Grant the Cloud Build service account privilege:
        * to invoke deployments with Google Cloud Deploy and to update the delivery pipeline and the target definitions
        * to invoke Google Cloud Deploy operations (act as a service account)
        * to deploy to Cloud Run

        ```sh
        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")@cloudbuild.gserviceaccount.com \
            --role="roles/clouddeploy.operator"

        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")@cloudbuild.gserviceaccount.com \
            --role="roles/iam.serviceAccountUser"

        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")@cloudbuild.gserviceaccount.com \
            --role="roles/run.admin"
        ```

    * Grant the Cloud Build and Google Cloud Deploy service account, default Compute Engine service account, privilege to deploy to Cloud Run:

        ```sh
        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")-compute@developer.gserviceaccount.com \
            --role="roles/run.developer"

        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")-compute@developer.gserviceaccount.com \
            --role="roles/clouddeploy.jobRunner"

        gcloud projects add-iam-policy-binding $PROJECT_ID \
            --member=serviceAccount:$(gcloud projects describe $PROJECT_ID \
            --format="value(projectNumber)")-compute@developer.gserviceaccount.com \
            --role="roles/iam.serviceAccountUser"
        ```

1. Replace PROJECT_ID placeholder with your Project Id
    * MacOS
        ```sh
        sed -i '.bak' "s/PROJECT_ID/$PROJECT_ID/g" **/*clouddeploy.yaml policy.yaml pom.xml
        ```
    * Linux
        ```sh
        sed -i "s/PROJECT_ID/$PROJECT_ID/g" **/*clouddeploy.yaml policy.yaml pom.xml
        ```

1. Deploy placeholder services:

    1. Deploy placeholder services for the private backend:

        ```sh
        cd backend
        gcloud run deploy guestbook-backend-dev \
            --source . \
            --no-allow-unauthenticated

        gcloud run deploy guestbook-backend-prod \
            --source . \
            --no-allow-unauthenticated
        cd ..
        ```

    1. Create a service account for the frontend service to invoke the private backend service:

        ```sh
        gcloud iam service-accounts create frontend-dev-identity
        gcloud iam service-accounts create frontend-prod-identity

        gcloud run services add-iam-policy-binding guestbook-backend-dev \
            --member serviceAccount:frontend-dev-identity@$PROJECT_ID.iam.gserviceaccount.com \
            --role roles/run.invoker

        gcloud run services add-iam-policy-binding guestbook-backend-prod \
            --member serviceAccount:frontend-prod-identity@$PROJECT_ID.iam.gserviceaccount.com \
            --role roles/run.invoker
        ```

    1. Deploy placeholder services for the public frontend:

        ```sh
        gcloud run deploy guestbook-frontend-dev \
            --image gcr.io/cloudrun/hello \
            --allow-unauthenticated \
            --service-account frontend-dev-identity@$PROJECT_ID.iam.gserviceaccount.com

        gcloud run deploy guestbook-frontend-prod \
            --image gcr.io/cloudrun/hello \
            --allow-unauthenticated \
            --service-account frontend-prod-identity@$PROJECT_ID.iam.gserviceaccount.com
        ```

        **⚠️ Note:** If your organization doesn’t allow public services, the frontend services may error during deployment. Change flag `--allow-unauthenticated` to `--no-allow-unauthenticated`. However, this means your frontend service will need an authentication header to access.


1. Set a **[Binary Authorization](https://cloud.google.com/binary-authorization/docs/deploy-cloud-build)** policy:

    ```sh
    gcloud container binauthz policy import policy.yaml
    ```

    **⚠️ Note:** For your project to have the built by Cloud Build attestor, you need to run a build first (see above).

1. Enable **Binary Authorization** on the Cloud Run services:

    ```sh
    gcloud run services update guestbook-backend-dev --binary-authorization=default
    gcloud run services update guestbook-frontend-dev --binary-authorization=default
    gcloud run services update guestbook-backend-prod --binary-authorization=default
    gcloud run services update guestbook-frontend-prod --binary-authorization=default
    ```

1. Create an **Artifact Registry [Docker repository](https://cloud.google.com/artifact-registry/docs/docker)**:

    ```sh
    gcloud artifacts repositories create containers \
        --repository-format=docker \
        --description="Docker repository"
    ```

1. Create an **Artifact Registry [Maven repository](https://cloud.google.com/artifact-registry/docs/java)**:

    ```sh
    gcloud artifacts repositories create guestbook-maven-repo \
        --repository-format=maven \
        --location=us-central1 \
        --description="My Maven repo" 
    ```

1. Create an **Artifact Registry [remote repository](https://cloud.google.com/artifact-registry/docs/repositories/remote-repo) (Preview)**: 
    This feature is not available for public access. To get access to this feature, 
    see the [access request page][access].

    With access uncomment `<repositories>...</repositories>` in [`pom.xml`](./pom.xml).

    ```sh
    gcloud artifacts repositories create guestbook-remote-repo \
        --repository-format=maven \
        --location=us-central1 \
        --description="My remote repo" \
        --mode=remote-repository \
        --remote-repo-config-desc="Maven Central" \
        --remote-mvn-repo=MAVEN-CENTRAL
    ```

1. Create your **Cloud Deploy** delivery pipeline and targets:

    ```sh
    gcloud deploy apply --file ./frontend/cloudrun.clouddeploy.yaml
    gcloud deploy apply --file ./backend/cloudrun.clouddeploy.yaml
    ```
   **⚠️ Note:** Ensure clouddeploy.yaml has the correct values. “PROJECT_ID” should have been already replaced with your Project Id


### Demo

1. Use the **Cloud Code source protect** ([request access][access]) plugin to view dependencies vulnerabilities:

    In [`backend/pom.xml`](./backend/pom.xml) locate the `com.google.code.gson:gson` dependency. The plugin should advise to update the dependency to v2.8.9+.

1. Submit the Cloud Build config:

    ```sh
    gcloud builds submit --config cloudrun.cloudbuild.yaml --substitutions SHORT_SHA=1234 --region us-central1
    ```

    The build does the following:

    * Caches dependency artifacts into an **Artifact Registry remote repo** ([request access][access]). The first time that you request a version of a package, Artifact Registry downloads and caches the package in the remote repository. The next time you request the same package version, Artifact Registry serves the cached copy.
    * Builds and stores a Java dependency artifacts to Artifact Registry 
    * Builds and push containers to Artifact Registry, where **[Container Analysis](https://cloud.google.com/container-analysis/docs/container-analysis)** provides integrated on-demand or automated scanning for base container images, Maven & Go packages in containers, and for non-containerized Maven packages.
    * Automatically signs the artifacts with the attestor: [“built-with-cloud-build”](https://cloud.google.com/binary-authorization/docs/deploy-cloud-build)
    * Creates a release via Cloud Deploy

1. View artifacts cached in the **Artifact Registry remote repository** ([request access][access]):

    ```sh
    gcloud artifacts files list --repository=guestbook-remote-repo
    ```

1. View artifact deployed to the **Artifact Registry Maven repository**:

    ```sh
    gcloud artifacts packages list --repository=guestbook-maven-repo --location=us-central1
    ```


1. View the container vulnerabilities, dependencies, and provenance via Cloud Build:
    * Open the [Cloud Build console](https://console.cloud.google.com/cloud-build/builds)
    * Click on the build ID to view the build
    * Click the "Build Artifacts" tab
    * Click "View" under ["Security Insights"](https://cloud.google.com/software-supply-chain-security/docs/sds/build-view-security-insights)
        * Cloud Build supports [SLSA Level 3 builds for container images](https://cloud.google.com/build/docs/securing-builds/view-build-provenance) and generates authenticated and non-falsifiable [build provenance](https://cloud.google.com/build/docs/securing-builds/view-build-provenance) for containerized applications.
        * Container Analysis provides standalone scanning (Preview) that identifies existing vulnerabilities and new vulnerabilities within the open source dependencies used by your Maven artifacts. This feature is not available for public access. To get access to this feature, see the [access request page][access].
    * Click on "Artifacts scanned" to view vulnerabilities in Artifact Registry

1. View **[Cloud Run Security](https://cloud.devsite.corp.google.com/software-supply-chain-security/docs/sds/deploy-run-view-security-insights)** insights:
    * [Navigate to a Cloud Run service](https://console.cloud.google.com/run/detail/us-central1/guestbook-backend-dev)
    * Click the "Revisions" tab
    * Click the right-hand "Security" tab to iew the container vulnerabilities, dependencies, and provenance

1. Deploy the release to production via **Cloud Deploy**:
    * [Navigate to the Cloud Deploy pipeline](https://console.cloud.google.com/deploy/delivery-pipelines/us-central1/cloudrun-guestbook-backend-delivery)
    * Click "Promote"

### Test the Binary Authorization policy

The Binary Authorization policy should prevent deployment of containers that
were not built by Cloud Build. This example shows a failed deployment when deploying
a locally built container.

1. Build the container locally via Jib (Cloud Build signs builds with built-by-cloudbuild attestor, so this will not be signed):

    ```sh
    cd backend
    mvn compile jib:build \
        -Dimage=us-central1-docker.pkg.dev/$PROJECT_ID/containers/java-guestbook-backend:blocked
    ```

1. Try to deploy the image:

    ```sh
    gcloud deploy releases create backend-release-blocked \
        --delivery-pipeline=cloudrun-guestbook-backend-delivery \
        --skaffold-file=./cloudrun.skaffold.yaml \
        --images=java-guestbook-backend=us-central1-docker.pkg.dev/$PROJECT_ID/containers/java-guestbook-backend:blocked
    ```

[access]: https://docs.google.com/forms/d/e/1FAIpQLSeBUSpLmXsvGhnKfYx7g-Cmd-oth9yXbUTZNFIL87cdGIu2RQ/viewform?resourcekey=0-tR1FN8wQtdR43sJixQL3jw