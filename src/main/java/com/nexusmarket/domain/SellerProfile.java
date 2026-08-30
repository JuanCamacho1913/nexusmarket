package com.nexusmarket.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SellerProfile {

    private String id;

    private String businessName;

    private String taxIdentification;

    private User user;

    private List<Warehouse> warehouses = new ArrayList<>();

    private List<Product> products = new ArrayList<>();
}
