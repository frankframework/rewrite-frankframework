## OpenRewrite Concepts Explained

## 🔧 Recipe

A high-level definition of a transformation or a sequence of transformations. Recipes describe what should be changed in the code.

Types of Recipes:

·         Declarative recipes: Written in YAML, referencing other recipes or visitors.

·         Imperative recipes: Written in Java or Kotlin, containing logic to apply visitors conditionally or in sequence.

## 👁️ Visitor

A Java class that traverses the Abstract Syntax Tree (AST) of source code. Visitors define how the code should be transformed.

Example:

A visitor might look for deprecated method calls and replace them with updated ones.

## 📄 YAML (YML)

A configuration format used to define recipes declaratively. Allows you to compose and configure recipes without writing Java code.

Example:

```
type: [specs.openrewrite.org/v1beta/recipe](//specs.openrewrite.org/v1beta/recipe)  
name: com.example.UpgradeSpringBoot  
displayName: Upgrade to Spring Boot 3  
recipeList:  
  - org.openrewrite.java.spring.boot3.UpgradeSpringBoot\_3\_0
```

## 🔌 Plugin

A way to extend OpenRewrite with custom recipes, visitors, or integrations. Plugins allow you to package and distribute your own transformations.
Use case:

You might create a plugin to enforce your organization’s coding standards or migration rules.


## 🧩 How They Work Together

1\. Visitors do the actual AST manipulation.

2\. Recipes orchestrate one or more visitors (or other recipes).

3\. YAML files define recipes declaratively for reuse and sharing.

4\. Plugins bundle recipes and visitors for use in build tools or CI pipelines.

