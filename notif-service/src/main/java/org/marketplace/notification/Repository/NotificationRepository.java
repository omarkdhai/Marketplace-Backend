package org.marketplace.notification.Repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.marketplace.notification.Entity.Notification;

import java.util.List;

@ApplicationScoped
public class NotificationRepository implements PanacheMongoRepository<Notification> {
    public List<Notification> findByUserId(String userId) {
        return find("userId", userId).list();
    }

    public List<Notification> findUnreadByUserId(String userId) {
        return find("userId = ?1 and read = false", userId).list();
    }
}
