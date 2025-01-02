package de.surala.containertool.dockerplugin

import org.gradle.process.BaseExecSpec
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.ProcessForkOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class StubExecSpec : ExecSpec {
    private val commandLine = mutableListOf<String>()
    private var standardOutputStream: OutputStream = ByteArrayOutputStream()
    private var errorOutputStream: OutputStream = ByteArrayOutputStream()

    override fun setCommandLine(vararg commandLine: Any?) {
        this.commandLine.clear()
        this.commandLine.addAll(commandLine.map { it.toString() })
    }

    override fun getCommandLine(): List<String> = commandLine

    override fun setCommandLine(args: MutableList<String>?) {
        this.commandLine.addAll(args ?: emptyList())
    }

    override fun setStandardOutput(outputStream: OutputStream?): BaseExecSpec {
        this.standardOutputStream = outputStream ?: ByteArrayOutputStream()
        return this
    }

    override fun setErrorOutput(outputStream: OutputStream?): BaseExecSpec {
        this.errorOutputStream = outputStream ?: ByteArrayOutputStream()
        return this
    }

    override fun getStandardOutput(): OutputStream = standardOutputStream

    override fun getErrorOutput(): OutputStream = errorOutputStream

    override fun setCommandLine(args: MutableIterable<*>?) {
        throw UnsupportedOperationException("Not implemented 1")
    }

    override fun commandLine(vararg args: Any?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 2")
    }

    override fun commandLine(args: MutableIterable<*>?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 3")
    }

    override fun args(vararg args: Any?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 4")
    }

    override fun args(args: MutableIterable<*>?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 5")
    }

    override fun setArgs(args: MutableList<String>?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 6")
    }

    override fun setArgs(args: MutableIterable<*>?): ExecSpec {
        throw UnsupportedOperationException("Not implemented 7")
    }

    override fun getArgs(): MutableList<String> {
        throw UnsupportedOperationException("Not implemented 8")
    }

    override fun getArgumentProviders(): MutableList<CommandLineArgumentProvider> {
        throw UnsupportedOperationException("Not implemented 9")
    }


    override fun getExecutable(): String {
        throw UnsupportedOperationException("Not implemented 11")
    }

    override fun setExecutable(executable: String?) {
        throw UnsupportedOperationException("Not implemented 12")
    }

    override fun setExecutable(executable: Any?) {
        throw UnsupportedOperationException("Not implemented 13")
    }

    override fun executable(executable: Any?): ProcessForkOptions {
        throw UnsupportedOperationException("Not implemented 14")
    }

    override fun getWorkingDir(): File {
        throw UnsupportedOperationException("Not implemented 15")
    }

    override fun setWorkingDir(dir: File?) {
        throw UnsupportedOperationException("Not implemented 16")
    }

    override fun setWorkingDir(dir: Any?) {
        throw UnsupportedOperationException("Not implemented 17 ")
    }

    override fun workingDir(dir: Any?): ProcessForkOptions {
        throw UnsupportedOperationException("Not implemented 18 ")
    }

    override fun getEnvironment(): MutableMap<String, Any> {
        throw UnsupportedOperationException("Not implemented 19")
    }

    override fun setEnvironment(environmentVariables: MutableMap<String, *>?) {
        throw UnsupportedOperationException("Not implemented 20")
    }

    override fun environment(environmentVariables: MutableMap<String, *>?): ProcessForkOptions {
        throw UnsupportedOperationException("Not implemented 21")
    }

    override fun environment(name: String?, value: Any?): ProcessForkOptions {
        throw UnsupportedOperationException("Not implemented 22")
    }

    override fun copyTo(options: ProcessForkOptions?): ProcessForkOptions {
        throw UnsupportedOperationException("Not implemented 23")
    }

    override fun setIgnoreExitValue(ignoreExitValue: Boolean): BaseExecSpec {
        throw UnsupportedOperationException("Not implemented 24")
    }

    override fun isIgnoreExitValue(): Boolean {
        throw UnsupportedOperationException("Not implemented 25")
    }

    override fun setStandardInput(inputStream: InputStream?): BaseExecSpec {
        throw UnsupportedOperationException("Not implemented 26")
    }

    override fun getStandardInput(): InputStream {
        throw UnsupportedOperationException("Not implemented 27")
    }
}

class StubExecResult(private val exitValue: Int) : ExecResult {
    override fun getExitValue(): Int = exitValue
    override fun assertNormalExitValue(): ExecResult {
        if (exitValue != 0) throw IllegalStateException("Process did not exit normally")
        return this
    }

    override fun rethrowFailure(): ExecResult = this
}