package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.company.CompanyUser;
import com.abelium.inatrace.types.CompanyStatus;
import com.abelium.inatrace.types.CompanyUserRole;
import com.abelium.inatrace.types.Language;
import com.abelium.inatrace.types.UserRole;
import com.abelium.inatrace.types.UserStatus;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * Creates a default SYSTEM_ADMIN user, a generic company and links them with COMPANY_ADMIN role,
 * so the system has a ready-to-use admin account after initializing a new database.
 *
 * Values can be overridden via environment / properties:
 *  - INATrace.admin.email (default: admin@example.com)
 *  - INATrace.admin.password (default: Admin123!)
 *  - INATrace.admin.name (default: Admin)
 *  - INATrace.admin.surname (default: User)
 *  - INATrace.admin.company.name (default: Demo Company)
 */
public class V2025_11_19_02_00__Create_Default_Admin_Company implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String email = environment.getProperty("INATrace.admin.email", "admin@example.com");
        String rawPassword = environment.getProperty("INATrace.admin.password", "Admin123!");
        String name = environment.getProperty("INATrace.admin.name", "Admin");
        String surname = environment.getProperty("INATrace.admin.surname", "User");
        String companyName = environment.getProperty("INATrace.admin.company.name", "Demo Company");

        // If user already exists, do nothing
        List<User> existingUsers = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        if (!existingUsers.isEmpty()) {
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Create SYSTEM_ADMIN user
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(encoder.encode(rawPassword));
        admin.setName(name);
        admin.setSurname(surname);
        admin.setLanguage(Language.ES);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(UserRole.SYSTEM_ADMIN);
        em.persist(admin);

        // Create generic company
        Company company = new Company();
        company.setName(companyName);
        company.setStatus(CompanyStatus.ACTIVE);
        company.setEmail(email);
        em.persist(company);

        // Link admin as COMPANY_ADMIN of the company
        CompanyUser companyUser = new CompanyUser();
        companyUser.setUser(admin);
        companyUser.setCompany(company);
        companyUser.setRole(CompanyUserRole.COMPANY_ADMIN);
        em.persist(companyUser);
    }
}
