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
public class DiscountInterestDto {

    private String cooperationCode;

    private String cooperationName;

    private String productCode;

    private String productName;

    private String projectNo;

    private String projectName;

    private BigDecimal serviceFee;


}
