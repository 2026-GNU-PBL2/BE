package pbl2.sub119.backend.party.provision.dto.response;

// 보기 버튼 클릭 시에만 반환하는 평문 비밀번호 응답
public record PartyProvisionPasswordRevealResponse(
        String sharedAccountPassword
) {
}