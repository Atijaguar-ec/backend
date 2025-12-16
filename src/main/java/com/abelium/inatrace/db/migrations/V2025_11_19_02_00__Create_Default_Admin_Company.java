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
 * Crea un usuario SYSTEM_ADMIN por defecto, una compañía genérica y los vincula con rol COMPANY_ADMIN,
 * para que el sistema tenga una cuenta de administrador lista para usar después de inicializar una nueva base de datos.
 *
 * Los valores se pueden configurar mediante variables de entorno o propiedades:
 *  - INATRACE_ADMIN_EMAIL / inatrace.admin.email (default: admin@example.com)
 *  - INATRACE_ADMIN_PASSWORD / inatrace.admin.password (default: Admin123!)
 *  - INATRACE_ADMIN_NAME / inatrace.admin.name (default: Admin)
 *  - INATRACE_ADMIN_SURNAME / inatrace.admin.surname (default: User)
 *  - INATRACE_ADMIN_COMPANY_NAME / inatrace.admin.company.name (default: Demo Company)
 */
public class V2025_11_19_02_00__Create_Default_Admin_Company implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String email = environment.getProperty("inatrace.admin.email", "admin@example.com");
        String rawPassword = environment.getProperty("inatrace.admin.password", "Admin123!");
        String name = environment.getProperty("inatrace.admin.name", "Admin");
        String surname = environment.getProperty("inatrace.admin.surname", "User");
        String companyName = environment.getProperty("inatrace.admin.company.name", "Demo Company");

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
