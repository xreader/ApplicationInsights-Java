
#Track the Impact of Your Deployments with Application Insights

This sample shows how to implement deployment markers in Application Insights for a Java web application using the Application Insights SDK for Java, Jenkins and Git.

##Prerequisite
Your web apps must be instrumented with the Application Insights Java SDK.
See [Get started with Application Insights in a Java web project](https://azure.microsoft.com/en-us/documentation/articles/app-insights-java-get-started/) to learn how this can be done. 

##Step 1 – Update your Jenkins build job to generate a build info file 
Add an "Execute shell" or "Execute Windows batch command" build step to your build.
On a Windows system this step should contain the following command:

    (echo git.commit=%GIT_COMMIT% & echo git.branch=%GIT_BRANCH% & echo git.repo=%GIT_URL%) > src\main\resources\source-origin.properties

On a Windows system this step should contain the following command:

    (echo git.commit=$GIT_COMMIT & echo git.branch=$GIT_BRANCH & echo git.repo=$GIT_URL) > src\main\resources\source-origin.properties

These commands generate a `source-origin.properties` file containing the following properties to be consumed by Application Insights:

- `git.commit` holding the commit hash
- `git.branch` holding the branch of the build 
- `git.repo` hodling the url of the git repository  

Finally, place this build step **before** the actual build (packaging) of your project.


##Step 2 – Add the build properties to the telemetry events you send with Application Insights
Download [this JAR](./applicationinsights-sample-plugins.jar?raw=true) containing the GitBuildInfoContextInitializer plugin and add it to your project’s CLASSPATH.

Then, edit your project’s ApplicationInsights.xml file and merge the following snippet of code into it:

    <ContextInitializers>
      <Add type="com.microsoft.applicationinsights.sample.plugins.GitBuildInfoContextInitializer"/>
    </ContextInitializers>

The source code of this context initializer is located [here](https://github.com/Microsoft/ApplicationInsights-Java/blob/master/samples/src/plugins/java/com/microsoft/applicationinsights/sample/plugins/GitBuildInfoContextInitializer.java).

##Step 3 – Build, deploy and view your telemetry events
After building and deploying your updated application browse to your Application Insights resource within the [Microsoft Azure Portal](http://portal.azure.com). 
Then, use Metric Explorer to group any of your metrics by the build properties that were sent from Jenkins.


