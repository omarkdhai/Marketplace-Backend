package com.marketplace.product.DTO;

import jakarta.ws.rs.FormParam;
import lombok.Data;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

@Data
public class CategoryInfoForm {

    @FormParam("name")
    @PartType("text/plain")
    private String name;

    @FormParam("description")
    @PartType("text/plain")
    private String description;

    @FormParam("miniPhoto")
    @PartType("application/octet-stream")
    private InputPart miniPhoto;

    @FormParam("maxiPhoto")
    @PartType("application/octet-stream")
    private InputPart maxiPhoto;
}
