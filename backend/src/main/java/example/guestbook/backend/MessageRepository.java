package example.guestbook.backend;

// import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository;
/**
 * represents a custom Mongo repository that stores GuestBookEntry objects
 */
// public interface MessageRepository extends
//     MongoRepository<GuestBookEntry, String> { }
@Repository
public interface MessageRepository extends FirestoreReactiveRepository<GuestBookEntry> { 
    Flux<GuestBookEntry> findByAuthor(String author);
}