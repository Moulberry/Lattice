package com.moulberry.lattice.annotation.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeFloatRange {

    // min/max/step when normally inputting values (e.g. moving a slider)
    float min();
    float max();
    String step() default "0.01";

    // clampMin/clampMax/clampStep when manually inputting values (e.g. right-clicking a slider)
    float clampMin() default Float.MIN_VALUE;
    float clampMax() default Float.MAX_VALUE;
    String clampStep() default "0";

}
