package com.moulberry.lattice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LatticeOption {

    /*
     * The title of the option
     * Will be translated, if translate is set to true
     */
    String title();

    /*
     * The description of the option
     * Will be translated, if translate is set to true
     * Two exclamation points can be used as a shorthand for the title, eg. if the title is
     * "my.config.option" then "!!.description" will resolve to "my.config.option.description"
     */
    String description() default "";

    /*
     * Whether to translate the title and description
     */
    boolean translate() default true;

}
