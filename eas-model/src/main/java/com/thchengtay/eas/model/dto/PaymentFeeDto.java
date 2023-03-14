package com.thchengtay.eas.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/14
 *
 */
@Getter
@Setter
public class PaymentFeeDto {

    private BigDecimal channelFee;

    private String channel;

    private String productCode;
    private String productName;
    private String projectCode;

    private String cooperationCode;
    private String cooperationName;

}
