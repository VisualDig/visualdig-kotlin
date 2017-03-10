package io.virtualdig

import java.io.File
import java.util.*

open class CommandRunner {
    open fun runCommand(command: String, workingDir: File): Process? {
        val split: List<String> = getSplitArgs(command)
        return ProcessBuilder(split)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
    }

    companion object {
        fun getSplitArgs(command: String): List<String> {
            val split: List<String> = command.split(" ")
            val finalSplit: MutableList<String> = ArrayList()
            for (index in 0..split.lastIndex) {
                val s = split[index]
                if (!finalSplit.none() && finalSplit.last().contains(s)) continue

                if (s.startsWith('"')) {
                    var joinedString = s
                    for (x in split.slice(index + 1..split.lastIndex)) {
                        joinedString = "$joinedString $x"
                        if (x.endsWith('"')) {
                            break
                        }
                    }
                    finalSplit.add(joinedString)
                } else {
                    finalSplit.add(s)
                }
            }
            return finalSplit.map { it.replace("\"", "") }
        }
    }
}
