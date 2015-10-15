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
 4. `Λ$`
 5. `α`, `β`, ...

If you find yourself using lots of type lambdas, and you don't mind
reserving those identifiers, then this compiler plugin is for you!

### Using the plugin

Kind-projector supports Scala 2.10, 2.11, and 2.12.0-M3.

To use this plugin in your own projects, add the following lines to
your `build.sbt` file:

```scala
resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1)

// if your project uses multiple Scala versions, use this for cross building
addCompilerPlugin("org.spire-math" % "kind-projector" % "0.7.1" cross CrossVersion.binary)
```

That's it!

Versions of the plugin earlier than 0.6.2 require a different
resolver. For these earlier releases, use this:

```scala
resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"
```

### Inline Syntax

The simplest syntax to use is the inline syntax. This syntax resembles
Scala's use of underscores to define anonymous functions like `_ + _`.

Since underscore is used for existential types in Scala (and it is
probably too late to change this syntax), we use `?` for the same
purpose. We also use `+?` and `-?` to handle covariant and
contravariant types parameters.

Here are a few examples:

```scala
Tuple2[?, Double]        // equivalent to: type R[A] = Tuple2[A, Double]
Either[Int, +?]          // equivalent to: type R[+A] = Either[Int, A]
Function2[-?, Long, +?]  // equivalent to: type R[-A, +B] = Function2[A, Long, B]
EitherT[?[_], Int, ?]    // equivalent to: type R[F[_], B] = EitherT[F, Int, B]
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
Lambda[A => (A, A)]              // equivalent to: type R[A] = (A, A)
Lambda[(A, B) => Either[B, A]]   // equivalent to: type R[A, B] = Either[B, A]
Lambda[A => Either[A, List[A]]]  // equivalent to: type R[A] = Either[A, List[A]]
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
λ[`-A` => Function1[A, Double]]          // equivalent to: type R[-A] = Function1[A, Double]
λ[(-[A], +[B]) => Function2[A, Int, B]]  // equivalent to: type R[-A, +B] = Function2[A, Int, B]
λ[`+A` => Either[List[A], List[A]]]      // equivalent to: type R[+A] = Either[List[A], List[A]]
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

Finally, variance annotations on higher-kinded sub-parameters are
supported using backticks:

```scala
Lambda[`x[+_]` => Q[x, List] // equivalent to: type R[x[+_]] = Q[x, List]
Lambda[`f[-_, +_]` => B[f]   // equivalent to: type R[f[-_, +_]] = B[f]
```

The function syntax with backtick type parameters is the most
expressive syntax kind-projector supports. The other syntaxes are
easier to read at the cost of being unable to express certain
(hopefully rare) type lambdas.

### Gotchas

The inline syntax is the tersest and is often preferable when
possible. However, there are some type lambdas which it cannot
express.

For example, imagine that we have `trait Functor[F[_]]`.

You might want to write `Functor[Future[List[?]]]`, expecting to get
something like:

```scala
type X[a] = Future[List[a]]
Functor[X]
```

However, `?` always binds at the tightest level, meaning that
`List[?]` is interpreted as `type X[a] = List[a]`, and that
`Future[List[?]]` is invalid.

In these cases you should prefer the lambda syntax, which would be
written as:

```scala
Functor[Lambda[a => Future[List[a]]]]
```

Other types which cannot be written correctly using inline syntax are:

 * `Lambda[a => (a, a)]` (repeated use of `a`).
 * `Lambda[(a, b) => Either[b, a]` (reverse order of type params).
 * `Lambda[(a, b) => Function1[a, Option[b]]` (similar to example).

(And of course, you can use `λ[...]` instead of `Lambda[...]` in any
of these expressions.)

### Under The Hood

This section shows the exact code produced for a few type lambda
expressions.

```scala
Either[Int, ?]
({type Λ$[β] = Either[Int, β]})#Λ$

Function2[-?, String, +?]
({type Λ$[-α, +γ] = Function2[α, String, γ]})#Λ$

Lambda[A => (A, A)]
({type Λ$[A] = (A, A)})#Λ$

Lambda[(`+A`, B) => Either[A, Option[B]]]
({type Λ$[+A, B] = Either[A, Option[B]]})#Λ$

Lambda[(A, B[_]) => B[A]]
({type Λ$[A, B[_]] = B[A]})#Λ$
```

As you can see, names like `Λ$` and `α` are forbidden because they
might conflict with names the plugin generates.

If you dislike these unicode names, pass `-Dkp:genAsciiNames=true` to
scalac to use munged ASCII names. This will use `L_kp` in place of
`Λ$`, `X_kp0` in place of `α`, and so on.

### Building the plugin

You can build kind-projector using SBT 0.13.0 or newer.

Here are some useful targets:

 * `compile`: compile the code
 * `package`: build the plugin jar
 * `test`: compile the test files (no tests run; compilation is the test)
 * `console`: launch a REPL with the plugin loaded so you can play around

You can use the plugin with `scalac` by specifying it on the
command-line. For instance:

```
scalac -Xplugin:kind-projector_2.10-0.6.0.jar test.scala
```

### Known issues & errata

When dealing with type parameters that take covariant or contravariant
type parameters, only the function syntax is supported. Huh???

Here's an example that highlights this issue:

```scala
def xyz[F[_[+_]]] = 12345
trait Q[A[+_], B[+_]]

// we can use kind-projector to adapt Q for xyz
xyz[λ[`x[+_]` => Q[x, List]] // ok

// but these don't work (although support for the second form
// could be added in a future release).
xyz[Q[?[+_], List]]          // invalid syntax
xyz[Q[?[`+_`], List]]        // unsupported
```

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
of `_`, which is why we chose to use `?` instead.

*Update: the Typelevel compiler contains a built-in syntax for
[type lambdas!](https://github.com/typelevel/scala/wiki/Differences#type-lambdas)*

### Future Work

As of 0.5.3, kind-projector should be able to support any type lambda
that can be expressed via type projections, at least using the
function syntax. If you come across a type for which kind-projector
lacks a syntax, please report it.

### Disclaimers

Kind projector is an unusual compiler plugin in that it runs *before*
the `typer` phase. This means that the rewrites and renaming we are
doing are relatively fragile, and the author disclaims all warranty or
liability of any kind.

(That said, there are currently no known bugs.)

If you are using kind-projector in one of your projects, please feel
free to get in touch to report problems (or a lack of problems)!

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the COPYING
file.

Copyright Erik Osheim, 2011-2015.
