# GherkPlugin
Gradle Plugin to generate kotlin code from Gherkin files

This plugin is intended to simplify Acceptance Test generation in Android Kotlin projects.
The plugin parses all .feature files in the project generating support classes and functions

## Getting Started

In order to start using the plugin you need to add this to your buildscript.

    repositories {
        maven { 
            url "https://plugins.gradle.org/m2/"  
          }
    }
    dependencies {
        classpath "gradle.plugin.com.easycode:gherk:0.6.8"
    }
    
 Apply the plugin in your project build.gradle.
 
    apply plugin: 'com.easycode.gherk'
    
 ## Configuration
 
 The plugin configuration is implemented inside a gherk block:
 
    gherk {
        outputDirectory = File(%OUTPUT_FOLDER%) // default = $buildDir/generated/gherk/src/
        featureDirectory = File(%INPUT_FOLDER%) // default = $projectDir/src/androidTest/assets/
        reportFolderName = %FOLDER_NAME // default = gherk-report/
    }
 
 * outputDirectory represents the folder where support kotlin files will be generated.
 * featureDirectory is explored recursively to get all files ending with ".feature"
 * reportFolderName is the name of the folder in the path "${context.cacheDir.absolutePath}/$reporFolderName$gherk_report.json"
 
 ## Running
 
 The plugin allows you to stablish a relationship between Gherkin Code (Test definition) and Test Implementation Code in the 
 same way Cucumber does. This approach avoid the need to use any custom InstrumentationTestRunner, and there is no need to 
 overwrite AndroidTestRunner.
 
 Given the following feature
 
    Feature: Going to walk
    
      Scenario: snowing
       Given it is snowing
       When I walk into the street
       Then I should have my umbrella

The code generated allow to write tests in the following way:

    @RunWith(AndroidJUnit4::class)
    class ExampleTestClass {

      @Test
      fun testSnowing() = test_Snowing {
          given_It_Is_Snowing { 
              //TODO
          }

          when_I_Walk_Into_The_Street { 
              //TODO 
          }

          then_I_Should_Have_My_Umbrella { 
              //TODO 
          }
      }
    
    }

 The method "test_Snowing" is storing the correct executing order defined in Gherkin template.
 
 ### Backgrounds
 
 Some scenarios under the same feature share a common background. 
 
 Given the following feature:
 
    Feature: Going to walk with background

     Background: going out
       When I Walk into the street

      Scenario: snowing with background
        Given it is snowing
        Then I should have my umbrella
 
 
 The way to implement these cases is the following:
 
    @RunWith(AndroidJUnit4::class)
    class ExampleTestClass {

        val background = backgroundGoingToWalkWithBackground {

           when_I_Walk_Into_The_Street {
               //TODO
           }
        }

       @Test
       fun testSnowingWithBackground() = test_Snowing_With_Background(background){

           given_It_Is_Snowing { 
                //TODO
           }

           then_I_Should_Have_My_Umbrella {
                //TODO
           }
       }

    }
 
 ### Outlines
 
Sometimes an scenario includes an array of examples to be acomplished in the test. The plugin generate the code necessary to
run the defined examples in the test. A loop iterate over the examples and run the same code multiple times passing the
tagged data as arguments of the functions.

Given this outline feature

    Feature: Going to walk

      Scenario Outline: weather
         Given It is <weather>
         Then I should should wear <clothes>

      Examples:
        | weather  | clothes |
        |  sunny   | t-shirt |
        |  cloudy  |  coat   |

 -
 
    @RunWith(AndroidJUnit4::class)
    class ExampleTestClass {

        @Test
        fun testOutline() = test_Weather { 
            given_It_Is_Weather { weather ->  
                //TODO
            }
            then_I_Should_Should_Wear_Clothes { clothes ->
                //TODO
            }
        }
    }
    
 ## Version
 
 Current version is 0.6.8
 

 ## License
 
    This project is licensed under the Apache 2.0 License -

    Copyright 2015 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
 
 
