package io.visualdig

import io.damo.aspen.Test
import org.assertj.core.api.Assertions.assertThat
import java.util.Arrays.asList

class CommandRunnerTest : Test({
    describe("#getSplitArgs") {
        test("simple") {
            val result = CommandRunner.getSplitArgs("partay now")

            assertThat(result).isEqualTo(asList("partay", "now"))
        }

        test("with double quotes") {
            val result = CommandRunner.getSplitArgs("partay \"now cow\"")

            assertThat(result).isEqualTo(asList("partay", "now cow"))
        }

        test("with more double quotes") {
            val result = CommandRunner.getSplitArgs("partay --fun \"now cow\" \"pow\"")

            assertThat(result).isEqualTo(asList("partay", "--fun", "now cow", "pow"))
        }
    }

})