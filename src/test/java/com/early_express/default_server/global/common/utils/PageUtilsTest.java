package com.early_express.default_server.global.common.utils;

import com.early_express.default_server.global.common.dto.PageInfo;
import com.early_express.default_server.global.presentation.dto.PageResponse;
import com.early_express.default_server.global.common.utils.PageUtils.PageUtilException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageUtils 테스트")
class PageUtilsTest {

    @Test
    @DisplayName("Page를 PageResponse로 변환")
    void convert_page_to_page_response() {
        // Given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageRequest, 23);

        // When
        PageResponse<String> response = PageUtils.toPageResponse(page);

        // Then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent()).containsExactly("item1", "item2", "item3");
        assertThat(response.getPageInfo().getPage()).isEqualTo(0);
        assertThat(response.getPageInfo().getSize()).isEqualTo(10);
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(23);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(3);
        assertThat(response.getPageInfo().getNumberOfElements()).isEqualTo(3);
        assertThat(response.getPageInfo().isFirst()).isTrue();
        assertThat(response.getPageInfo().isLast()).isFalse();
        assertThat(response.getPageInfo().isHasNext()).isTrue();
        assertThat(response.getPageInfo().isHasPrevious()).isFalse();
    }

    @Test
    @DisplayName("정렬 정보를 포함한 Page를 PageResponse로 변환")
    void convert_page_with_sort_to_page_response() {
        // Given
        List<String> content = Arrays.asList("item1", "item2");
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate")
                .and(Sort.by(Sort.Direction.ASC, "name"));
        PageRequest pageRequest = PageRequest.of(0, 10, sort);
        Page<String> page = new PageImpl<>(content, pageRequest, 2);

        // When
        PageResponse<String> response = PageUtils.toPageResponse(page);

        // Then
        assertThat(response.getPageInfo().getSort()).hasSize(2);
        assertThat(response.getPageInfo().getSort().get(0).getProperty()).isEqualTo("createdDate");
        assertThat(response.getPageInfo().getSort().get(0).getDirection())
                .isEqualTo(PageInfo.SortInfo.Direction.DESC);
        assertThat(response.getPageInfo().getSort().get(1).getProperty()).isEqualTo("name");
        assertThat(response.getPageInfo().getSort().get(1).getDirection())
                .isEqualTo(PageInfo.SortInfo.Direction.ASC);
    }

    @Test
    @DisplayName("엔티티를 DTO로 변환하면서 PageResponse 생성")
    void convert_page_with_mapper_to_page_response() {
        // Given
        List<TestEntity> entities = Arrays.asList(
                new TestEntity(1L, "name1"),
                new TestEntity(2L, "name2")
        );
        PageRequest pageRequest = PageRequest.of(1, 5);
        Page<TestEntity> page = new PageImpl<>(entities, pageRequest, 12);

        // When
        PageResponse<TestDto> response = PageUtils.toPageResponse(page, entity ->
                new TestDto(entity.getId(), entity.getName().toUpperCase())
        );

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("NAME1");
        assertThat(response.getContent().get(1).getName()).isEqualTo("NAME2");
        assertThat(response.getPageInfo().getPage()).isEqualTo(1);
        assertThat(response.getPageInfo().getSize()).isEqualTo(5);
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(12);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(3);
        assertThat(response.getPageInfo().isFirst()).isFalse();
        assertThat(response.getPageInfo().isLast()).isFalse();
    }

    @Test
    @DisplayName("빈 Page 변환")
    void convert_empty_page_to_page_response() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<String> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

        // When
        PageResponse<String> response = PageUtils.toPageResponse(emptyPage);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPageInfo().isEmpty()).isTrue();
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(0);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(0);
        assertThat(response.getPageInfo().isFirst()).isTrue();
        assertThat(response.getPageInfo().isLast()).isTrue();
    }

    @Test
    @DisplayName("마지막 페이지 변환")
    void convert_last_page_to_page_response() {
        // Given
        List<String> content = Arrays.asList("item1", "item2");
        PageRequest pageRequest = PageRequest.of(2, 10);
        Page<String> page = new PageImpl<>(content, pageRequest, 22);

        // When
        PageResponse<String> response = PageUtils.toPageResponse(page);

        // Then
        assertThat(response.getPageInfo().getPage()).isEqualTo(2);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(3);
        assertThat(response.getPageInfo().isFirst()).isFalse();
        assertThat(response.getPageInfo().isLast()).isTrue();
        assertThat(response.getPageInfo().isHasNext()).isFalse();
        assertThat(response.getPageInfo().isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("page가 null일 때 PageUtilException 예외 발생")
    void throw_exception_when_page_is_null() {
        // When & Then
        assertThatThrownBy(() -> PageUtils.toPageResponse(null))
                .isInstanceOf(PageUtilException.class)
                .hasMessageContaining("페이지 객체는 null일 수 없습니다");
    }

    @Test
    @DisplayName("mapper가 null일 때 PageUtilException 예외 발생")
    void throw_exception_when_mapper_is_null() {
        // Given
        List<TestEntity> entities = Arrays.asList(new TestEntity(1L, "name1"));
        Page<TestEntity> page = new PageImpl<>(entities);

        // When & Then
        assertThatThrownBy(() -> PageUtils.toPageResponse(page, null))
                .isInstanceOf(PageUtilException.class)
                .hasMessageContaining("매퍼 함수는 null일 수 없습니다");
    }

    // 테스트용 내부 클래스
    private static class TestEntity {
        private final Long id;
        private final String name;

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class TestDto {
        private final Long id;
        private final String name;

        public TestDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}