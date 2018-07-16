/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */
package org.scify.jedai.blockprocessing.comparisoncleaning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.scify.jedai.utilities.enumerations.WeightingScheme;

/**
 *
 * @author gap2
 */
public abstract class CanopyClustering extends AbstractCanopyClustering {

    private final double t1;
    private final double t2;

    public CanopyClustering(double w1, double w2, WeightingScheme wScheme) {
        super(wScheme);
        t1 = w1;
        t2 = w2;
    }

    @Override
    protected void getBilateralBlocks() {
        final List<Integer> entityIds1 = new ArrayList<>();
        for (int i = 0; i < datasetLimit; i++) {
            entityIds1.add(i);
        }
        Collections.shuffle(entityIds1);

        removedEntities.clear();
        int d2Entities = noOfEntities - datasetLimit;        
        final Iterator iterator = entityIds1.iterator();
        while (iterator.hasNext() && removedEntities.size() < d2Entities) {
            // Get current element:
            int currentId = (Integer) iterator.next();

            // Start a new cluster:
            retainedNeighbors.clear();
//            setBilateralValidEntities(currentId);
//            for (int neighborId : validEntities) {
//                double weight = getWeight(currentId, neighborId);
//                
//                // Inclusion threshold:
//                if (t1 <= weight) {
//                    retainedNeighbors.add(neighborId);
//                }
//
//                // Removal threshold:
//                if (t2 <= weight) {
//                    removedEntities.add(neighborId);
//                }
//            }
//            
//            addBilateralBlock(currentId);
        }
    }

    @Override
    protected void getUnilateralBlocks() {
        final List<Integer> entityIds = new ArrayList<>();
        for (int i = 0; i < noOfEntities; i++) {
            entityIds.add(i);
        }
        Collections.shuffle(entityIds);

        removedEntities.clear();
        final Iterator iter = entityIds.iterator();
        while (removedEntities.size() < entityIds.size()) {
            // Get next element:
            int currentId = (Integer) iter.next();
            
            // Remove first element:
            removedEntities.add(currentId);
            
            // Start a new cluster:
            retainedNeighbors.clear();
//            setUnilateralValidEntities(currentId);
//            for (int neighborId : validEntities) {
//                double jaccardSim = counters[neighborId] / (entityIndex.getNoOfEntityBlocks(currentId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
//                
//                // Inclusion threshold:
//                if (t1 <= jaccardSim) {
//                    retainedNeighbors.add(neighborId);
//                }
//
//                // Removal threshold:
//                if (t2 <= jaccardSim) {
//                    removedEntities.add(neighborId);
//                }
//            }
//            
//            addUnilateralBlock(currentId);
        }
    }
}
