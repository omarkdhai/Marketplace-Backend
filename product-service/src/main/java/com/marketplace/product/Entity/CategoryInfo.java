package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.*;
import org.bson.types.ObjectId;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfo extends PanacheMongoEntity {

    public ObjectId id;
    public String name;
    public String description;

}
