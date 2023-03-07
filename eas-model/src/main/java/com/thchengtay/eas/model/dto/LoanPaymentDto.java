package com.thchengtay.eas.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Getter
@Setter
public class LoanPaymentDto {

    private String cooperationCode;

    private String cooperationName;

    private String projectCode;

    private BigDecimal paymentAmount;

    private BigDecimal realPaymentAmount;

    private BigDecimal fee;

    private int accType;

    private String channel;


}
