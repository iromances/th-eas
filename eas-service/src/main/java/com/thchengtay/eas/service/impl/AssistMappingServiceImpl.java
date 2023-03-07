package com.thchengtay.eas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.javaws.CacheUtil;
import com.thchengtay.eas.common.CacheUtils;
import com.thchengtay.eas.dao.AssistMappingMapper;
import com.thchengtay.eas.model.entity.AssistMappingEntity;
import com.thchengtay.eas.service.AssistMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Slf4j
@Service
public class AssistMappingServiceImpl extends ServiceImpl<AssistMappingMapper, AssistMappingEntity> implements AssistMappingService {

    @Override
    public AssistMappingEntity getByProjectCode(String projectCode) {
        AssistMappingEntity assistMapping = (AssistMappingEntity) CacheUtils.get(projectCode);
        if (assistMapping != null){
            return assistMapping;
        }

        LambdaQueryWrapper<AssistMappingEntity> queryWrapper= Wrappers.lambdaQuery();
        queryWrapper.eq(AssistMappingEntity::getProjectCode, projectCode);
        AssistMappingEntity assistMappingEntity = getOne(queryWrapper);
        CacheUtils.put(projectCode, assistMappingEntity);
        return assistMappingEntity;
    }
}
