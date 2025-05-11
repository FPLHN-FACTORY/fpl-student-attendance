package udpm.hn.studentattendance.core.teacher.teachingschedule.model.response;

public interface TCTSDetailPlanDateResponse {
    Integer getLateArrival();

    String getDescription();

    String getPlanDateId();

    String getLink();

    String getRoom();

    Integer getType();
}
