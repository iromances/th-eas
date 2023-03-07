package com.thchengtay.eas.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Getter
@Setter
public class LoanOrderCriteria {

    //对账单出账日条件
    private LocalDate billDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
