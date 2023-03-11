package com.thchengtay.eas.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.thchengtay.eas.model.dto.schedule.VoucherExecuteParam;
import com.thchengtay.eas.model.entity.VoucherEntity;

import javax.xml.rpc.ServiceException;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public interface VoucherService extends IService<VoucherEntity> {

    void importVoucher(VoucherExecuteParam voucherExecute);

    void push(JSONObject jsonObject) throws Exception;

}
