# Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

resource "google_clouddeploy_target" "dev" {
  location    = var.google_cloud_region
  count       = var.use_cloud_run ? 0 : 1 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  name        = "dev-cluster"
  description = "dev cluster"

  gke {
    cluster = format("projects/%s/locations/us-central1/clusters/dev-cluster", local.final_project_id)
  }
  require_approval = false
}


resource "google_clouddeploy_target" "prod" {
  location    = var.google_cloud_region
  count       = var.use_cloud_run ? 0 : 1 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  name        = "prod-cluster"
  description = "production cluster"

  gke {
    cluster = format("projects/%s/locations/us-central1/clusters/prod-cluster", local.final_project_id)
  }
  require_approval = false
}

resource "google_clouddeploy_delivery_pipeline" "primary" {
  location    = var.google_cloud_region
  count       = var.use_cloud_run ? 0 : 1 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  name        = "guestbook-app-delivery"
  description = "main application pipeline"

  serial_pipeline {
    stages {
      profiles  = []
      target_id = google_clouddeploy_target.dev[0].id
    }

    stages {
      profiles  = []
      target_id = google_clouddeploy_target.prod[0].id
    }
  }
}