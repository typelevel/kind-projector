## Kind Projector

### Dedication

> "But I don't want to go among mad people," Alice remarked.
> 
> "Oh, you can't help that," said the Cat: "we're all mad here. I'm mad.
> You're mad."
> 
> "How do you know I'm mad?" said Alice.
> 
> "You must be," said the Cat, "or you wouldn't have come here."
>  
> --Lewis Carroll, "Alice's Adventures in Wonderland"

### Overview

One piece of Scala syntactic noise that often trips people up is the
use of type projections to implement anonymous, partially-applied
types. For example:

```scala
// partially-applied type named "IntOrA"
type IntOrA[A] = Either[Int, A]

// type projection implementing the same type anonymously (without a name).
({type L[A] = Either[Int, A]})#L
```

Many people have wished for a better way to do this.

The goal of this plugin is to add a syntax for type lambdas. We do
this by rewriting syntactically valid programs into new programs,
letting us seem to add new keywords to the language. This is achieved
through a compiler plugin performing an (un-typed) tree
transformation.

One problem with this approach is that it changes the meaning of
(potentially) valid programs. In practice this means that you must
avoid defining the following identifiers:

 1. Lambda
 2. λ
 3. ?
 4. L_kp
 5. X_kp0, X_kp1, ...

If you find yourself using lots of type lambdas, and you don't mind reserving
those identifiers, then this compiler plugin is for you!

### Examples

Two syntaxes are supported. The first resembles the `_ + _` syntax for
anonymous functions and turns things like: 

```scala
Either[?, Double]
Tuple3[Int, ?, ?]
```

into type projections like:

```scala
({type L_kp[X_kp0] = Either[X_kp0, Double]})#L_kp
({type L_kp[X_kp1, X_kp2] = Tuple3[Int, X_kp1, X_kp2]})#L_kp
```

The second resembles the `(x, y) => x + y` syntax for anonymous functions and
turns things like:

```scala
Lambda[A => (A, A)]
Lambda[(A, B) => Either[A, Option[B]]]
```

into type projections like:

```scala
({type L_kp[A] = (A, A)})#L_kp
({type L_kp[A, B] = Either[A, Option[B]]})#L_kp
```

You can also use unicode if you like that sort of thing:

```scala
λ[A => (A, A)]
λ[(A, B) => Either[A, Option[B]]]
```

### Using the plugin

To use this plugin in your own projects, add the following lines to
your `build.sbt` file:

```scala
resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

// for scala 2.10
addCompilerPlugin("org.spire-math" % "kind-projector_2.10" % "0.4.0")

// for scala 2.9.3
//addCompilerPlugin("org.spire-math" % "kind-projector_2.9.3" % "0.4.0")
```

That's it!

### Building the plugin

You can build kind-projector using SBT 0.13.0 or newer.

Here are some useful targets:

 * compile: compile the code
 * package: build the plugin jar
 * test: compile the test files (no tests run; compilation is the test)
 * console: you can play around with the plugin using the console

You can use the plugin with `scalac` by specifying it on the
command-line. For instance:

```
scalac -Xplugin:kind-projector_2.10-0.4.0.jar test.scala
```

### Known issues & errata

There have been suggestions for better syntax, like
`[A, B]Either[B, A]` or `[A, B] => Either[B, A]` instead of
`Lambda[(A, B) => Either[B, A]]`.  Unfortunately this would actually
require modifying the parser (i.e. the language itself) which is
outside the scope of this project (at least, until there is an earlier
compiler phase to plug into).

Others have noted that it would be nicer to be able to use `_` for
types the way we do for values, so that we could use `Either[Int, _]`
to define a type lambda the way we use `3 + _` to define a
function. Unfortunately, it's probably too late to modify the meaning
of _, which is why we chose to use `?` instead.

Support for existentials has recently been added. The syntax is as
follows:

```scala
Lambda[A[_] => List[A[Int]]]
Lambda[(A, B[_]) => B[A]]
```

### Future Work

Variance annotations are not yet supported. It's like that the wilcard
syntax will use `+?` and `-?`, e.g. `Function2[-?, Int, +?]`.

The right approach for the lambda syntax is a bit less clear; possible
candidates are:

 * `Lambda[(-[A], +[B]) => Function2[A, Int, B]]`
 * `Lambda[(-_A, +_B) => Function2[A, Int, B]]`
 
 (Obviously, other names could be used instead of `+` and `-` here.)
 
### Disclaimers

This is only working in the most fragile sense. If you try "fancy"
things like `Either[Int, ?][Double]`, you will probably not like the
result. This project is clearly an abuse of the compiler plugin
framework and the author disclaims all warranty or liability of any
kind.

That said, if you end up using this plugin, even in a toy project,
please let me know!

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php. 

Copyright Erik Osheim, 2011-2014.
