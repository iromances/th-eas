package com.thchengtay.eas.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thchengtay.common.core.BaseController;
import com.thchengtay.common.core.Result;
import com.thchengtay.eas.model.entity.AssistAccountEntity;
import com.thchengtay.eas.model.entity.VoucherEntity;
import com.thchengtay.eas.model.vo.VoucherRequestVo;
import com.thchengtay.eas.service.AssistAccountService;
import com.thchengtay.eas.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private AssistAccountService assistAccountService;


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
        if (StringUtils.isNotBlank(requestVo.getBatchNo())){
            queryWrapper.eq(VoucherEntity::getBatchNo, requestVo.getBatchNo());
        }
        IPage<VoucherEntity> result = voucherService.page(page, queryWrapper);

        List<String> seqNos = result.getRecords().stream().map(VoucherEntity::getSeqNo).collect(Collectors.toList());

        LambdaQueryWrapper<AssistAccountEntity> assistAccountQueryWrapper= Wrappers.lambdaQuery();
        assistAccountQueryWrapper.in(AssistAccountEntity::getSeqNo, seqNos);
        List<AssistAccountEntity> assistAccountList = assistAccountService.list(assistAccountQueryWrapper);
        for (VoucherEntity voucherEntity : result.getRecords()) {
            List<AssistAccountEntity> matchedAssistAccount = assistAccountList.stream()
                    .filter(assistAccountEntity -> assistAccountEntity.getSeqNo().equals(voucherEntity.getSeqNo())).collect(Collectors.toList());

            voucherEntity.setAssistAccountList(matchedAssistAccount);
        }

        return success(toResultMap(result.getRecords(), result.getTotal()));
    }


}
