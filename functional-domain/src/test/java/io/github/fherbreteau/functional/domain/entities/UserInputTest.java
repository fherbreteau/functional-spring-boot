package io.github.fherbreteau.functional.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

import org.junit.jupiter.api.Test;

class UserInputTest {

    @Test
    void inputItemContentShouldBePreserved() {
        UserInput input = UserInput.builder("name").withForce(false).build();
        assertThat(input)
                .hasToString("UserInput{userId=null, name='name', password='null', groupId=null, groups='[]', newName='null', force=false, append=false}")
                .extracting(UserInput::isForce, BOOLEAN)
                .isFalse();
    }

    @Test
    void outputItemFromThrowableShouldUseThrowableMessage() {
        assertThat(Output.failure(new Exception("message")))
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("message");
    }
}
