package com.thchengtay.eas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thchengtay.cache.util.RedisIdWorker;
import com.thchengtay.eas.dao.VoucherMapper;
import com.thchengtay.eas.model.dto.*;
import com.thchengtay.eas.model.dto.schedule.VoucherExecuteParam;
import com.thchengtay.eas.model.entity.AssistAccountEntity;
import com.thchengtay.eas.model.entity.AssistMappingEntity;
import com.thchengtay.eas.model.entity.VoucherEntity;
import com.thchengtay.eas.model.enums.EASSubjectEnum;
import com.thchengtay.eas.service.AssistAccountService;
import com.thchengtay.eas.service.AssistMappingService;
import com.thchengtay.eas.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Slf4j
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, VoucherEntity> implements VoucherService {
    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private AssistAccountService assistAccountService;
    @Autowired
    private AssistMappingService assistMappingService;
    @Autowired
    private RedisIdWorker redisIdWorker;

    private static final String cretor = "黄丹红";
    private static final String companyNumber = "001";
    private static final String ENTRY_D = "借";
    private static final String ENTRY_C = "贷";
    private static final String VOUCHER_TYPE = "记账凭证";

    private LocalDate today;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String batchNo;

    @Transactional
    @Override
    public void importVoucher(VoucherExecuteParam voucherExecuteParam) {

        initExecuteParams(voucherExecuteParam);

        //-----------------------------放款部分-------------------------------------------
        //放款单凭证
        storageLoanOrderVoucher();
        //放款详单凭证&手续费凭证
        storageRemitOrder();

        //-----------------------------回款部分-------------------------------------------
        //线下回款总额
        storageOfflinePayment();

        //线下回款明细
        storageOnlineStatementDetail();

        //线上回款--账务部分
        storageOnlineSaps();


        //线上回款---支付部分
        storageOnlinePayment();
    }

    private void storageOnlinePayment(){
        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setStartTime(startTime);
        criteria.setEndTime(endTime);
        List<RepaymentPlanPayDetailDto> repaymentPlanPayDetailList = voucherMapper.listOnlinePayDetail(criteria);

        //合作方费用&&先行通费用
        List<RepaymentPlanPaySubjectDetailDto> repaymentPlanPaySubjectDetailList = voucherMapper.listOnlinePaySubjectDetail(criteria);


        List<VoucherEntity> voucherList = new ArrayList<>();
        List<AssistAccountEntity> assistAccountList = new ArrayList<>();
        for (RepaymentPlanPayDetailDto repaymentPlanPayDetail : repaymentPlanPayDetailList) {
            VoucherEntity statementVoucher = new VoucherEntity();
            statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            statementVoucher.setBatchNo(batchNo);
            statementVoucher.setCompanyNumber(companyNumber);
            statementVoucher.setVoucherAbstract("收款消费金融-" + repaymentPlanPayDetail.getProductName()+ "-线上还款-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            statementVoucher.setVoucherNumber("1");
            statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            statementVoucher.setBookedDate(today.toString());
            statementVoucher.setBizDate(today.plusDays(-1).toString());
            statementVoucher.setVoucherType(VOUCHER_TYPE);
            statementVoucher.setAccountNumber(EASSubjectEnum.S_101209.getValue());
            statementVoucher.setAccountName(EASSubjectEnum.S_101209.getDesc());
            statementVoucher.setCreator(cretor);
            statementVoucher.setEntrydc(ENTRY_D);
            statementVoucher.setOriginalAmount(repaymentPlanPayDetail.getTotalAmount());
            statementVoucher.setCreditAmount(statementVoucher.getOriginalAmount());
            voucherList.add(statementVoucher);

            AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(repaymentPlanPayDetail.getProjectNo());
            //辅助账
            AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
            assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

            assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
            assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
            assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

            assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
            assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
            assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());

            assistAccountList.add(assistAccountEntity);
        }

        for (RepaymentPlanPaySubjectDetailDto repaymentPlanPaySubjectDetail : repaymentPlanPaySubjectDetailList) {

            AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(repaymentPlanPaySubjectDetail.getProjectNo());


            VoucherEntity statementVoucher = new VoucherEntity();
            statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            statementVoucher.setBatchNo(batchNo);
            statementVoucher.setCompanyNumber(companyNumber);
            //收款消费金融-即科医美-线上还款-日期-合作方转付
            statementVoucher.setVoucherAbstract("收款消费金融-"
                    + repaymentPlanPaySubjectDetail.getProductName()+ "-线上还款-"
                    + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-合作方转付");
            statementVoucher.setVoucherNumber("1");
            statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            statementVoucher.setBookedDate(today.toString());
            statementVoucher.setBizDate(today.plusDays(-1).toString());
            statementVoucher.setVoucherType(VOUCHER_TYPE);
            statementVoucher.setAccountNumber(EASSubjectEnum.S_101209.getValue());
            statementVoucher.setAccountName(EASSubjectEnum.S_101209.getDesc());
            statementVoucher.setCreator(cretor);
            statementVoucher.setEntrydc(ENTRY_C);
            statementVoucher.setOriginalAmount(repaymentPlanPaySubjectDetail.getTotalAmount());
            statementVoucher.setCreditAmount(statementVoucher.getOriginalAmount());
            voucherList.add(statementVoucher);

            //辅助账
            AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
            assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

            assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
            assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
            assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

            assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
            assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
            assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());
            assistAccountList.add(assistAccountEntity);


            VoucherEntity feeVoucher = new VoucherEntity();
            feeVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            feeVoucher.setBatchNo(batchNo);
            feeVoucher.setCompanyNumber(companyNumber);
            //收款消费金融-即科医美-线上还款-日期-合作方转付
            feeVoucher.setVoucherAbstract("收款消费金融-"
                    + repaymentPlanPaySubjectDetail.getProductName()+ "-线上还款-"
                    + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-手续费");
            feeVoucher.setVoucherNumber("1");
            feeVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            feeVoucher.setBookedDate(today.toString());
            feeVoucher.setBizDate(today.plusDays(-1).toString());
            feeVoucher.setVoucherType(VOUCHER_TYPE);
            feeVoucher.setAccountNumber(EASSubjectEnum.S_22410210.getValue());
            feeVoucher.setAccountName(EASSubjectEnum.S_22410210.getDesc());
            feeVoucher.setCreator(cretor);
            feeVoucher.setEntrydc(ENTRY_C);
            feeVoucher.setOriginalAmount(repaymentPlanPaySubjectDetail.getTotalAmount());
            feeVoucher.setCreditAmount(feeVoucher.getOriginalAmount());
            voucherList.add(feeVoucher);

            //辅助账
            AssistAccountEntity feeAssistAccountEntity = new AssistAccountEntity();
            feeAssistAccountEntity.setSeqNo(feeVoucher.getSeqNo());

            feeAssistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
            feeAssistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
            feeAssistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

            feeAssistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
            feeAssistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
            feeAssistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());
            assistAccountList.add(feeAssistAccountEntity);
        }

        super.saveBatch(voucherList);
        assistAccountService.saveBatch(assistAccountList);

    }


    private void storageOnlineSaps(){
        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setBillDate(today);
        List<RepaymentPlanDto> repaymentPlanList = voucherMapper.listOnlinePayment(criteria);
        //筛选资方本金和服务费生成凭证
        List<RepaymentPlanDto> collect = repaymentPlanList.stream()
                .filter(rp -> rp.getSubjectNo().equals("P0001") || rp.getSubjectNo().equals("P0003")).collect(Collectors.toList());
        List<VoucherEntity> voucherList = new ArrayList<>();
        List<AssistAccountEntity> assistAccountList = new ArrayList<>();

        for (RepaymentPlanDto repaymentPlan : collect) {
            if (repaymentPlan.getSubjectNo().equals("P0003")){
                VoucherEntity statementVoucher = new VoucherEntity();
                statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
                statementVoucher.setBatchNo(batchNo);
                statementVoucher.setCompanyNumber(companyNumber);
                statementVoucher.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费");
                statementVoucher.setVoucherNumber("1");
                statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                statementVoucher.setBookedDate(today.toString());
                statementVoucher.setBizDate(today.plusDays(-1).toString());
                statementVoucher.setVoucherType(VOUCHER_TYPE);
                statementVoucher.setAccountNumber(EASSubjectEnum.S_1122020702.getValue());
                statementVoucher.setAccountName(EASSubjectEnum.S_1122020702.getDesc());
                statementVoucher.setCreator(cretor);
                statementVoucher.setEntrydc(ENTRY_C);
                statementVoucher.setOriginalAmount(repaymentPlan.getTotalAmount());
                statementVoucher.setCreditAmount(statementVoucher.getOriginalAmount());
                voucherList.add(statementVoucher);

                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(repaymentPlan.getProjectNo());
                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getCustomerAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getCustomerAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getCustomerAssistName());

                assistAccountEntity.setAsstActType1("项目");
                assistAccountEntity.setAsstActNumber1(repaymentPlan.getProductCode());
                assistAccountEntity.setAsstActName1(repaymentPlan.getProductName());

                assistAccountEntity.setAsstActType2(assistMapping.getOrgAssistType());
                assistAccountEntity.setAsstActNumber2(assistMapping.getOrgAssistCode());
                assistAccountEntity.setAsstActName2(assistMapping.getOrgAssistName());
                assistAccountList.add(assistAccountEntity);


                VoucherEntity serviceFeeNoVerify = new VoucherEntity();
                serviceFeeNoVerify.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeNoVerify.setBatchNo(batchNo);
                serviceFeeNoVerify.setCompanyNumber(companyNumber);
                serviceFeeNoVerify.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费");
                serviceFeeNoVerify.setVoucherNumber("1");
                serviceFeeNoVerify.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeNoVerify.setBookedDate(today.toString());
                serviceFeeNoVerify.setBizDate(today.plusDays(-1).toString());
                serviceFeeNoVerify.setVoucherType(VOUCHER_TYPE);
                serviceFeeNoVerify.setAccountNumber(EASSubjectEnum.S_22211603.getValue());
                serviceFeeNoVerify.setAccountName(EASSubjectEnum.S_22211603.getDesc());
                serviceFeeNoVerify.setCreator(cretor);
                serviceFeeNoVerify.setEntrydc(ENTRY_D);
                BigDecimal multiply = repaymentPlan.getTotalAmount().divide(new BigDecimal("1.06"), 2).multiply(new BigDecimal("0.06"));
                serviceFeeNoVerify.setOriginalAmount(multiply.setScale(2, RoundingMode.HALF_UP));
                serviceFeeNoVerify.setDebitAmount(serviceFeeNoVerify.getOriginalAmount());
                    //辅助账
                AssistAccountEntity serviceFeeNoVerifyAssistAccountEntity = new AssistAccountEntity();
                serviceFeeNoVerifyAssistAccountEntity.setSeqNo(serviceFeeNoVerify.getSeqNo());
                serviceFeeNoVerifyAssistAccountEntity.setAsstActType("项目");
                serviceFeeNoVerifyAssistAccountEntity.setAsstActNumber(repaymentPlan.getProductCode());
                serviceFeeNoVerifyAssistAccountEntity.setAsstActName(repaymentPlan.getProductName());
                assistAccountList.add(serviceFeeNoVerifyAssistAccountEntity);
                voucherList.add(serviceFeeNoVerify);

                VoucherEntity serviceFeeNoVerify2 = new VoucherEntity();
                serviceFeeNoVerify2.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeNoVerify2.setBatchNo(batchNo);
                serviceFeeNoVerify2.setCompanyNumber(companyNumber);
                serviceFeeNoVerify2.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费");
                serviceFeeNoVerify2.setVoucherNumber("1");
                serviceFeeNoVerify2.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeNoVerify2.setBookedDate(today.toString());
                serviceFeeNoVerify2.setBizDate(today.plusDays(-1).toString());
                serviceFeeNoVerify2.setVoucherType(VOUCHER_TYPE);
                serviceFeeNoVerify2.setAccountNumber(EASSubjectEnum.S_2221010214.getValue());
                serviceFeeNoVerify2.setAccountName(EASSubjectEnum.S_2221010214.getDesc());
                serviceFeeNoVerify2.setCreator(cretor);
                serviceFeeNoVerify2.setEntrydc(ENTRY_C);
                serviceFeeNoVerify2.setOriginalAmount(serviceFeeNoVerify.getOriginalAmount());
                serviceFeeNoVerify2.setCreditAmount(serviceFeeNoVerify.getOriginalAmount());
                //辅助账
                AssistAccountEntity serviceFeeNoVerify2AssistAccountEntity = new AssistAccountEntity();
                serviceFeeNoVerify2AssistAccountEntity.setSeqNo(serviceFeeNoVerify2.getSeqNo());
                serviceFeeNoVerify2AssistAccountEntity.setAsstActType("项目");
                serviceFeeNoVerify2AssistAccountEntity.setAsstActNumber(repaymentPlan.getProductCode());
                serviceFeeNoVerify2AssistAccountEntity.setAsstActName(repaymentPlan.getProductName());
                assistAccountList.add(serviceFeeNoVerify2AssistAccountEntity);
                voucherList.add(serviceFeeNoVerify2);


                //-------------------------------------收入确认部分-------------------------------------------------------
                //直接表内计算，取资方服务费
                VoucherEntity serviceFeeConfirm_1122020702 = new VoucherEntity();
                serviceFeeConfirm_1122020702.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_1122020702.setBatchNo(batchNo);
                serviceFeeConfirm_1122020702.setCompanyNumber(companyNumber);
                serviceFeeConfirm_1122020702.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费-收入确认");
                serviceFeeConfirm_1122020702.setVoucherNumber("1");
                serviceFeeConfirm_1122020702.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_1122020702.setBookedDate(today.toString());
                serviceFeeConfirm_1122020702.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_1122020702.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_1122020702.setAccountNumber(EASSubjectEnum.S_1122020702.getValue());
                serviceFeeConfirm_1122020702.setAccountName(EASSubjectEnum.S_1122020702.getDesc());
                serviceFeeConfirm_1122020702.setCreator(cretor);
                serviceFeeConfirm_1122020702.setEntrydc(ENTRY_D);
                serviceFeeConfirm_1122020702.setOriginalAmount(statementVoucher.getOriginalAmount());
                serviceFeeConfirm_1122020702.setDebitAmount(serviceFeeConfirm_1122020702.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_1122020702);

                //辅助账
                AssistAccountEntity serviceFeeConfirm_1122020702AssistAccountEntity = new AssistAccountEntity();
                serviceFeeConfirm_1122020702AssistAccountEntity.setSeqNo(serviceFeeConfirm_1122020702.getSeqNo());

                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActType(assistMapping.getCustomerAssistType());
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActNumber(assistMapping.getCustomerAssistCode());
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActName(assistMapping.getCustomerAssistName());

                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActType1("项目");
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActNumber1(repaymentPlan.getProductCode());
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActName1(repaymentPlan.getProductName());

                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActType2(assistMapping.getOrgAssistType());
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActNumber2(assistMapping.getOrgAssistCode());
                serviceFeeConfirm_1122020702AssistAccountEntity.setAsstActName2(assistMapping.getOrgAssistName());
                assistAccountList.add(assistAccountEntity);

                //应还服务费（资金方）-待转销项税额
                VoucherEntity serviceFeeConfirm_60010209 = new VoucherEntity();
                serviceFeeConfirm_60010209.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_60010209.setBatchNo(batchNo);
                serviceFeeConfirm_60010209.setCompanyNumber(companyNumber);
                serviceFeeConfirm_60010209.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费-收入确认");
                serviceFeeConfirm_60010209.setVoucherNumber("1");
                serviceFeeConfirm_60010209.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_60010209.setBookedDate(today.toString());
                serviceFeeConfirm_60010209.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_60010209.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_60010209.setAccountNumber(EASSubjectEnum.S_60010209.getValue());
                serviceFeeConfirm_60010209.setAccountName(EASSubjectEnum.S_60010209.getDesc());
                serviceFeeConfirm_60010209.setCreator(cretor);
                serviceFeeConfirm_60010209.setEntrydc(ENTRY_C);
                serviceFeeConfirm_60010209.setOriginalAmount(serviceFeeConfirm_1122020702.getOriginalAmount().subtract(serviceFeeNoVerify.getOriginalAmount()));
                serviceFeeConfirm_60010209.setCreditAmount(serviceFeeConfirm_60010209.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_60010209);

                //辅助账
                AssistAccountEntity serviceFeeConfirm_60010209AssistAccountEntity = new AssistAccountEntity();
                serviceFeeConfirm_60010209AssistAccountEntity.setSeqNo(serviceFeeConfirm_60010209.getSeqNo());

                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActType(assistMapping.getOrgAssistType());
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActNumber(assistMapping.getOrgAssistCode());
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActName(assistMapping.getOrgAssistName());

                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActType1("职员");
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActNumber1("benbu");
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActName1("通汇天津本部");

                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActType2("项目");
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActNumber2(repaymentPlan.getProductCode());
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActName2(repaymentPlan.getProductName());

                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActType3("客户");
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActNumber3(repaymentPlan.getCooperationCode());
                serviceFeeConfirm_60010209AssistAccountEntity.setAsstActName3(repaymentPlan.getCooperationName());
                assistAccountList.add(serviceFeeConfirm_60010209AssistAccountEntity);

                //取表内待转销项税额
                VoucherEntity serviceFeeConfirm_22211603 = new VoucherEntity();
                serviceFeeConfirm_22211603.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_22211603.setBatchNo(batchNo);
                serviceFeeConfirm_22211603.setCompanyNumber(companyNumber);
                serviceFeeConfirm_22211603.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-服务费-收入确认");
                serviceFeeConfirm_22211603.setVoucherNumber("1");
                serviceFeeConfirm_22211603.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_22211603.setBookedDate(today.toString());
                serviceFeeConfirm_22211603.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_22211603.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_22211603.setAccountNumber(EASSubjectEnum.S_22211603.getValue());
                serviceFeeConfirm_22211603.setAccountName(EASSubjectEnum.S_22211603.getDesc());
                serviceFeeConfirm_22211603.setCreator(cretor);
                serviceFeeConfirm_22211603.setEntrydc(ENTRY_C);
                serviceFeeConfirm_22211603.setOriginalAmount(serviceFeeNoVerify.getOriginalAmount());
                serviceFeeConfirm_22211603.setCreditAmount(serviceFeeConfirm_22211603.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_22211603);

                //辅助账
                AssistAccountEntity serviceFeeConfirm_22211603AssistAccountEntity = new AssistAccountEntity();
                serviceFeeConfirm_22211603AssistAccountEntity.setSeqNo(serviceFeeConfirm_22211603.getSeqNo());

                serviceFeeConfirm_22211603AssistAccountEntity.setAsstActType("项目");
                serviceFeeConfirm_22211603AssistAccountEntity.setAsstActNumber(repaymentPlan.getProductCode());
                serviceFeeConfirm_22211603AssistAccountEntity.setAsstActName(repaymentPlan.getProductName());

                assistAccountList.add(assistAccountEntity);

            }else {

                VoucherEntity statementVoucher = new VoucherEntity();
                statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
                statementVoucher.setBatchNo(batchNo);
                statementVoucher.setCompanyNumber(companyNumber);
                statementVoucher.setVoucherAbstract("收款消费金融-" + repaymentPlan.getProductName()+ "-线上还款-本金");
                statementVoucher.setVoucherNumber("1");
                statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                statementVoucher.setBookedDate(today.toString());
                statementVoucher.setBizDate(today.plusDays(-1).toString());
                statementVoucher.setVoucherType(VOUCHER_TYPE);
                statementVoucher.setAccountNumber(EASSubjectEnum.S_1122020701.getValue());
                statementVoucher.setAccountName(EASSubjectEnum.S_1122020701.getDesc());
                statementVoucher.setCreator(cretor);
                statementVoucher.setEntrydc(ENTRY_C);
                statementVoucher.setOriginalAmount(repaymentPlan.getTotalAmount());
                statementVoucher.setCreditAmount(statementVoucher.getOriginalAmount());
                voucherList.add(statementVoucher);

                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(repaymentPlan.getProjectNo());
                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getCustomerAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getCustomerAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getCustomerAssistName());

                assistAccountEntity.setAsstActType1("项目");
                assistAccountEntity.setAsstActNumber1(repaymentPlan.getProductCode());
                assistAccountEntity.setAsstActName1(repaymentPlan.getProductName());

                assistAccountEntity.setAsstActType2(assistMapping.getOrgAssistType());
                assistAccountEntity.setAsstActNumber2(assistMapping.getOrgAssistCode());
                assistAccountEntity.setAsstActName2(assistMapping.getOrgAssistName());
                assistAccountList.add(assistAccountEntity);
            }
        }

        //全民钱包不走我司线上支付，根据账单以及账单的结算数据单独生成支付凭证
        List<RepaymentPlanDto> qmqbList = repaymentPlanList.stream()
                .filter(rp -> rp.getCooperationCode().equals("HZF000038") && (rp.getSubjectNo().equals("P0001") || rp.getSubjectNo().equals("P0003"))).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(qmqbList)){

            Map<String, List<RepaymentPlanDto>> groupByProduct = qmqbList.stream().collect(Collectors.groupingBy(RepaymentPlanDto::getProductCode));
            groupByProduct.forEach((qk,qv)->{
                RepaymentPlanDto qmqb = qv.get(0);

                VoucherEntity statementVoucher = new VoucherEntity();
                statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
                statementVoucher.setBatchNo(batchNo);
                statementVoucher.setCompanyNumber(companyNumber);
                //收款消费金融-合作方名称-线上还款-日期
                statementVoucher.setVoucherAbstract("收款消费金融-" + qmqb.getProductName()+ "-线上还款-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) );
                statementVoucher.setVoucherNumber("1");
                statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                statementVoucher.setBookedDate(today.toString());
                statementVoucher.setBizDate(today.plusDays(-1).toString());
                statementVoucher.setVoucherType(VOUCHER_TYPE);
                statementVoucher.setAccountNumber(EASSubjectEnum.S_101209.getValue());
                statementVoucher.setAccountName(EASSubjectEnum.S_101209.getDesc());
                statementVoucher.setCreator(cretor);
                statementVoucher.setEntrydc(ENTRY_D);
                for (RepaymentPlanDto repaymentPlanDto : qv) {
                    statementVoucher.setOriginalAmount(statementVoucher.getOriginalAmount().add(repaymentPlanDto.getTotalAmount()));
                }
                statementVoucher.setDebitAmount(statementVoucher.getOriginalAmount());
                voucherList.add(statementVoucher);

                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(qmqb.getProjectNo());
                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

                assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
                assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
                assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());
                assistAccountList.add(assistAccountEntity);
            });
        }

        super.saveBatch(voucherList);
        assistAccountService.saveBatch(assistAccountList);

    }

    private void storageOnlineStatementDetail(){
        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setBillDate(today);
        List<StatementResourceDto> statementList = voucherMapper.listOfflineStatementDetail(criteria);

        Map<String, List<StatementResourceDto>> groupStatementResources = statementList
                .stream().collect(Collectors.groupingBy(StatementResourceDto -> StatementResourceDto.getCooperationCode() + "-" + StatementResourceDto.getStatementType().getDesc()  + "-" + StatementResourceDto.getSubjectNo()));

        List<VoucherEntity> voucherList = new ArrayList<>();
        List<AssistAccountEntity> assistAccountList = new ArrayList<>();

        groupStatementResources.forEach((k,v)->{

            StatementResourceDto firstStatementResource = v.get(0);

            VoucherEntity statementVoucher = new VoucherEntity();
            statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            statementVoucher.setBatchNo(batchNo);
            statementVoucher.setCompanyNumber(companyNumber);
            statementVoucher.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName()+ "-" + firstStatementResource.getStatementType().getDesc()  + "-" + firstStatementResource.getSubjectName());
            statementVoucher.setVoucherNumber("1");
            statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            statementVoucher.setBookedDate(today.toString());
            statementVoucher.setBizDate(today.plusDays(-1).toString());
            statementVoucher.setVoucherType(VOUCHER_TYPE);
            statementVoucher.setAccountNumber(EASSubjectEnum.S_1122020701.getValue());
            statementVoucher.setAccountName(EASSubjectEnum.S_1122020701.getDesc());
            statementVoucher.setCreator(cretor);
            statementVoucher.setEntrydc(ENTRY_C);
            for (StatementResourceDto statementResource : v) {
                statementVoucher.setOriginalAmount(statementVoucher.getOriginalAmount().add(statementResource.getTotalAmount()));
            }
            statementVoucher.setCreditAmount(statementVoucher.getOriginalAmount());
            voucherList.add(statementVoucher);


            Map<String, List<StatementResourceDto>> groupByProductCode = v.stream().collect(Collectors.groupingBy(StatementResourceDto::getProductCode));
            groupByProductCode.forEach((pk,pv)->{
                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(pv.get(0).getProjectNo());
                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getCustomerAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getCustomerAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getCustomerAssistName());

                assistAccountEntity.setAsstActType1("项目");
                assistAccountEntity.setAsstActNumber1(pv.get(0).getProductCode());
                assistAccountEntity.setAsstActName1(pv.get(0).getProductName());

                assistAccountEntity.setAsstActType2(assistMapping.getOrgAssistType());
                assistAccountEntity.setAsstActNumber2(assistMapping.getOrgAssistCode());
                assistAccountEntity.setAsstActName2(assistMapping.getOrgAssistName());
                assistAccountList.add(assistAccountEntity);
            });


            //------------------------------  收款消费金融-合作方名称-对账单名称-服务费  -------------------------------------
            if (firstStatementResource.getSubjectNo().equals("P0003")){
                Map<String, List<StatementResourceDto>> confirmGroupByProduct = v.stream().collect(Collectors.groupingBy(StatementResourceDto::getProductCode));
                //待转销项税额
                VoucherEntity serviceFeeNoVerify = new VoucherEntity();
                serviceFeeNoVerify.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeNoVerify.setBatchNo(batchNo);
                serviceFeeNoVerify.setCompanyNumber(companyNumber);
                serviceFeeNoVerify.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName() + "-" + firstStatementResource.getStatementType().getDesc() + "-服务费");
                serviceFeeNoVerify.setVoucherNumber("1");
                serviceFeeNoVerify.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeNoVerify.setBookedDate(today.toString());
                serviceFeeNoVerify.setBizDate(today.plusDays(-1).toString());
                serviceFeeNoVerify.setVoucherType(VOUCHER_TYPE);
                serviceFeeNoVerify.setAccountNumber(EASSubjectEnum.S_22211603.getValue());
                serviceFeeNoVerify.setAccountName(EASSubjectEnum.S_22211603.getDesc());
                serviceFeeNoVerify.setCreator(cretor);
                serviceFeeNoVerify.setEntrydc(ENTRY_D);
                BigDecimal multiply = firstStatementResource.getTotalAmount().divide(new BigDecimal("1.06"), 2).multiply(new BigDecimal("0.06"));
                serviceFeeNoVerify.setOriginalAmount(multiply.setScale(2, RoundingMode.HALF_UP));
                serviceFeeNoVerify.setDebitAmount(serviceFeeNoVerify.getOriginalAmount());
                confirmGroupByProduct.forEach((cpk, cpv)->{
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(serviceFeeNoVerify.getSeqNo());
                    assistAccountEntity.setAsstActType("项目");
                    assistAccountEntity.setAsstActNumber(cpv.get(0).getProductCode());
                    assistAccountEntity.setAsstActName(cpv.get(0).getProductName());
                    assistAccountList.add(assistAccountEntity);
                });
                voucherList.add(serviceFeeNoVerify);

                VoucherEntity serviceFeeNoVerify2 = new VoucherEntity();
                serviceFeeNoVerify2.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeNoVerify2.setBatchNo(batchNo);
                serviceFeeNoVerify2.setCompanyNumber(companyNumber);
                serviceFeeNoVerify2.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName() + "-" + firstStatementResource.getStatementType().getDesc() + "-服务费");
                serviceFeeNoVerify2.setVoucherNumber("1");
                serviceFeeNoVerify2.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeNoVerify2.setBookedDate(today.toString());
                serviceFeeNoVerify2.setBizDate(today.plusDays(-1).toString());
                serviceFeeNoVerify2.setVoucherType(VOUCHER_TYPE);
                serviceFeeNoVerify2.setAccountNumber(EASSubjectEnum.S_2221010214.getValue());
                serviceFeeNoVerify2.setAccountName(EASSubjectEnum.S_2221010214.getDesc());
                serviceFeeNoVerify2.setCreator(cretor);
                serviceFeeNoVerify2.setEntrydc(ENTRY_C);
                serviceFeeNoVerify2.setOriginalAmount(serviceFeeNoVerify.getOriginalAmount());
                serviceFeeNoVerify2.setCreditAmount(serviceFeeNoVerify.getOriginalAmount());
                confirmGroupByProduct.forEach((cpk, cpv)->{
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(serviceFeeNoVerify2.getSeqNo());
                    assistAccountEntity.setAsstActType("项目");
                    assistAccountEntity.setAsstActNumber(cpv.get(0).getProductCode());
                    assistAccountEntity.setAsstActName(cpv.get(0).getProductName());
                    assistAccountList.add(assistAccountEntity);
                });
                voucherList.add(serviceFeeNoVerify2);




                //-------------------------------------收入确认部分-------------------------------------------------------
                //直接表内计算，取资方服务费
                VoucherEntity serviceFeeConfirm_1122020702 = new VoucherEntity();
                serviceFeeConfirm_1122020702.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_1122020702.setBatchNo(batchNo);
                serviceFeeConfirm_1122020702.setCompanyNumber(companyNumber);
                serviceFeeConfirm_1122020702.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName() + "-" + firstStatementResource.getStatementType().getDesc() + "-服务费-收入确认");
                serviceFeeConfirm_1122020702.setVoucherNumber("1");
                serviceFeeConfirm_1122020702.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_1122020702.setBookedDate(today.toString());
                serviceFeeConfirm_1122020702.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_1122020702.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_1122020702.setAccountNumber(EASSubjectEnum.S_1122020702.getValue());
                serviceFeeConfirm_1122020702.setAccountName(EASSubjectEnum.S_1122020702.getDesc());
                serviceFeeConfirm_1122020702.setCreator(cretor);
                serviceFeeConfirm_1122020702.setEntrydc(ENTRY_D);
                serviceFeeConfirm_1122020702.setOriginalAmount(statementVoucher.getOriginalAmount());
                serviceFeeConfirm_1122020702.setDebitAmount(serviceFeeConfirm_1122020702.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_1122020702);

                confirmGroupByProduct.forEach((cpk, cpv)->{
                    AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(cpv.get(0).getProjectNo());
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(serviceFeeConfirm_1122020702.getSeqNo());

                    assistAccountEntity.setAsstActType(assistMapping.getCustomerAssistType());
                    assistAccountEntity.setAsstActNumber(assistMapping.getCustomerAssistCode());
                    assistAccountEntity.setAsstActName(assistMapping.getCustomerAssistName());

                    assistAccountEntity.setAsstActType1("项目");
                    assistAccountEntity.setAsstActNumber1(cpv.get(0).getProductCode());
                    assistAccountEntity.setAsstActName1(cpv.get(0).getProductName());

                    assistAccountEntity.setAsstActType2(assistMapping.getOrgAssistType());
                    assistAccountEntity.setAsstActNumber2(assistMapping.getOrgAssistCode());
                    assistAccountEntity.setAsstActName2(assistMapping.getOrgAssistName());
                    assistAccountList.add(assistAccountEntity);
                });

                //应还服务费（资金方）-待转销项税额
                VoucherEntity serviceFeeConfirm_60010209 = new VoucherEntity();
                serviceFeeConfirm_60010209.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_60010209.setBatchNo(batchNo);
                serviceFeeConfirm_60010209.setCompanyNumber(companyNumber);
                serviceFeeConfirm_60010209.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName() + "-" + firstStatementResource.getStatementType().getDesc() + "-服务费-收入确认");
                serviceFeeConfirm_60010209.setVoucherNumber("1");
                serviceFeeConfirm_60010209.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_60010209.setBookedDate(today.toString());
                serviceFeeConfirm_60010209.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_60010209.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_60010209.setAccountNumber(EASSubjectEnum.S_60010209.getValue());
                serviceFeeConfirm_60010209.setAccountName(EASSubjectEnum.S_60010209.getDesc());
                serviceFeeConfirm_60010209.setCreator(cretor);
                serviceFeeConfirm_60010209.setEntrydc(ENTRY_C);
                serviceFeeConfirm_60010209.setOriginalAmount(serviceFeeConfirm_1122020702.getOriginalAmount().subtract(serviceFeeNoVerify.getOriginalAmount()));
                serviceFeeConfirm_60010209.setCreditAmount(serviceFeeConfirm_60010209.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_60010209);
                confirmGroupByProduct.forEach((cpk, cpv)->{
                    AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(cpv.get(0).getProjectNo());
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(serviceFeeConfirm_60010209.getSeqNo());

                    assistAccountEntity.setAsstActType(assistMapping.getOrgAssistType());
                    assistAccountEntity.setAsstActNumber(assistMapping.getOrgAssistCode());
                    assistAccountEntity.setAsstActName(assistMapping.getOrgAssistName());

                    assistAccountEntity.setAsstActType1("职员");
                    assistAccountEntity.setAsstActNumber1("benbu");
                    assistAccountEntity.setAsstActName1("通汇天津本部");

                    assistAccountEntity.setAsstActType2("项目");
                    assistAccountEntity.setAsstActNumber2(cpv.get(0).getProductCode());
                    assistAccountEntity.setAsstActName2(cpv.get(0).getProductName());

                    assistAccountEntity.setAsstActType3("客户");
                    assistAccountEntity.setAsstActNumber3(cpv.get(0).getCooperationCode());
                    assistAccountEntity.setAsstActName3(cpv.get(0).getCooperationName());
                    assistAccountList.add(assistAccountEntity);
                });

                //取表内待转销项税额
                VoucherEntity serviceFeeConfirm_22211603 = new VoucherEntity();
                serviceFeeConfirm_22211603.setSeqNo(redisIdWorker.nextSecondId4(""));
                serviceFeeConfirm_22211603.setBatchNo(batchNo);
                serviceFeeConfirm_22211603.setCompanyNumber(companyNumber);
                serviceFeeConfirm_22211603.setVoucherAbstract("收款消费金融-" + firstStatementResource.getCooperationName() + "-" + firstStatementResource.getStatementType().getDesc() + "-服务费-收入确认");
                serviceFeeConfirm_22211603.setVoucherNumber("1");
                serviceFeeConfirm_22211603.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                serviceFeeConfirm_22211603.setBookedDate(today.toString());
                serviceFeeConfirm_22211603.setBizDate(today.plusDays(-1).toString());
                serviceFeeConfirm_22211603.setVoucherType(VOUCHER_TYPE);
                serviceFeeConfirm_22211603.setAccountNumber(EASSubjectEnum.S_22211603.getValue());
                serviceFeeConfirm_22211603.setAccountName(EASSubjectEnum.S_22211603.getDesc());
                serviceFeeConfirm_22211603.setCreator(cretor);
                serviceFeeConfirm_22211603.setEntrydc(ENTRY_C);
                serviceFeeConfirm_22211603.setOriginalAmount(serviceFeeNoVerify.getOriginalAmount());
                serviceFeeConfirm_22211603.setCreditAmount(serviceFeeConfirm_22211603.getOriginalAmount());
                voucherList.add(serviceFeeConfirm_22211603);

                confirmGroupByProduct.forEach((cpk, cpv)->{
                    AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(cpv.get(0).getProjectNo());
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(serviceFeeConfirm_22211603.getSeqNo());

                    assistAccountEntity.setAsstActType("项目");
                    assistAccountEntity.setAsstActNumber(cpv.get(0).getProductCode());
                    assistAccountEntity.setAsstActName(cpv.get(0).getProductName());

                    assistAccountList.add(assistAccountEntity);
                });
            }
        });
    }


    private void storageOfflinePayment(){
        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setBillDate(today);
        List<StatementDto> statementList = voucherMapper.listOfflineStatement(criteria);

        Map<String, List<StatementDto>> groupByCooperation = statementList
                .stream().collect(Collectors.groupingBy(statementDto -> statementDto.getCooperationCode() + "-" + statementDto.getStatementType().getDesc()));

        List<VoucherEntity> voucherList = new ArrayList<>();
        List<AssistAccountEntity> assistAccountList = new ArrayList<>();

        groupByCooperation.forEach((k,v)->{

            StatementDto firstStatement = v.get(0);
            VoucherEntity statementVoucher = new VoucherEntity();
            statementVoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            statementVoucher.setBatchNo(batchNo);
            statementVoucher.setCompanyNumber(companyNumber);
            statementVoucher.setVoucherAbstract("收款消费金融-" + firstStatement.getCooperationName()+ "-" + firstStatement.getStatementType().getDesc()  + "-" + today);
            statementVoucher.setVoucherNumber("1");
            statementVoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            statementVoucher.setBookedDate(today.toString());
            statementVoucher.setBizDate(today.plusDays(-1).toString());
            statementVoucher.setVoucherType(VOUCHER_TYPE);
            statementVoucher.setAccountNumber(EASSubjectEnum.S_100201.getValue());
            statementVoucher.setAccountName(EASSubjectEnum.S_100201.getDesc());
            statementVoucher.setCreator(cretor);
            statementVoucher.setEntrydc(ENTRY_D);
            for (StatementDto statementDto : v) {
                statementVoucher.setOriginalAmount(statementVoucher.getOriginalAmount().add(statementDto.getTotalAmount()));
            }
            statementVoucher.setDebitAmount(statementVoucher.getOriginalAmount());
            voucherList.add(statementVoucher);


            Map<String, List<StatementDto>> groupByProjectNo = v.stream().collect(Collectors.groupingBy(StatementDto::getProjectNo));
            groupByProjectNo.forEach((pk,pv)->{

                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(pk);

                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(statementVoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

                assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
                assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
                assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());
                assistAccountList.add(assistAccountEntity);
            });
        });
    }


    private void storageRemitOrder(){

        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setStartTime(startTime);
        criteria.setEndTime(endTime);
        List<LoanPaymentDto> loanPaymentList = voucherMapper.listLoanPayment(criteria);

        Map<String, List<LoanPaymentDto>> groupByCooperationCode = loanPaymentList.stream().collect(Collectors.groupingBy(LoanPaymentDto::getCooperationCode));

        List<AssistAccountEntity> assistAccountList = new ArrayList<>();
        List<VoucherEntity> voucherList = new ArrayList<>();
        groupByCooperationCode.forEach((k,v)->{

            //放款手续费凭证
            remitOrderFeeVoucher(v, assistAccountList, voucherList);

            //商户&担保方放款订单凭证
            remitOrderDetailVoucher(v, assistAccountList, voucherList);

        });

        super.saveBatch(voucherList);
        assistAccountService.saveBatch(assistAccountList);
    }



    /***
     *  放款至商户&担保方凭证
     * @param loanPaymentList
     * @param assistAccountList
     * @param voucherList
     */
    private void remitOrderDetailVoucher(List<LoanPaymentDto> loanPaymentList,
                                      List<AssistAccountEntity> assistAccountList,
                                      List<VoucherEntity> voucherList){

        LoanPaymentDto loanPayment = loanPaymentList.get(0);

        //放款详单拆分担保方  &  商户
        List<LoanPaymentDto> shList = new ArrayList<>();
        List<LoanPaymentDto> dbfList = new ArrayList<>();
        for (LoanPaymentDto value : loanPaymentList) {
            if (value.getAccType() == 1){
                shList.add(value);
            }else {
                dbfList.add(value);
            }
        }

        if (CollectionUtils.isNotEmpty(shList)){
            VoucherEntity shvoucher = new VoucherEntity();
            shvoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            shvoucher.setBatchNo(batchNo);
            shvoucher.setCompanyNumber(companyNumber);
            shvoucher.setVoucherAbstract("支付消费金融-" + loanPayment.getCooperationName() + "-" + today);
            shvoucher.setVoucherNumber("1");
            shvoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            shvoucher.setBookedDate(today.toString());
            shvoucher.setBizDate(today.plusDays(-1).toString());
            shvoucher.setVoucherType(VOUCHER_TYPE);
            shvoucher.setAccountNumber(EASSubjectEnum.S_101208.getValue());
            shvoucher.setAccountName(EASSubjectEnum.S_101208.getDesc());
            shvoucher.setCreator(cretor);
            shvoucher.setEntrydc(ENTRY_C);
            for (LoanPaymentDto shloanPayment : shList) {
                shvoucher.setOriginalAmount(shvoucher.getOriginalAmount().add(shloanPayment.getRealPaymentAmount()));
                shvoucher.setDebitAmount(shvoucher.getOriginalAmount());
                shvoucher.setCreditAmount(BigDecimal.ZERO);
            }
            voucherList.add(shvoucher);

            Map<String, List<LoanPaymentDto>> groupByProjectCode = shList.stream().collect(Collectors.groupingBy(LoanPaymentDto::getProjectCode));
            groupByProjectCode.forEach((gk,gv)->{

                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(gk);

                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(shvoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

                assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
                assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
                assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());

                assistAccountList.add(assistAccountEntity);
            });
        }

        if (CollectionUtils.isNotEmpty(dbfList)){
            VoucherEntity dbfvoucher = new VoucherEntity();
            dbfvoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
            dbfvoucher.setBatchNo(batchNo);
            dbfvoucher.setCompanyNumber(companyNumber);
            dbfvoucher.setVoucherAbstract("支付消费金融-" + loanPayment.getCooperationName() + today);
            dbfvoucher.setVoucherNumber("1");
            dbfvoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
            dbfvoucher.setBookedDate(today.toString());
            dbfvoucher.setBizDate(today.plusDays(-1).toString());
            dbfvoucher.setVoucherType(VOUCHER_TYPE);
            dbfvoucher.setAccountNumber(EASSubjectEnum.S_101208.getValue());
            dbfvoucher.setAccountName(EASSubjectEnum.S_101208.getDesc());
            dbfvoucher.setCreator(cretor);
            dbfvoucher.setEntrydc(ENTRY_C);
            for (LoanPaymentDto dbfloanPayment : dbfList) {
                dbfvoucher.setOriginalAmount(dbfvoucher.getOriginalAmount().add(dbfloanPayment.getRealPaymentAmount()));
                dbfvoucher.setDebitAmount(dbfvoucher.getOriginalAmount());
                dbfvoucher.setCreditAmount(BigDecimal.ZERO);
            }
            voucherList.add(dbfvoucher);

            Map<String, List<LoanPaymentDto>> groupByProjectCode = dbfList.stream().collect(Collectors.groupingBy(LoanPaymentDto::getProjectCode));
            groupByProjectCode.forEach((gk,gv)->{
                AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(gk);

                //辅助账
                AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                assistAccountEntity.setSeqNo(dbfvoucher.getSeqNo());

                assistAccountEntity.setAsstActType(assistMapping.getBankAccountAssistType());
                assistAccountEntity.setAsstActNumber(assistMapping.getBankAccountAssistCode());
                assistAccountEntity.setAsstActName(assistMapping.getBankAccountAssistName());

                assistAccountEntity.setAsstActType1(assistMapping.getFinancialOrgAssistType());
                assistAccountEntity.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
                assistAccountEntity.setAsstActName1(assistMapping.getFinancialOrgAssistName());

                assistAccountList.add(assistAccountEntity);
            });
        }
    }

    /***
     *  放款手续费凭证
     * @param loanPaymentList
     * @param assistAccountList
     * @param voucherList
     */
    private void remitOrderFeeVoucher(List<LoanPaymentDto> loanPaymentList,
                                      List<AssistAccountEntity> assistAccountList,
                                      List<VoucherEntity> voucherList){
        LoanPaymentDto loanPayment = loanPaymentList.get(0);

        VoucherEntity feevoucher = new VoucherEntity();
        feevoucher.setSeqNo(redisIdWorker.nextSecondId4(""));
        feevoucher.setBatchNo(batchNo);
        feevoucher.setCompanyNumber(companyNumber);
        feevoucher.setVoucherAbstract("支付消费金融-" + loanPayment.getCooperationName()+ "-手续费-" + today);
        feevoucher.setVoucherNumber("1");
        feevoucher.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        feevoucher.setBookedDate(today.toString());
        feevoucher.setBizDate(today.plusDays(-1).toString());
        feevoucher.setVoucherType(VOUCHER_TYPE);
        feevoucher.setAccountNumber(EASSubjectEnum.S_1123020201.getValue());
        feevoucher.setAccountName(EASSubjectEnum.S_1123020201.getDesc());
        feevoucher.setCreator(cretor);
        feevoucher.setEntrydc(ENTRY_D);
        for (LoanPaymentDto feeLoanPayment : loanPaymentList) {
            feevoucher.setOriginalAmount(feevoucher.getOriginalAmount().add(feeLoanPayment.getFee()));
        }
        feevoucher.setDebitAmount(feevoucher.getOriginalAmount());
        voucherList.add(feevoucher);

        VoucherEntity feevoucher2 = new VoucherEntity();
        feevoucher2.setSeqNo(redisIdWorker.nextSecondId4(""));
        feevoucher2.setBatchNo(batchNo);
        feevoucher2.setCompanyNumber(companyNumber);
        feevoucher2.setVoucherAbstract("支付消费金融-" + loanPayment.getCooperationName() + "-手续费-" + today);
        feevoucher2.setVoucherNumber("1");
        feevoucher2.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        feevoucher2.setBookedDate(today.toString());
        feevoucher2.setBizDate(today.plusDays(-1).toString());
        feevoucher2.setVoucherType(VOUCHER_TYPE);
        feevoucher2.setAccountNumber(EASSubjectEnum.S_101208.getValue());
        feevoucher2.setAccountName(EASSubjectEnum.S_101208.getDesc());
        feevoucher2.setCreator(cretor);
        feevoucher2.setEntrydc(ENTRY_C);
        feevoucher2.setOriginalAmount(feevoucher.getOriginalAmount());
        feevoucher2.setCreditAmount(feevoucher.getOriginalAmount());
        voucherList.add(feevoucher2);

        Map<String, List<LoanPaymentDto>> collect = loanPaymentList.stream().collect(Collectors.groupingBy(LoanPaymentDto::getProjectCode));
        collect.forEach((pk,pv)->{
            AssistMappingEntity assistMapping = assistMappingService.getByProjectCode(pk);
            //辅助账
            AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
            assistAccountEntity.setSeqNo(feevoucher.getSeqNo());

            assistAccountEntity.setAsstActType(assistMapping.getOrgAssistType());
            assistAccountEntity.setAsstActNumber(assistMapping.getOrgAssistCode());
            assistAccountEntity.setAsstActName(assistMapping.getOrgAssistName());

            assistAccountEntity.setAsstActType1(assistMapping.getSupplierAssistType());
            assistAccountEntity.setAsstActNumber1(assistMapping.getSupplierAssistCode());
            assistAccountEntity.setAsstActName1(assistMapping.getSupplierAssistName());
            assistAccountList.add(assistAccountEntity);

            //辅助账
            AssistAccountEntity assistAccountEntity2 = new AssistAccountEntity();
            assistAccountEntity2.setSeqNo(feevoucher2.getSeqNo());

            assistAccountEntity2.setAsstActType(assistMapping.getBankAccountAssistType());
            assistAccountEntity2.setAsstActNumber(assistMapping.getBankAccountAssistCode());
            assistAccountEntity2.setAsstActName(assistMapping.getBankAccountAssistName());

            assistAccountEntity2.setAsstActType1(assistMapping.getFinancialOrgAssistType());
            assistAccountEntity2.setAsstActNumber1(assistMapping.getFinancialOrgAssistCode());
            assistAccountEntity2.setAsstActName1(assistMapping.getFinancialOrgAssistName());
            assistAccountList.add(assistAccountEntity2);
        });
    }


    private void storageLoanOrderVoucher(){
        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setStartTime(startTime);
        criteria.setEndTime(endTime);
        List<LoanOrderDto> loanOrderList = voucherMapper.listLoanOrderVoucher(criteria);

        List<AssistAccountEntity> assistAccountList = new ArrayList<>();
        List<VoucherEntity> voucherList = new ArrayList<>();
        Map<String, List<LoanOrderDto>> groupListByCooperationCode = loanOrderList.stream().collect(Collectors.groupingBy(LoanOrderDto::getCooperationCode));
        Iterator<Map.Entry<String, List<LoanOrderDto>>> iterator = groupListByCooperationCode.entrySet().iterator();
        int i  = 0;
        while (iterator.hasNext()){
            Map.Entry<String, List<LoanOrderDto>> next = iterator.next();
            //同一合作方编号集合、不同产品、不同付息模式
            List<LoanOrderDto> values = next.getValue();
            //同一客户根据贴息模式分组处理，一笔放款的模式一组，多笔放款的一组
            List<LoanOrderDto> txList = new ArrayList<>();
            List<LoanOrderDto> fxList = new ArrayList<>();
            for (LoanOrderDto value : values) {
                if (value.getModel() == 2){
                    fxList.add(value);
                }else {
                    txList.add(value);
                }
            }
            LoanOrderDto loanOrder = values.get(0);

            txLoanOrderVoucher(txList, voucherList, assistAccountList);

            if (CollectionUtils.isNotEmpty(fxList)){
                VoucherEntity fxvoucherEntity = new VoucherEntity();
                fxvoucherEntity.setSeqNo(redisIdWorker.nextSecondId4(""));
                fxvoucherEntity.setBatchNo(batchNo);
                fxvoucherEntity.setCompanyNumber(companyNumber);
                fxvoucherEntity.setVoucherAbstract("支付消费金融-" + loanOrder.getCooperationName() + "-"  + today);
                fxvoucherEntity.setVoucherNumber(i++ + "");
                fxvoucherEntity.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
                fxvoucherEntity.setBookedDate(today.toString());
                fxvoucherEntity.setBizDate(today.plusDays(-1).toString());
                fxvoucherEntity.setVoucherType(VOUCHER_TYPE);
                fxvoucherEntity.setAccountNumber(EASSubjectEnum.S_1122020701.getValue());
                fxvoucherEntity.setAccountName(EASSubjectEnum.S_1122020701.getDesc());
                fxvoucherEntity.setCreator(cretor);
                fxvoucherEntity.setEntrydc(ENTRY_D);
                for (LoanOrderDto fxLoanOrder : fxList) {
                    fxvoucherEntity.setOriginalAmount(fxvoucherEntity.getOriginalAmount().add(fxLoanOrder.getPrincipal()));
                    fxvoucherEntity.setDebitAmount(fxvoucherEntity.getOriginalAmount());
                    fxvoucherEntity.setCreditAmount(BigDecimal.ZERO);
                }
                voucherList.add(fxvoucherEntity);

                Map<String, List<LoanOrderDto>> fxGropuByProductCode = fxList.stream().collect(Collectors.groupingBy(LoanOrderDto::getProjectCode));
                fxGropuByProductCode.forEach((fk,fv)->{
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(fxvoucherEntity.getSeqNo());

                    assistAccountEntity.setAsstActType("客户");
                    assistAccountEntity.setAsstActNumber(loanOrder.getCooperationCode());
                    assistAccountEntity.setAsstActName(loanOrder.getCooperationName());

                    assistAccountEntity.setAsstActType1("项目");
                    assistAccountEntity.setAsstActNumber1(loanOrder.getProjectCode());
                    assistAccountEntity.setAsstActName1(loanOrder.getProjectName());

                    assistAccountEntity.setAsstActType2("行政组织");
                    assistAccountEntity.setAsstActNumber2("001000");
                    assistAccountEntity.setAsstActName2("通汇诚泰本部");
                    assistAccountList.add(assistAccountEntity);
                });
            }
        }

        super.saveBatch(voucherList);
        assistAccountService.saveBatch(assistAccountList);
    }


    /***
     *  贴息放款凭证
     *  1.本金凭证
     *  2.资金方服务费凭证
     *  3.应交税费-待转销项税额-消费金融
     *  4.应交税费-应交增值税-内贸及保理业务-销项税额（消费金融）
     * @param txList
     * @param assistAccountList
     * @param voucherList
     */
    private void txLoanOrderVoucher(List<LoanOrderDto> txList,
                                    List<VoucherEntity> voucherList,
                                    List<AssistAccountEntity> assistAccountList){

        if (CollectionUtils.isEmpty(txList)){
            return;
        }

        LoanOrderDto loanOrder = txList.get(0);

        VoucherEntity txPrincipalVoucherEntity = new VoucherEntity();
        txPrincipalVoucherEntity.setSeqNo(redisIdWorker.nextSecondId4(""));
        txPrincipalVoucherEntity.setBatchNo(batchNo);
        txPrincipalVoucherEntity.setCompanyNumber(companyNumber);
        txPrincipalVoucherEntity.setVoucherAbstract("支付消费金融-" + loanOrder.getCooperationName() +"-" + today);
        txPrincipalVoucherEntity.setVoucherNumber("1");
        txPrincipalVoucherEntity.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        txPrincipalVoucherEntity.setBookedDate(today.toString());
        txPrincipalVoucherEntity.setBizDate(today.plusDays(-1).toString());
        txPrincipalVoucherEntity.setVoucherType(VOUCHER_TYPE);
        txPrincipalVoucherEntity.setAccountNumber(EASSubjectEnum.S_1122020701.getValue());
        txPrincipalVoucherEntity.setAccountName(EASSubjectEnum.S_1122020701.getDesc());
        txPrincipalVoucherEntity.setCreator(cretor);
        txPrincipalVoucherEntity.setEntrydc(ENTRY_D);

        VoucherEntity serviceFeeVoucherEntity = new VoucherEntity();
        serviceFeeVoucherEntity.setSeqNo(redisIdWorker.nextSecondId4(""));
        serviceFeeVoucherEntity.setBatchNo(batchNo);
        serviceFeeVoucherEntity.setCompanyNumber(companyNumber);
        serviceFeeVoucherEntity.setVoucherAbstract("支付消费金融-" + loanOrder.getCooperationName() + "-"  + today);
        serviceFeeVoucherEntity.setVoucherNumber("1");
        serviceFeeVoucherEntity.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        serviceFeeVoucherEntity.setBookedDate(today.toString());
        serviceFeeVoucherEntity.setBizDate(today.plusDays(-1).toString());
        serviceFeeVoucherEntity.setVoucherType(VOUCHER_TYPE);
        serviceFeeVoucherEntity.setAccountNumber(EASSubjectEnum.S_1122020702.getValue());
        serviceFeeVoucherEntity.setAccountName(EASSubjectEnum.S_1122020702.getDesc());
        serviceFeeVoucherEntity.setCreator(cretor);
        serviceFeeVoucherEntity.setEntrydc(ENTRY_C);
        for (LoanOrderDto txLoanOrder : txList) {
            txPrincipalVoucherEntity.setOriginalAmount(txPrincipalVoucherEntity.getOriginalAmount().add(txLoanOrder.getPrincipal()));
            txPrincipalVoucherEntity.setDebitAmount(txPrincipalVoucherEntity.getOriginalAmount());
            txPrincipalVoucherEntity.setCreditAmount(BigDecimal.ZERO);

            serviceFeeVoucherEntity.setOriginalAmount(serviceFeeVoucherEntity.getOriginalAmount().add(txLoanOrder.getServiceFee()));
            serviceFeeVoucherEntity.setDebitAmount(BigDecimal.ZERO);
            serviceFeeVoucherEntity.setCreditAmount(serviceFeeVoucherEntity.getOriginalAmount());
        }

        voucherList.add(txPrincipalVoucherEntity);
        voucherList.add(serviceFeeVoucherEntity);


        Map<String, List<LoanOrderDto>> txGropuByProductCode = txList.stream().collect(Collectors.groupingBy(LoanOrderDto::getProductCode));
        txGropuByProductCode.forEach((fk,fv)->{
            //辅助账
            AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
            assistAccountEntity.setSeqNo(txPrincipalVoucherEntity.getSeqNo());

            assistAccountEntity.setAsstActType("客户");
            assistAccountEntity.setAsstActNumber(loanOrder.getCooperationCode());
            assistAccountEntity.setAsstActName(loanOrder.getCooperationName());

            assistAccountEntity.setAsstActType1("项目");
            assistAccountEntity.setAsstActNumber1(loanOrder.getProductCode());
            assistAccountEntity.setAsstActName1(loanOrder.getProductName());

            assistAccountEntity.setAsstActType2("行政组织");
            assistAccountEntity.setAsstActNumber2("001000");
            assistAccountEntity.setAsstActName2("通汇诚泰本部");


            //辅助账
            AssistAccountEntity assistAccountEntity2 = new AssistAccountEntity();
            assistAccountEntity2.setSeqNo(serviceFeeVoucherEntity.getSeqNo());

            assistAccountEntity2.setAsstActType("客户");
            assistAccountEntity2.setAsstActNumber(loanOrder.getCooperationCode());
            assistAccountEntity2.setAsstActName(loanOrder.getCooperationName());

            assistAccountEntity2.setAsstActType1("项目");
            assistAccountEntity2.setAsstActNumber1(loanOrder.getProductCode());
            assistAccountEntity2.setAsstActName1(loanOrder.getProductName());

            assistAccountEntity2.setAsstActType2("行政组织");
            assistAccountEntity2.setAsstActNumber2("001000");
            assistAccountEntity2.setAsstActName2("通汇诚泰本部");
            assistAccountList.add(assistAccountEntity2);
        });

        VoucherEntity serviceFeeVoucherEntity2 = new VoucherEntity();
        serviceFeeVoucherEntity2.setSeqNo(redisIdWorker.nextSecondId4(""));
        serviceFeeVoucherEntity2.setBatchNo(batchNo);
        serviceFeeVoucherEntity2.setCompanyNumber(companyNumber);
        serviceFeeVoucherEntity2.setVoucherAbstract("支付消费金融-" + loanOrder.getCooperationName() + "-"  + today);
        serviceFeeVoucherEntity2.setVoucherNumber("1");
        serviceFeeVoucherEntity2.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        serviceFeeVoucherEntity2.setBookedDate(today.toString());
        serviceFeeVoucherEntity2.setBizDate(today.plusDays(-1).toString());
        serviceFeeVoucherEntity2.setVoucherType(VOUCHER_TYPE);
        serviceFeeVoucherEntity2.setAccountNumber(EASSubjectEnum.S_22211603.getValue());
        serviceFeeVoucherEntity2.setAccountName(EASSubjectEnum.S_22211603.getDesc());
        serviceFeeVoucherEntity2.setCreator(cretor);
        serviceFeeVoucherEntity2.setEntrydc(ENTRY_D);

        BigDecimal multiply = serviceFeeVoucherEntity.getOriginalAmount().divide(new BigDecimal("1.06"), 2).multiply(new BigDecimal("0.06"));
        serviceFeeVoucherEntity2.setOriginalAmount(multiply.setScale(2, RoundingMode.HALF_UP));
        serviceFeeVoucherEntity2.setDebitAmount(serviceFeeVoucherEntity2.getOriginalAmount());

        VoucherEntity serviceFeeVoucherEntity3 = new VoucherEntity();
        serviceFeeVoucherEntity3.setSeqNo(redisIdWorker.nextSecondId4(""));
        serviceFeeVoucherEntity3.setBatchNo(batchNo);
        serviceFeeVoucherEntity3.setCompanyNumber(companyNumber);
        serviceFeeVoucherEntity3.setVoucherAbstract("支付消费金融-" + loanOrder.getCooperationName() + "-"  + today);
        serviceFeeVoucherEntity3.setVoucherNumber("1");
        serviceFeeVoucherEntity3.setPeriodNumber(today.format(DateTimeFormatter.ofPattern("yyyyMM")));
        serviceFeeVoucherEntity3.setBookedDate(today.toString());
        serviceFeeVoucherEntity3.setBizDate(today.plusDays(-1).toString());
        serviceFeeVoucherEntity3.setVoucherType(VOUCHER_TYPE);
        serviceFeeVoucherEntity3.setAccountNumber(EASSubjectEnum.S_2221010214.getValue());
        serviceFeeVoucherEntity3.setAccountName(EASSubjectEnum.S_2221010214.getDesc());
        serviceFeeVoucherEntity3.setCreator(cretor);
        serviceFeeVoucherEntity3.setEntrydc(ENTRY_C);
        serviceFeeVoucherEntity3.setOriginalAmount(serviceFeeVoucherEntity2.getOriginalAmount());
        serviceFeeVoucherEntity3.setCreditAmount(serviceFeeVoucherEntity2.getOriginalAmount());

        voucherList.add(serviceFeeVoucherEntity2);
        voucherList.add(serviceFeeVoucherEntity3);

        Map<String, List<LoanOrderDto>> groupByProduct = txList.stream().collect(Collectors.groupingBy(LoanOrderDto::getProductCode));
        groupByProduct.forEach((gk,gv)->{

            AssistAccountEntity serviceFee2AssistAccount = new AssistAccountEntity();
            serviceFee2AssistAccount.setSeqNo(serviceFeeVoucherEntity2.getSeqNo());
            serviceFee2AssistAccount.setAsstActType1("项目");
            serviceFee2AssistAccount.setAsstActNumber1(gv.get(0).getProjectCode());
            serviceFee2AssistAccount.setAsstActName1(gv.get(0).getProjectName());
            assistAccountList.add(serviceFee2AssistAccount);
        });
    }


    private void initExecuteParams(VoucherExecuteParam voucherExecuteParam){
        if (voucherExecuteParam.getExecuteDate() == null){
            voucherExecuteParam.setExecuteDate(LocalDate.now());
        }
        today = voucherExecuteParam.getExecuteDate();
       /* LocalDateTime startTime = LocalDateTime.of(today, LocalTime.of(0,0,0,0));
        LocalDateTime endTime = startTime.plusDays(1);*/
        startTime = LocalDateTime.of(2023,3,6,0,0,0,0);
        endTime = LocalDateTime.of(2023,3,7,0,0,0,0);
        //业务系统---放款订单  合作方、产品、付息方式分组
        batchNo = redisIdWorker.nextDayId4("");
    }


}
