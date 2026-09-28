package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueObjects.WarehouseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Warehouse {

    private String id;

    private String name;

    private WarehouseType type;

    private String location;

    private SellerProfile sellerProfile;
}
