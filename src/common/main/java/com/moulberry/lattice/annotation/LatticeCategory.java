package com.moulberry.lattice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeCategory {

    /*
     * The name of the category
     * Will be translated, if translate is set to true
     */
    String name();

    /*
     * Whether to translate the name
     */
    boolean translate() default true;

}
