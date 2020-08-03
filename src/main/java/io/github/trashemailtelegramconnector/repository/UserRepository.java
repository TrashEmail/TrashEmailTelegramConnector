package io.github.trashemailtelegramconnector.repository;

import io.github.trashemailtelegramconnector.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    public List<User> findByChatIdAndIsActiveTrue(long chatId);
    public User findByEmailIdAndIsActiveTrue(String emailId);
    public User findByEmailIdAndChatId(String emailId , long chatId);
    public boolean existsByEmailIdAndIsActiveTrue(String emailId);
    public void delete(User user);

    @Query(value = "SELECT * FROM user_email_to_chat_id_mapping " +
            "where create_date_time <= " +
            "NOW() - INTERVAL 10 MINUTE and is_temp=0x1", nativeQuery = true)
    public List<User> getUserIdsCreatedBeforeTenMinutes();

    public long count();

    @Query(value = "select max(u.id) from User u")
    public long getTotalEmailIds();

    @Query(value = "select max(u.id) from User u where u.isActive = true")
    public long getActiveEmailIds();

    @Query(value = "SELECT count(DISTINCT user.chatId) FROM User user")
    public long getTotalChatIdCount();

    @Query(value = "SELECT count(DISTINCT user.chatId) FROM User user where user.isActive = true ")
    public long getActiveChatIdCount();

}
