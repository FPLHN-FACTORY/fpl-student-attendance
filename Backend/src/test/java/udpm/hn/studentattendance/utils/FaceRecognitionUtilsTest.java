package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FaceRecognitionUtilsTest {
    @Test
    void testParseEmbedding_valid() {
        String json = "[0.1, 0.2, 0.3]";
        double[] arr = FaceRecognitionUtils.parseEmbedding(json);
        assertArrayEquals(new double[] { 0.1, 0.2, 0.3 }, arr, 1e-9);
    }

    @Test
    void testParseEmbedding_invalid() {
        String json = "not a json";
        double[] arr = FaceRecognitionUtils.parseEmbedding(json);
        assertEquals(0, arr.length);
    }

    @Test
    void testParseEmbeddings_valid() {
        String json = "[[0.1,0.2],[0.3,0.4]]";
        List<double[]> list = FaceRecognitionUtils.parseEmbeddings(json);
        assertEquals(2, list.size());
        assertArrayEquals(new double[] { 0.1, 0.2 }, list.get(0), 1e-9);
        assertArrayEquals(new double[] { 0.3, 0.4 }, list.get(1), 1e-9);
    }

    @Test
    void testParseEmbeddings_invalid() {
        String json = "not a json";
        List<double[]> list = FaceRecognitionUtils.parseEmbeddings(json);
        assertTrue(list.isEmpty());
    }

    @Test
    void testCosineSimilarity() {
        double[] v1 = { 1, 0 };
        double[] v2 = { 1, 0 };
        assertEquals(1.0, FaceRecognitionUtils.cosineSimilarity(v1, v2), 1e-9);
        double[] v3 = { 0, 1 };
        assertEquals(0.0, FaceRecognitionUtils.cosineSimilarity(v1, v3), 1e-9);
    }

    @Test
    void testIsSameFace() {
        double[] v1 = { 1, 0 };
        double[] v2 = { 1, 0 };
        assertTrue(FaceRecognitionUtils.isSameFace(v1, v2, 0.5));
        assertFalse(FaceRecognitionUtils.isSameFace(v1, new double[] { 0, 1 }, 0.5));
    }

    @Test
    void testIsSameFaces() {
        List<double[]> emb1 = List.of(new double[] { 1, 0 }, new double[] { 0, 1 });
        double[] emb2 = { 1, 0 };
        assertTrue(FaceRecognitionUtils.isSameFaces(emb1, emb2, 0.5));
        assertFalse(FaceRecognitionUtils.isSameFaces(emb1, new double[] { -1, 0 }, 0.9));
    }

    @Test
    void testIsSameFaceAndResult() {
        List<double[]> emb1 = List.of(new double[] { 1, 0 }, new double[] { 0, 1 });
        double[] emb2 = { 1, 0 };
        double[] result = FaceRecognitionUtils.isSameFaceAndResult(emb1, emb2, 0.5);
        assertNotNull(result);
        assertArrayEquals(new double[] { 1, 0 }, result, 1e-9);
        assertNull(FaceRecognitionUtils.isSameFaceAndResult(emb1, new double[] { -1, 0 }, 0.9));
    }
}