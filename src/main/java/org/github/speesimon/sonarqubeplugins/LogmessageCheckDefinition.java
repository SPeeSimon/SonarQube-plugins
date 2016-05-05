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
package org.github.speesimon.sonarqubeplugins;

import java.util.ArrayList;
import java.util.List;

import org.github.speesimon.sonarqubeplugins.checks.Slf4jLogMessageCheck;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.java.Java;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

public class LogmessageCheckDefinition implements RulesDefinition {
	public static final String REPOSITORY_KEY = "speesimon";
	
	public void define(Context context) {
		NewRepository repository = context.createRepository(REPOSITORY_KEY, Java.KEY);
		repository.setName("speesimon");
		
		List<Class> checks = new ArrayList<>(1);
		checks.add(Slf4jLogMessageCheck.class);
		AnnotationBasedRulesDefinition.load(repository, Java.KEY, checks);

		repository.done();
	}

}
