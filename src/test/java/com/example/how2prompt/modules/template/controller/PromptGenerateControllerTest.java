package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptResponse;
import com.example.how2prompt.modules.template.service.GuestGenerateQuotaService;
import com.example.how2prompt.modules.template.service.PromptGenerateService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PromptGenerateControllerTest {

    private final PromptGenerateService promptGenerateService = mock(PromptGenerateService.class);
    private final GuestGenerateQuotaService guestGenerateQuotaService = mock(GuestGenerateQuotaService.class);
    private final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final PromptGenerateController controller =
            new PromptGenerateController(promptGenerateService, guestGenerateQuotaService);

    @Test
    void generate_guestConsumesQuotaBeforeGenerate() {
        UUID templateId = UUID.randomUUID();
        GeneratePromptRequest request = new GeneratePromptRequest();
        GeneratePromptResponse response = response(templateId);
        when(httpRequest.getRemoteAddr()).thenReturn("203.0.113.10");
        when(promptGenerateService.generate(templateId, request, null)).thenReturn(response);

        controller.generate(templateId, request, null, httpRequest);

        InOrder order = inOrder(guestGenerateQuotaService, promptGenerateService);
        order.verify(guestGenerateQuotaService).checkAndConsume(templateId, "203.0.113.10");
        order.verify(promptGenerateService).generate(templateId, request, null);
    }

    @Test
    void generate_authenticatedUserSkipsGuestQuota() {
        UUID templateId = UUID.randomUUID();
        AuthenticatedUser user =
                new AuthenticatedUser(UUID.randomUUID(), "user@example.com", UUID.randomUUID(), false);
        GeneratePromptRequest request = new GeneratePromptRequest();
        when(promptGenerateService.generate(templateId, request, user)).thenReturn(response(templateId));

        controller.generate(templateId, request, user, httpRequest);

        verifyNoInteractions(guestGenerateQuotaService);
        verify(promptGenerateService).generate(templateId, request, user);
    }

    private static GeneratePromptResponse response(UUID templateId) {
        return new GeneratePromptResponse(
                templateId,
                UUID.randomUUID(),
                null,
                null,
                "Rendered",
                null,
                false,
                Map.of(),
                null,
                null,
                2
        );
    }
}
