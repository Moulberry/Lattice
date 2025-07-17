package com.moulberry.lattice.annotation.widget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeWidgetSlider {

    // Allows ctrl+clicking or right-clicking to input number values directly
    boolean allowAlternateInput() default true;

}
