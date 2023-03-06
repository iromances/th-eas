package com.thchengtay.eas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thchengtay.cache.util.RedisIdWorker;
import com.thchengtay.eas.dao.AssisMappingMapper;
import com.thchengtay.eas.dao.VoucherMapper;
import com.thchengtay.eas.model.dto.LoanOrderCriteria;
import com.thchengtay.eas.model.dto.LoanOrderDto;
import com.thchengtay.eas.model.dto.schedule.VoucherExecuteParam;
import com.thchengtay.eas.model.entity.AssistAccountEntity;
import com.thchengtay.eas.model.entity.VoucherEntity;
import com.thchengtay.eas.model.enums.EASSubjectEnum;
import com.thchengtay.eas.service.AssistAccountService;
import com.thchengtay.eas.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bouncycastle.cms.PasswordRecipientId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

        //支付系统---放款数据&手续费
    }

    @Override
    public void push() {

    }

    private void initExecuteParams(VoucherExecuteParam voucherExecuteParam){
        if (voucherExecuteParam.getExecuteDate() == null){
            voucherExecuteParam.setExecuteDate(LocalDate.now());
        }
        LocalDate today = voucherExecuteParam.getExecuteDate();
       /* LocalDateTime startTime = LocalDateTime.of(today, LocalTime.of(0,0,0,0));
        LocalDateTime endTime = startTime.plusDays(1);*/
        LocalDateTime startTime = LocalDateTime.of(2020,1,1,0,0,0,0);
        LocalDateTime endTime = LocalDateTime.of(2023,1,1,0,0,0,0);
        //业务系统---放款订单  合作方、产品、付息方式分组
        String batchNo = redisIdWorker.nextDayId4("");

        voucherExecuteParam.setStartTime(startTime);
        voucherExecuteParam.setEndTime(endTime);
        voucherExecuteParam.setBatchNo(batchNo);
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

            if (CollectionUtils.isNotEmpty(txList)){
                VoucherEntity txPrincipalVoucherEntity = new VoucherEntity();
                txPrincipalVoucherEntity.setSeqNo(redisIdWorker.nextSecondId4(""));
                txPrincipalVoucherEntity.setBatchNo(batchNo);
                txPrincipalVoucherEntity.setCompanyNumber(companyNumber);
                txPrincipalVoucherEntity.setVoucherAbstract("支付消费金融" + loanOrder.getCooperationName() + today);
                txPrincipalVoucherEntity.setVoucherNumber(i++ + "");
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
                serviceFeeVoucherEntity.setVoucherAbstract("支付消费金融" + loanOrder.getCooperationName() + today);
                serviceFeeVoucherEntity.setVoucherNumber(i++ + "");
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
            }

            if (CollectionUtils.isNotEmpty(fxList)){
                VoucherEntity fxvoucherEntity = new VoucherEntity();
                fxvoucherEntity.setSeqNo(redisIdWorker.nextSecondId4(""));
                fxvoucherEntity.setBatchNo(batchNo);
                fxvoucherEntity.setCompanyNumber(companyNumber);
                fxvoucherEntity.setVoucherAbstract("支付消费金融" + loanOrder.getCooperationName() + today);
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

                Map<String, List<LoanOrderDto>> fxGropuByProductCode = fxList.stream().collect(Collectors.groupingBy(LoanOrderDto::getProductCode));
                fxGropuByProductCode.forEach((fk,fv)->{
                    //辅助账
                    AssistAccountEntity assistAccountEntity = new AssistAccountEntity();
                    assistAccountEntity.setSeqNo(fxvoucherEntity.getSeqNo());

                    assistAccountEntity.setAsstActType("客户");
                    assistAccountEntity.setAsstActNumber(loanOrder.getCooperationCode());
                    assistAccountEntity.setAsstActName(loanOrder.getCooperationName());

                    assistAccountEntity.setAsstActType1("项目");
                    assistAccountEntity.setAsstActNumber1(loanOrder.getProductCode());
                    assistAccountEntity.setAsstActName1(loanOrder.getProductName());

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


















}
