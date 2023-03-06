package com.thchengtay.eas.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/2
 *
 */
@Getter
@Setter
@TableName("source_detail")
public class SourceDetailEntity {

    //凭证序列号
    private String seqNo;

    //源系统
    private String sys;

    //数据类型
    private String dataType;

    //业务编号
    private String bizKey;

}
