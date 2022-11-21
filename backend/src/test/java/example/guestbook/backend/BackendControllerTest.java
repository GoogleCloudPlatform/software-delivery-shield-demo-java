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

package example.guestbook.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BackendController.class)
public class BackendControllerTest {

  @MockBean private MessageRepository mockRepository;

  @Autowired private MockMvc mockMvc;

  @Test
  public void failsPostMessages() throws Exception {
    this.mockMvc.perform(post("/messages")).andExpect(status().isBadRequest());
  }

  @Test
  public void succeedsPostMessages() throws Exception {
    GuestBookEntry entry = new GuestBookEntry();
    entry.setAuthor("author");
    entry.setMessage("message");
    Gson gson = new Gson();
    this.mockMvc
        .perform(post("/messages").content(gson.toJson(entry)).contentType("application/json"))
        .andExpect(status().isOk());
  }

  @Test
  public void succeedsGetMessages() throws Exception {
    this.mockMvc.perform(get("/messages")).andExpect(status().isOk());
  }
}
