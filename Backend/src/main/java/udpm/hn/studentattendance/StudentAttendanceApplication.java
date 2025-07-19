package udpm.hn.studentattendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class StudentAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentAttendanceApplication.class, args);
    }

}
