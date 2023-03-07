

package com.thchengtay.eas.model.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.Getter;

public enum StatementTypeEnum implements IEnum<Integer> {
    COMPENSATORY(1000, "代偿","compensatory","DC"),
    BUYBACK(2000, "回购","buyback","HG"),
    OFFLINE(3000, "线下还款","offline","XH"),
    REFUND(4000, "退费","refund","TF"),
    XXTSERVICE(5000, "先行通服务费","xxt","XF"),
    REFUND_TICKET(6000, "退票","refundTicket","TP"),
    PAYMENT_FEE(7000, "支付手续费","paymentFee","PF");

    private Integer value;
    private String desc;
    private String name;
    @Getter
    private String sequencePrefix;

    StatementTypeEnum(Integer value, String desc, String name, String prefix) {
        this.value = value;
        this.desc = desc;
        this.name = name;
        this.sequencePrefix=prefix;
    }



    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static StatementTypeEnum getByValue(Integer value) {
        StatementTypeEnum[] var1 = values();
        int var2 = var1.length;
        for(int var3 = 0; var3 < var2; ++var3) {
            StatementTypeEnum pme = var1[var3];
            if (pme.getValue().equals(value)) {
                return pme;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
