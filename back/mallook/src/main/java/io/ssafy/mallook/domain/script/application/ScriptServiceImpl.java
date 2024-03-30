package io.ssafy.mallook.domain.script.application;

import io.ssafy.mallook.domain.chatgpt.dto.request.QuestionDto;
import io.ssafy.mallook.domain.chatgpt.dto.response.GptResponseDto;
import io.ssafy.mallook.domain.chatgpt.service.GptService;
import io.ssafy.mallook.domain.member.dao.MemberRepository;
import io.ssafy.mallook.domain.member.entity.Member;
import io.ssafy.mallook.domain.product.dao.mongo.ProductsCustomRepository;
import io.ssafy.mallook.domain.product.dao.mongo.ProductsRepository;
import io.ssafy.mallook.domain.product.dto.request.ProductHotKeywordDto;
import io.ssafy.mallook.domain.product.dto.response.ProductsListDto;
import io.ssafy.mallook.domain.script.dao.ScriptRepository;
import io.ssafy.mallook.domain.script.dto.request.ScriptCreatDto;
import io.ssafy.mallook.domain.script.dto.request.ScriptDeleteListDto;
import io.ssafy.mallook.domain.script.dto.request.ScriptKeywordDto;
import io.ssafy.mallook.domain.script.dto.response.ScriptDetailDto;
import io.ssafy.mallook.domain.script.dto.response.ScriptListDto;
import io.ssafy.mallook.domain.script.dto.response.ScriptProductDto;
import io.ssafy.mallook.domain.script.entity.Script;
import io.ssafy.mallook.global.common.code.ErrorCode;
import io.ssafy.mallook.global.exception.BaseExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ScriptServiceImpl implements ScriptService {

    private final MemberRepository memberRepository;
    private final ScriptRepository scriptRepository;
    private final ProductsRepository mongoProductsRepository;
    private final ProductsCustomRepository productsCustomRepository;
    private final GptService gptService;

    @Override
    public Long getMaxScriptId() {
        return scriptRepository.findMaxId();
    }

    @Override
    public Slice<ScriptListDto> getScriptList(Long cursor, UUID id, Pageable pageable) {
        Member proxyMember = memberRepository.getReferenceById(id);

        return scriptRepository.findByIdLessThanAndMemberOrderByIdDesc(cursor, proxyMember, pageable)
                .map(ScriptListDto::toDto);
    }

    @Override
    public Slice<ScriptListDto> getScriptList(Long cursor, Pageable pageable) {
        return scriptRepository.findByIdLessThanOrderByIdDesc(cursor, pageable)
                .map(ScriptListDto::toDto);
    }

    @Override
    public List<ScriptProductDto> getRecommendProductById(Long scriptId) {
        Script proxyScript = scriptRepository.getReferenceById(scriptId);
        List<String> scriptKeyword = proxyScript.getKeywordList();
        ProductHotKeywordDto productHotKeywordDto = ProductHotKeywordDto.builder()
                .hotKeywordList(scriptKeyword)
                .build();
        String cursor = mongoProductsRepository.findFirstByOrderByIdDesc().getId().toString();

        return productsCustomRepository.findByKeywordList(productHotKeywordDto, cursor)
                .stream()
                .map(ScriptProductDto::toScriptProductDto)
                .limit(6)
                .collect(toList());
    }

    @Override
    public Slice<ProductsListDto> getRecommendProductDetail(Long scriptId, String cursor) {
        Script proxyScript = scriptRepository.getReferenceById(scriptId);
        List<String> scriptKeyword = proxyScript.getKeywordList();
        ProductHotKeywordDto productHotKeywordDto = ProductHotKeywordDto.builder()
                .hotKeywordList(scriptKeyword)
                .build();

        return productsCustomRepository.findByKeywordList(productHotKeywordDto, cursor);
    }

    @Override
    public ScriptDetailDto getScriptDetail(Long scriptId) {
        return scriptRepository.findById(scriptId)
                .map(ScriptDetailDto::toDto)
                .orElseThrow(() -> new BaseExceptionHandler(ErrorCode.NOT_FOUND_SCRIPT));
    }

    @Override
    @Transactional
    public void createScript(ScriptCreatDto scriptCreateDto, UUID id) {
        Member proxyMember = memberRepository.getReferenceById(id);
        // ChatGPT에 질문 전달
        String scriptContent = String.join(", ", scriptCreateDto.keywordsList());
        QuestionDto questionDto = QuestionDto.builder()
                .content(scriptContent)
                .build();
        GptResponseDto gptResponseDto = gptService.askQuestion(questionDto);
        Script script = scriptCreateDto.toEntity(proxyMember, gptResponseDto.answer());
        scriptRepository.save(script);
    }

    @Override
    @Transactional
    public void deleteScript(ScriptDeleteListDto scriptDeleteListDto) {
        log.info(scriptDeleteListDto.toString());
        scriptRepository.deleteScript(scriptDeleteListDto.toDeleteList());
    }
}
