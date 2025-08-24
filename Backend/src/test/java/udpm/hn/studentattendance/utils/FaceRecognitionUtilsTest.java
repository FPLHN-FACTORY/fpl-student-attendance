package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FaceRecognitionUtilsTest {
    @Test
    void testParseEmbedding_valid() {
        String json = "[0.1, 0.2, 0.3]";
        float[] arr = FaceRecognitionUtils.parseEmbedding(json);
        assertArrayEquals(new float[] { 0.1f, 0.2f, 0.3f }, arr, 1e-6f);
    }

    @Test
    void testParseEmbedding_invalid() {
        String json = "not a json";
        float[] arr = FaceRecognitionUtils.parseEmbedding(json);
        assertEquals(0, arr.length);
    }

    @Test
    void testParseEmbeddings_valid() {
        String json = "[[0.1,0.2],[0.3,0.4]]";
        List<float[]> list = FaceRecognitionUtils.parseEmbeddings(json);
        assertEquals(2, list.size());
        assertArrayEquals(new float[] { 0.1f, 0.2f }, list.get(0), 1e-6f);
        assertArrayEquals(new float[] { 0.3f, 0.4f }, list.get(1), 1e-6f);
    }

    @Test
    void testParseEmbeddings_invalid() {
        String json = "not a json";
        List<float[]> list = FaceRecognitionUtils.parseEmbeddings(json);
        assertTrue(list.isEmpty());
    }

    @Test
    void testCosineSimilarity() {
        float[] v1 = { 1, 0 };
        float[] v2 = { 1, 0 };
        assertEquals(1.0, FaceRecognitionUtils.cosineSimilarity(v1, v2), 1e-9);
        float[] v3 = { 0, 1 };
        assertEquals(0.0, FaceRecognitionUtils.cosineSimilarity(v1, v3), 1e-9);
    }

    @Test
    void testIsSameFace() {
        float[] v1 = { 1, 0 };
        float[] v2 = { 1, 0 };
        assertTrue(FaceRecognitionUtils.isSameFace(v1, v2, 0.5));
        assertFalse(FaceRecognitionUtils.isSameFace(v1, new float[] { 0, 1 }, 0.5));
    }

    @Test
    void testIsSameFaces() {
        List<float[]> emb1 = List.of(new float[] { 1, 0 }, new float[] { 0, 1 });
        float[] emb2 = { 1, 0 };
        assertTrue(FaceRecognitionUtils.isSameFaces(emb1, emb2, 0.5));
        assertFalse(FaceRecognitionUtils.isSameFaces(emb1, new float[] { -1, 0 }, 0.9));
    }

    @Test
    void testIsSameFaceAndResult() {
        List<float[]> emb1 = List.of(new float[] { 1, 0 }, new float[] { 0, 1 });
        float[] emb2 = { 1, 0 };
        float[] result = FaceRecognitionUtils.isSameFaceAndResult(emb1, emb2, 0.5);
        assertNotNull(result);
        assertArrayEquals(new float[] { 1, 0 }, result, 1e-6f);
        assertNull(FaceRecognitionUtils.isSameFaceAndResult(emb1, new float[] { -1, 0 }, 0.9));
    }
}
