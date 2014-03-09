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

 1. `Lambda` and `λ`
 2. `?`, `+?`, and `-?`
 4. `L_kp`
 5. `X_kp0`, `X_kp1`, ...

If you find yourself using lots of type lambdas, and you don't mind
reserving those identifiers, then this compiler plugin is for you!

### Using the plugin

To use this plugin in your own projects, add the following lines to
your `build.sbt` file:

```scala
resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

// for scala 2.10
addCompilerPlugin("org.spire-math" % "kind-projector_2.10" % "0.5.0")

// for scala 2.9.3
//addCompilerPlugin("org.spire-math" % "kind-projector_2.9.3" % "0.5.0")
```

That's it!

### Inline Syntax

The simplest syntax to use is the inline syntax. This syntax resembles
Scala's use of underscores to define anonymous functions like `_ + _`.

Since underscore is used for existential types in Scala (and it is
probably too late to change this syntax), we use `?` for the same
purpose. We also use `+?` and `-?` to handle covariant and
contravariant types parameters.

Here are a few examples:

```scala
Tuple2[?, Double]   // equivalent to: type R[A] = Tuple2[A, Double]
Either[Int, +?]     // equivalent to: type R[+A] = Either[Int, A]
Tuple3[?, Long, ?]  // equivalent to: type R[A, B] = Tuple3[A, Long, B]
```

As you can see, this syntax works when each type parameter in the type
lambda is only used in the body once, and in the same order. For more
complex type lambda expressions, you will need to use the function
syntax.

### Function Syntax

The more powerful syntax to use is the function syntax. This syntax
resembles anonymous functions like `x => x + 1` or `(x, y) => x + y`.
In the case of type lambdas, we wrap the entire function type in a
`Lambda` or `λ` type. Both names are equivalent: the former may be
easier to type or say, and the latter is less verbose.

Here are some examples:

```scala
Lambda[A => (A, A)]             // equivalent to: type R[A] = (A, A)
Lambda[(A, B) => Either[B, A]]  // equivalent to: type R[A, B] = Either[B, A]
Lambda[A => Either[A, List[A]]  // equivalent to: type R[A] = Either[A, List[A]]
```

Since types like `(+A, +B) => Either[A, B]` are not syntactically
valid, we provide two alternate methods to specify variance when using
function syntax:

 * Plus/minus: `(+[A], +[B]) => Either[A, B]`
 * Backticks: ``(`+A`, `+B`) => Either[A, B]``

(Note that unlike names like `?`, `+` and `-` do not have to be
reserved. They will only be interpreted this way when used in
parameters to `Lambda[...]` types, which should never conflict with
other usage.)

Here are some examples with variance:

```scala
λ[`-A` => Function1[A, Double]]         // equivalent to: type R[-A] = Function1[A, Double]
λ[(-[A], +[B]) => Function2[A, Int, B]] // equivalent to: type R[-A, +B] = Function2[A, Int, B]
λ[`+A` => Either[List[A], List[A]]      // equivalent to: type R[+A] = Either[List[A], List[A]]
```

The function syntax also supports higher-kinded types as type
parameters. The syntax overloads the existential syntax in this case
(since the type parameters to a type lambda should never contain an
existential).

Here are a few examples with higher-kinded types:

```scala
Lambda[A[_] => List[A[Int]]]  // equivalent to: type R[A[_]] = List[A[Int]]
Lambda[(A, B[_]) => B[A]]     // equivalent to: type R[A, B[_]] = B[A]
```

### Under The Hood

This section shows the exact code produced for a few type lambda
expressions.

```scala
Either[Int, ?]
({type L_kp[X_kp1] = (Int, X_kp1)})#L_kp

Function2[-?, String, +?]
({type L_kp[-X_kp0, +X_kp2] = Function2[X_kp0, String, X_kp2)})#L_kp

Lambda[A => (A, A)]
({type L_kp[A] = (A, A)})#L_kp

Lambda[(`+A`, B) => Either[A, Option[B]]]
({type L_kp[+A, B] = Either[A, Option[B]]})#L_kp

Lambda[(A, B[_]) => B[A]]
({type L_kp[A, B[_]] = B[A]})#L_kp
```

As you can see, the reason that names like `L_kp` and `X_kp0` are
forbidden is that they would potentially conflict with the names of
types generated by the plugin.

### Building the plugin

You can build kind-projector using SBT 0.13.0 or newer.

Here are some useful targets:

 * compile: compile the code
 * package: build the plugin jar
 * test: compile the test files (no tests run; compilation is the test)
 * console: launch a REPL with the plugin loaded so you can play around

You can use the plugin with `scalac` by specifying it on the
command-line. For instance:

```
scalac -Xplugin:kind-projector_2.10-0.5.0.jar test.scala
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

### Future Work

As of 0.5.0, kind-projector should be able to support any type lambda
that can be expressed via type projections. If you come across a type
for which kind-projector lacks a syntax, please report it.

### Disclaimers

Kind projector is an unusual compiler plugin in that it runs *before*
the `typer` phase. This means that the rewrites and renaming we are
doing is relatively fragile, and the author disclaims all warranty or
liability of any kind.

If you are using kind-projector in one of your projects, please feel
free to get in touch to report problems (or a lack of problems)!

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php. 

Copyright Erik Osheim, 2011-2014.
