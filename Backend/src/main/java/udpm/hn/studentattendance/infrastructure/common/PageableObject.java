package udpm.hn.studentattendance.infrastructure.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageableObject<T> {

    private List<T> data;

    private long totalPages;

    private int currentPage;

    public PageableObject(Page<T> page) {
        this.data = page.getContent();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
    }

    public static <T> PageableObject<T> of(Page<T> page) {
        return new PageableObject<>(page);
    }

}
