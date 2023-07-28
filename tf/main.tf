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

terraform {
  required_version = ">= 0.13"

  required_providers {
    google = {
      version = "~> 4.36"
      source  = "hashicorp/google"
    }
    random = {
      version = "~> 3.5.1"
      source  = "hashicorp/random"
    }
    time = {
      version = "~> 3.5.1"
      source  = "hashicorp/time"
    }
  }
}

resource "random_id" "suffix" {
  prefix      = var.project_name
  byte_length = 4
}

resource "google_project" "generated_project" {
  count           = var.existing_project_id == null ? 1 : 0 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  project_id      = local.gen_project_id
  name            = local.gen_project_id
  billing_account = var.google_billing_account

  lifecycle {
    # ignoring org_id changes allows the project to be created in whatever org
    # the user is part of by default, without having to explicitly include the
    # org id in the terraform config.
    ignore_changes = [org_id]
    precondition {
      condition     = var.google_billing_account != null
      error_message = "If an existing Project ID is not specified, you must provide a billing account that we can use to create one for you"
    }
  }
}


provider "google" {
  project = local.final_project_id
}

data "google_project" "project_info" {
  project_id = local.final_project_id
}

## Enable Services

resource "google_project_service" "sds_demo_services" {
  project = local.final_project_id
  service = "${each.value}.googleapis.com"

  for_each = toset([
    "artifactregistry",
    "binaryauthorization",
    "cloudbuild",
    "clouddeploy",
    "container",
    "containeranalysis",
    "containerscanning",
    "containersecurity",
    "run"
  ])
}

resource "time_sleep" "wait_for_services" {
  create_duration = "300s"
  depends_on      = [google_project_service.sds_demo_services]
}

## Artifacts

resource "google_artifact_registry_repository" "containers" {
  description   = "SDS Java Demo Docker repository"
  format        = "DOCKER"
  location      = var.google_cloud_region
  repository_id = "containers"
}

resource "google_artifact_registry_repository" "guestbook_remote_repo" {
  description   = "SDS Java Demo remote repo"
  format        = "MAVEN"
  location      = var.google_cloud_region
  repository_id = "guestbook-maven-repo"
}

# Set IAM policy
resource "google_project_iam_member" "cb_deploy_operator" {
  project = local.final_project_id
  role    = "roles/clouddeploy.operator"
  member  = format("serviceAccount:%d@cloudbuild.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}

resource "google_project_iam_member" "cb_sa_user" {
  project = local.final_project_id
  role    = "roles/iam.serviceAccountUser"
  member  = format("serviceAccount:%d@cloudbuild.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}

resource "google_project_iam_member" "cb_container_admin" {
  project = local.final_project_id
  role    = "roles/container.admin"
  member  = format("serviceAccount:%d@cloudbuild.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}

resource "google_project_iam_member" "default_container_dev" {
  project = local.final_project_id
  role    = "roles/container.developer"
  member  = format("serviceAccount:%d-compute@developer.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}

resource "google_project_iam_member" "default_deploy_runner" {
  project = local.final_project_id
  role    = "roles/clouddeploy.jobRunner"
  member  = format("serviceAccount:%d-compute@developer.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}

resource "google_project_iam_member" "default_ar_reader" {
  project = local.final_project_id
  role    = "roles/artifactregistry.reader"
  member  = format("serviceAccount:%d-compute@developer.gserviceaccount.com", var.existing_project_id == null ? google_project.generated_project[0].number : data.google_project.project_info.number)
}