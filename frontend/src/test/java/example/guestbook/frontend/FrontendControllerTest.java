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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

public class FrontendControllerTest {

  private FrontendController controller = new FrontendController();

  @Test
  public void succeedsPost() throws Exception {
    GuestBookEntry entry = new GuestBookEntry();
    entry.setAuthor("author");
    entry.setMessage("message");
    String response = controller.post(entry);
    Assertions.assertEquals("redirect:/", response);
  }

  @Test
  public void succeedsGet() throws Exception {
    Model model = new ExtendedModelMap();
    String response = controller.main(model);
    Assertions.assertEquals("home", response);
  }
}
