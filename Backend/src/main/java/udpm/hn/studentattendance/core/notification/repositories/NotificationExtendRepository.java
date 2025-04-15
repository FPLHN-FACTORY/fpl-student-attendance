package udpm.hn.studentattendance.core.notification.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest;
import udpm.hn.studentattendance.core.notification.model.response.NotificationResponse;
import udpm.hn.studentattendance.repositories.NotificationRepository;

import java.util.Optional;

@Repository
public interface NotificationExtendRepository extends NotificationRepository {

    @Query(value = """
        SELECT new udpm.hn.studentattendance.core.notification.model.response.NotificationResponse(
                n.id, 
                n.type, 
                n.data, 
                n.status, 
                n.createdAt
            )
        FROM Notification n
        WHERE 
            n.idUser = :#{#request.idUser} AND
            (:#{#request.status} IS NULL OR n.status = :#{#request.entityStatus})
        ORDER BY n.createdAt DESC
    """, countQuery = """
        SELECT COUNT(n.id)
        FROM Notification n
        WHERE 
            n.idUser = :#{#request.idUser} AND
            (:#{#request.status} IS NULL OR n.status = :#{#request.entityStatus})
    """)
    Page<NotificationResponse> getAllByFilter(Pageable pageable, NotificationFilterRequest request);

    @Query(value = """
        SELECT new udpm.hn.studentattendance.core.notification.model.response.NotificationResponse(
                n.id, 
                n.type, 
                n.data, 
                n.status, 
                n.createdAt
            )
        FROM Notification n
        WHERE 
            n.idUser = :#{#request.idUser} AND
            n.id = :id
    """)
    Optional<NotificationResponse> getDataById(String id, String idUser);

    @Query(value = """
        SELECT 
            COUNT(id)
        FROM notification
        WHERE 
            id_user = :idUser AND
            status = 1
    """, nativeQuery = true)
    int count(String idUser);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM notification
        WHERE
            id_user = :#{#request.idUser} AND
            id IN(:#{#request.ids})
    """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteMultipleById(NotificationModifyRequest request);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM notification
        WHERE
            id_user = :idUser
    """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllNotification(String idUser);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE notification
        SET
            status = 0
        WHERE
            id_user = :idUser
    """, countQuery = "SELECT 1", nativeQuery = true)
    int markReadAll(String idUser);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE notification
        SET
            status = 0
        WHERE
            id_user = :#{#request.idUser} AND
            id IN(:#{#request.ids})
    """, countQuery = "SELECT 1", nativeQuery = true)
    int markReadMultipleById(NotificationModifyRequest request);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE notification
        SET
            status = 1
        WHERE
            id_user = :#{#request.idUser} AND
            id IN(:#{#request.ids})
    """, countQuery = "SELECT 1", nativeQuery = true)
    int markUnreadMultipleById(NotificationModifyRequest request);

}
