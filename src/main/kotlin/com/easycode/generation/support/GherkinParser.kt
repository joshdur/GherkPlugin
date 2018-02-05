package com.easycode.generation.support

import gherkin.AstBuilder
import gherkin.Parser
import gherkin.TokenMatcher
import gherkin.ast.GherkinDocument
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


fun parseFromUri(inputStream: InputStream): GherkinDocument {
    return try {
        val matcher = TokenMatcher()
        val parser = Parser(AstBuilder())
        parser.parse(InputStreamReader(inputStream), matcher)
    } catch (io: IOException) {
        throw IOException("Current user.dir is ${System.getProperty("user.dir")}", io)
    }
}



