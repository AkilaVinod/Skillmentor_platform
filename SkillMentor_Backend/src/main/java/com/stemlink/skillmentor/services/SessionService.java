package com.stemlink.skillmentor.services;


import com.stemlink.skillmentor.dto.request.SessionRequestDTO;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SessionService {

    Session createNewSession(SessionRequestDTO sessionRequestDTO);

    Page<Session> getAllSessions(Pageable pageable);

    Session getSessionById(Long id);

    Session updateSessionById(Long id, SessionRequestDTO updatedSessionDTO);

    void deleteSession(Long id);

    Session enrollSession(UserPrincipal userPrincipal, SessionRequestDTO sessionRequestDTO);

    List<Session> getSessionsByStudentEmail(String email);
}

