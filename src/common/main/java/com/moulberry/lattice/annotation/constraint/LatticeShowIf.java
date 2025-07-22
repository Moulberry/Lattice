package com.moulberry.lattice.annotation.constraint;

import com.moulberry.lattice.LatticeDynamicFrequency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeShowIf {

    String function();
    LatticeDynamicFrequency frequency() default LatticeDynamicFrequency.ONCE;

}
