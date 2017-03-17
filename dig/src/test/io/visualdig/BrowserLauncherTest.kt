package io.visualdig

import com.nhaarman.mockito_kotlin.*
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import java.io.File
import java.net.URI

class BrowserLauncherTest : Test({
    describe("osx") {
        test("DIG_BROWSER set to firefox") {
            val env = mapOf("DIG_BROWSER" to "firefox")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(true)
            whenever(operatingSystemMock.isLinux()).thenReturn(false)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand("firefox -safe-mode \"http://www.example.com/\"", File("/"))
        }

        test("DIG_BROWSER set to chrome") {
            val env = mapOf("DIG_BROWSER" to "chrome")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(true)
            whenever(operatingSystemMock.isLinux()).thenReturn(false)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand(check {
                assertThat(it).contains("chrome --app=\"http://www.example.com/\"")
                assertThat(it).contains("--no-first-run")
                assertThat(it).contains("--user-data-dir=/tmp/")
                assertThat(it).contains("--disk-cache-dir=/dev/null")
            }, eq(File("/")))
        }

        test("env variable not found") {
            val env: Map<String, String> = emptyMap()
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(true)
            whenever(operatingSystemMock.isLinux()).thenReturn(false)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand(check {
                assertThat(it).contains("\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\" --app=\"http://www.example.com/\"")
                assertThat(it).contains("--no-first-run")
                assertThat(it).contains("--user-data-dir=/tmp/")
                assertThat(it).contains("--disk-cache-dir=/dev/null")
            }, eq(File("/")))
        }

        test("headless mode on") {
            val env = mapOf("DIG_BROWSER" to "firefox")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(true)
            whenever(operatingSystemMock.isLinux()).thenReturn(false)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            assertThatExceptionOfType(NotImplementedError::class.java).isThrownBy({browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = true)})

            verifyZeroInteractions(commandRunnerMock)
        }
    }

    describe("linux") {
        test("DIG_BROWSER set to firefox") {
            val env = mapOf("DIG_BROWSER" to "firefox")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(false)
            whenever(operatingSystemMock.isLinux()).thenReturn(true)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand("firefox -safe-mode \"http://www.example.com/\"", File("/"))
        }

        test("DIG_BROWSER set to chrome") {
            val env = mapOf("DIG_BROWSER" to "chrome")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(false)
            whenever(operatingSystemMock.isLinux()).thenReturn(true)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand(check {
                assertThat(it).contains("chrome --app=\"http://www.example.com/\"")
                assertThat(it).contains("--no-first-run")
                assertThat(it).contains("--user-data-dir=/tmp/")
                assertThat(it).contains("--disk-cache-dir=/dev/null")
            }, eq(File("/")))
        }

        test("env variable not found") {
            val env: Map<String, String> = emptyMap()
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(false)
            whenever(operatingSystemMock.isLinux()).thenReturn(true)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = false)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand(check {
                assertThat(it).contains("google-chrome --app=\"http://www.example.com/\"")
                assertThat(it).contains("--no-first-run")
                assertThat(it).contains("--user-data-dir=/tmp/")
                assertThat(it).contains("--disk-cache-dir=/dev/null")
            }, eq(File("/")))
        }

        test("headless mode on") {
            val env = mapOf("DIG_BROWSER" to "firefox")
            val commandRunnerMock: CommandRunner = mock()
            val operatingSystemMock: OperatingSystem = mock()
            whenever(operatingSystemMock.isOSX()).thenReturn(false)
            whenever(operatingSystemMock.isLinux()).thenReturn(true)
            whenever(operatingSystemMock.isWindows()).thenReturn(false)

            val browserLaunch = BrowserLauncher(env, commandRunnerMock, operatingSystemMock)

            val didLaunch = browserLaunch.launchBrowser(URI("http://www.example.com/"), headless = true)

            assertThat(didLaunch).isTrue()

            verify(commandRunnerMock).runCommand(any(), any())
            verify(commandRunnerMock).runCommand("xvfb-run firefox -safe-mode \"http://www.example.com/\"", File("/"))
        }
    }
})