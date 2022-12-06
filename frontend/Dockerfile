# Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Use maven to compile the java application.
# https://hub.docker.com/_/maven
FROM maven:3-eclipse-temurin-19-alpine AS build-env

# Set the working directory to /app
WORKDIR /app

# copy the pom.xml file to download dependencies
COPY pom.xml ./

# Copy the rest of the working directory contents into the container
COPY . ./

# Compile the application.
RUN mvn package -DskipTests

# Build runtime image.
FROM eclipse-temurin:19-jre

# Copy the compiled files over.
COPY --from=build-env /app/target /app/

# Starts java app with debugging server at port 5005.
ENTRYPOINT ["java", "-jar", "/app/frontend-1.0.jar"]