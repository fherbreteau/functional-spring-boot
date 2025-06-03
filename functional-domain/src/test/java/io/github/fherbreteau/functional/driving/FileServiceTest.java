package io.github.fherbreteau.functional.driving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.CompositeItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckUnsupportedItemCommand;
import io.github.fherbreteau.functional.domain.entities.Failure;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.Path;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.path.CompositePathParserFactory;
import io.github.fherbreteau.functional.domain.path.PathParser;
import io.github.fherbreteau.functional.driving.impl.FileServiceImpl;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("rawtypes")
class FileServiceTest {
    private FileService fileService;
    @Mock
    private CompositeItemCommandFactory commandFactory;
    @Mock
    private CompositePathParserFactory pathFactory;
    @Mock
    private User actor;
    @Mock
    private CheckCommand checkCommand;
    @Mock
    private Command<Output> executeCommand;
    @Mock
    private Item item;

    @BeforeEach
    void setup() {
        fileService = new FileServiceImpl(commandFactory, pathFactory);
    }

    @Test
    void testAccessExistingPathShouldSucceed() {
        // GIVEN
        PathParser parser = mock(PathParser.class);
        given(parser.resolve(actor)).willReturn(Path.ROOT);
        given(pathFactory.createParser(Path.ROOT, "/")).willReturn(parser);
        // WHEN
        Path path = fileService.getPath("/", actor);
        // THEN
        assertThat(path).isNotNull()
                .satisfies(p -> assertThat(path.isError()).isFalse())
                .satisfies(p -> assertThat(path.getItem()).isEqualTo(Folder.getRoot()));
    }

    @Test
    void testAccessUnknownPathShouldFail() {
        // GIVEN
        Path error = Path.error(Failure.failure(String.format("%s not found in %s for %s", "unknown", Folder.getRoot().getHandle(), actor)));
        PathParser parser = mock(PathParser.class);
        given(parser.resolve(actor)).willReturn(error);
        given(pathFactory.createParser(Path.ROOT, "/unknown")).willReturn(parser);

        // WHEN
        Path path = fileService.getPath("/unknown", actor);
        // THEN
        assertThat(path).isNotNull()
                .satisfies(p -> assertThat(p.isError()).isTrue())
                .satisfies(p -> assertThat(p.getError().getMessage()).isEqualTo("unknown not found in 00000000-0000-0000-0000-000000000000 for actor"))
                .satisfies(p -> assertThat(p.getError().getReasons()).isEmpty());
    }

    @Test
    void testProcessKnownCommandShouldOutputAResult() {
        // Given
        given(commandFactory.createCommand(any(), any())).willReturn(checkCommand);
        given(checkCommand.execute(actor)).willReturn(executeCommand);
        given(executeCommand.execute(actor)).willReturn(Output.success(new Object()));
        // When
        Output<File> result = fileService.processCommand(ItemCommandType.TOUCH, actor, ItemInput.builder(item).build());
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getValue)
                .isNotNull();
        assertThat(result).extracting(Output::isFailure)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isFalse();
        assertThat(result)
                .extracting(Output::getFailure)
                .isNull();
    }

    @Test
    void testProcessUnknownCommandShouldOutputAResult() {
        // Given
        given(commandFactory.createCommand(any(), any()))
                .willAnswer(invocation -> {
                    ItemCommandType type = invocation.getArgument(0);
                    ItemInput itemInput = invocation.getArgument(1);
                    return new CheckUnsupportedItemCommand(null, null, type, itemInput);
                });
        // When
        Output<File> result = fileService.processCommand(ItemCommandType.TOUCH, actor, ItemInput.builder(item).build());
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isFalse();
        assertThat(result)
                .extracting(Output::getValue)
                .isNull();
        assertThat(result).extracting(Output::isFailure)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .isNotNull()
                .extracting(Failure::getMessage)
                .isNotNull();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getReasons, list(String.class))
                .isEmpty();
    }
}
