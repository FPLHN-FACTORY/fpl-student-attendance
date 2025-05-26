package udpm.hn.studentattendance.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = {
            "/",
            "/admin-panel",
            "/admin-panel/**",
            "/Admin",
            "/Admin/**",
            "/Staff",
            "/Staff/**",
            "/Teacher",
            "/Teacher/**",
            "/Student",
            "/Student/**"
    })
    public String forward() {
        return "forward:/index.html";
    }

}
