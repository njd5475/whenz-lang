package com.anor.roar.whenzint.mapping;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ByteBufferMap {

	private List<ByteBufferMapping> mappings = new LinkedList<>();
	
	public ByteBufferMap(ByteBufferMapping...toAdd) {
		this.mappings.addAll(Arrays.asList(toAdd));
	}
	
	public void addBuffer(ByteBufferMapping map) {
		this.mappings.add(map);
	}
	
}
