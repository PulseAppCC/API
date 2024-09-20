package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.user.User;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository for interacting with {@link User}'s.
 *
 * @author Braydon
 */
@Repository
public interface UserRepository extends MongoRepository<User, Long> {
    /**
     * Find a user by their email.
     *
     * @param email the email of the user
     * @return the user with the email
     */
    User findByEmailIgnoreCase(@NonNull String email);
}