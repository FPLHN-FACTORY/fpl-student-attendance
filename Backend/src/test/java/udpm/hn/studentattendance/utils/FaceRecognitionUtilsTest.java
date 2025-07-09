package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FaceRecognitionUtilsTest {
    @Test
    void canInstantiate() {
        FaceRecognitionUtils utils = new FaceRecognitionUtils();
        assertThat(utils).isNotNull();
    }
    // Thêm test cho các method chính nếu có
}