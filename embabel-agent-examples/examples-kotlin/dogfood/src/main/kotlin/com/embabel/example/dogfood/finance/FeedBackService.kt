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
import com.embabel.agent.api.common.create
import com.embabel.common.ai.model.LlmOptions
import com.embabel.example.dogfood.research.Critique
import org.springframework.stereotype.Component

@Component
class FeedBackService {

    fun evaluate(
        query: String,
        response: String,
        context: OperationContext,
        llmOptions: LlmOptions
    ): Critique = context.promptRunner(llmOptions).createObject(
        """
        Your task is to evaluate if the response for the query
        is in line with the context information provided.
        
        Query:
        $query
        
        Response:
        $response
        
        """.trimIndent(),
        Critique::class.java
    )

    fun rewritePromptWithFeedback(
        critique: Critique,
        originalPrompt: String,
        llmOptions: LlmOptions,
        context: OperationContext
    ): String {
        return context.promptRunner(llmOptions).create(
            """
        Rewrite the user-facing prompt based on the critique below.
        Return only the new prompt, no explanations or formatting.

        Original Prompt:
        <$originalPrompt>

        Critique:
        <${critique.reasoning}>
        """.trimIndent()
        )
    }

}