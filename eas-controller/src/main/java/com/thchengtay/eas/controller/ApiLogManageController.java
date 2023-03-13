package com.thchengtay.eas.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thchengtay.common.core.BaseController;
import com.thchengtay.common.core.Result;
import com.thchengtay.eas.model.entity.ApiLogEntity;
import com.thchengtay.eas.model.vo.ApiLogRequestVo;
import com.thchengtay.eas.service.ApiLogService;
import com.thchengtay.eas.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@RestController
public class ApiLogManageController extends BaseController {
    @Autowired
    private ApiLogService apiLogService;
    @Autowired
    private VoucherService voucherService;

    @GetMapping("/api/log/index")
    public Result index(ApiLogRequestVo requestVo){
        IPage<ApiLogEntity> page = new Page<>(requestVo.getCurrent(), requestVo.getSize());

        LambdaQueryWrapper<ApiLogEntity> queryWrapper= Wrappers.lambdaQuery();
        if (requestVo.getSearchTime() != null){
            queryWrapper.ge(ApiLogEntity::getRequestTime, LocalDateTime.of(LocalDate.parse(requestVo.getSearchTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.of(0,0,0)));
            queryWrapper.lt(ApiLogEntity::getRequestTime, LocalDateTime.of(LocalDate.parse(requestVo.getSearchTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.of(0,0,0)).plusDays(1));
        }
        if (StringUtils.isNotBlank(requestVo.getStatus())){
            queryWrapper.eq(ApiLogEntity::getStatus, requestVo.getStatus());
        }

        IPage<ApiLogEntity> result = apiLogService.page(page, queryWrapper);
        for (ApiLogEntity record : result.getRecords()) {
            if (record.getStatus().equals("3")){
                record.setStatus("失败");
            }else if (record.getStatus().equals("4")){
                record.setStatus("成功");
            }
        }
        return success(toResultMap(result.getRecords(), result.getTotal()));
    }


    @PostMapping("/api/log/retry/{id}")
    public Result retry(@PathVariable Long id) throws Exception {
        ApiLogEntity apiLog = apiLogService.getById(id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("batchNo", apiLog.getBatchNo());
        jsonObject.put("id", id);
        voucherService.push(jsonObject);
        return success();
    }
}
