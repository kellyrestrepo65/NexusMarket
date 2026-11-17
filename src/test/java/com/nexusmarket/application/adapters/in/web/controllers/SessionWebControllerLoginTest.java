package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import com.nexusmarket.domain.ports.in.RegisterBuyerUseCase;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.EntityId;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionWebControllerLoginTest {

    private final ConsultUserUseCase consultUser = mock(ConsultUserUseCase.class);
    private final UserRepositoryPort repository = mock(UserRepositoryPort.class);
    private final SessionWebController controller = new SessionWebController(consultUser,
            mock(RegisterBuyerUseCase.class), repository, false);

    @Test
    void logsInWithIdentityDocument() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(EntityId.of(100045));
        when(repository.findByIdentityDocument("111111")).thenReturn(Optional.of(user));
        MockHttpSession session = new MockHttpSession();

        controller.login(" 111111 ", session);

        assertThat(WebSession.actor(session)).isEqualTo(EntityId.of(100045));
        verify(consultUser).execute(EntityId.of(100045), EntityId.of(100045));
    }

    @Test
    void logsInWithUserIdWhenNoDocumentMatches() {
        when(repository.findByIdentityDocument("100000")).thenReturn(Optional.empty());
        MockHttpSession session = new MockHttpSession();

        controller.login("100000", session);

        assertThat(WebSession.actor(session)).isEqualTo(EntityId.of(100000));
    }
}
