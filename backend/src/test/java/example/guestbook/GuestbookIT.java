package example.guestbook;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.deploy.v1.CloudDeployClient;
import com.google.cloud.deploy.v1.CloudDeployClient.ListRolloutsPagedResponse;
import com.google.cloud.deploy.v1.ReleaseName;
import com.google.cloud.deploy.v1.Rollout;
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import com.google.common.collect.Iterables;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient.ListDockerImagesPagedResponse;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient.ListPackagesPagedResponse;
import com.google.devtools.artifactregistry.v1.RepositoryName;
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
  final String location = "us-central1";
  final String remoteRepo = "guestbook-remote-repo";
  final String mavenRepo = "guestbook-maven-repo";
  final String containerRepo = "containers";
  final String frontendImage = "java-guestbook-frontend";
  final String backendImage = "java-guestbook-backend";
  final String mavenArtifact = "com.example.guestbook:java-guestbook-pojo";
  final String pipeline = "guestbook-app-delivery";
  static String release;

  @BeforeAll
  public static void init() {
    projectId = System.getenv("PROJECT_ID");
    assertThat(projectId).isNotNull();
    String shortSha = System.getenv("SHORT_SHA");
    assertThat(shortSha).isNotNull();
    release = "release-" + shortSha;
  }

  @Test
  public void verifyArtifactRegistry_containers() throws IOException {
    try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
      RepositoryName parent = RepositoryName.of(projectId, location, containerRepo);
      ListDockerImagesPagedResponse response =
          artifactRegistryClient.listDockerImages(parent.toString());
      assertThat(Iterables.size(response.iterateAll())).isEqualTo(2);
    }
  }

  @Test
  public void verifyArtifactRegistry_remote() throws IOException {
    try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
      RepositoryName parent = RepositoryName.of(projectId, location, remoteRepo);

      ListPackagesPagedResponse response = artifactRegistryClient.listPackages(parent.toString());
      assertThat(Iterables.size(response.iterateAll())).isAtLeast(1);
    }
  }

  @Test
  public void verifyArtifactRegistry_maven() throws IOException {
    try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
      RepositoryName parent = RepositoryName.of(projectId, location, remoteRepo);

      ListPackagesPagedResponse response = artifactRegistryClient.listPackages(parent.toString());

      assertThat(Iterables.size(response.iterateAll())).isAtLeast(1);
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
        failed =
            rollout.getState() == Rollout.State.FAILED
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
  public void verifyContainerAnalysis() throws IOException {
    try (ContainerAnalysisClient containerAnalysisClient = ContainerAnalysisClient.create()) {
      GrafeasClient grafeasClient = containerAnalysisClient.getGrafeasClient();

      ListOccurrencesPagedResponse occurences =
          grafeasClient.listOccurrences(ProjectName.of(projectId), "");

      List<Occurrence> frontendFiltered =
          StreamSupport.stream(occurences.getPage().iterateAll().spliterator(), false)
              .filter(
                  occ ->
                      occ.getResourceUri().contains(frontendImage)
                          && occ.getBuild().getProvenance() != null)
              .collect(Collectors.toList());

      List<Occurrence> backendFiltered =
          StreamSupport.stream(occurences.iterateAll().spliterator(), false)
              .filter(
                  occ ->
                      occ.getResourceUri().contains(backendImage)
                          && occ.getBuild().getProvenance() != null)
              .collect(Collectors.toList());

      List<Occurrence> mvnFiltered =
          StreamSupport.stream(occurences.iterateAll().spliterator(), false)
              .filter(
                  occ ->
                      occ.getResourceUri().contains(mavenArtifact)
                          && occ.getBuild().getProvenance() != null)
              .collect(Collectors.toList());

      System.out.println(frontendFiltered);
      System.out.println(backendFiltered);
      System.out.println(mvnFiltered);
      assertThat(frontendFiltered).isNotEmpty();
      assertThat(backendFiltered).isNotEmpty();
      assertThat(mvnFiltered).isNotEmpty();
    }
  }
}
