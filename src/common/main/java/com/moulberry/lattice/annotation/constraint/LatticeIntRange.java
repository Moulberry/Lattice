package com.moulberry.lattice.annotation.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeIntRange {

    // min/max/step when normally inputting values (e.g. moving a slider)
    int min();
    int max();
    int step() default 1;

    // clampMin/clampMax/clampStep when manually inputting values (e.g. right-clicking a slider)
    int clampMin() default Integer.MIN_VALUE;
    int clampMax() default Integer.MAX_VALUE;
    int clampStep() default 1;

}
