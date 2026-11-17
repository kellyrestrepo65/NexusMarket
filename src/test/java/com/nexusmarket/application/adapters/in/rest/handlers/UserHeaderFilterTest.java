package com.nexusmarket.application.adapters.in.rest.handlers;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserHeaderFilterTest {

    private final UserRepositoryPort repository = mock(UserRepositoryPort.class);
    private final UserHeaderFilter filter = new UserHeaderFilter(repository);

    private String headerSeenByController(String uri, String header) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (header != null) {
            request.addHeader("X-User-Id", header);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return ((HttpServletRequest) chain.getRequest()).getHeader("X-User-Id");
    }

    @Test
    void documentIsTranslatedToUserId() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(EntityId.of(100045));
        when(repository.findByIdentityDocument("111111")).thenReturn(Optional.of(user));

        assertThat(headerSeenByController("/api/cart", "111111")).isEqualTo("100045");
    }

    @Test
    void unknownValueIsKeptAsId() throws Exception {
        when(repository.findByIdentityDocument("100000")).thenReturn(Optional.empty());

        assertThat(headerSeenByController("/api/users", "100000")).isEqualTo("100000");
    }

    @Test
    void missingHeaderStaysMissing() throws Exception {
        assertThat(headerSeenByController("/api/users", null)).isNull();
    }

    @Test
    void nonApiRequestsAreNotTouched() throws Exception {
        assertThat(headerSeenByController("/login", "111111")).isEqualTo("111111");
    }
}
