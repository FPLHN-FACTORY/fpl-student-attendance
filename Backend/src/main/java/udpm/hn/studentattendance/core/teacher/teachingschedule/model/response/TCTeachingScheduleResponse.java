package udpm.hn.studentattendance.core.teacher.teachingschedule.model.response;

public interface TCTeachingScheduleResponse {

    Integer getIndexs();

    String getIdPlanDate();

    Long getStartTeaching();

    Long getEndTeaching();

    String getShift();

    String getSubjectCode();

    String getFactoryName();

    String getProjectName();

    String getDescription();

    Integer getLateArrival();

    String getFactoryId();

    String getUserId();

    Integer getType();

    String getLink();

    String getRoom();

}
