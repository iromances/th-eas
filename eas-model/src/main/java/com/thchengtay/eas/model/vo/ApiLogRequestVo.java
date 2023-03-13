package com.thchengtay.eas.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;

/**
 * @auth shihao
 * @date created on 2023/3/13 18:57
 * @desc
 **/
@Getter
@Setter
public class ApiLogRequestVo extends Page {


    private String searchTime;

    private String status;


}
