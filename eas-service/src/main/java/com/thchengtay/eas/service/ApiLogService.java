package com.thchengtay.eas.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thchengtay.eas.model.entity.ApiLogEntity;

import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public interface ApiLogService extends IService<ApiLogEntity> {


    List<ApiLogEntity> listNeedRetryData();

}
