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

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/** defines the data associated with a single guest book entry */
// @Document(collectionName = "guestbookEntries")
@Entity
public class GuestBookEntry {
  // @DocumentId String id;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String author;
  private String message;
  private long date;

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getDate() {
    return this.date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public String toString() {
    return String.format("Author=%s;Message=%s;Date=%s", this.author, this.message, this.date);
  }
}
