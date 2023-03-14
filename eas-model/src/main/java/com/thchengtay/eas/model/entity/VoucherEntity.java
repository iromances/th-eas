package com.thchengtay.eas.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/2
 *
 */
@Getter
@Setter
@TableName("voucher")
public class VoucherEntity {
    //凭证序列号
    private String seqNo;
    //批次号
    private String batchNo;
    //EAS公司编码
    private String companyNumber;
    //凭证摘要
    private String voucherAbstract;
    //凭证号
    private String voucherNumber;
    //会计期间编码
    private String periodNumber;
    //记账日期
    private String bookedDate;
    //业务日期
    private String bizDate;
    //凭证类型
    private String voucherType;
    //制单人
    private String creator;
    //分录行号
    private String entrySeq;
    //科目编码
    private String accountNumber;
    //科目名称
    private String accountName;
    //币种
    private String currencyNumber;
    //借贷方向
    private String entrydc;
    //原币金额
    private BigDecimal originalAmount = BigDecimal.ZERO;
    //借方金额
    private BigDecimal debitAmount = BigDecimal.ZERO;
    //贷方金额
    private BigDecimal creditAmount = BigDecimal.ZERO;
    //现金流量标记
    private String itemFlag;
    //对方科目分录号
    private String oppAccountSeq;
    //现金流量原币金额
    private BigDecimal cashflowAmountOriginal;
    //现金流量本位币金额
    private BigDecimal cashflowAmountLocal;

    @TableField(exist = false)
    private List<AssistAccountEntity> assistAccountList;
}
