package example.guestbook.frontend;

// import com.google.cloud.firestore.annotation.DocumentId;
// import com.google.cloud.spring.data.firestore.Document;

/**
 * defines the data associated with a single guest book entry
 */
// @Document(collectionName = "guestbookEntries")
public class GuestBookEntry {
    // @DocumentId String id;
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
        return String.format("Author=%s;Message=%s;Date=%s", this.author, this.message, this.date);
    }
}
