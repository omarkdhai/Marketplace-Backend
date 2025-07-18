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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    private double discount = 0.0;

    private String imgUrl;

    public Product(String name, String description, double price, ProductStatus status, Instant creationDate, List<CategoryInfo> categories, List<String> keywords, double discount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.creationDate = creationDate;
        this.categories = categories;
        this.keywords = keywords;
        this.discount = discount;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }

    public double getEffectivePrice() {
        if (this.discount <= 0 || this.discount > 100) {
            return this.price; // No discount or invalid discount
        }
        return this.price * (1 - (this.discount / 100.0));
    }
}
