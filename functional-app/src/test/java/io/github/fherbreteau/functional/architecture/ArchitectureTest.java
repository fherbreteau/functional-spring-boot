package io.github.fherbreteau.functional.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@AnalyzeClasses(packages = "io.github.fherbreteau.functional", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    public static final ArchRule ENTITIES_RULE = classes()
            .that().resideInAnyPackage("..domain.entities..")
            .should().onlyBeAccessed().byAnyPackage("..domain..", "..driving..", "..driven..", "..mapper..", "..infra..", "..exception..", "..rules..", "..service..", "..config..");

    @ArchTest
    public static final ArchRule DRIVING_RULE = classes()
            .that().resideInAnyPackage("..domain.access..", "..domain.command..", "..domain.path..", "..domain.rules..", "..domain.user..")
            .should().onlyBeAccessed().byAnyPackage("..domain..", "..driving..", "..config..");

    @ArchTest
    public static final ArchRule SERVICE_RULE = classes()
            .that().resideInAnyPackage("..driving..")
            .should().onlyBeAccessed().byAnyPackage("..domain..", "..driving..", "..config..", "..service..");

    @ArchTest
    public static final ArchRule CONTROLLER_RULE = classes()
            .that().resideInAnyPackage("..service..")
            .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..", "..config..");

    @ArchTest
    public static final ArchRule DRIVEN_RULE = classes()
            .that().resideInAnyPackage("..driven..")
            .should().onlyBeAccessed().byAnyPackage("..domain..");
}
