package com.example.how2prompt.modules.taxonomy.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.UpdateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TaxonomyAdminService.class, CategoryQueryService.class, TagQueryService.class})
@Testcontainers
@EnabledIfDockerAvailable
class TaxonomyAdminServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("how2prompt_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private TaxonomyAdminService taxonomyAdminService;

    @Autowired
    private CategoryQueryService categoryQueryService;

    @Autowired
    private TagQueryService tagQueryService;

    @Test
    void createCategory_and_buildTree_success() {
        // 1. Tạo category cha
        CreateCategoryRequest rootReq = new CreateCategoryRequest();
        rootReq.setSlug("programming");
        rootReq.setNameI18n(Map.of("en", "Programming"));
        rootReq.setSortOrder(1);
        CategoryTreeResponse root = taxonomyAdminService.createCategory(rootReq);
        assertThat(root.getSlug()).isEqualTo("programming");

        // 2. Tạo category con
        CreateCategoryRequest childReq = new CreateCategoryRequest();
        childReq.setSlug("java");
        childReq.setNameI18n(Map.of("en", "Java"));
        childReq.setParentId(root.getId());
        childReq.setSortOrder(2);
        CategoryTreeResponse child = taxonomyAdminService.createCategory(childReq);
        assertThat(child.getParentId()).isEqualTo(root.getId());

        // 3. Lấy cây danh mục
        List<CategoryTreeResponse> tree = categoryQueryService.findTree();
        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getSlug()).isEqualTo("programming");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getSlug()).isEqualTo("java");
    }

    @Test
    void createCategory_duplicateSlug_throwsConflictException() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setSlug("duplicate");
        req.setNameI18n(Map.of("en", "Duplicate"));
        taxonomyAdminService.createCategory(req);

        assertThatThrownBy(() -> taxonomyAdminService.createCategory(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void validateCategoryParent_cycleDetection_throwsBadRequestException() {
        // Tạo Grandparent -> Parent -> Child
        CreateCategoryRequest gReq = new CreateCategoryRequest();
        gReq.setSlug("grandparent");
        gReq.setNameI18n(Map.of("en", "G"));
        CategoryTreeResponse g = taxonomyAdminService.createCategory(gReq);

        CreateCategoryRequest pReq = new CreateCategoryRequest();
        pReq.setSlug("parent");
        pReq.setNameI18n(Map.of("en", "P"));
        pReq.setParentId(g.getId());
        CategoryTreeResponse p = taxonomyAdminService.createCategory(pReq);

        CreateCategoryRequest cReq = new CreateCategoryRequest();
        cReq.setSlug("child");
        cReq.setNameI18n(Map.of("en", "C"));
        cReq.setParentId(p.getId());
        CategoryTreeResponse c = taxonomyAdminService.createCategory(cReq);

        // Thử cập nhật parent của Grandparent thành Child -> Cycle!
        UpdateCategoryRequest cycleReq = new UpdateCategoryRequest();
        cycleReq.setParentId(c.getId());

        assertThatThrownBy(() -> taxonomyAdminService.updateCategory(g.getId(), cycleReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cyclic parent hierarchy detected");

        // Thử cập nhật parent của Parent thành chính nó
        UpdateCategoryRequest selfReq = new UpdateCategoryRequest();
        selfReq.setParentId(p.getId());

        assertThatThrownBy(() -> taxonomyAdminService.updateCategory(p.getId(), selfReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category cannot be its own parent");
    }

    @Test
    void tagCrud_and_popularTags_success() {
        CreateTagRequest req1 = new CreateTagRequest();
        req1.setSlug("spring-boot");
        req1.setName("Spring Boot");
        TagResponse tag1 = taxonomyAdminService.createTag(req1);
        assertThat(tag1.getSlug()).isEqualTo("spring-boot");

        CreateTagRequest req2 = new CreateTagRequest();
        req2.setSlug("jpa");
        req2.setName("JPA");
        taxonomyAdminService.createTag(req2);

        List<TagResponse> popular = tagQueryService.findPopular(10);
        assertThat(popular).hasSize(2);

        taxonomyAdminService.deleteTag(tag1.getId());
        assertThat(tagQueryService.findPopular(10)).hasSize(1);
    }
}
