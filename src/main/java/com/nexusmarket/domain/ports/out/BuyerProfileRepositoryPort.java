package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.BuyerProfile;

public interface BuyerProfileRepositoryPort {

    BuyerProfile save(BuyerProfile buyerProfile);
}
