package com.hepexta.rabbitmq.hometask.model;

import com.hepexta.rabbitmq.hometaskcustomretrywithdlx.model.UpdateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private String customer;
    private String good;
    private BigDecimal price;
    private UpdateStatus status;
}
