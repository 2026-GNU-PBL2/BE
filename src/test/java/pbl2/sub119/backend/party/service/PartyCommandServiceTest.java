package pbl2.sub119.backend.party.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.mapper.PartyMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PartyCommandServiceTest {

    @Autowired
    private PartyCommandService partyCommandService;

    @Autowired
    private PartyMapper partyMapper;

    @Test
    @DisplayName("파티 생성 성공")
    void createPartySuccess() {
        // given
        Long hostUserId = 1L;
        PartyCreateRequest request = new PartyCreateRequest("f759e32a-b0ef-4afb-9731-eb72e388367e", 4, 4500);

        // when
        PartyCreateResponse response = partyCommandService.createParty(hostUserId, request);

        // then
        Party party = partyMapper.findById(response.partyId());
        assertThat(party).isNotNull();
        assertThat(party.getHostUserId()).isEqualTo(hostUserId);
        assertThat(party.getCurrentMemberCount()).isEqualTo(1);
    }
}