package org.marketplace.notification.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.marketplace.notification.Enum.NotifType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Notification extends PanacheMongoEntity {

    private ObjectId id;
    private String userId;
    private String title;
    private String message;
    private NotifType type;
    private boolean read;
    private LocalDateTime createdAt;


}
