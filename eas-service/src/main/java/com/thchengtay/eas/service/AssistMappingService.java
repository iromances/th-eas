package com.thchengtay.eas.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thchengtay.eas.model.entity.AssistMappingEntity;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public interface AssistMappingService extends IService<AssistMappingEntity> {

    AssistMappingEntity getByProjectCode(String projectCode);

}
