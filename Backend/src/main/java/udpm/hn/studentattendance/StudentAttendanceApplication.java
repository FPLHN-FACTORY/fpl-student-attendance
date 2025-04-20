package udpm.hn.studentattendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class StudentAttendanceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StudentAttendanceApplication.class, args);
    }

}
