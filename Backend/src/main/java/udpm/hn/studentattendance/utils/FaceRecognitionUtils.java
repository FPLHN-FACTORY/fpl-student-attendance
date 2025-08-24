package udpm.hn.studentattendance.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class FaceRecognitionUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static float[] parseEmbedding(String embeddingStr) {
        try {
            return objectMapper.readValue(embeddingStr, float[].class);
        } catch (JsonProcessingException e) {
            return new float[0];
        }
    }

    public static List<float[]> parseEmbeddings(String embeddingsStr) {
        try {
            return objectMapper.readValue(embeddingsStr, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public static double cosineSimilarity(float[] vec1, float[] vec2) {
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

    public static boolean isSameFaces(List<float[]> emb1, float[] emb2, double threshold) {
        for (float[] input : emb1) {
            if (isSameFace(input, emb2, threshold)) {
                return true;
            }
        }
        return false;
    }

    public static float[] isSameFaceAndResult(List<float[]> emb1, float[] emb2, double threshold) {
        for (float[] input : emb1) {
            if (isSameFace(input, emb2, threshold)) {
                return input;
            }
        }
        return null;
    }

    public static boolean isSameFace(float[] emb1, float[] emb2, double threshold) {
        return cosineSimilarity(emb1, emb2) > threshold;
    }

}
