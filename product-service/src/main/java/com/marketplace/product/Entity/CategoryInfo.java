package com.marketplace.product.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marketplace.product.Enum.CategoryType;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfo extends PanacheMongoEntity {

    public ObjectId id;
    public String name;
    public String description;
    @BsonIgnore
    @JsonIgnore
    private List<CategoryType> type;

}
