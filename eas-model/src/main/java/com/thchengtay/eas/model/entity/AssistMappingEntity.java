package com.thchengtay.eas.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Getter
@Setter
@TableName("assist_mapping")
public class AssistMappingEntity {

    private String sys;

    private String projectCode;

    private String customerAssistType;

    private String customerAssistCode;

    private String customerAssistName;

    private String bankAccountAssistType;

    private String bankAccountAssistCode;

    private String bankAccountAssistName;

    private String financialOrgAssistType;

    private String financialOrgAssistCode;

    private String financialOrgAssistName;

    private String supplierAssistType;

    private String supplierAssistCode;

    private String supplierAssistName;

    private String orgAssistType;

    private String OrgAssistCode;

    private String orgAssistName;

}
