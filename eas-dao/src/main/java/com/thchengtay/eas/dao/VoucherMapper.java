package com.thchengtay.eas.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thchengtay.eas.model.dto.*;
import com.thchengtay.eas.model.entity.VoucherEntity;

import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public interface VoucherMapper extends BaseMapper<VoucherEntity> {


    List<LoanOrderDto> listLoanOrderVoucher(LoanOrderCriteria criteria);


    List<LoanPaymentDto> listLoanPayment(LoanOrderCriteria criteria);

    List<StatementDto> listOfflineStatement(LoanOrderCriteria criteria);

    List<StatementResourceDto> listOfflineStatementDetail(LoanOrderCriteria criteria);


    List<RepaymentPlanDto> listOnlinePayment(LoanOrderCriteria criteria);

    List<RepaymentPlanPayDetailDto> listOnlinePayDetail(LoanOrderCriteria criteria);

    List<RepaymentPlanPaySubjectDetailDto> listOnlinePaySubjectDetail(LoanOrderCriteria criteria);


    List<VoucherEntity> listByBatchNo(String batchNo);

    List<DiscountInterestDto> discountInterest(LoanOrderCriteria criteria);

    List<PaymentFeeDto> baofuPaymentFee(LoanOrderCriteria criteria);

}
