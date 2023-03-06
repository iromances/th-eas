package com.thchengtay.eas.model.dto.schedule;

import lombok.Getter;
import lombok.Setter;

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
public class VoucherExecuteParam {

    private LocalDate executeDate;



    /******************                 内部参数   **********************/
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String batchNo;


}
