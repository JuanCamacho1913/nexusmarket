package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.ReturnRequest;

public interface ReturnRequestRepositoryPort {

    ReturnRequest save(ReturnRequest returnRequest);
}
