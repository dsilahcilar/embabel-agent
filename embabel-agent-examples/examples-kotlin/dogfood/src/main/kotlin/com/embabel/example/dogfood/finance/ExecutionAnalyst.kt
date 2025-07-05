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

import StrategyExecutionPlan
import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelProvider.Companion.CHEAPEST_ROLE
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.byRole
import org.slf4j.LoggerFactory

data class ExecutionRequest(
    val tradingStrategy: Strategy,
    val riskProfile: String,
    val investmentPeriod: String,
)

@Agent(
    description = """
        The Strategic Execution Planner is a specialized reasoning agent designed to generate a comprehensive, 
        actionable execution plan for trading and investment activities. It operates by deeply analyzing and 
        integrating three core user inputs—Trading Strategy, Risk Attitude, and Investment Period—to produce a tailored
        and logically coherent plan.
   """
)
class ExecutionAnalyst(
    var properties: FinanceAnalystProperties,
    var chatService: DumbChatService
) {

    private val logger = LoggerFactory.getLogger(ExecutionAnalyst::class.java)

    init {
        logger.info("Execution analyst agent initialized: $properties")
    }

    @Action(
        post = [ReportStates.SELECTED_STRATEGY],
        canRerun = true
    )
    fun promptUserToChoseTradingStrategy(
        tradingReport: TradingReport,
        context: OperationContext
    ): Strategy? {
        val strategyMap: Map<Int, String> = tradingReport.strategies
            .mapIndexed { index, strategy -> index + 1 to strategy.name }
            .toMap()

        val initialPrompt = buildString {
            appendLine("💡 Please review the available investment strategies below and select the one you'd like to explore further:\n")
            strategyMap.forEach { index, strategy ->
                appendLine("$index. $strategy")
            }
            append("\n👉 Enter the number corresponding to the strategy you're most interested in (e.g., 1, 2, 3):")
        }

        val selectedStrategy = chatService.promptUser(
            initialPrompt,
            context,
            LlmOptions(properties.openAiModelName)
        )
        return tradingReport.strategies.find { it.name == strategyMap[selectedStrategy.toInt()] }
    }

    @Action
    fun extractExecutionRequest(
        strategy: Strategy,
        riskProfile: RiskProfile,
        investmentPeriod: InvestmentPeriod,
    ) = ExecutionRequest(
        strategy,
        riskProfile.riskProfile,
        investmentPeriod.investmentPeriod
    )

    @Condition(ReportStates.SELECTED_STRATEGY)
    fun selectedStrategy(strategy: Strategy?) = strategy != null


    @Action
    fun generateExecutionStrategy(request: ExecutionRequest): StrategyExecutionPlan = using(
        llm = LlmOptions(byRole(CHEAPEST_ROLE))
    ).create(
        """
        Generate a comprehensive and reasoned execution plan tailored to the user's input.

        Inputs:
        Trading Strategy: ${request.tradingStrategy}
        Risk Attitude: ${request.riskProfile}
        Investment Period: ${request.investmentPeriod}
        
        General Requirements for the Analysis:

        Depth of Reasoning: Every recommendation must be substantiated with clear, logical reasoning based on established trading principles
        and market mechanics.
        Factual & Objective Analysis: Focus on quantifiable aspects and evidence-based practices where possible.
        Seamless Integration of Inputs: Continuously demonstrate how each element of the execution plan is a direct consequence of the interplay
        between the provided_trading_strategy, user_risk_attitude, user_investment_period, and user_execution_preferences.
        Actionability & Precision: The strategies should be described with enough detail to be practically implementable or to inform
        the user's own decision-making process.
        Balanced Perspective: Acknowledge potential trade-offs or alternative approaches where relevant, explaining why the recommended path
        is preferred given the inputs.

        """.trimIndent()
    )

//    @Action
//    fun generateMarkdownFile(executionPlan: StrategyExecutionPlan) =
//        executionPlan.toMarkDown(properties.openAiModelName)


    @AchievesGoal(
        description = "Generate execution report for the selected trading strategy, incorporating user risk profile and investment period.",
    )
    @Action(outputBinding = OutputBindings.STRATEGY_PLAN)
    fun acceptStrategy(
        executionPlan: StrategyExecutionPlan,
    ) = executionPlan

    companion object {
        object ReportStates {
            const val SELECTED_STRATEGY = "selectedStrategy"
        }

        object OutputBindings {
            const val STRATEGY_PLAN = "executionStrategyReport"
        }
    }

}
