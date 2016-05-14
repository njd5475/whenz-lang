package com.anor.roar.whenzint.patterns;

public interface Pattern {
	
	public boolean matches(String command);

	public Object resolve(String concate);
}
