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

resource "google_container_cluster" "dev_cluster" {
  name     = "dev-cluster"
  count    = var.use_cloud_run ? 0 : 1 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  location = var.google_cloud_region
  binary_authorization {
    evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"
  }

  enable_autopilot = true
  ip_allocation_policy {}
}

resource "google_container_cluster" "prod_cluster" {
  name     = "prod-cluster"
  count    = var.use_cloud_run ? 0 : 1 # Used to "enable" or "disable" a resource conditionally. This isn't actually used for increasing the quantity of the resource. See https://github.com/hashicorp/terraform/issues/21953 for context.
  location = var.google_cloud_region
  binary_authorization {
    evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"
  }

  enable_autopilot = true
  ip_allocation_policy {}
}