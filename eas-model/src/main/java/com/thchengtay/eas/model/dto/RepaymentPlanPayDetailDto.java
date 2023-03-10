package com.thchengtay.eas.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/7
 *
 */
@Getter
@Setter
public class RepaymentPlanPayDetailDto {

    private String cooperationCode;

    private String cooperationName;

    private String projectNo;

    private String productCode;

    private String productName;

    private BigDecimal totalAmount;

    private BigDecimal feeAmount;


}
