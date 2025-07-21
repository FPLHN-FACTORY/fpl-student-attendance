package udpm.hn.studentattendance.infrastructure.excel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteExcelConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteExcelConstant.URL_API_SCHEDULED_STUDENT)
public class EXStudentScheduledRestController {
}
