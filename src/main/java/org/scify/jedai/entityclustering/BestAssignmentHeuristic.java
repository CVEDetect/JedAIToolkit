/*
* Copyright [2016-2020] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package org.scify.jedai.entityclustering;

import com.esotericsoftware.minlog.Log;
import org.scify.jedai.datamodel.Comparison;
import org.scify.jedai.datamodel.EquivalenceCluster;
import org.scify.jedai.datamodel.SimilarityPairs;

import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Manos
 */
public class BestAssignmentHeuristic extends AbstractCcerEntityClustering {

    protected float[][] matrix; // inverted similarity matrix (cost matrix)

    private int[] selectedColumn;

    private int numMoves;

    public BestAssignmentHeuristic() {
        this(0.5f);
    }

    public BestAssignmentHeuristic(float simTh) {
        super(simTh);
    }

    private boolean acceptSwap(float D) {
        return (D < 0.0);
    }

    private void execute() {
        Random rand = new Random();
        int numRows = matrix.length;
        for (int i = 0; i < numMoves; i++) {
            int randomint = rand.nextInt(10);
            for (int numTry = 0; numTry < 150000; numTry++) {
                int row1 = rand.nextInt(numRows);
                int row2 = rand.nextInt(numRows);
                while (row1 == row2) {
                    row2 = rand.nextInt(numRows);
                }
                swapColumns(row1, row2);
            }
        }
    }

    @Override
    public EquivalenceCluster[] getDuplicates(SimilarityPairs simPairs) {
        Log.info("Input comparisons\t:\t" + simPairs.getNoOfComparisons());
        
        matchedIds.clear();
        if (simPairs.getNoOfComparisons() == 0) {
            return new EquivalenceCluster[0];
        }

        initializeData(simPairs);
        if (!isCleanCleanER) {
            return null; //the method is only applicable to Clean-Clean ER
        }

        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        int matrixSize = Math.max(noOfEntities - datasetLimit, datasetLimit);
        float[][] simMatrix = new float[matrixSize][matrixSize];
        while (iterator.hasNext()) {
            final Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                simMatrix[comparison.getEntityId1()][comparison.getEntityId2()] = comparison.getUtilityMeasure();
            }
        }
        init(getNegative(simMatrix));

        execute();

        int[] solutionHeuristic = getSolution();

        for (int i = 0; i < solutionHeuristic.length; i++) {
            int e1 = i;
            int e2 = solutionHeuristic[i];
            if (simMatrix[e1][e2] < threshold) {
                continue;
            }
            e2 += datasetLimit;

            //skip already matched entities (unique mapping contraint for clean-clean ER)
            if (matchedIds.contains(e1) || matchedIds.contains(e2)) {
                System.err.println("id already in the graph");
            }

            similarityGraph.addEdge(e1, e2);
            matchedIds.add(e1);
            matchedIds.add(e2);
        }

        return getConnectedComponents();
    }

    private void getInitialSolution() {
        for (int i = 0; i < matrix.length; i++) {
            selectedColumn[i] = i;
        }
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates clusters after heuristically solving the assignment problem. ";
    }

    @Override
    public String getMethodName() {
        return "Assignment Problem Heuristic Clustering";
    }
    
    private float[][] getNegative(float[][] initMatrix) {
        int N = initMatrix.length;
        float[][] negMatrix = new float[N][N];
        for (int i = 0; i < initMatrix.length; i++) {
            for (int j = 0; j < initMatrix[i].length; j++) {
                negMatrix[i][j] = 1.0f - initMatrix[i][j];
            }
        }
        return negMatrix;
    }

    private int[] getSolution() {
        return selectedColumn;
    }
    
    public void init(float[][] matrix) {
        this.matrix = matrix;
        //System.out.println(this.numMoves);
        this.selectedColumn = new int[matrix.length];
        this.numMoves = noOfEntities;//9999999
        //this.numMoves = 1000000000;//9999999
        /*System.out.println(this.numMoves*150000+" *1");
        System.out.println(noOfEntities+" noEntities");*/
        getInitialSolution();
    }
    
    public void setNumMoves(int numMoves) {
        this.numMoves = numMoves;
    }

    private void swapColumns(int row1, int row2) {
        int col1 = selectedColumn[row1];
        int col2 = selectedColumn[row2];
        float D = matrix[row1][col2] + matrix[row2][col1] - (matrix[row1][col1] + matrix[row2][col2]);
        if (acceptSwap(D)) {
            selectedColumn[row1] = col2;
            selectedColumn[row2] = col1;
        }
    }

}
