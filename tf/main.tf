//  Copyright 2022 Google LLC
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

provider "google" {
  project = "PROJECT_ID"
}

resource "google_project_service" "artifactregistry_googleapis_com" {
  service = "artifactregistry.googleapis.com"
}

resource "google_project_service" "binaryauthorization_googleapis_com" {
  service = "binaryauthorization.googleapis.com"
}

resource "google_project_service" "cloudbuild_googleapis_com" {
  service = "cloudbuild.googleapis.com"
}

resource "google_project_service" "clouddeploy_googleapis_com" {
  service = "clouddeploy.googleapis.com"
}

resource "google_project_service" "container_googleapis_com" {
  service = "container.googleapis.com"
}

resource "google_project_service" "containeranalysis_googleapis_com" {
  service = "containeranalysis.googleapis.com"
}
resource "google_project_service" "containerscanning_googleapis_com" {
  service = "containerscanning.googleapis.com"
}

resource "google_project_service" "containersecurity_googleapis_com" {
  service = "containersecurity.googleapis.com"
}

resource "google_project_service" "run_googleapis_com" {
  service = "run.googleapis.com"
}

resource "google_artifact_registry_repository" "containers" {
  description   = "Docker repository"
  format        = "DOCKER"
  location      = "us-central1"
  repository_id = "containers"
}

resource "google_artifact_registry_repository" "guestbook_remote_repo" {
  description   = "My remote repo"
  format        = "MAVEN"
  location      = "us-central1"
  repository_id = "guestbook-remote-repo"
}

# Set IAM policy
resource "google_project_iam_member" "cb_deploy_operator" {
  project = "your-project-id"
  role    = "roles/clouddeploy.operator"
  member  = "serviceAccount:PROJECT_NUM@cloudbuild.gserviceaccount.com"
}

resource "google_project_iam_member" "cb_sa_user" {
  project = "your-project-id"
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:PROJECT_NUM@cloudbuild.gserviceaccount.com"
}

resource "google_project_iam_member" "cb_container_admin" {
  project = "your-project-id"
  role    = "roles/container.admin"
  member  = "serviceAccount:PROJECT_NUM@cloudbuild.gserviceaccount.com"
}

resource "google_project_iam_member" "default_container_dev" {
  project = "your-project-id"
  role    = "roles/container.developer"
  member  = "serviceAccount:PROJECT_NUM-compute@developer.gserviceaccount.com"
}

resource "google_project_iam_member" "default_deploy_runner" {
  project = "your-project-id"
  role    = "roles/clouddeploy.jobRunner"
  member  = "serviceAccount:PROJECT_NUM-compute@developer.gserviceaccount.com"
}

resource "google_project_iam_member" "default_ar_reader" {
  project = "your-project-id"
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:PROJECT_NUM-compute@developer.gserviceaccount.com"
}

# Set Binary Authorization policy
resource "google_binary_authorization_policy" "policy" {
  default_admission_rule {
    evaluation_mode         = "REQUIRE_ATTESTATION"
    enforcement_mode        = "ENFORCED_BLOCK_AND_AUDIT_LOG"
    require_attestations_by = ["projects/PROJECT_ID/attestors/built-by-cloud-build"]
  }

  global_policy_evaluation_mode = "ENABLE"
}

# GKE Clusters
resource "google_container_cluster" "dev_cluster" {
  name                     = "dev-cluster"
  location                 = "us-central1"
  binary_authorization {
    evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"
  }

    enable_autopilot = true
    ip_allocation_policy {
  }
}

resource "google_container_cluster" "prod_cluster" {
  name                     = "prod-cluster"
  location                 = "us-central1"
  binary_authorization {
    evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"
  }
    enable_autopilot = true
    ip_allocation_policy {
  }
}

# Cloud Deploy
resource "google_clouddeploy_target" "dev" {
  location = "us-central1"
  name     = "dev-cluster"
  description = "dev cluster"

  gke {
    cluster = "projects/PROJECT_ID/locations/us-central1/clusters/dev-cluster"
  }
  require_approval = false
}

resource "google_clouddeploy_target" "prod" {
  location = "us-central1"
  name     = "prod-cluster"
  description = "production cluster"

  gke {
    cluster = "projects/PROJECT_ID/locations/us-central1/clusters/prod-cluster"
  }
  require_approval = false
}


resource "google_clouddeploy_delivery_pipeline" "primary" {
  location = "us-central1"
  name     = "guestbook-app-delivery"
  description = "main application pipeline"

  serial_pipeline {
    stages {
      profiles  = []
      target_id = google_clouddeploy_target.dev.name
    }

    stages {
      profiles  = []
      target_id = google_clouddeploy_target.prod.name
    }
  }
}
