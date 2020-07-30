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

    public List<User> findByEmailIdEndsWith(String domain);

    @Query(value = "select max(u.id) from User u")
    public long getTotalEmailIds();

    @Query(value = "select max(u.id) from User u where u.isActive = true")
    public long getActiveEmailIds();

    @Query(value = "SELECT count(DISTINCT user.chatId) FROM User user")
    public long getTotalChatIdCount();

    @Query(value = "SELECT count(DISTINCT user.chatId) FROM User user where user.isActive = true ")
    public long getActiveChatIdCount();

    @Query(
            value = "SELECT count(user.emailId) FROM User user " +
                    "WHERE DATE (user.createDateTime) = CURDATE() "
    )
    public long getEmailIdsCreatedTodayCount();

    /*
    In JPA DB dialects for date manipulations are not good, so using java
    based approach.
    */
    @Query(
            value = "SELECT count(user.emailId) FROM User user " +
                    "WHERE DATE(user.createDateTime) >= :oneWeekOldDate and " +
                    "DATE(user.createDateTime) <= :today " +
                    "GROUP BY DATE(user.createDateTime) " +
                    "ORDER BY DATE(user.createDateTime)"
    )
    public List<Long> getEmailIdsCreatedInWeek(
            @Param("today") Date currDate,
            @Param("oneWeekOldDate") Date oneWeekOldDate
    );

}
