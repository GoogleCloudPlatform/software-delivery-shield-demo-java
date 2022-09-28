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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** defines the REST endpoints managed by the server. */
@RestController
public class BackendController {

  @Autowired MessageRepository repository;

  /**
   * endpoint for retrieving all guest book entries stored in database
   *
   * @return a list of GuestBookEntry objects
   */
  @GetMapping("/messages")
  public final Iterable<GuestBookEntry> getMessages() {
    Iterable<GuestBookEntry> msgList = this.repository.findAll();

    return msgList;
  }

  /**
   * endpoint for adding a new guest book entry to the database
   *
   * @param message a message object passed in the HTTP POST request
   * @return
   */
  @PostMapping("/messages")
  public final GuestBookEntry addMessage(@RequestBody GuestBookEntry message) {
    message.setDate(System.currentTimeMillis());
    return this.repository.save(message);
  }
}
