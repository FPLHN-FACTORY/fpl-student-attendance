package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PageableObjectTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        List<String> data = Arrays.asList("a", "b");
        PageableObject<String> po = new PageableObject<>(data, 5L, 2);
        assertEquals(data, po.getData());
        assertEquals(5L, po.getTotalPages());
        assertEquals(2, po.getCurrentPage());
    }

    @Test
    void testPageConstructorAndOfStatic() {
        Page<String> page = mock(Page.class);
        when(page.getContent()).thenReturn(Arrays.asList("x", "y"));
        when(page.getTotalPages()).thenReturn(3);
        when(page.getNumber()).thenReturn(1);

        PageableObject<String> po1 = new PageableObject<>(page);
        assertEquals(Arrays.asList("x", "y"), po1.getData());
        assertEquals(3, po1.getTotalPages());
        assertEquals(1, po1.getCurrentPage());

        PageableObject<String> po2 = PageableObject.of(page);
        assertEquals(Arrays.asList("x", "y"), po2.getData());
        assertEquals(3, po2.getTotalPages());
        assertEquals(1, po2.getCurrentPage());
    }
}