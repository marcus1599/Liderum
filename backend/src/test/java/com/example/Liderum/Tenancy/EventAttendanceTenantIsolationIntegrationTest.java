package com.example.Liderum.Tenancy;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Enums.AttendanceStatus;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Messaging.GuildEventCreatedPublisher;
import com.example.Liderum.Repository.AttendanceRepository;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Entities.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test_only_event_attendance_tenant_secret",
        "liderum.registration.rate-limit.limit=100"
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class EventAttendanceTenantIsolationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired GuildRepository guildRepository;
    @Autowired UserRepository userRepository;
    @Autowired EventRepository eventRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @MockBean GuildEventCreatedPublisher guildEventCreatedPublisher;

    @Test
    void eventListAndCreationAreTenantScoped() throws Exception {
        Fixture a = fixture("event-list-a");
        Fixture b = fixture("event-list-b");
        Event eventA = event(a.guild(), "Event A");
        event(b.guild(), "Event B");

        String body = mockMvc.perform(get("/events").header("Authorization", bearer(a.token())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).contains("Event A").doesNotContain("Event B");

        String created = mockMvc.perform(post("/events").header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Created A\",\"date\":\"2030-01-01T10:00:00\",\"description\":\"A\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long createdId = objectMapper.readTree(created).get("id").asLong();
        assertThat(eventRepository.findById(createdId).orElseThrow().getGuild().getId()).isEqualTo(a.guild().getId());
        assertThat(eventA.getGuild().getId()).isEqualTo(a.guild().getId());
    }

    @Test
    void eventReadUpdateAndDeleteDoNotCrossGuild() throws Exception {
        Fixture a = fixture("event-mutate-a");
        Fixture b = fixture("event-mutate-b");
        Event eventB = event(b.guild(), "Protected B");

        mockMvc.perform(get("/events/" + eventB.getId()).header("Authorization", bearer(a.token())))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/events/" + eventB.getId()).header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Changed\",\"date\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/events/" + eventB.getId()).header("Authorization", bearer(a.token())))
                .andExpect(status().isNotFound());

        assertThat(eventRepository.findById(eventB.getId())).isPresent();
        assertThat(eventRepository.findById(eventB.getId()).orElseThrow().getName()).isEqualTo("Protected B");
    }

    @Test
    void attendanceListAndReadAreTenantScoped() throws Exception {
        Fixture a = fixture("attendance-read-a");
        Fixture b = fixture("attendance-read-b");
        Event eventA = event(a.guild(), "Event A");
        Event eventB = event(b.guild(), "Event B");
        Member memberA = member(a.guild(), "Member A");
        Member memberB = member(b.guild(), "Member B");
        Attendance attendanceA = attendance(memberA, eventA);
        Attendance attendanceB = attendance(memberB, eventB);

        String body = mockMvc.perform(get("/attendances").header("Authorization", bearer(a.token())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode list = objectMapper.readTree(body);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("id").asLong()).isEqualTo(attendanceA.getId());
        assertThat(list.get(0).get("id").asLong()).isNotEqualTo(attendanceB.getId());
        mockMvc.perform(get("/attendances/" + attendanceB.getId()).header("Authorization", bearer(a.token())))
                .andExpect(status().isNotFound());
    }

    @Test
    void attendanceUpdateAndDeleteDoNotCrossGuild() throws Exception {
        Fixture a = fixture("attendance-mutate-a");
        Fixture b = fixture("attendance-mutate-b");
        Attendance attendanceB = attendance(member(b.guild(), "Member B"), event(b.guild(), "Event B"));

        mockMvc.perform(put("/attendances/" + attendanceB.getId()).header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":1,\"eventId\":1,\"status\":\"PRESENTE\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/attendances/" + attendanceB.getId()).header("Authorization", bearer(a.token())))
                .andExpect(status().isNotFound());
        assertThat(attendanceRepository.findById(attendanceB.getId())).isPresent();
        assertThat(attendanceRepository.findById(attendanceB.getId()).orElseThrow().getStatus())
                .isEqualTo(AttendanceStatus.FALTOU);
    }

    @Test
    void attendanceRejectsMemberFromAnotherGuildWithoutMutation() throws Exception {
        Fixture a = fixture("attendance-member-cross-a");
        Fixture b = fixture("attendance-member-cross-b");
        Event eventA = event(a.guild(), "Event A");
        Member memberB = member(b.guild(), "Member B");
        long before = attendanceRepository.count();

        mockMvc.perform(post("/attendances").header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + memberB.getId() + ",\"eventId\":" + eventA.getId() + ",\"status\":\"PRESENTE\"}"))
                .andExpect(status().isNotFound());
        assertThat(attendanceRepository.count()).isEqualTo(before);
    }

    @Test
    void attendanceRejectsEventFromAnotherGuildWithoutMutation() throws Exception {
        Fixture a = fixture("attendance-event-cross-a");
        Fixture b = fixture("attendance-event-cross-b");
        Member memberA = member(a.guild(), "Member A");
        Event eventB = event(b.guild(), "Event B");
        long before = attendanceRepository.count();

        mockMvc.perform(post("/attendances").header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + memberA.getId() + ",\"eventId\":" + eventB.getId() + ",\"status\":\"PRESENTE\"}"))
                .andExpect(status().isNotFound());
        assertThat(attendanceRepository.count()).isEqualTo(before);
    }

    @Test
    void attendanceCreationUsesOnlySameGuildReferences() throws Exception {
        Fixture a = fixture("attendance-create-a");
        Event eventA = event(a.guild(), "Event A");
        Member memberA = member(a.guild(), "Member A");

        mockMvc.perform(post("/attendances").header("Authorization", bearer(a.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + memberA.getId() + ",\"eventId\":" + eventA.getId() + ",\"status\":\"PRESENTE\"}"))
                .andExpect(status().isOk());
        assertThat(attendanceRepository.findAllByEventGuildId(a.guild().getId())).hasSize(1);
    }

    @Test
    void insufficientRoleGetsForbiddenWhileCrossGuildResourcesStayNotFound() throws Exception {
        Fixture a = fixture("role-boundary-a");
        Event eventA = event(a.guild(), "Event A");
        Member memberA = member(a.guild(), "Member A");
        String soldierToken = createUserAndLogin(a.token(), "role-boundary-soldier", GuildRole.SOLDADO);

        mockMvc.perform(put("/events/" + eventA.getId()).header("Authorization", bearer(soldierToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Denied\",\"date\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/attendances").header("Authorization", bearer(soldierToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + memberA.getId() + ",\"eventId\":" + eventA.getId() + ",\"status\":\"PRESENTE\"}"))
                .andExpect(status().isForbidden());

        assertThat(eventRepository.findById(eventA.getId()).orElseThrow().getName()).isEqualTo("Event A");
        assertThat(attendanceRepository.findAllByEventGuildId(a.guild().getId())).isEmpty();
    }

    private Fixture fixture(String prefix) throws Exception {
        String username = prefix + "-owner";
        String response = mockMvc.perform(post("/auth/register-guild").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guildName\":\"Guild " + prefix + "\",\"serverName\":\"Server\",\"username\":\"" + username + "\",\"email\":\"" + username + "@example.test\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();
        return new Fixture(userRepository.findById(id).orElseThrow().getGuild(), login(username));
    }

    private String login(String username) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String createUserAndLogin(String token, String username, GuildRole role) throws Exception {
        mockMvc.perform(post("/users").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + username
                                + "@example.test\",\"password\":\"password123\",\"role\":\""
                                + role.name() + "\"}"))
                .andExpect(status().isOk());
        return login(username);
    }

    private Event event(Guild guild, String name) {
        return eventRepository.save(Event.builder().guild(guild).name(name).date(LocalDateTime.of(2030, 1, 1, 10, 0)).description(name).build());
    }

    private Member member(Guild guild, String nickname) {
        return memberRepository.save(Member.builder().guild(guild).nickname(nickname).phone("5511999999999")
                .guildRole(GuildRole.SOLDADO).rank("Recruit").classe(Classe.GUERREIRO).build());
    }

    private Attendance attendance(Member member, Event event) {
        return attendanceRepository.save(Attendance.builder().member(member).event(event).status(AttendanceStatus.FALTOU).build());
    }

    private String bearer(String token) { return "Bearer " + token; }

    private record Fixture(Guild guild, String token) { }
}
