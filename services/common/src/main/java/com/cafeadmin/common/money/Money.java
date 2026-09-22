package com.cafeadmin.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal of(BigDecimal value) {
        return value == null ? zero() : value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal value, int quantity) {
        return of(of(value).multiply(BigDecimal.valueOf(quantity)));
    }

    public static BigDecimal tax(BigDecimal amount, BigDecimal rate) {
        return of(of(amount).multiply(rate == null ? BigDecimal.ZERO : rate));
    }
}
