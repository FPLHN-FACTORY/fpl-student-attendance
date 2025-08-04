package udpm.hn.studentattendance.core.support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.support.model.request.SupportEmailRequest;
import udpm.hn.studentattendance.core.support.service.SupportService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

import java.util.List;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_SUPPORT)
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupportController {

    private final SupportService supportService;

    @PostMapping(value = "/send-mail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendSupportRequest(
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        SupportEmailRequest request = new SupportEmailRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setFiles(files != null ? files.toArray(new MultipartFile[0]) : null);

        return supportService.sendSupportMail(request);
    }

    @PostMapping("/send-mail-json")
    public ResponseEntity<?> sendSupportRequestJson(@Valid @RequestBody SupportEmailRequest supportEmailRequest) {
        return supportService.sendSupportMail(supportEmailRequest);
    }
}