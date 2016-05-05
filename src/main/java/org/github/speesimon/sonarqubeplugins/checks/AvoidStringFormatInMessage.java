/**
 * Copyright 2016 SpeeSimon
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.github.speesimon.sonarqubeplugins.checks;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.ast.visitors.SubscriptionVisitor;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.java.matcher.MethodMatcherCollection;
import org.sonar.java.matcher.NameCriteria;
import org.sonar.java.matcher.TypeCriteria;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

/**
 * This {@link Rule} checks if a slf4j log message is called in a bad way. Bad:
 * 
 * <pre>
 * logger.debug(String.format("The negative effect on the performance is %s", "GREAT"));
 * logger.debug("This is also " + notGood);
 * logger.debug(generateLoggingString());
 * </pre>
 * 
 * Good:
 * 
 * <pre>
 * logger.debug("The negative effect on the performance is {}", LOW);
 * </pre>
 */
@Rule(key = "Methods4logmsg", 
	name = "Log messages should not use \"String.format()\" or method calls",
	description = "Using <code>String.format()</code> or any other method call as a log message has an impact on the performance. "
		+ "<br>Change it into a String with {}-placeholders, like <code>logger.debug(\"The negative effect on the performance is {}\", \"LOW\")</code>"
		+ "<br>See <a href=\"http://www.slf4j.org/faq.html#logging_performance\">http://www.slf4j.org/faq.html#logging_performance</a>", 
	tags = {"bad-practice", "convention", "suspicious", "performance" }, 
	priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EFFICIENCY_COMPLIANCE)
@SqaleConstantRemediation("10min")
public class AvoidStringFormatInMessage extends SubscriptionVisitor {

	private static final String STRING_CLASSNAME = "java.lang.String";
	private static final String SLF4J_LOGGER_CLASSNAME = "org.slf4j.Logger";
	private static final String SLF4J_MARKER_CLASSNAME = "org.slf4j.Marker";

	/**
	 * Only looking for Method Invocation.
	 */
	@Override
	public List<Kind> nodesToVisit() {
		return Arrays.asList(Kind.METHOD_INVOCATION);
	}

	/**
	 * Matchers that match for a call on the {@link Logger}.
	 * 
	 * @return
	 */
	private static boolean isLoggingInvocation(MethodInvocationTree tree) {
		return MethodMatcherCollection.create(
				MethodMatcher.create().typeDefinition(SLF4J_LOGGER_CLASSNAME).name(NameCriteria.any()).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition(SLF4J_LOGGER_CLASSNAME).name(NameCriteria.any()).addParameter(STRING_CLASSNAME) .addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition(SLF4J_LOGGER_CLASSNAME).name(NameCriteria.any()).addParameter(SLF4J_MARKER_CLASSNAME).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition(SLF4J_LOGGER_CLASSNAME).name(NameCriteria.any()).addParameter(SLF4J_MARKER_CLASSNAME).addParameter(STRING_CLASSNAME).addParameter(TypeCriteria.anyType())
			).anyMatch(tree);
	}

	/**
	 * Log invocation that start with the {@link Marker} class. This means the
	 * log message is the 2nd argument, instead of the 1st.
	 * 
	 * @return
	 */
	private static boolean logInvocationStartsWithMarker(MethodInvocationTree tree) {
		return MethodMatcherCollection.create(
				MethodMatcher.create().name(NameCriteria.any()).addParameter(SLF4J_MARKER_CLASSNAME).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().name(NameCriteria.any()).addParameter(SLF4J_MARKER_CLASSNAME).addParameter(STRING_CLASSNAME).addParameter(TypeCriteria.anyType())
			).anyMatch(tree);
	}

	/**
	 * Matchers that match for a call to {@link String#format(String, Object...)}.
	 * 
	 * @return
	 */
	private static boolean isStringFormatInvocation(MethodInvocationTree tree) {
		return MethodMatcherCollection.create(
				MethodMatcher.create().typeDefinition(STRING_CLASSNAME).name(NameCriteria.is("format")).addParameter(STRING_CLASSNAME).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition(STRING_CLASSNAME).name(NameCriteria.is("format")).addParameter(TypeCriteria.anyType())
			).anyMatch(tree);
	}


	@Override
	public void visitNode(Tree tree) {
		if (isLoggingInvocation((MethodInvocationTree) tree)) {
			final ExpressionTree logMessageArgument = getLogMessageArgument((MethodInvocationTree) tree);

			switch (logMessageArgument.kind()) {
			case PLUS:
				checkPlus(logMessageArgument);
				break;
			case METHOD_INVOCATION:
			case METHOD_REFERENCE:
				checkMethod(logMessageArgument);
				break;
			case STRING_LITERAL:
			case IDENTIFIER:
			default:
				break;
			}
		}
	}

	/**
	 * Retrieve the argument containing the Log message.
	 * If a {@link Marker} is also provided, this is the 2nd argument and not the 1st.
	 * @param methodInvocationTree The method invocation tree
	 * @return The argument expression tree
	 */
	private static final ExpressionTree getLogMessageArgument(MethodInvocationTree methodInvocationTree) {
		if (logInvocationStartsWithMarker(methodInvocationTree)) {
			return methodInvocationTree.arguments().get(1);
		}
		return methodInvocationTree.arguments().get(0);
	}

	/**
	 * Warn about the concatenating in the log message.
	 * @param tree
	 */
	private void checkPlus(ExpressionTree tree) {
		BinaryExpressionTree plusArgument = (BinaryExpressionTree) tree;
		if (isOnlyStringConcatenate(plusArgument)) {
			context.reportIssue(this, tree, "Avoid String concatenating in log messages");
		} else {
			context.reportIssue(this, tree, "Avoid Object concatenating in log messages");
		}
	}

	/**
	 * Check if the {@link BinaryExpressionTree} exists of only String literals.
	 * @param tree
	 * @return <code>true</code> if only String literals are concatenated. Otherwise <code>false</code>
	 */
	private static boolean isOnlyStringConcatenate(BinaryExpressionTree tree) {
		boolean left = tree.leftOperand().is(Kind.STRING_LITERAL)
				|| (tree.leftOperand().is(Kind.PLUS) && isOnlyStringConcatenate((BinaryExpressionTree) tree.leftOperand()));

		if (left) {
			return tree.rightOperand().is(Kind.STRING_LITERAL)
					|| (tree.rightOperand().is(Kind.PLUS) && isOnlyStringConcatenate((BinaryExpressionTree) tree.rightOperand()));
		}
		return false;
	}

	/**
	 * Warn about the method invocation for the log message
	 * @param tree
	 */
	private void checkMethod(ExpressionTree tree) {
		MethodInvocationTree methodArgument = (MethodInvocationTree) tree;

		if (isStringFormatInvocation(methodArgument)) {
			// error
			context.reportIssue(this, tree, "String.format() should not be used as a log message.");
			return;
		}

		context.reportIssue(this, tree, "Avoid method call as a log messages.");
	}

}
