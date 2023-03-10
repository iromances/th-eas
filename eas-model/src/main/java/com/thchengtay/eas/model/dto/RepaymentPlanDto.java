package com.thchengtay.eas.model.dto;

import com.thchengtay.eas.model.enums.StatementTypeEnum;
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
public class RepaymentPlanDto {

    private String cooperationCode;

    private String cooperationName;

    private String projectNo;

    private String productCode;

    private String productName;



    private BigDecimal totalAmount;

    private String subjectNo;

    private String subjectName;


}
