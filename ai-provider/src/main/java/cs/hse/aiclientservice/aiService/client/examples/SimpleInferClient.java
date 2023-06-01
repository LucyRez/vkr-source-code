// Copyright (c) 2021, NVIDIA CORPORATION & AFFILIATES. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package cs.hse.aiclientservice.aiService.client.examples;

import com.google.common.collect.Lists;
import cs.hse.aiclientservice.aiService.client.*;
import cs.hse.aiclientservice.aiService.client.pojo.DataType;
import cs.hse.aiclientservice.aiService.s3controller.AmazonClient;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author xiafei.qiuxf
 * @date 2021/7/28
 */
public class SimpleInferClient {

    Logger logger = LoggerFactory.getLogger(SimpleInferClient.class);

    public float[][][] oneDimToThreeDim(float[] oneDimArray, int width, int height, int depth) {
        float[][][] threeDimArray = new float[depth][height][width];
        int index = 0;
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    threeDimArray[z][y][x] = oneDimArray[index++];
                }
            }
        }
        return threeDimArray;
    }

    private void floatArrayToJpg(float[][] floatArray, float[][] inputFloatArray, int width, int height, String fileName) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                float value = floatArray[y][x];
                int colorValue = (int) (inputFloatArray[y][x] * 128);
                Color color = new Color(colorValue, colorValue, colorValue);
                if (value > 0.5) {
                    colorValue = (int) (inputFloatArray[y][x] * 255);
                    color = new Color(0, colorValue, 0);
                }
                image.setRGB(x, y, color.getRGB());
            }
        }

        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        ImageIO.write(image, "jpg", fileOutputStream);
        fileOutputStream.close();
    }



    public float[] intArrayToFloatArray(int[] intArray) {
        var collectionMin = Arrays.stream(intArray).min().getAsInt();
        var collectionMax = Arrays.stream(intArray).max().getAsInt();
        float[] floatArray = new float[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            floatArray[i] = ((float) intArray[i] - (float) collectionMin + 1) / ((float) collectionMax - (float) collectionMin + 1);
        }
        return floatArray;
    }

    private float countVolume(float[][] floatArray, int width, int height) {
        float result = 0.0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = floatArray[y][x];
                if (value > 0.5) {
                    result++;
                }
            }
        }
        return result;

    }

    public float getMaskData(String key, float[][][] inputThreeDimArray, float[] arrForInfer, int width, int height, int depth) {
        // Initialize the data
        boolean isBinary0 = false;
        InferInput input0 = new InferInput("INPUT__0", new long[]{1L, 1L, 512, 512}, DataType.FP32);
        List<Integer> lst_0 = IntStream.rangeClosed(1, 512 * 512).boxed().collect(Collectors.toList());
        input0.setData(arrForInfer, isBinary0);
        logger.info("Set the data to input");


        List<InferInput> inputs = Lists.newArrayList(input0);
        List<InferRequestedOutput> outputs = Lists.newArrayList(
                new InferRequestedOutput("OUTPUT__0", isBinary0));

        try {
            InferenceServerClient client = new InferenceServerClient("127.0.0.1:8000", 2000000000, 2000000000);
            logger.info("Initialized client");

            InferResult result = client.infer("unet", inputs, outputs);

            logger.info("get the result");
            // Get the output arrays from the results
            float[] op0 = result.getOutputAsFloat("OUTPUT__0");

            logger.info("Got the result: " + op0.length);

            float[][][] threeDimArray = oneDimToThreeDim(op0, width, height, depth);
            System.out.println(threeDimArray.length);
            System.out.println(threeDimArray[0].length);
            System.out.println(threeDimArray[0][1].length);

            float totalVolume = 0f;

            for (int i = 0; i < threeDimArray.length; i++) {
                String fileName = key + "_mask" + ".jpg";
                floatArrayToJpg(threeDimArray[i], inputThreeDimArray[i], width, height, fileName);
                totalVolume += countVolume(threeDimArray[i], width, height);
            }

            float thickness = 2.5f;
            float step = 16;
            float thicknessResolution = 0.5f * 0.5f;
            totalVolume *= thickness * thicknessResolution;
            totalVolume *= step;

            return totalVolume;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return 0f;
    }

}

