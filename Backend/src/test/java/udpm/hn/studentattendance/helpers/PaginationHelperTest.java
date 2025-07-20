package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

import static org.assertj.core.api.Assertions.*;

class PaginationHelperTest {

    // Concrete test implementation of PageableRequest
    static class TestPageableRequest extends PageableRequest {
    }

    @Test
    void canInstantiate() {
        PaginationHelper helper = new PaginationHelper();
        assertThat(helper).isNotNull();
    }

    @Test
    void testCreatePageableWithDefaultSort() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSortBy("created_at");
        request.setOrderBy("desc");
        Pageable pageable = PaginationHelper.createPageable(request);
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("created_at")).isNotNull();
    }

    @Test
    void testCreatePageableWithCustomSort() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(2);
        request.setSize(5);
        request.setSortBy("name");
        request.setOrderBy("ASC");
        Pageable pageable = PaginationHelper.createPageable(request, "created_at");
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testCreatePageableWithDefaultSortAndOrder() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(1);
        request.setSize(0);
        request.setSortBy("");
        request.setOrderBy("");
        Pageable pageable = PaginationHelper.createPageable(request, "id", "DESC");
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isGreaterThan(0);
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testCreatePageableWithNullSortByAndOrderBy() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(1);
        request.setSize(0);
        request.setSortBy(null);
        request.setOrderBy(null);
        Pageable pageable = PaginationHelper.createPageable(request, "id", "ASC");
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testCreatePageableWithCustomSortAndOrder() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(3);
        request.setSize(20);
        request.setSortBy("updated_at");
        request.setOrderBy("DESC");
        Pageable pageable = PaginationHelper.createPageable(request, "created_at", "ASC");
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("updated_at")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("updated_at").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testCreatePageableWithEmptySortBy() {
        TestPageableRequest request = new TestPageableRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSortBy("");
        request.setOrderBy("ASC");
        Pageable pageable = PaginationHelper.createPageable(request, "default_field");
        assertThat(pageable.getSort().getOrderFor("default_field")).isNotNull();
    }
}
