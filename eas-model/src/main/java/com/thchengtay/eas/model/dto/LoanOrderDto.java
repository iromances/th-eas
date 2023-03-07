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
public class LoanOrderDto {

    //进件申请单号
    private String applicationNo;

    //付息模式
    private int model;

    //放款申请单号
    private String loanApplicationNo;

    private BigDecimal principal;

    private BigDecimal serviceFee;

    private BigDecimal xxtFee;

    private BigDecimal geexFee;

    private BigDecimal guaranteeFee;

    private BigDecimal otherFee;

    private String cooperationCode;

    private String cooperationName;

    private String projectCode;

    private String projectName;

    private String productCode;

    private String productName;

}
