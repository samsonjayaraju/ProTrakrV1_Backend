package com.klup.protrackr;

import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.JwtService;
import com.klup.protrackr.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProtrackrApiFlowTests {

    @Autowired WebApplicationContext wac;
    MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    }

    @Test
    void facultyRole_canAccessAdminEndpoints_andExportCsv() throws Exception {
        var faculty = createUser("faculty1@example.com", UserRole.FACULTY, "pass1234");
        String token = tokenFor(faculty);

        mvc.perform(get("/api/users/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/exports/students.csv")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("students.csv")));
    }

    @Test
    void studentCannotAccessOtherStudentsProject() throws Exception {
        var s1 = createUser("s1@example.com", UserRole.STUDENT, "pass1234");
        var s2 = createUser("s2@example.com", UserRole.STUDENT, "pass1234");
        Project p = createProject(s1, "P1");

        mvc.perform(get("/api/projects/" + p.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(s2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void projectDetails_allowsTeamMemberAccess() throws Exception {
        var owner = createUser("owner@example.com", UserRole.STUDENT, "pass1234");
        var member = createUser("member@example.com", UserRole.STUDENT, "pass1234");
        Project p = createProject(owner, "Team Project");
        p.getTeamMembers().add(member);
        projectRepository.save(p);

        mvc.perform(get("/api/projects/" + p.getId() + "/details")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(member)))
                .andExpect(status().isOk());
    }

    @Test
    void preferencesAndPasswordUpdate_work() throws Exception {
        var s1 = createUser("prefs@example.com", UserRole.STUDENT, "oldpass123");
        String token = tokenFor(s1);

        mvc.perform(get("/api/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("""
                                {"currentPassword":"oldpass123","newPassword":"newpass123"}
                                """))
                .andExpect(status().isOk());

        User refreshed = userRepository.findById(s1.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newpass123", refreshed.getPasswordHash())).isTrue();
    }

    @Test
    void portfolioPublicAccess_respectsEnabledFlag() throws Exception {
        var s1 = createUser("portfolio@example.com", UserRole.STUDENT, "pass1234");
        String token = tokenFor(s1);

        mvc.perform(get("/api/portfolio/public/" + s1.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/portfolio/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("""
                                {"publicProfileEnabled":true,"slug":"portfolio-student"}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/portfolio/public/slug/portfolio-student"))
                .andExpect(status().isOk());
    }

    private User createUser(String email, UserRole role, String rawPassword) {
        User u = new User();
        u.setFullName(email.split("@")[0]);
        u.setEmail(email);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(u);
    }

    private Project createProject(User owner, String title) {
        Project p = new Project();
        p.setOwner(owner);
        p.setTitle(title);
        p.setStatus("DRAFT");
        p.setProgress(0);
        return projectRepository.save(p);
    }

    private String tokenFor(User u) {
        return jwtService.generateToken(UserPrincipal.fromUser(u));
    }
}
