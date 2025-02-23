package udpm.hn.studentattendance.infrastructure.common;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.PaginationConstant;

@Setter
@Getter
public abstract class PageableRequest {

    private int page = PaginationConstant.DEFAULT_PAGE;

    private int size = PaginationConstant.DEFAULT_SIZE;

    private String orderBy = PaginationConstant.DEFAULT_ORDER_BY;

    private String sortBy = PaginationConstant.DEFAULT_SORT_BY;

}
