/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.example.dogfood.finance

import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import com.embabel.example.dogfood.research.Critique
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.springframework.stereotype.Component

@Component
class DumbChatService(val terminal: Terminal, val feedBackService: FeedBackService) {

    private fun promptUser(prompt: String): String {
        val lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build()
        lineReader.printAbove(prompt)
        return lineReader.readLine("You: ")
    }

    fun promptUser(
        initialPrompt: String,
        context: OperationContext,
        llmOptions: LlmOptions,
        maxRetries: Int = 5
    ): String {
        var prompt = initialPrompt
        var userResponse: String
        var critique: Critique

        repeat(maxRetries) {
            userResponse = promptUser(prompt)
            critique =
                feedBackService.evaluate(initialPrompt, userResponse, context, llmOptions)

            if (critique.accepted) return userResponse

            prompt = feedBackService.rewritePromptWithFeedback(critique, prompt, llmOptions, context)
        }

        throw IllegalStateException("Unable to get acceptable risk profile after $maxRetries attempts")
    }


}