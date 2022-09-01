package example.guestbook.backend;

import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.data.firestore.FirestoreTemplate;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * defines the REST endpoints managed by the server.
 */
@RestController
public class BackendController {

    @Autowired private MessageRepository repository;
    // private final MessageRepository repository;
    // private final FirestoreTemplate firestoreTemplate;

    // public BackendController(MessageRepository repository, FirestoreTemplate firestoreTemplate) {
    //     this.repository = repository;
    //     this.firestoreTemplate = firestoreTemplate;
    // }

    /**
     * endpoint for retrieving all guest book entries stored in database
     * @return a list of GuestBookEntry objects
     */
    @GetMapping("/messages")
    public final Flux<GuestBookEntry> getMessages() {
        Sort byCreation = Sort.by(Sort.Direction.DESC, "_id");
        Flux<GuestBookEntry> msgList = this.repository.findAll();
        // System.out.println(this.firestoreTemplate.findAll(GuestBookEntry.class));
        return msgList;
    }

    /**
     * endpoint for adding a new guest book entry to the database
     * @param message a message object passed in the HTTP POST request
     * @return 
     */
    @PostMapping("/messages")
    public final Mono<GuestBookEntry> addMessage(@RequestBody GuestBookEntry message) {
        System.out.println(message.toString());
        message.setDate(System.currentTimeMillis());
        return this.repository.save(message);
    }
}
