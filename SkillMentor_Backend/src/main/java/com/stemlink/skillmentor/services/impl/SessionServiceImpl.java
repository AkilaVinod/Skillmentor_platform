package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.Repositories.MentorRepository;
import com.stemlink.skillmentor.Repositories.SessionRepository;
import com.stemlink.skillmentor.Repositories.StudentRepository;
import com.stemlink.skillmentor.Repositories.SubjectRepository;
import com.stemlink.skillmentor.constants.PaymentStatus;
import com.stemlink.skillmentor.constants.SessionStatus;
import com.stemlink.skillmentor.dto.request.SessionRequestDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import com.stemlink.skillmentor.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    @CacheEvict(value = {"sessions","session","studentSessions"}, allEntries = true)
    public Session createNewSession(SessionRequestDTO sessionRequestDTO){
        try{
            Student student = studentRepository.findById(sessionRequestDTO.getStudentId()).orElseThrow(
                    () -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND)
            );

            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionRequestDTO.getMentorId())).orElseThrow(
                    () -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND)
            );

            Subject subject = subjectRepository.findById(sessionRequestDTO.getSubjectId()).orElseThrow(
                    () -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND)
            );

            ValidationUtils.validateMentorAvailability(mentor, sessionRequestDTO.getSessionAt(), sessionRequestDTO.getDurationMinutes());
            ValidationUtils.validateStudentAvailability(student, sessionRequestDTO.getSessionAt(), sessionRequestDTO.getDurationMinutes());

            Session session = modelMapper.map(sessionRequestDTO , Session.class);
            session.setStudent(student);
            session.setMentor(mentor);
            session.setSubject(subject);

            return sessionRepository.save(session);

        } catch (SkillMentorException skillMentorException) {
            log.error("Dependencies not found to map: {}, Failed to create new session", skillMentorException.getMessage());
            throw skillMentorException;

        } catch (Exception exception) {
            log.error("Failed to create session", exception);
            throw new SkillMentorException("Failed to create new session", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "sessions", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Session> getAllSessions(Pageable pageable) {
        log.info("Fetching sessions page {} from DB", pageable.getPageNumber());
        return sessionRepository.findAll(pageable);
    }

    @Cacheable(value = "session", key = "#id")
    public Session getSessionById(Long id) {
        log.info("Fetching session {} from DB", id);
        return sessionRepository.findById(id).orElseThrow(
                () -> new SkillMentorException("Session not found",HttpStatus.NOT_FOUND)
        );
    }

    @CacheEvict(value = {"sessions","session","studentSessions"}, allEntries = true)
    public Session updateSessionById(Long id, SessionRequestDTO updatedSessionDTO){

        Session session = sessionRepository.findById(id).orElseThrow(
                () -> new SkillMentorException("Session not found",HttpStatus.NOT_FOUND)
        );

        modelMapper.map(updatedSessionDTO, session);

        if (updatedSessionDTO.getStudentId() != null) {
            Student student = studentRepository.findById(updatedSessionDTO.getStudentId())
                    .orElseThrow(() -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND));
            session.setStudent(student);
        }

        if (updatedSessionDTO.getMentorId() != null) {
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(updatedSessionDTO.getMentorId()))
                    .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));
            session.setMentor(mentor);
        }

        if (updatedSessionDTO.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updatedSessionDTO.getSubjectId())
                    .orElseThrow(() -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND));
            session.setSubject(subject);
        }

        return sessionRepository.save(session);
    }

    @CacheEvict(value = {"sessions","session","studentSessions"}, allEntries = true)
    public void deleteSession(Long id) {

        Session session = sessionRepository.findById(id).orElseThrow(() ->
                new SkillMentorException("Session not found", HttpStatus.NOT_FOUND)
        );

        sessionRepository.delete(session);
    }

    @CacheEvict(value = {"sessions","session","studentSessions"}, allEntries = true)
    public Session enrollSession(UserPrincipal userPrincipal, SessionRequestDTO sessionRequestDTO){

        Student student = studentRepository.findByEmail(userPrincipal.getEmail())
                .orElseGet(() -> {
                    Student s = new Student();
                    s.setStudentId(userPrincipal.getId());
                    s.setEmail(userPrincipal.getEmail());
                    s.setFirstName(userPrincipal.getFirstName());
                    s.setLastName(userPrincipal.getLastName());
                    return studentRepository.save(s);
                });

        Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionRequestDTO.getMentorId()))
                .orElseThrow(() -> new RuntimeException("Mentor not found with mentorId: " + sessionRequestDTO.getMentorId()));

        Subject subject = subjectRepository.findById(sessionRequestDTO.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + sessionRequestDTO.getSubjectId()));

        Session session = new Session();
        session.setStudent(student);
        session.setMentor(mentor);
        session.setSubject(subject);
        session.setSessionAt(sessionRequestDTO.getSessionAt());
        session.setDurationMinutes(sessionRequestDTO.getDurationMinutes() != null ? sessionRequestDTO.getDurationMinutes() : 60);
        session.setSessionStatus(SessionStatus.CONFIRMED);
        session.setPaymentStatus(PaymentStatus.PENDING);

        return sessionRepository.save(session);
    }

    @Cacheable(value = "studentSessions", key = "#email")
    public List<Session> getSessionsByStudentEmail(String email){
        log.info("Fetching sessions for student {} from DB", email);
        return sessionRepository.findByStudent_Email(email);
    }

}