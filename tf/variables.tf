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

#-----------------------------------------------------------------------------
# Project Info
#-----------------------------------------------------------------------------

variable "project_name" {
  type        = string
  description = "the base name to use when creating resources. a randomized suffix will be added."
  default     = "sds-java-demo"
}

variable "existing_project_id" {
  type        = string
  description = "If there is an existing Google Cloud project you want to use, define it here. If this is left as null, one will be created for you."
  default     = null
}

variable "google_cloud_region" {
  type        = string
  description = "the Google Cloud region in which to create resources"
  default     = "us-central1"
}

variable "google_billing_account" {
  type        = string
  description = "the ID of your Google Cloud billing account"
  default     = null
}

variable "use_cloud_run" {
  type    = bool
  default = false
}