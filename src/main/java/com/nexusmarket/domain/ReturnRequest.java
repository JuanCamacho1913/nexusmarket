package com.nexusmarket.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest {

    private String id;

    private String reason;

    private Order order;
}
