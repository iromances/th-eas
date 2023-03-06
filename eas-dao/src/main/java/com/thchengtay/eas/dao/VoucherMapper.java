package com.thchengtay.eas.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thchengtay.eas.model.dto.LoanOrderCriteria;
import com.thchengtay.eas.model.dto.LoanOrderDto;
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



}
