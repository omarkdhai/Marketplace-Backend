package com.marketplace.product.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.smallrye.common.constraint.NotNull;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfo extends PanacheMongoEntity {

    public ObjectId id;
    @NotNull
    public String name;
    @NotNull
    public String description;

    private byte[] miniPhoto;
    private byte[] maxiPhoto;
}
