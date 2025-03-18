package udpm.hn.studentattendance.core.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_STUDENT_MANAGEMENT + "/test")
@RequiredArgsConstructor
public class testRestController {

    @GetMapping
    public ResponseEntity<ApiResponse> test() {
        return RouterHelper.createResponseApi(ApiResponse.success("Thành công"), HttpStatus.OK);
    }

}
