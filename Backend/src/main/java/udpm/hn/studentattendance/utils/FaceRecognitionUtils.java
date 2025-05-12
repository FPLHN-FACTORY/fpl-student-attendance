package udpm.hn.studentattendance.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class FaceRecognitionUtils {

    public final static double THRESHOLD = 0.45;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static double[] parseEmbedding(String embeddingStr) {
        try {
            return objectMapper.readValue(embeddingStr, double[].class);
        } catch (JsonProcessingException e) {
            return new double[0];
        }
    }

    public static List<double[]> parseEmbeddings(String embeddingsStr) {
        try {
            return objectMapper.readValue(embeddingsStr, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public static double calculateEuclideanDistance(double[] emb1, double[] emb2) {
        if (emb1.length != emb2.length) {
            return THRESHOLD;
        }

        double sum = 0.0;
        for (int i = 0; i < emb1.length; i++) {
            sum += Math.pow(emb1[i] - emb2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static boolean isSameFace(List<double[]> emb1, double[] emb2) {
        return isSameFace(emb1, emb2, THRESHOLD);
    }

    public static boolean isSameFace(List<double[]> emb1, double[] emb2, double threshold) {
        for (double[] input : emb1) {
            if (calculateEuclideanDistance(input, emb2) < threshold) {
                return true;
            }
        }
        return false;
    }

}
