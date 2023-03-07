package com.thchengtay.eas.model.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

/***
 *
 *
 * @auth shihao
 * @since 2023/3/6
 *
 */
public enum EASSubjectEnum implements IEnum<String> {
    S_1122020701("1122020701", "应收账款-系统外-消费金融-本金"),
    S_101208("101208", "第三方支付平台-付款"),
    S_1122020702("1122020702", "应收账款-系统外-消费金融-手续费"),


    S_22211603("22211603", "应交税费-待转销项税额-消费金融"),
    S_2221010214("2221010214", "应交税费-应交增值税-内贸及保理业务-销项税额（消费金融）"),
    S_1123020201("1123020201", "预付账款-系统外-暂付款-未收发票暂付款"),
    S_100201("100201", "银行存款-活期"),
    S_60010209("60010209", "主营业务收入-系统外-消费金融"),
    ;


    private String value;

    private String desc;


    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    EASSubjectEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
