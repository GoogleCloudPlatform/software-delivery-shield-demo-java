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

package example.guestbook;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.deploy.v1.CloudDeployClient;
import com.google.cloud.deploy.v1.CloudDeployClient.ListRolloutsPagedResponse;
import com.google.cloud.deploy.v1.ReleaseName;
import com.google.cloud.deploy.v1.Rollout;
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import com.google.common.collect.Iterables;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient;
import com.google.devtools.artifactregistry.v1.DockerImage;
import com.google.devtools.artifactregistry.v1.DockerImageName;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient.ListDockerImagesPagedResponse;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient.ListPackagesPagedResponse;
import com.google.devtools.artifactregistry.v1.RepositoryName;

import io.grafeas.v1.BuildOccurrence;
import io.grafeas.v1.GrafeasClient;
import io.grafeas.v1.GrafeasClient.ListOccurrencesPagedResponse;
import io.grafeas.v1.Occurrence;
import io.grafeas.v1.ProjectName;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GuestbookIT {
    static String projectId;
    static String location = "us-central1";
    static String remoteRepo;
    static String mavenRepo;
    static String containerRepo;
    static String pipeline;
    static String release;
    final String frontendImage = "java-guestbook-frontend";
    final String backendImage = "java-guestbook-backend";
    final String mavenArtifact = "com.example.guestbook:java-guestbook-pojo";
    List<DockerImage> dockerImages;

    @BeforeAll
    public static void init() {
        projectId = System.getenv("PROJECT_ID");
        assertThat(projectId).isNotNull();

        String shortSha = System.getenv("SHORT_SHA");
        assertThat(shortSha).isNotNull();
        release = "release-" + shortSha;

        remoteRepo = System.getenv().getOrDefault("REMOTE_REPO", "guestbook-remote-repo");

        mavenRepo = System.getenv().getOrDefault("MAVEN_REPO", "guestbook-maven-repo");

        containerRepo = System.getenv().getOrDefault("CONTAINERS", "containers");

        pipeline = System.getenv().getOrDefault("PIPELINE", "guestbook-app-delivery");
    }

    @Test
    public void verifyArtifactRegistry_containers() throws IOException {
        try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
            RepositoryName parent = RepositoryName.of(projectId, location, containerRepo);
            ListDockerImagesPagedResponse response = artifactRegistryClient
                    .listDockerImages(parent.toString());
            dockerImages = StreamSupport
                    .stream(response.getPage().iterateAll().spliterator(), false)
                    .collect(Collectors.toList());
            assertThat(dockerImages.size()).isEqualTo(2);
        }
    }

    @Test
    public void verifyArtifactRegistry_remote() throws IOException {
        if (Boolean.valueOf(System.getenv("TEST_REMOTE_REPO"))) {
            try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
                RepositoryName parent = RepositoryName.of(projectId, location, remoteRepo);

                ListPackagesPagedResponse response = artifactRegistryClient
                        .listPackages(parent.toString());

                // Packages are found in remote repo
                assertThat(Iterables.size(response.iterateAll())).isAtLeast(1);
            }
        }
    }

    @Test
    public void verifyArtifactRegistry_maven() throws IOException {
        if (Boolean.valueOf(System.getenv("TEST_MAVEN_REPO"))) {
            try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
                RepositoryName parent = RepositoryName.of(projectId, location, mavenRepo);

                ListPackagesPagedResponse response = artifactRegistryClient
                        .listPackages(parent.toString());

                // Package is found in maven repo
                assertThat(Iterables.size(response.iterateAll())).isAtLeast(1);
            }
        }
    }

    @Test
    public void verifyCloudDeploy_rollout() throws InterruptedException, IOException {
        try (CloudDeployClient cloudDeployClient = CloudDeployClient.create()) {
            String releaseName = ReleaseName.of(projectId, location, pipeline, release).toString();
            ListRolloutsPagedResponse rollouts = cloudDeployClient.listRollouts(releaseName);
            Rollout rollout = Iterables.getFirst(rollouts.iterateAll(), null);

            boolean finished = false;
            boolean failed = false;
            long backoffTime = 0;
            long backoffDelay = 1_000; // Start wait with delay of 1,000 ms
            final long backoffTimeout = 10 * 60 * 1_000; // Time out at 10 minutes

            while (!finished && !failed && backoffTime < backoffTimeout) {
                finished = rollout.getState() == Rollout.State.SUCCEEDED;
                failed = rollout.getState() == Rollout.State.FAILED
                        || rollout.getState() == Rollout.State.UNRECOGNIZED;

                if (finished) {
                    assertThat(rollout.getState()).isEqualTo(Rollout.State.SUCCEEDED);
                } else {
                    System.out.println("Waiting for rollout: " + rollout.getState());
                    Thread.sleep(backoffDelay);
                    backoffTime += backoffDelay;
                    backoffDelay *= 2; // Double the delay to provide exponential backoff.
                }
                rollout = cloudDeployClient.getRollout(rollout.getName());
            }
            assertThat(rollout.getState()).isEqualTo(Rollout.State.SUCCEEDED);
        }
    }

    @Test
    public void verifyContainerAnalysis_provenance() throws IOException {
        try (ContainerAnalysisClient containerAnalysisClient = ContainerAnalysisClient.create()) {
            GrafeasClient grafeasClient = containerAnalysisClient.getGrafeasClient();
            for (DockerImage dockerImage : dockerImages) {
                ListOccurrencesPagedResponse occurences = grafeasClient.listOccurrences(
                        ProjectName.of(projectId),
                        String.format("resourceUrl=\"https://%s\" AND kind=\"BUILD\"",
                                dockerImage.getUri()));

                List<Occurrence> occurrenceList = StreamSupport
                        .stream(occurences.getPage().iterateAll().spliterator(), false)
                        .collect(Collectors.toList());
                assertThat(occurrenceList.size()).isAtLeast(2);

                for (Occurrence imageOccurrence : occurrenceList) {
                    BuildOccurrence build = imageOccurrence.getBuild();
                    assertThat(
                            build.getProvenance() != null || build.getIntotoProvenance() != null);
                }

            }
        }
    }
}
