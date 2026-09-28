package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.SellerProfile;

public interface SellerProfileRepositoryPort {

    SellerProfile save(SellerProfile sellerProfile);
}
