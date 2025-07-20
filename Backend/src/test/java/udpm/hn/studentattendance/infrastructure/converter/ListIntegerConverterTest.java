package udpm.hn.studentattendance.infrastructure.converter;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ListIntegerConverterTest {
    @Test
    void testListIntegerConverterExists() {
        ListIntegerConverter converter = new ListIntegerConverter();
        assertNotNull(converter);
    }

    @Test
    void testConvertToDatabaseColumn() {
        ListIntegerConverter converter = new ListIntegerConverter();
        List<Integer> integers = List.of(1, 2, 3, 4, 5);

        String result = converter.convertToDatabaseColumn(integers);

        assertNotNull(result);
        assertEquals("1,2,3,4,5", result);
    }

    @Test
    void testConvertToDatabaseColumnWithEmptyList() {
        ListIntegerConverter converter = new ListIntegerConverter();
        List<Integer> integers = List.of();

        String result = converter.convertToDatabaseColumn(integers);

        assertEquals("", result);
    }

    @Test
    void testConvertToDatabaseColumnWithNull() {
        ListIntegerConverter converter = new ListIntegerConverter();

        String result = converter.convertToDatabaseColumn(null);

        assertEquals("", result);
    }

    @Test
    void testConvertToEntityAttribute() {
        ListIntegerConverter converter = new ListIntegerConverter();
        String dbData = "1,2,3,4,5";

        List<Integer> result = converter.convertToEntityAttribute(dbData);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(1, result.get(0));
        assertEquals(5, result.get(4));
    }

    @Test
    void testConvertToEntityAttributeWithEmptyString() {
        ListIntegerConverter converter = new ListIntegerConverter();

        List<Integer> result = converter.convertToEntityAttribute("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEntityAttributeWithNull() {
        ListIntegerConverter converter = new ListIntegerConverter();

        List<Integer> result = converter.convertToEntityAttribute(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
