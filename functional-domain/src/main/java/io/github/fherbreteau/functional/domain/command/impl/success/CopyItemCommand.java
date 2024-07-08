package io.github.fherbreteau.functional.domain.command.impl.success;

import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.domain.entities.Error;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

import java.io.InputStream;

public class CopyItemCommand extends AbstractModifyItemCommand<Item> {
    private final ContentRepository contentRepository;
    private final Item source;
    private final Item destination;

    public CopyItemCommand(ItemRepository repository, ContentRepository contentRepository, AccessUpdater accessUpdater,
                           Item source, Item destination) {
        super(repository, accessUpdater);
        this.contentRepository = contentRepository;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public Output<Item> execute(User actor) {
        Item newItem;
        if (source instanceof File file) {
            newItem = File.builder()
                    .withName(getDestinationName())
                    .withParent(getDestinationFolder())
                    .withOwner(actor)
                    .withOwnerAccess(AccessRight.readWrite())
                    .withGroupAccess(AccessRight.readOnly())
                    .withOtherAccess(AccessRight.none())
                    .withContentType(file.getContentType())
                    .build();
        } else {
            newItem = Folder.builder()
                    .withName(getDestinationName())
                    .withParent(getDestinationFolder())
                    .withOwner(actor)
                    .withOwnerAccess(AccessRight.full())
                    .withGroupAccess(AccessRight.readExecute())
                    .withOtherAccess(AccessRight.none())
                    .build();
        }
        Item item = accessUpdater.createItem(repository.create(newItem));
        if (item instanceof File output && source instanceof File input) {
            Output<Item> init = contentRepository.initContent(output);
            if (init.isError()) {
                return copyError(init);
            }
            Output<InputStream> content = contentRepository.readContent(input);
            if (content.isError()) {
                return copyError(content);
            }
            return contentRepository.writeContent(output, content.getValue());
        }
        return Output.success(item);
    }

    private Folder getDestinationFolder() {
        return destination.isFile() ? destination.getParent() : (Folder) destination;
    }

    private String getDestinationName() {
        return  destination.isFile() ? destination.getName() : source.getName();
    }

    private <I, O> Output<O> copyError(Output<I> input) {
        Error error = input.getError();
        return Output.error(error.getMessage(), error.getReasons());
    }
}
