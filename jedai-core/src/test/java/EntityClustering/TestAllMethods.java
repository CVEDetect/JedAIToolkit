/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
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
package EntityClustering;

import BlockBuilding.IBlockBuilding;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockProcessing.IBlockProcessing;
import Utilities.DataStructures.UnilateralDuplicatePropagation;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.IEntityReader;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityMatching.IEntityMatching;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.EntityClusteringDerMethod;
import Utilities.Enumerations.EntityMatchingMethod;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class TestAllMethods {

    public static void main(String[] args) {
        String entitiesFilePath = "C:\\Users\\GAP2\\Downloads\\coraProfiles";
        String groundTruthFilePath = "C:\\Users\\GAP2\\Downloads\\coraIdDuplicates";

        IEntityReader eReader = new EntitySerializationReader(entitiesFilePath);
        List<EntityProfile> profiles = eReader.getEntityProfiles();
        System.out.println("Input Entity Profiles\t:\t" + profiles.size());

        IGroundTruthReader gtReader = new GtSerializationReader(groundTruthFilePath);
        final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(gtReader.getDuplicatePairs(eReader.getEntityProfiles()));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        double time1 = System.currentTimeMillis();

        IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(BlockBuildingMethod.STANDARD_BLOCKING);
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles, null);
        System.out.println("Original blocks\t:\t" + blocks.size());

        StringBuilder blockingWorkflowConf = new StringBuilder();
        StringBuilder blockingWorkflowName = new StringBuilder();
        blockingWorkflowConf.append(blockBuildingMethod.getMethodConfiguration());
        blockingWorkflowName.append(blockBuildingMethod.getMethodName());

        IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(BlockBuildingMethod.STANDARD_BLOCKING);
        if (blockCleaningMethod != null) {
            blocks = blockCleaningMethod.refineBlocks(blocks);
            blockingWorkflowConf.append("\n").append(blockCleaningMethod.getMethodConfiguration());
            blockingWorkflowName.append("->").append(blockCleaningMethod.getMethodName());
        }

        IBlockProcessing comparisonCleaningMethod = BlockBuildingMethod.getDefaultComparisonCleaning(BlockBuildingMethod.STANDARD_BLOCKING);
        if (comparisonCleaningMethod != null) {
            blocks = comparisonCleaningMethod.refineBlocks(blocks);
            blockingWorkflowConf.append("\n").append(comparisonCleaningMethod.getMethodConfiguration());
            blockingWorkflowName.append("->").append(comparisonCleaningMethod.getMethodName());
        }

        double time2 = System.currentTimeMillis();

        BlocksPerformance blp = new BlocksPerformance(blocks, duplicatePropagation);
        blp.setStatistics();
        blp.printStatistics(time2 - time1, blockingWorkflowConf.toString(), blockingWorkflowName.toString());

        for (EntityMatchingMethod emMethod : EntityMatchingMethod.values()) {

            double time3 = System.currentTimeMillis();

            IEntityMatching em = EntityMatchingMethod.getDefaultConfiguration(emMethod);
            SimilarityPairs simPairs = em.executeComparisons(blocks, profiles);

            double time4 = System.currentTimeMillis();

            for (EntityClusteringDerMethod ecMethod : EntityClusteringDerMethod.values()) {
                double time5 = System.currentTimeMillis();

                IEntityClustering ec = EntityClusteringDerMethod.getDefaultConfiguration(ecMethod);
                List<EquivalenceCluster> entityClusters = ec.getDuplicates(simPairs);

                double time6 = System.currentTimeMillis();

                StringBuilder matchingWorkflowConf = new StringBuilder();
                StringBuilder matchingWorkflowName = new StringBuilder();
                matchingWorkflowConf.append(em.getMethodConfiguration());
                matchingWorkflowName.append(em.getMethodName());
                matchingWorkflowConf.append("\n").append(ec.getMethodConfiguration());
                matchingWorkflowName.append("->").append(ec.getMethodName());
                
//                try {
//                    PrintToFile.toCSV(entityClusters, "/home/ethanos/workspace/JedAIToolkitNew/rest.csv");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }

                ClustersPerformance clp = new ClustersPerformance(entityClusters, duplicatePropagation);
                clp.setStatistics();
                clp.printStatistics(time6 - time5 + time4 - time3, matchingWorkflowConf.toString(), matchingWorkflowName.toString());
            }
        }
    }
}
