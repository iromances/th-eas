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
@TableName("assist_account")
public class AssistAccountEntity {

    private String seqNo; //凭证号唯一标识',
    private String asstSeq; //辅助账行号',
    private String assistAbstract;  //辅助账摘要',
    private String assistBizDate; //业务日期',
    private String assistEndDate; //到期日',
    private String cpCustomerNumber; //客户编码',
    private String cpSupplierNumber; //供应商编码',
    private String cpOrg_unitNumber; //公司编码',
    private String cpMaterialNumber; //物料编码',
    private String settlementType;  //结算方式',
    private String settlementNumber;  //结算编号',
    private String bizNumber;    //业务编号',
    private String ticketNumber;   //票证号码',
    private String invoiceNumber;    //发票号码',
    private String asstActType;   //核算项目类型',
    private String asstActNumber; //核算项目编码',
    private String asstActName;    //核算项目名称',
    private String asstActType1;   //核算项目类型',
    private String asstActNumber1; //核算项目编码',
    private String asstActName1;    //核算项目名称',
    private String asstActType2;   //核算项目类型',
    private String asstActNumber2; //核算项目编码',
    private String asstActName2;    //核算项目名称',

}
