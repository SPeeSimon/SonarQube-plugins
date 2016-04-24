# SonarQube-plugins
Plugin for the static analysis platform [SonarQube](http://www.sonarqube.org/).
This plugins add some rules to the Java language.

# Current rules
* Log messages should not use "String.format()"

# How to install
* Place the jar file in the SONARQUBE_HOME/extensions/plugins directory.
* Restart the SonarQube server.
* Open a web browser and login to your SonarQube.
* Go to Rules.
* Find the rules listed above.
* Activate the rule for your Quality Profiles.

The rules are now active and will check your code when performing analysis.
