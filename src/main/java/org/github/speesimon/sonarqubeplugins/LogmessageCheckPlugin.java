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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;


public class LogmessageCheckPlugin extends SonarPlugin {
	
	public List getExtensions() {
		return Arrays.asList( LogmessageCheckDefinition.class, LogmessageCheckRegistrar.class );
	}

}
