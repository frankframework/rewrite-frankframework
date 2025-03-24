# rewrite-frankframework
rewrite-frankframework is an OpenRewrite-based application which includes "recipes" and tools designed to assist developers with migrating XML configuration files built on Frank!Framework. 
The project uses OpenRewrite, a framework for programmatically transforming and refactoring code. These transformations are done based on logic defined in visitors, which are made available through recipes.

## Features
* **`Automated upgrading:`** Upgrade XML configurations within just a couple of minutes through pre-defined recipes.
* **`Reusable recipes:`** Reuse parameterized recipes to expand upon the pre-defined recipes with ease though yaml.
* **`Plug-and-play:`** No need to integrate this standalone tool into your own Frank!Framework project. Clone the project, and you're good to go.

## Limitations
* **`Limited version support:`** Currently we only support migrations between Frank!Framework-version 7.3.x through 9.0.x.
* **`Limited coverage:`** Currently we don't provide full coverage for all needed migrations. This project is still a work in progress and should be treated as such, don't blindly trust the output before commiting any changes to your project!

## Requirements
* **`Required frank-runner installation:`** To run the installRecipes and runRecipes scripts users need to have [frank-runner](https://github.com/wearefrank/frank-runner) installed in the same directory as this project.

## Getting started
### 1. Clone the repository
```git clone https://github.com/frankframework/rewrite-frankframework```

```cd rewrite-frankframework```
### 2. Build the project
Run the installation script using Command Prompt (applies to the next script, as well). This might take a while. 
```.\installRecipes.bat```

The batch script installs the recipes using the provided Maven wrapper, so there's no need to have Maven installed locally.
### 3. Run recipes
Run recipes on your Frank!Framework project by providing the relative or absolute path to your target project. 
After proving the path you should provide the target version of Frank!Framework to reference the needed recipes. This requires a certain syntax: [major version]_[minor version] (for example "7_4" references the recipes needed to migrate from 7.3.x to 7.4.x).
Optionally you can provide your current Frank!Framework version if you want to run a range of recipes, without this argument it only runs a single recipe for the specified minor version.

**Note: Not correctly ending the target path with a "\" (for example: ..\..\myproject, instead of ..\..\myproject\) when using runRecipes.bat will run the script in the parent directory (the wrong directory), and could cause unwanted changes to other projects!**

```cd .\reciperunner\```

```.\runRecipes.bat [relative path your project] [target Frank!Framework version] [current Frank!Framework version]```

An example for migrating from 7.3.x to 9.0.x could be:

```.\runRecipes.bat ..\..\myproject\ 9_0 7_3```

*Tip: you can also copy the "reciperunner" folder and paste it in your project's parent folder to more conveniently access your target project through the relative path argument.*

## How to contribute
We endorse contributions to this project! If you would like to suggest any changes or even add additional missing recipes, please follow the following steps:
1. Fork the repository and create a new branch.
2. Make your changes (e.g., new recipes, bug fixes, etc.).
3. Create a pull request with a description of your changes.
4. Ensure that your changes don't break existing functionality by running the test cases.
