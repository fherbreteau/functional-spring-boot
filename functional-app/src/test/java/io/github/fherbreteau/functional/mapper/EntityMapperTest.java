package io.github.fherbreteau.functional.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.model.GroupDTO;
import io.github.fherbreteau.functional.model.ItemDTO;
import io.github.fherbreteau.functional.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EntityMapperTest {

    private EntityMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new EntityMapper();
    }

    @Test
    void shouldReturnNullWhenValueIsNotAnItem() {
        ItemDTO dto = mapper.mapToItem(new Object());
        assertThat(dto).isNull();
    }

    @Test
    void shouldReturnAnEmptyListWhenValueIsNotAnItemNorACollection() {
        List<ItemDTO> dtos = mapper.mapToItemList(new Object());
        assertThat(dtos).isEmpty();
    }

    @Test
    void shouldReturnASingleElementListWhenValueIsAnItem() {
        List<ItemDTO> dtos = mapper.mapToItemList(File.builder()
                .withName("name")
                .withOwner(User.builder("user").build()).build());
        assertThat(dtos).hasSize(1);
    }

    @Test
    void shouldReturnNullWhenValueIsNotAnUser() {
        UserDTO dto = mapper.mapToUser(new Object());
        assertThat(dto).isNull();
    }

    @Test
    void shouldReturnAnEmptyListWhenValueIsNotAGroupNorACollection() {
        List<GroupDTO> dtos = mapper.mapToGroupList(new Object());
        assertThat(dtos).isEmpty();
    }

    @Test
    void shouldReturnASingleElementListWhenValueIsAGroup() {
        List<GroupDTO> dtos = mapper.mapToGroupList(Group.builder("name").build());
        assertThat(dtos).hasSize(1);
    }

    @Test
    void shouldReturnAnUnprocessableEntityWhenInputIsNotAnInputStream() {
        ResponseEntity<InputStreamResource> response = mapper.mapStream(new Object(), null);
        assertThat(response).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
