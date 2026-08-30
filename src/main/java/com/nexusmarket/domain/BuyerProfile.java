package com.nexusmarket.domain;

import com.nexusmarket.valueObjects.CommercialStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BuyerProfile {

    private String id;

    private String mainAddress;

    private List<String> additionalAddresses = new ArrayList<>();

    private CommercialStatus commercialStatus = CommercialStatus.ACTIVE;

    private User user;
}
