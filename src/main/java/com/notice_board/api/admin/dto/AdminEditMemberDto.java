package com.notice_board.api.admin.dto;

import com.notice_board.api.mypage.dto.EditMemberDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class AdminEditMemberDto extends EditMemberDto {
    @Schema(description = "관리자 여부 Y, N (SUPER_ADMIN 만 관리자 지정 가능)")
    private String adminYn;
}