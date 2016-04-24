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

import java.util.List;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.matcher.MethodMatcher;
import org.sonar.java.matcher.NameCriteria;
import org.sonar.java.matcher.TypeCriteria;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.google.common.collect.ImmutableList;

/**
 * This {@link Rule} checks if a slf4j log message is called in a bad way.
 * Bad:
 * <pre>
 *  logger.debug(String.format("The negative effect on the performance is %s", "GREAT"));
 * </pre>
 * Good:
 * <pre>
 *  logger.debug("The negative effect on the performance is {}", "LOW");
 * </pre>
 */
@Rule(key = "StringFormat4logmsg", name = "Log messages should not use \"String.format()\"",
	description = "Using <code>String.format()</code> as a log message has an impact on the performance. "
				+ "<br>Change it into a String with {}-placeholders, like <code>logger.debug(\"The negative effect on the performance is {}\", \"LOW\")</code>"
				+ "<br>See <a href=\"http://www.slf4j.org/faq.html#logging_performance\">http://www.slf4j.org/faq.html#logging_performance</a>",
	tags = {"bad-practice", "convention", "suspicious", "performance"}, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EFFICIENCY_COMPLIANCE)
@SqaleConstantRemediation("10min")
public class AvoidStringFormatInMessage extends BaseTreeVisitor implements JavaFileScanner {
	private JavaFileScannerContext context;

	private static final String STRING_CLASSNAME = String.class.getName();

	protected List<MethodMatcher> getLogInvocationMatchers() {
		return ImmutableList.of(
				MethodMatcher.create().typeDefinition("org.slf4j.Logger").name(NameCriteria.any()).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition("org.slf4j.Logger").name(NameCriteria.any()).addParameter(STRING_CLASSNAME).addParameter(TypeCriteria.anyType())
			);
	}

	protected List<MethodMatcher> getStringFormatInvocationMatchers() {
		return ImmutableList.of(
				MethodMatcher.create().typeDefinition(STRING_CLASSNAME).name(NameCriteria.is("format")).addParameter(STRING_CLASSNAME).addParameter(TypeCriteria.anyType()),
				MethodMatcher.create().typeDefinition(STRING_CLASSNAME).name(NameCriteria.is("format")).addParameter(TypeCriteria.anyType())
			);
	}

	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		boolean isLogCall = isLogging(tree);

		if (isLogCall) {
			switch (tree.arguments().get(0).kind()) {
			case STRING_LITERAL:
			case IDENTIFIER:
				break;
			case PLUS:
				checkPlus(tree);
				break;
			case METHOD_INVOCATION:
			case METHOD_REFERENCE:
				checkMethod(tree);
				break;
			default:
//				context.reportIssue(this, tree, "Log4j method usage (unknown) " + concatenate(tree));
				break;
			}
		}

		super.visitMethodInvocation(tree);
	}


	private final boolean isLogging(MethodInvocationTree tree) {
		if (tree.arguments().size() > 0) {
			for (MethodMatcher matcher : getLogInvocationMatchers()) {
				if ( matcher.matches(tree) ){
					return true;
				}
			}
		}
		return false;
	}


	private void checkPlus(MethodInvocationTree tree) {
		BinaryExpressionTree plusArgument = (BinaryExpressionTree) tree.arguments().get(0);
		if ( isOnlyStringConcatenate(plusArgument) ) {
			context.reportIssue(this, tree, "Avoid String concatenating in log messages");
		} else {
			context.reportIssue(this, tree, "Avoid Object concatenating in log messages");
		}
	}
	

	private boolean isOnlyStringConcatenate(BinaryExpressionTree tree){
		boolean left = tree.leftOperand().is(Kind.STRING_LITERAL) || 
						(tree.leftOperand().is(Kind.PLUS) && isOnlyStringConcatenate((BinaryExpressionTree) tree.leftOperand()) );
		
		if( left ){
			return tree.rightOperand().is(Kind.STRING_LITERAL) || 
					(tree.rightOperand().is(Kind.PLUS) && isOnlyStringConcatenate((BinaryExpressionTree) tree.rightOperand()) );
		}
		return false;
	}
	

	private void checkMethod(MethodInvocationTree tree) {
		MethodInvocationTree methodArgument = (MethodInvocationTree) tree.arguments().get(0);

		for (MethodMatcher matcher : getStringFormatInvocationMatchers()) {
			if (matcher.matches(methodArgument)) {
				// error
				context.reportIssue(this, tree, "String.format() should not be used as a log message.");
				return;
			}
		}
		
		context.reportIssue(this, tree, "Avoid method call for log messages.");
	}

}
