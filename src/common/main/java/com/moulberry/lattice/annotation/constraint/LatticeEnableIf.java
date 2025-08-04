package com.moulberry.lattice.annotation.constraint;

import com.moulberry.lattice.LatticeDynamicFrequency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeEnableIf {

    String function(); // Function must take no arguments and return a boolean
    LatticeDynamicFrequency frequency() default LatticeDynamicFrequency.ONCE;

}
