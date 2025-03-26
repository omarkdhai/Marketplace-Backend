package com.marketplace.product.DTO;



import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.InputStream;
import java.time.Instant;
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

    private Instant creationDate;

    @FormParam("medias")
    @PartType("application/octet-stream")
    private List<InputPart> medias;

    @FormParam("keywords")
    @PartType("text/plain")
    private String keywords;

    @FormParam("photo")
    @PartType("application/octet-stream")
    private InputPart photo;

}