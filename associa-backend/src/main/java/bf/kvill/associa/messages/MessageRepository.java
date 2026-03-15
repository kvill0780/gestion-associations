package bf.kvill.associa.messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.association.id = :associationId AND (m.sender.id = :userId OR m.receiver.id = :userId) ORDER BY m.createdAt DESC")
    List<Message> findAllForUser(@Param("associationId") Long associationId, @Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.association.id = :associationId AND ((m.sender.id = :userId AND m.receiver.id = :otherUserId) OR (m.sender.id = :otherUserId AND m.receiver.id = :userId)) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("associationId") Long associationId,
                                   @Param("userId") Long userId,
                                   @Param("otherUserId") Long otherUserId);

    long countByAssociationIdAndReceiverIdAndReadAtIsNull(Long associationId, Long receiverId);
}
