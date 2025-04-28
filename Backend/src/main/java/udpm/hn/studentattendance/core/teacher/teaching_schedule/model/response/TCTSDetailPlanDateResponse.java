package udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response;

public interface TCTSDetailPlanDateResponse {
    Integer getLateArrival();

    String getDescription();

    String getPlanDateId();

    String getLink();

    Integer getType();
}
