package com.early_express.default_server.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.early_express.default_server")
public class DomainArchitectureTest {

    /**
     * 4개 레이어 구조와 의존성 방향 검증
     */
    @ArchTest
    static final ArchRule layered_architecture =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Application").definedBy("..domain..application..")
                    .layer("Domain").definedBy("..domain..domain..")
                    .layer("Infrastructure").definedBy("..domain..infrastructure..")
                    .layer("Presentation").definedBy("..domain..presentation..")
                    .whereLayer("Domain").mayNotAccessAnyLayer()
                    .whereLayer("Application").mayOnlyAccessLayers("Domain")
                    .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
                    .whereLayer("Presentation").mayOnlyAccessLayers("Domain", "Application");

    /**
     * domain 레이어는 exception과 model 패키지만 허용
     */
    @ArchTest
    static final ArchRule domain_package_structure =
            classes()
                    .that().resideInAPackage("..domain..domain..")
                    .should().resideInAnyPackage(
                            "..domain..domain.exception..",
                            "..domain..domain.model.."
                    );
}