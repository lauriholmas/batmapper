package com.glaurung.batMap.gui;

import com.glaurung.batMap.controller.MapperPlugin;

public class MockPlugin extends MapperPlugin {

	public MockPlugin(){
		super();
	}
	
	@Override
	public void saveRipAction(String ripString) {
		System.out.println(super.MAKERIPACTION+ripString);
	}
	
	@Override
	public void toggleRipAction(boolean mode) {
		System.out.println(mode);
	}
	
}
