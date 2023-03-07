package com.thchengtay.eas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thchengtay.cache.util.RedisIdWorker;
import com.thchengtay.eas.dao.VoucherMapper;
import com.thchengtay.eas.model.dto.LoanOrderCriteria;
import com.thchengtay.eas.model.dto.LoanOrderDto;
import com.thchengtay.eas.model.dto.LoanPaymentDto;
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


    @Transactional
    @Override
    public void importVoucher(VoucherExecuteParam voucherExecuteParam) {

        initExecuteParams(voucherExecuteParam);
        //放款单凭证
        storageLoanOrderVoucher(voucherExecuteParam);
        //放款详单凭证&手续费凭证
        storageRemitOrder(voucherExecuteParam);

    }

    private void storageRemitOrder(VoucherExecuteParam voucherExecuteParam){

        LoanOrderCriteria criteria = new LoanOrderCriteria();
        criteria.setStartTime(voucherExecuteParam.getStartTime());
        criteria.setEndTime(voucherExecuteParam.getEndTime());
        List<LoanPaymentDto> loanPaymentList = voucherMapper.listLoanPayment(criteria);

        Map<String, List<LoanPaymentDto>> groupByCooperationCode = loanPaymentList.stream().collect(Collectors.groupingBy(LoanPaymentDto::getCooperationCode));

        List<AssistAccountEntity> assistAccountList = new ArrayList<>();
        List<VoucherEntity> voucherList = new ArrayList<>();
        groupByCooperationCode.forEach((k,v)->{

            //放款手续费凭证
            remitOrderFeeVoucher(voucherExecuteParam, v, assistAccountList, voucherList);

            //商户&担保方放款订单凭证
            remitOrderDetailVoucher(voucherExecuteParam, v, assistAccountList, voucherList);

        });

        super.saveBatch(voucherList);
        assistAccountService.saveBatch(assistAccountList);
    }



    /***
     *  放款至商户&担保方凭证
     * @param voucherExecuteParam
     * @param loanPaymentList
     * @param assistAccountList
     * @param voucherList
     */
    private void remitOrderDetailVoucher(VoucherExecuteParam voucherExecuteParam,
                                      List<LoanPaymentDto> loanPaymentList,
                                      List<AssistAccountEntity> assistAccountList,
                                      List<VoucherEntity> voucherList){

        String batchNo = voucherExecuteParam.getBatchNo();
        LocalDate today = voucherExecuteParam.getExecuteDate();
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
     * @param voucherExecuteParam
     * @param loanPaymentList
     * @param assistAccountList
     * @param voucherList
     */
    private void remitOrderFeeVoucher(VoucherExecuteParam voucherExecuteParam,
                                      List<LoanPaymentDto> loanPaymentList,
                                      List<AssistAccountEntity> assistAccountList,
                                      List<VoucherEntity> voucherList){
        String batchNo = voucherExecuteParam.getBatchNo();
        LocalDate today = voucherExecuteParam.getExecuteDate();
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


    private void storageLoanOrderVoucher(VoucherExecuteParam voucherExecuteParam){
        LocalDateTime startTime = voucherExecuteParam.getStartTime();
        LocalDateTime endTime = voucherExecuteParam.getEndTime();
        String batchNo = voucherExecuteParam.getBatchNo();
        LocalDate today = voucherExecuteParam.getExecuteDate();

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

            txLoanOrderVoucher(voucherExecuteParam, txList, voucherList, assistAccountList);

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
     * @param voucherExecuteParam
     * @param txList
     * @param assistAccountList
     * @param voucherList
     */
    private void txLoanOrderVoucher(VoucherExecuteParam voucherExecuteParam,
                                    List<LoanOrderDto> txList,
                                    List<VoucherEntity> voucherList,
                                    List<AssistAccountEntity> assistAccountList){

        if (CollectionUtils.isEmpty(txList)){
            return;
        }

        String batchNo = voucherExecuteParam.getBatchNo();
        LocalDate today = voucherExecuteParam.getExecuteDate();
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
        LocalDate today = voucherExecuteParam.getExecuteDate();
       /* LocalDateTime startTime = LocalDateTime.of(today, LocalTime.of(0,0,0,0));
        LocalDateTime endTime = startTime.plusDays(1);*/
        LocalDateTime startTime = LocalDateTime.of(2023,3,6,0,0,0,0);
        LocalDateTime endTime = LocalDateTime.of(2023,3,7,0,0,0,0);
        //业务系统---放款订单  合作方、产品、付息方式分组
        String batchNo = redisIdWorker.nextDayId4("");

        voucherExecuteParam.setStartTime(startTime);
        voucherExecuteParam.setEndTime(endTime);
        voucherExecuteParam.setBatchNo(batchNo);
    }


}
