package udpm.hn.studentattendance.helpers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;
import udpm.hn.studentattendance.infrastructure.constants.PaginationConstant;

public class PaginationHelper {

    public static Pageable createPageable(PageableRequest request, String defaultSortBy) {
        return PageRequest.of(
                request.getPage() - 1,
                request.getSize() == 0 ? PaginationConstant.DEFAULT_SIZE : request.getSize(),
                Sort.by(
                        (Sort.Direction.fromString(
                                request.getOrderBy()) == Sort.Direction.DESC ||
                                request.getOrderBy() == null
                        ) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        (request.getSortBy() == null
                                || request.getSortBy().isEmpty()
                        ) ? defaultSortBy : request.getSortBy()
                ));
    }

    public static Pageable createPageable(PageableRequest request) {
        return createPageable(request, "created_at");
    }


    public static Pageable createPageable(PageableRequest request, String defaultSortBy, String defaultOrderBy) {
        int page = request.getPage() - 1;
        int size = request.getSize() == 0 ? PaginationConstant.DEFAULT_SIZE : request.getSize();
        Sort.Direction direction = request.getOrderBy() == null || request.getOrderBy().isEmpty()
                ? Sort.Direction.fromString(defaultOrderBy)
                : Sort.Direction.fromString(request.getOrderBy());
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = defaultSortBy;
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }



}
