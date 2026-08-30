package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.WarehouseType;
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
