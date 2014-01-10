package com.soundcenter.soundcenter.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.SCLocation;
import com.soundcenter.soundcenter.lib.data.Station;

public class IntersectionDetection {
	
	public static List<Short> getBiomeInRange(SCLocation loc) {
		List<Short> matches = new ArrayList<Short>();
		for (Entry<Short, Station> entry : Client.database.biomes.entrySet()) {
			Station biome = entry.getValue();
			if (loc.getBiome().equals(biome.getName())) {
				matches.add(entry.getKey());
			}
		}
		return matches;
	}
	
	public static List<Short> getWorldInRange(SCLocation loc) {
		List<Short> matches = new ArrayList<Short>();
		for (Entry<Short, Station> entry : Client.database.worlds.entrySet()) {
			Station world = entry.getValue();
			if (loc.getWorld().equals(world.getName())) {
				matches.add(entry.getKey());
			}
		}
		return matches;
	}
	
	public static HashMap<Short, Double> getBoxesInRange(SCLocation loc) {
		HashMap<Short, Double> matches = new HashMap<Short, Double>();
		
		for (Map.Entry<Short, Station> entry : Client.database.boxes.entrySet())
        {
            short id = entry.getKey();
            Station box = entry.getValue();
            SCLocation center = box.getLocation();
            try {
            	//check if location is roughly in rangeof the box, before calculating exact distance
            	if (locIsNear(center, loc, box.getRange())) {
		            double distance = center.distance(loc);
		            
		            /* if in range of box, add box id and distance to list */
		            if (!isNaN(distance) && distance <= box.getRange()) {
		            	matches.put(id, distance);
		            }
            	}
            } catch (IllegalArgumentException e) {}
        }
		
		return matches;
	}
	
	public static HashMap<Short, Double> getAreasInRange(SCLocation loc) {
		HashMap<Short, Double> matches = new HashMap<Short, Double>();
		
		for (Map.Entry<Short, Station> entry : Client.database.areas.entrySet())
        {
            short id = entry.getKey();
            Station area = entry.getValue();
            double distance = distToAreaBorder(loc, area, false);
            
            /* if in area, add area id and distance to border to list */
            if (distance > 0) {
            	matches.put(id, distance);
            }
        }
		return matches;
	}
	
	private static double distToAreaBorder(SCLocation loc, Station area, Boolean calcDistIfOutside) {
		
		if (!loc.getWorld().equals(area.getWorld()))
			return -1;
		
		double dist = 0;
		boolean contains = false;
		
		SCLocation min = area.getMin();
		SCLocation max = area.getMax();
		
		
		// check if cuboid contains loc
		boolean betweenX = false;
		boolean betweenY = false;
		boolean betweenZ = false;
		if ((loc.getX() <= max.getX()) && (loc.getX() >= min.getX()))
			betweenX = true;
		if ((loc.getY() <= max.getY()) && (loc.getY() >= min.getY()))
			betweenY = true;
		if ((loc.getZ() <= max.getZ()) && (loc.getZ() >= min.getZ()))
			betweenZ = true;
		if(betweenX && betweenZ && betweenY)
			contains = true;
		
		if (contains || calcDistIfOutside) {
			//calculate distance to borders
			double distX = Math.min(Math.abs(min.getX() - loc.getX()), Math.abs(max.getX() - loc.getX()));
			double distY = Math.min(Math.abs(min.getY() - loc.getY()), Math.abs(max.getY() - loc.getY()));
			double distZ = Math.min(Math.abs(min.getZ() - loc.getZ()), Math.abs(max.getZ() - loc.getZ()));
			
			//get minimum distance
			if (contains)
				dist = Math.min(Math.min(distX, distY), Math.min(distZ, Math.min(distX, distY)));
			
			else {
				if (betweenX && betweenY)
					dist = distZ;
				else if (betweenX && betweenZ)
					dist = distY;
				else if (betweenY && betweenZ)
					dist = distX;
				else if (betweenX)
					dist = Math.sqrt(Math.pow(distY, 2) + Math.pow(distZ, 2));
				else if (betweenY)
					dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distZ, 2));
				else if (betweenZ)
					dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
				else
					dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2) + Math.pow(distZ, 2));
				
				//use negative distance for points from outside
				dist = -dist;
			}
		} 
			 
		return dist;
	}
	
	private static boolean locIsNear(SCLocation loc1, SCLocation loc2, int range) {
		if (loc1.getWorld().equals(loc2.getWorld())) {
			//check if location are near each other
			if ((Math.abs(loc1.getX()-loc2.getX()) <= range) && (Math.abs(loc1.getY()-loc2.getY()) <= range)
					&& (Math.abs(loc1.getZ()-loc2.getZ()) <= range)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isNaN(double x) {
		return x != x;
	}
	
}
