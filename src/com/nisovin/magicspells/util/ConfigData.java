package com.nisovin.magicspells.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ConfigData {
	String field() default "";
	String dataType() default "";
	String defaultValue() default "null";
	String description() default "";
	String[] mutuallyExclusiveWith() default {};
	String[] hasPriorityOver() default {};
	ConfigDataSchema dataSchema() default ConfigDataSchema.NORMAL;
}
