package com.thchengtay.eas.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

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
public class ApiLogEntity {

    private String batchNo;
    private String requestType;
    private String url;
    private String reqestBody;
    private String requestTime;
    private String responseCode;
    private String responseMessage;
    private String responseTime;
    private String responseBody;
    private String status;
    private String needRetryCount;
    private String realRetryCount;
    private Long time;

}
