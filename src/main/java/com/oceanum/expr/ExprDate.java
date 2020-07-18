package com.oceanum.expr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
public class ExprDate {
    private Date date;

    public ExprDate() {
        this.date = new Date();
    }

    public ExprDate(Date date) {
        this.date = date;
    }

    public String format(String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public ExprDate shift(Duration duration) {
        long newTime = date.getTime() + duration.toMillis();
        return new ExprDate(new Date(newTime));
    }

    public Long timestamp() {
        return date.getTime();
    }

    public Long time() {
        return timestamp();
    }
}
