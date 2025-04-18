package experiments.AlphaZero;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.nn.*;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;

import java.util.ArrayList;
import java.util.List;

public class NetworkFactory {

    /**
     * current approach: the hidden layer for the policy head should have as its size the smallest
     * power of two that is larger or equal to the policy size itself.
     * @param policySize
     * @return hidden layer size
     */
    public static int hiddenLayerSize(int policySize) {
        int pos = (int) Math.ceil(log2(policySize));
        return (int) Math.pow(2,pos);
    }

    public static double log2(double n) {
        return Math.log(n)/Math.log(2);
    }

    public static Block mlpBlock(int[] sizeOfPolicyOutputsPerDimension, int numberOfColors) {
        SequentialBlock body = new SequentialBlock();
        body
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder()
                        .setUnits(256)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Linear.builder()
                        .setUnits(256)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Linear.builder()
                        .setUnits(256)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Linear.builder()
                        .setUnits(256)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu);
        List<Block> parallelBlocks = new ArrayList<>();
        SequentialBlock valueBlock = new SequentialBlock();
        valueBlock
                .add(Linear.builder()
                        .setUnits(32)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Linear.builder()
                        .setUnits(numberOfColors)
                        .build());
        parallelBlocks.add(valueBlock);
        for(int moveDimension=0; moveDimension<sizeOfPolicyOutputsPerDimension.length; moveDimension++) {
            SequentialBlock policyBlock = new SequentialBlock();
            policyBlock
                    .add(Linear.builder()
                            .setUnits(hiddenLayerSize(sizeOfPolicyOutputsPerDimension[moveDimension]))
                            .build())
                    .add(BatchNorm.builder().build())
                    .add(Activation::relu)
                    .add(Linear.builder()
                            .setUnits(sizeOfPolicyOutputsPerDimension[moveDimension])
                            .build());
            parallelBlocks.add(policyBlock);
        }
        ParallelBlock outputBlocks = new ParallelBlock(
                list -> {
                    NDArray[] outputArrays = new NDArray[list.size()];
                    for(int i=0; i<list.size(); i++) {
                        NDList outputList = list.get(i);
                        outputArrays[i] = outputList.singletonOrThrow();
                    }
                    return new NDList(outputArrays);
                },
                parallelBlocks);
        SequentialBlock resultBlock = new SequentialBlock();
        resultBlock
                .add(body)
                .add(outputBlocks);
        return resultBlock;
    }

    public static Block quantileBlock(int[] sizeOfPolicyOutputsPerDimension, int numberOfColors) {
        int numberOfHeads = 0;
        SequentialBlock residualBlock = new SequentialBlock();
        residualBlock //128-16, 64-32, 64-32-16, 32-64-16, 64-128-16? 64-128-32-16?
                .add(new ResidualBlock(32, true, null))
                .add(new ResidualBlock(16, true, null))
                .add(Blocks.batchFlattenBlock());
        List<Block> parallelBlocks = new ArrayList<>();
        SequentialBlock valueBaseBlock = new SequentialBlock();
        valueBaseBlock
                .add(Linear.builder()
                        .setUnits(32)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu);
        List<Block> innerParallelBlocks = new ArrayList<>();
        SequentialBlock valueHead = new SequentialBlock();
        valueHead
                .add(Linear.builder()
                        .setUnits(numberOfColors)
                        .build());
        innerParallelBlocks.add(valueHead);
        numberOfHeads++;
        SequentialBlock lowerQuantileHead = new SequentialBlock();
        lowerQuantileHead
                .add(Linear.builder()
                        .setUnits(numberOfColors)
                        .build());
        innerParallelBlocks.add(lowerQuantileHead);
        numberOfHeads++;
        SequentialBlock upperQuantileHead = new SequentialBlock();
        upperQuantileHead
                .add(Linear.builder()
                        .setUnits(numberOfColors)
                        .build());
        innerParallelBlocks.add(upperQuantileHead);
        numberOfHeads++;
        ParallelBlock valueHeads = new ParallelBlock(
                list -> {
                    NDArray[] outputArrays = new NDArray[list.size()];
                    for(int i=0; i<list.size(); i++) {
                        NDList outputList = list.get(i);
                        outputArrays[i] = outputList.singletonOrThrow();
                    }
                    return new NDList(outputArrays);
                },
                innerParallelBlocks);
        SequentialBlock valueBlock = new SequentialBlock();
        valueBlock
                .add(valueBaseBlock)
                .add(valueHeads);
        parallelBlocks.add(valueBlock);
        for(int moveDimension=0; moveDimension<sizeOfPolicyOutputsPerDimension.length; moveDimension++) {
            SequentialBlock policyHead = new SequentialBlock();
            policyHead
                    .add(Linear.builder()
                            .setUnits(hiddenLayerSize(sizeOfPolicyOutputsPerDimension[moveDimension]))
                            .build())
                    .add(BatchNorm.builder().build())
                    .add(Activation::relu)
                    .add(Linear.builder()
                            .setUnits(sizeOfPolicyOutputsPerDimension[moveDimension])
                            .build());
            parallelBlocks.add(policyHead);
            numberOfHeads++;
        }
        int finalNumberOfHeads = numberOfHeads;
        ParallelBlock outputBlocks = new ParallelBlock(
                list -> {
                    NDArray[] outputArrays = new NDArray[finalNumberOfHeads];
                    int index = 0;
                    for(int i=0; i<list.size(); i++) {
                        NDList outputList = list.get(i);
                        if(outputList.size()>1) {
                            for(int j=0; j<outputList.size(); j++) {
                                outputArrays[index++] = outputList.get(j);
                            }
                        } else {
                            outputArrays[index++] = outputList.singletonOrThrow();
                        }
                    }
                    return new NDList(outputArrays);
                },
                parallelBlocks);
        SequentialBlock resultBlock = new SequentialBlock();
        resultBlock
                .add(residualBlock)
                .add(outputBlocks);
        return resultBlock;
    }

    public static Block residualEvaluationAndPolicyBlock(int[] sizeOfPolicyOutputsPerDimension, int numberOfColors) {
        if(false) {
            return mlpBlock(sizeOfPolicyOutputsPerDimension, numberOfColors);
        } else {
            SequentialBlock residualBlock = new SequentialBlock();
            residualBlock //128-16, 64-32, 64-32-16, 32-64-16, 64-128-16? 64-128-32-16?
//                    .add(new ResidualBlock(128, true, null))
//                    .add(new ResidualBlock(64, true, null))
                    .add(new ResidualBlock(32, true, null))
                    .add(new ResidualBlock(16, true, null))
                    .add(Blocks.batchFlattenBlock());
            List<Block> parallelBlocks = new ArrayList<>();
            SequentialBlock valueBlock = new SequentialBlock();
            valueBlock
                    .add(Linear.builder()
                            .setUnits(32)
                            .build())
                    .add(BatchNorm.builder().build())
                    .add(Activation::relu)
                    .add(Linear.builder()
                            .setUnits(numberOfColors)
                            .build());
            parallelBlocks.add(valueBlock);
            for(int moveDimension=0; moveDimension<sizeOfPolicyOutputsPerDimension.length; moveDimension++) {
                SequentialBlock policyBlock = new SequentialBlock();
                policyBlock
                        .add(Linear.builder()
                                .setUnits(hiddenLayerSize(sizeOfPolicyOutputsPerDimension[moveDimension]))
                                .build())
                        .add(BatchNorm.builder().build())
                        .add(Activation::relu)
                        .add(Linear.builder()
                                .setUnits(sizeOfPolicyOutputsPerDimension[moveDimension])
                                .build());
                parallelBlocks.add(policyBlock);
            }
            ParallelBlock outputBlocks = new ParallelBlock(
                    list -> {
                        NDArray[] outputArrays = new NDArray[list.size()];
                        for(int i=0; i<list.size(); i++) {
                            NDList outputList = list.get(i);
                            outputArrays[i] = outputList.singletonOrThrow();
                        }
                        return new NDList(outputArrays);
                    },
                    parallelBlocks);
            SequentialBlock resultBlock = new SequentialBlock();
            resultBlock
                    .add(residualBlock)
                    .add(outputBlocks);
            return resultBlock;
        }
    }

}
