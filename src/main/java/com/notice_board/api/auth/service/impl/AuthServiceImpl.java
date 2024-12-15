package com.notice_board.api.auth.service.impl;

import com.notice_board.api.auth.dto.MemberDto;
import com.notice_board.api.auth.service.AuthService;
import com.notice_board.api.file.dto.FileDto;
import com.notice_board.api.file.service.FileService;
import com.notice_board.common.component.CommonExceptionResultMessage;
import com.notice_board.common.exception.CustomException;
import com.notice_board.model.Member;
import com.notice_board.model.User;
import com.notice_board.model.commons.File;
import com.notice_board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service("authService")
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private final FileService fileService;

    @Override
    public void checkEmail(String email) {
        this.validEmail(email);

        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            throw new CustomException(CommonExceptionResultMessage.EMAIL_DUPLICATE_FAIL);
        }
    }

    @Override
    public void signUp(MemberDto memberDto) throws IOException {
        String email = memberDto.getEmail();
        String name = memberDto.getName();
        String password = memberDto.getPassword();

        if (StringUtils.isEmpty(name)) {
            throw new CustomException(CommonExceptionResultMessage.VALID_FAIL);
        }

        this.checkEmail(email);
        this.validPassword(password);

        User user = modelMapper.map(memberDto, User.class);
        user.setPassword(passwordEncoder.encode(password));

        User saveUser = memberRepository.save(user);

        if (saveUser.getId() == null) {
            throw new CustomException(CommonExceptionResultMessage.DB_FAIL, "회원가입에 실패했습니다.");
        }

        MultipartFile profileImg = memberDto.getProfileImg();
        if (profileImg != null && !profileImg.isEmpty()) {
            if (!fileService.ExtCheck(new MultipartFile[]{profileImg}, "image")) { // 확장자 검사
                throw new CustomException(CommonExceptionResultMessage.FILE_UPLOAD_FAIL, "허용되지 않은 첨부파일 확장자");
            }
            FileDto fileDto = fileService.saveFile(profileImg, Member.FileType.PROFILE_IMG.name());
            saveUser.getMemberFiles().put(Member.FileType.PROFILE_IMG, modelMapper.map(fileDto, File.class));
        }

    }

    @Override
    public void validEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            throw new CustomException(CommonExceptionResultMessage.VALID_FAIL);
        }

        // 이메일 형식에 대한 정규식
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(regex)) {
            throw new CustomException(CommonExceptionResultMessage.INPUT_VALID_FAIL, "이메일 형식을 맞춰주세요.");
        }
    }

    @Override
    public void validPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new CustomException(CommonExceptionResultMessage.VALID_FAIL);
        }

        // 영어 대소문자, 숫자, 특수문자 포함, 8~20자
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,20}$";
        if (!password.matches(regex)) {
            throw new CustomException(CommonExceptionResultMessage.INPUT_VALID_FAIL, "비밀번호 형식을 맞춰주세요.");
        }
    }
}