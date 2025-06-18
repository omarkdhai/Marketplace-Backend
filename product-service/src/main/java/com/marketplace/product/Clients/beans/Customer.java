package com.marketplace.product.Clients.beans;

import io.quarkus.arc.All;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {

    String customerId;
}
