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
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;

public class LogmessageCheckRegistrar implements CheckRegistrar {

	@Override
	public void register(RegistrarContext registrarContext) {
		
		List<Class<? extends JavaCheck>> classesChecks = new ArrayList<>();
		classesChecks.add(Slf4jLogMessageCheck.class);
		List<Class<? extends JavaCheck>> testClassesChecks = classesChecks;
		registrarContext.registerClassesForRepository(LogmessageCheckDefinition.REPOSITORY_KEY, classesChecks, testClassesChecks);
	}

}
