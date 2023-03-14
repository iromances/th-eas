package com.thchengtay.eas.controller;

import com.thchengtay.common.core.BaseController;
import com.thchengtay.common.core.Result;
import com.thchengtay.eas.model.dto.schedule.VoucherExecuteParam;
import com.thchengtay.eas.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@RestController
public class TestController extends BaseController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping("/test")
    public Result test(@RequestBody VoucherExecuteParam param) throws Exception {
        voucherService.importVoucher(param);
        return success();
    }

    @PostMapping("/push")
    public Result test(@RequestBody Map<String, String> param) throws Exception {
        String batchNo = param.get("batchNo");
        voucherService.push(batchNo, null);
        return success();
    }

}
