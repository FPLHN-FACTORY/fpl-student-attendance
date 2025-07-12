package udpm.hn.studentattendance.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_staff")
@DynamicUpdate
public class UserStaff extends PrimaryEntity implements UserDetails {

    @Column(name = "email_fe", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String emailFe;

    @Column(name = "email_fpt", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String emailFpt;

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String name;

    @Column(name = "code", length = EntityProperties.LENGTH_CODE)
    private String code;

    @Column(name = "image", length = EntityProperties.LENGTH_TEXT)
    private String image;

    @OneToMany
    @JsonIgnore
    @JoinColumn(name = "id_user_staff")
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) {
            return java.util.Collections.emptyList();
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getCode().name()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return code;
    }

    public String getEmail() {
        if (emailFe != null && !emailFe.trim().isEmpty()) {
            return emailFe;
        }
        return emailFpt;
    }

}
