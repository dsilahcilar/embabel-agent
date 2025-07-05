package com.embabel.example.dogfood.finance

import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.core.types.HasInfoString

fun <T : HasInfoString> generateMarkdownFile(
    instance: T,
    llmModel: String,
    userPrompt: String? = null,
    agentName: String? = null,
    context: OperationContext,
): String =
    context.promptRunner(
        llm = LlmOptions(llmModel)
    ).create<String>(
        """
       Convert the provided JSON data into a well-formatted Markdown document. Follow these guidelines:
       ${
            userPrompt.let {
                "•	User Prompt: (Insert the original prompt the user asked)"
            }
        }
        ${
            agentName.let {
                "•	Agent Name: (Insert the name of the agent generating this report)"
            }
        }

        1. **Structure**: Create a clear hierarchy using appropriate heading levels (# ## ### etc.)
        2. **Formatting**: 
           - Use **bold** for important keys or labels
           - Use `code formatting` for values, especially technical data
           - Create tables for structured data when appropriate
           - Use bullet points or numbered lists for arrays
        3. **Content Organization**:
           - Start with a main title if there's a clear document subject
           - Group related information under relevant sections
           - Preserve the logical structure of the JSON data
        4. **Data Presentation**:
           - Convert objects into sections with key-value pairs
           - Transform arrays into lists or tables as appropriate
           - Handle nested structures with proper indentation
           - Include all data from the JSON, don't omit any fields
        
        Please convert this JSON data to Markdown format:
        
        <${instance.infoString(true)}>

        """.trimIndent()
    )


fun HasInfoString.toMarkDown(
    llmModel: String,
    userPrompt: String?,
    agentName: String?,
    context: OperationContext
) =
    generateMarkdownFile(this, llmModel, userPrompt, agentName, context)
