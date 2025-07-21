package udpm.hn.studentattendance.helpers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.PaginationConstant;

public class PaginationHelper {

    public static Pageable createPageable(PageableRequest request, String defaultSortBy) {
        return createPageable(request, defaultSortBy, "ASC");
    }

    public static Pageable createPageable(PageableRequest request) {
        return createPageable(request, "created_at", "DESC");
    }

    public static Pageable createPageable(PageableRequest request, String defaultSortBy, String defaultOrderBy) {
        int page = request.getPage() - 1;
        int size = request.getSize() == 0 ? PaginationConstant.DEFAULT_SIZE : request.getSize();

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(
                    (request.getOrderBy() == null || request.getOrderBy().isEmpty())
                            ? defaultOrderBy
                            : request.getOrderBy()
            );
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.fromString(defaultOrderBy);
        }

        String sortBy = (request.getSortBy() == null || request.getSortBy().isEmpty())
                ? defaultSortBy
                : request.getSortBy();

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

}