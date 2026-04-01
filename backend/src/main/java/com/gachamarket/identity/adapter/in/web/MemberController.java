package com.gachamarket.identity.adapter.in.web;

import com.gachamarket.identity.adapter.in.web.dto.request.UpdateNicknameRequest;
import com.gachamarket.identity.adapter.in.web.dto.response.MemberProfileResponse;
import com.gachamarket.identity.adapter.in.web.dto.response.PointTransactionResponse;
import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.dto.result.PointTransactionResult;
import com.gachamarket.identity.application.port.in.ChargeFreePointsUseCase;
import com.gachamarket.identity.application.port.in.GetMemberProfileUseCase;
import com.gachamarket.identity.application.port.in.GetPointHistoryUseCase;
import com.gachamarket.identity.application.port.in.UpdateNicknameUseCase;
import com.gachamarket.support.ApiResponse;
import com.gachamarket.support.AuthUserExtractor;
import com.gachamarket.support.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final UpdateNicknameUseCase updateNicknameUseCase;
    private final ChargeFreePointsUseCase chargeFreePointsUseCase;
    private final GetPointHistoryUseCase getPointHistoryUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> me() {
        Long memberId = AuthUserExtractor.getRequiredMemberId();
        MemberProfileResult result = getMemberProfileUseCase.getProfile(memberId);
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(result)));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateNickname(
            @RequestBody UpdateNicknameRequest request
    ) {
        Long memberId = AuthUserExtractor.getRequiredMemberId();
        MemberProfileResult result = updateNicknameUseCase.updateNickname(
                request.toCommand(memberId)
        );
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(result)));
    }

    @PostMapping("/me/free-charge")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> chargeFreePoints() {
        Long memberId = AuthUserExtractor.getRequiredMemberId();
        MemberProfileResult result = chargeFreePointsUseCase.chargeFreePoints(memberId);
        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(result)));
    }

    @GetMapping("/me/points")
    public ResponseEntity<ApiResponse<PageResponse<PointTransactionResponse>>> getPointHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = AuthUserExtractor.getRequiredMemberId();
        PageResponse<PointTransactionResult> result = getPointHistoryUseCase.getHistory(memberId, page, size);
        PageResponse<PointTransactionResponse> response = new PageResponse<>(
                result.content().stream().map(PointTransactionResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
