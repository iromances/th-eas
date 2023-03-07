package com.thchengtay.eas.model.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public enum AssistTypeEnum implements IEnum<Integer> {


    ;

    private Integer value;

    private String desc;


    AssistTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
