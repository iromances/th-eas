package com.thchengtay.eas.schedule;

import com.alibaba.fastjson.JSONObject;
import com.thchengtay.eas.model.entity.ApiLogEntity;
import com.thchengtay.eas.service.ApiLogService;
import com.thchengtay.eas.service.VoucherService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2022/12/2
 *
 */
@Component
public class VoucherImportSchedule {
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private ApiLogService apiLogService;

    @XxlJob("voucherImportSchedule")
    public ReturnT<String> process(String params) throws Exception{
        JSONObject jsonObject = new JSONObject();
        if (!StringUtils.isEmpty(params)){
            jsonObject = JSONObject.parseObject(params);
        }

        List<ApiLogEntity> needRetryList = apiLogService.listNeedRetryData();
        for (ApiLogEntity apiLogEntity : needRetryList) {
            voucherService.push(jsonObject.getString("batchNo"), null);
        }
        return ReturnT.SUCCESS;
    }

}
