package com.glaurung.batMap.gui;

import java.util.Collection;

import com.glaurung.batMap.vo.Exit;

public class GraphUtils {

/**
 * This method will check if its ok to add an exit, or if it already exists
 * @param outEdges
 * @param exitDir
 * @return
 */
	public static boolean canAddExit(Collection<Exit> outEdges, String exitDir) {
		if(outEdges == null){
			return true;
		}
    	for(Exit exit: outEdges){
    		if(exit.getExit().equalsIgnoreCase(exitDir)){
    			return false;
    		}
    	}
    	return true;
	}
}
