package udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response;

public interface Teacher_TeachingScheduleResponse {
    Integer getIndexs();

    String getIdPlanDate();

    Long getTeachingDay();

    Integer getShift();

    String getSubjectCode();

    String getFactoryName();

    String getProjectName();

    String getDescription();

    Integer getLateArrival();

}
