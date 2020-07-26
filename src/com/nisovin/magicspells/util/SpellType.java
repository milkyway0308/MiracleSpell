package com.nisovin.magicspells.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface SpellType {

	SpellTypes[] types();
	
}