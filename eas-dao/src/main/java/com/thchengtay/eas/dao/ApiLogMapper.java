package com.thchengtay.eas.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thchengtay.eas.model.entity.ApiLogEntity;

import java.util.List;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public interface ApiLogMapper extends BaseMapper<ApiLogEntity> {


    List<ApiLogEntity> listNeedRetryData();



}
