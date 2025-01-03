package io.github.fherbreteau.functional.config;

import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeGroupCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeModeCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeOwnerCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.CreateGroupCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.CreateItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.CreateUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DeleteGroupCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DeleteItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DeleteUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DownloadCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ListChildrenCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UnsupportedItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UnsupportedUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UpdateGroupCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UpdateUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UploadCommandFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandFactoryConfiguration {

    @Bean
    public ChangeGroupCommandFactory changeGroupCommandFactory() {
        return new ChangeGroupCommandFactory();
    }

    @Bean
    public ChangeModeCommandFactory changeModeCommandFactory() {
        return new ChangeModeCommandFactory();
    }

    @Bean
    public ChangeOwnerCommandFactory changeOwnerCommandFactory() {
        return new ChangeOwnerCommandFactory();
    }

    @Bean
    public CreateItemCommandFactory createItemCommandFactory() {
        return new CreateItemCommandFactory();
    }

    @Bean
    public DeleteItemCommandFactory deleteItemCommandFactory() {
        return new DeleteItemCommandFactory();
    }

    @Bean
    public DownloadCommandFactory downloadCommandFactory() {
        return new DownloadCommandFactory();
    }

    @Bean
    public ListChildrenCommandFactory listChildrenCommandFactory() {
        return new ListChildrenCommandFactory();
    }

    @Bean
    public UnsupportedItemCommandFactory unsupportedCommandFactory() {
        return new UnsupportedItemCommandFactory();
    }

    @Bean
    public UploadCommandFactory uploadCommandFactory() {
        return new UploadCommandFactory();
    }

    @Bean
    public CreateGroupCommandFactory groupAddCommandFactory() {
        return new CreateGroupCommandFactory();
    }

    @Bean
    public DeleteGroupCommandFactory groupDeleteCommandFactory() {
        return new DeleteGroupCommandFactory();
    }

    @Bean
    public UpdateGroupCommandFactory groupModifyCommandFactory() {
        return new UpdateGroupCommandFactory();
    }

    @Bean
    public UnsupportedUserCommandFactory unsupportedUserCommandFactory() {
        return new UnsupportedUserCommandFactory();
    }

    @Bean
    public CreateUserCommandFactory userAddCommandFactory() {
        return new CreateUserCommandFactory();
    }

    @Bean
    public DeleteUserCommandFactory userDeleteCommandFactory() {
        return new DeleteUserCommandFactory();
    }

    @Bean
    public UpdateUserCommandFactory userModifyCommandFactory() {
        return new UpdateUserCommandFactory();
    }
}
