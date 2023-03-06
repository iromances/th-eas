package com.thchengtay.eas.schedule;

import com.alibaba.fastjson.JSONObject;
import com.thchengtay.eas.model.dto.schedule.VoucherExecuteParam;
import com.thchengtay.eas.service.VoucherService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/***
 *
 *
 * @auth shihao
 * @since 2022/12/2
 *
 */
@Component
public class VoucherSchedule {

    @Autowired
    private VoucherService voucherService;

    @XxlJob("projectLoanAnalyseSchedule")
    public ReturnT<String> process(String params) throws Exception{
        VoucherExecuteParam voucherExecute = new VoucherExecuteParam();
        if (!StringUtils.isEmpty(params)){
            voucherExecute = JSONObject.parseObject(params, VoucherExecuteParam.class);
        }
        voucherService.importVoucher(voucherExecute);
        return ReturnT.SUCCESS;
    }

}
