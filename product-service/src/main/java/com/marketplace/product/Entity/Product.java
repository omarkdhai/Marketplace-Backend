package com.marketplace.product.Entity;

import com.marketplace.product.Enum.ProductStatus;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Product extends PanacheMongoEntity {

    private ObjectId id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private double price;

    @NotNull
    private ProductStatus status;

    private Instant creationDate;

    private boolean favorite = false;

    private List<CategoryInfo> categories;

    private String photo;
    private List<byte[]> medias;

    private List<String> keywords;

    /*public Product(String name, String description, double price, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }*/

    public Product(String name, String description, double price, ProductStatus status, Instant creationDate, List<CategoryInfo> categories, List<String> keywords) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.creationDate = creationDate;
        this.categories = categories;
        this.keywords = keywords;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }
}
