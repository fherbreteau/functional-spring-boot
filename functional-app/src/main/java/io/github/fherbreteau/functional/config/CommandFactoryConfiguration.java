package io.github.fherbreteau.functional.config;

import io.github.fherbreteau.functional.domain.command.factory.impl.*;
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
    public CopyItemCommandFactory copyItemCommandFactory() {
        return new CopyItemCommandFactory();
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
    public MoveItemCommandFactory moveCommandFactory() {
        return new MoveItemCommandFactory();
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

    @Bean
    public GetUserCommandFactory identityCommandFactory() {
        return new GetUserCommandFactory();
    }

    @Bean
    public GetGroupCommandFactory groupCommandFactory() {
        return new GetGroupCommandFactory();
    }
}
