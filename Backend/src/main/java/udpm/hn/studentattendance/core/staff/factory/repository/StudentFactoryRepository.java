package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.StudentFactoryResponse;
import udpm.hn.studentattendance.repositories.UserStudentProjectRepository;

@Repository
public interface StudentFactoryRepository extends UserStudentProjectRepository {

//    @Query(value =
//            """
//
//                    """
//            ,
//            countQuery =
//                    """
//                            """
//            , nativeQuery = true
//    )
//    Page<StudentFactoryResponse> getUserStudentInFactory(Pageable pageable, StudentFactoryRequest studentFactoryRequest);
}
