package com.thchengtay.eas.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thchengtay.common.core.BaseController;
import com.thchengtay.common.core.Result;
import com.thchengtay.eas.model.entity.VoucherEntity;
import com.thchengtay.eas.model.vo.VoucherRequestVo;
import com.thchengtay.eas.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@RestController
public class VoucherManageController extends BaseController {

    @Autowired
    private VoucherService voucherService;


    @GetMapping("/voucher/index")
    public Result index(VoucherRequestVo requestVo){
        IPage<VoucherEntity> page = new Page<>(requestVo.getCurrent(), requestVo.getSize());

        LambdaQueryWrapper<VoucherEntity> queryWrapper= Wrappers.lambdaQuery();
        if (requestVo.getSearchTime() != null){
            queryWrapper.eq(VoucherEntity::getBookedDate, LocalDate.parse(requestVo.getSearchTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (StringUtils.isNotBlank(requestVo.getAccountNumber())){
            queryWrapper.eq(VoucherEntity::getAccountNumber, requestVo.getAccountNumber());
        }
        IPage<VoucherEntity> result = voucherService.page(page, queryWrapper);
        return success(toResultMap(result.getRecords(), result.getTotal()));
    }


}
