package pbl2.sub119.backend.mail.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.mail.controller.docs.ReceivedMailDocs;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailDetailResponse;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailResponse;
import pbl2.sub119.backend.mail.service.ReceivedMailService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mails")
@RequiredArgsConstructor
public class ReceivedMailController implements ReceivedMailDocs {

    private final ReceivedMailService receivedMailService;

    @Override
    @GetMapping
    public ResponseEntity<List<ReceivedMailResponse>> getMyMails(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(receivedMailService.getMyMails(accessor));
    }

    @Override
    @GetMapping("/{mailId}")
    public ResponseEntity<ReceivedMailDetailResponse> getMail(
            @Auth final Accessor accessor,
            @PathVariable final Long mailId
    ) {
        return ResponseEntity.ok(receivedMailService.getMail(accessor, mailId));
    }
}
