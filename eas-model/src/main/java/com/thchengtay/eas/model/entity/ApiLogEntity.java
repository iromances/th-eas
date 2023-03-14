package com.thchengtay.eas.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.thchengtay.common.bo.BaseBo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/2
 *
 */
@Getter
@Setter
@TableName("api_log")
public class ApiLogEntity extends BaseBo {

    private Long pid;

    private String batchNo;

    private String requestType;

    private String url;

    private String reqestBody;

    private LocalDateTime requestTime;

    private String responseCode;

    private String responseMessage;

    private LocalDateTime responseTime;

    private String responseBody;

    private String status;

    private Integer needRetryCount;

    private Integer realRetryCount;

    private Long time;
}
