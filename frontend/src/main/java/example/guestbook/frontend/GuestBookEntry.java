package example.guestbook.frontend;

import org.springframework.cloud.gcp.data.firestore.Document;

import com.google.cloud.firestore.annotation.DocumentId;

/**
 * defines the data associated with a single guest book entry
 */
@Document(collectionName = "guestbookEntries")
public class GuestBookEntry {
    @DocumentId
    private String id;
    private String author;
    private String message;
    private long date;

    public final String getAuthor() {
        return author;
    }

    public final void setAuthor(String author) {
        this.author = author;
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(String message) {
        this.message = message;
    }

    public final long getDate() {
        return this.date;
    }

    public final void setDate(long date) {
        this.date = date;
    }

    public final String toString() {
        return this.author + " " + this.message;
    }
}
