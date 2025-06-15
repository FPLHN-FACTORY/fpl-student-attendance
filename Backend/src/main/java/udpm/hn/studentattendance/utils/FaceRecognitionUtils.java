package udpm.hn.studentattendance.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class FaceRecognitionUtils {

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

    public static double cosineSimilarity(double[] vec1, double[] vec2) {
        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static boolean isSameFaces(List<double[]> emb1, double[] emb2, double threshold) {
        for (double[] input : emb1) {
            if (isSameFace(input, emb2, threshold)) {
                return true;
            }
        }
        return false;
    }

    public static double[] isSameFaceAndResult(List<double[]> emb1, double[] emb2, double threshold) {
        for (double[] input : emb1) {
            if (isSameFace(input, emb2, threshold)) {
                return input;
            }
        }
        return null;
    }

    public static boolean isSameFace(double[] emb1, double[] emb2, double threshold) {
        double a = cosineSimilarity(emb1, emb2);
        System.out.println("cosineSimilarity: " + a);
        return a > threshold;
    }

}
