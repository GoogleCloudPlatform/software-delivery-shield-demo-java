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

package example.guestbook.frontend;

import com.example.guestbook.GuestBookEntry;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

/**
 * defines the REST endpoints managed by the server.
 */
@Controller
public class FrontendController {

  private String backendUri = String.format("%s/messages",
      System.getenv("GUESTBOOK_API_ADDR"));

  /**
   * endpoint for the landing page
   * 
   * @param model defines model for html template
   * @return the name of the html template to render
   */
  @GetMapping("/")
  public final String main(final Model model) {
    RestTemplate restTemplate = new RestTemplate();
    try {
      GuestBookEntry[] response;
      // When deployed on Cloud Run, add an Authorization header to the private
      // service request.
      if (System.getenv("K_SERVICE") != null) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getIdToken(backendUri));
        System.out.println(getIdToken(backendUri));
        HttpEntity entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(backendUri, HttpMethod.GET, entity,
            GuestBookEntry[].class).getBody();
      } else {
        response = restTemplate.getForObject(backendUri,
            GuestBookEntry[].class);
      }
      model.addAttribute("messages", response);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error retrieving messages from backend.");
      model.addAttribute("noBackend", true);
    }

    return "home";
  }

  /**
   * endpoint for handling form submission
   * 
   * @param formMessage holds date entered in html form
   * @return redirects back to home page
   * @throws URISyntaxException when there is an issue with the backend uri
   */
  @RequestMapping(value = "/post", method = RequestMethod.POST)
  public final String post(final GuestBookEntry formMessage)
      throws URISyntaxException {
    URI url = new URI(backendUri);

    RestTemplate restTemplate = new RestTemplate();
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "application/json");
      // When deployed on Cloud Run, add an Authorization header to the private
      // service request.
      if (System.getenv("K_SERVICE") != null) {
        httpHeaders.set("Authorization", "Bearer " + getIdToken(backendUri));
      }
      HttpEntity<GuestBookEntry> httpEntity = new HttpEntity<GuestBookEntry>(formMessage, httpHeaders);

      restTemplate.postForObject(url, httpEntity, String.class);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error posting message to backend.");
    }

    return "redirect:/";
  }

  /**
   * method to retrieve ID token for the service
   * 
   * @param url The private service url
   * @return ID token
   * @throws IOException
   */
  private final String getIdToken(String url) throws IOException {
    // Retrieve Application Default Credentials
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    IdTokenCredentials tokenCredentials = IdTokenCredentials.newBuilder()
        .setIdTokenProvider((IdTokenProvider) credentials)
        .setTargetAudience(url)
        .build();

    // Create an ID token
    return tokenCredentials.refreshAccessToken().getTokenValue();
  }

}
