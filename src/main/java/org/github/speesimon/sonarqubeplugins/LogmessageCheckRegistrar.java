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
import java.util.Collections;
import java.util.List;

import org.github.speesimon.sonarqubeplugins.checks.AvoidStringFormatInMessage;
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;

public class LogmessageCheckRegistrar implements CheckRegistrar {

	@Override
	public void register(RegistrarContext registrarContext) {
		
		List<Class<? extends JavaCheck>> list = new ArrayList<>();
		list.add(AvoidStringFormatInMessage.class);
		List<Class<? extends JavaCheck>> list2 = Collections.emptyList();
		registrarContext.registerClassesForRepository(LogmessageCheckDefinition.REPOSITORY_KEY, list, list2);
	}

}