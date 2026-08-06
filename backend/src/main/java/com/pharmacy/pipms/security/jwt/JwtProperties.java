package com.pharmacy.pipms.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private AccessToken accessToken = new AccessToken();
    private RefreshTokenProps refreshToken = new RefreshTokenProps();
    private ControlledSubstance controlledSubstance = new ControlledSubstance();

    @Getter @Setter
    public static class AccessToken {
        private PatientDoctor patientDoctor = new PatientDoctor();
        private Staff staff = new Staff();
        private Admin admin = new Admin();
    }

    @Getter @Setter
    public static class PatientDoctor { private long expirationMs; }
    @Getter @Setter
    public static class Staff { private long expirationMs; }
    @Getter @Setter
    public static class Admin { private long expirationMs; }

    @Getter @Setter
    public static class RefreshTokenProps {
        private StaffRt staff = new StaffRt();
        private PatientRt patient = new PatientRt();
    }
    @Getter @Setter
    public static class StaffRt { private long expirationMs; }
    @Getter @Setter
    public static class PatientRt { private long expirationMs; }

    @Getter @Setter
    public static class ControlledSubstance { private long reauthWindowMs; }
}