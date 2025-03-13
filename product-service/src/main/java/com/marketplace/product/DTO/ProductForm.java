package com.marketplace.product.DTO;


import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Entity.Product;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Data
public class ProductForm {

    @FormParam("name")
    @PartType("text/plain")
    private String name;

    @FormParam("description")
    @PartType("text/plain")
    private String description;

    @FormParam("price")
    @PartType("text/plain")
    private double price;

    @FormParam("status")
    @PartType("text/plain")
    private String status;

    @FormParam("categories")
    @PartType("text/plain")
    private String categories;

    @FormParam("medias")
    @PartType("text/plain")
    private String medias;

    @FormParam("keywords")
    @PartType("text/plain")
    private String keywords;

    @FormParam("photo")
    @PartType("application/octet-stream")
    private InputStream photo;

}