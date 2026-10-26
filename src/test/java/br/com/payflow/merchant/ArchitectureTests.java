package br.com.payflow.merchant;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

// Guards the Clean Architecture + Vertical Slice rules described in the README.
class ArchitectureTests {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("br.com.payflow.merchant");

    private static final String[] FRAMEWORKS = {
            "org.springframework..", "jakarta..", "com.mongodb..", "org.bson..", "io.swagger..",
            "tools.jackson..", "com.fasterxml.jackson.."};

    // Spring wiring (*Slice), HTTP adapters (*Endpoint, with nested request/response records) and properties.
    private static final String ADAPTER_NAMES = ".*(Slice|Endpoint|Properties)(\\$.*)?";

    private static final DescribedPredicate<JavaClass> ENDPOINTS_OR_WEB = DescribedPredicate.describe(
            "an Endpoint or web infrastructure",
            javaClass -> javaClass.getPackageName().endsWith(".merchant.api")
                    || javaClass.getPackageName().endsWith(".merchant.infrastructure.web")
                    || javaClass.getName().matches(".*Endpoint(\\$.*)?"));

    @Test
    void featureSlicesDoNotDependOnEachOther() {
        slices().matching("br.com.payflow.merchant.features.(*)..")
                .should().notDependOnEachOther()
                .check(CLASSES);
    }

    @Test
    void domainIsFrameworkFreeAndIndependentOfOuterLayers() {
        noClasses().that().resideInAPackage("..merchant.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
                .orShould().dependOnClassesThat().resideInAnyPackage(
                        "..merchant.features..", "..merchant.infrastructure..", "..merchant.api..")
                .check(CLASSES);
    }

    @Test
    void useCasesAndPortsAreFrameworkFree() {
        noClasses().that().resideInAPackage("..merchant.features..")
                .and().haveNameNotMatching(ADAPTER_NAMES)
                .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
                .check(CLASSES);
    }

    @Test
    void slicesNeverReachInfrastructureDirectly() {
        noClasses().that().resideInAPackage("..merchant.features..")
                .should().dependOnClassesThat().resideInAPackage("..merchant.infrastructure..")
                .check(CLASSES);
    }

    @Test
    void sharedApiRepresentationIsOnlyUsedByEndpointsAndWebInfrastructure() {
        classes().that().resideInAPackage("..merchant.api..")
                .should().onlyBeAccessed().byClassesThat(ENDPOINTS_OR_WEB)
                .check(CLASSES);
    }
}
