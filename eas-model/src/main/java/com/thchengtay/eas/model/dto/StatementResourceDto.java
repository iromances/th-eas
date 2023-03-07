package com.thchengtay.eas.model.dto;

import com.thchengtay.eas.model.enums.StatementTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/7
 *
 */
@Getter
@Setter
public class StatementResourceDto {

    private StatementTypeEnum statementType;

    private String projectNo;

    private String productCode;

    private String productName;

    private String cooperationCode;

    private String cooperationName;

    private BigDecimal totalAmount;

    private String subjectNo;

    private String subjectName;


}
