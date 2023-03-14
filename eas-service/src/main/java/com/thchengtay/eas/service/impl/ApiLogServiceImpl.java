package com.thchengtay.eas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thchengtay.eas.dao.ApiLogMapper;
import com.thchengtay.eas.model.entity.ApiLogEntity;
import com.thchengtay.eas.service.ApiLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
@Slf4j
@Service
public class ApiLogServiceImpl extends ServiceImpl<ApiLogMapper, ApiLogEntity> implements ApiLogService {


    @Override
    public List<ApiLogEntity> listNeedRetryData() {
        return baseMapper.listNeedRetryData();
    }
}
