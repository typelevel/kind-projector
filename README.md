## Kind Projector

[![Build Status](https://travis-ci.org/typelevel/kind-projector.svg?branch=master)](https://travis-ci.org/typelevel/kind-projector)

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
 2. `*`, `+*`, and `-*`
 3. `?`, `+?`, and `-?` ([deprecated syntax](https://github.com/typelevel/kind-projector/issues/108); same meaning as the above)
 4. `Λ$`
 5. `α$`, `β$`, ...

If you find yourself using lots of type lambdas, and you don't mind
reserving those identifiers, then this compiler plugin is for you!

### Using the plugin

Kind-projector supports Scala 2.10, 2.11, 2.12, and 2.13.

To use this plugin in your own projects, add the following lines to
your `build.sbt` file:

```scala
resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// if your project uses multiple Scala versions, use this for cross building
addCompilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary)

// if your project uses both 2.10 and polymorphic lambdas
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" =>
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
  case _ =>
    Nil
})
```
_Note_: for multi-project builds - put `addCompilerPlugin` clause into settings section for each sub-project.

For maven projects, add the plugin to the configuration of the
maven-scala-plugin (remember to use `_2.10`, `_2.11` or
`_2.12` as appropriate):

    <plugin>
      <groupId>net.alchim31.maven</groupId>
      <artifactId>scala-maven-plugin</artifactId>
      ...
      <configuration>
        <compilerPlugins>
          <compilerPlugin>
            <groupId>org.typelevel</groupId>
            <artifactId>kind-projector_2.12</artifactId>
            <version>0.10.1</version>
          </compilerPlugin>
        </compilerPlugins>
      </configuration>
    </plugin>

That's it!

Versions of the plugin earlier than 0.10.0 were released under a
different organization (`org.spire-math`).

### Inline Syntax

The simplest syntax to use is the inline syntax. This syntax resembles
Scala's use of underscores to define anonymous functions like `_ + _`.

Since underscore is used for existential types in Scala (and it is
probably too late to change this syntax), we use `*` for the same
purpose. We also use `+*` and `-*` to handle covariant and
contravariant types parameters.

Here are a few examples:

```scala
Tuple2[*, Double]        // equivalent to: type R[A] = Tuple2[A, Double]
Either[Int, +*]          // equivalent to: type R[+A] = Either[Int, A]
Function2[-*, Long, +*]  // equivalent to: type R[-A, +B] = Function2[A, Long, B]
EitherT[*[_], Int, *]    // equivalent to: type R[F[_], B] = EitherT[F, Int, B]
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

(Note that unlike names like `*`, `+` and `-` do not have to be
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

### Type lambda gotchas

The inline syntax is the tersest and is often preferable when
possible. However, there are some type lambdas which it cannot
express.

For example, imagine that we have `trait Functor[F[_]]`.

You might want to write `Functor[Future[List[*]]]`, expecting to get
something like:

```scala
type X[a] = Future[List[a]]
Functor[X]
```

However, `*` always binds at the tightest level, meaning that
`List[*]` is interpreted as `type X[a] = List[a]`, and that
`Future[List[*]]` is invalid.

In these cases you should prefer the lambda syntax, which would be
written as:

```scala
Functor[Lambda[a => Future[List[a]]]]
```

Other types which cannot be written correctly using inline syntax are:

 * `Lambda[a => (a, a)]` (repeated use of `a`).
 * `Lambda[(a, b) => Either[b, a]]` (reverse order of type params).
 * `Lambda[(a, b) => Function1[a, Option[b]]]` (similar to example).

(And of course, you can use `λ[...]` instead of `Lambda[...]` in any
of these expressions.)

### Under The Hood

This section shows the exact code produced for a few type lambda
expressions.

```scala
Either[Int, *]
({type Λ$[β$0$] = Either[Int, β$0$]})#Λ$

Function2[-*, String, +*]
({type Λ$[-α$0$, +γ$0$] = Function2[α$0$, String, γ$0$]})#Λ$

Lambda[A => (A, A)]
({type Λ$[A] = (A, A)})#Λ$

Lambda[(`+A`, B) => Either[A, Option[B]]]
({type Λ$[+A, B] = Either[A, Option[B]]})#Λ$

Lambda[(A, B[_]) => B[A]]
({type Λ$[A, B[_]] = B[A]})#Λ$
```

As you can see, names like `Λ$` and `α$` are forbidden because they
might conflict with names the plugin generates.

If you dislike these unicode names, pass `-Dkp:genAsciiNames=true` to
scalac to use munged ASCII names. This will use `L_kp` in place of
`Λ$`, `X_kp0$` in place of `α$`, and so on.

### Polymorphic lambda values

Scala does not have built-in syntax or types for anonymous function
values which are polymorphic (i.e. which can be parameterized with
types). To illustrate that consider both of these methods:

```scala
def firstInt(xs: List[Int]): Option[Int] = xs.headOption
def firstGeneric[A](xs: List[A]): Option[A] = xs.headOption
```

Having implemented these methods, we can see that the second just
generalizes the first to work with any type: the function bodies are
identical. We'd like to be able to rewrite each of these methods as a
function value, but we can only represent the first method
(`firstInt`) this way:

```scala
val firstInt0: List[Int] => Option[Int] = _.headOption
val firstGeneric0 <what to put here???>
```

(One reason to want to do this rewrite is that we might have a method
like `.map` which we'd like to pass an anonymous function value.)

Several libraries define their own polymorphic function types, such as
the following polymorphic version of `Function1` (which we can use to
implement `firstGeneric0`):

```scala
trait PolyFunction1[-F[_], +G[_]] {
  def apply[A](fa: F[A]): G[A]
}

val firstGeneric0: PolyFunction1[List, Option] =
  new PolyFunction1[List, Option] {
    def apply[A](xs: List[A]): Option[A] = xs.headOption
  }
```

It's nice that `PolyFunction1` enables us to express polymorphic
function values, but at the level of syntax it's not clear that we've
saved much over defining a polymorphic method (i.e. `firstGeneric`).

Since 0.9.0, Kind-projector provides a value-level rewrite to fix this
issue and make polymorphic functions (and other types that share their
general shape) easier to work with:

```scala
val firstGeneric0 = λ[PolyFunction1[List, Option]](_.headOption)
```

Either `λ` or `Lambda` can be used (in a value position) to trigger
this rewrite. By default, the rewrite assumes that the "target method"
to define is called `apply` (as in the previous example), but a
different method can be selected via an explicit call.

In the following example we are using the polymorphic lambda syntax to
define a `run` method on an instance of the `PF` trait:

```scala
trait PF[-F[_], +G[_]] {
  def run[A](fa: F[A]): G[A]
}

val f = Lambda[PF[List, Option]].run(_.headOption)
```

It's possible to nest this syntax. Here's an example taken from
[the wild](http://www.slideshare.net/timperrett/enterprise-algebras-scala-world-2016/49)
of using nested polymorphic lambdas to remove boilerplate:

```scala
// without polymorphic lambdas, as in the slide
def injectFC[F[_], G[_]](implicit I: Inject[F, G]) =
  new (FreeC[F, *] ~> FreeC[G, *]) {
    def apply[A](fa: FreeC[F, A]): FreeC[G, A] =
      fa.mapSuspension[Coyoneda[G, *]](
        new (Coyoneda[F, *] ~> Coyoneda[G, *]) {
          def apply[B](fb: Coyoneda[F, B]): Coyoneda[G, B] = fb.trans(I)
        }
      )
  }

// with polymorphic lambdas
def injectFC[F[_], G[_]](implicit I: Inject[F, G]) =
  λ[FreeC[F, *] ~> FreeC[G, *]](
    _.mapSuspension(λ[Coyoneda[F, *] ~> Coyoneda[G, *]](_.trans(I)))
  )
```

Kind-projector's support for type lambdas operates at the *type level*
(in type positions), whereas this feature operates at the *value
level* (in value positions). To avoid reserving too many names the `λ`
and `Lambda` names were overloaded to do both (mirroring the
relationship between types and their companion objects).

Here are some examples of expressions, along with whether the lambda
symbol involved represents a type (traditional type lambda) or a value
(polymorphic lambda):

```scala
// type lambda (type level)
val functor: Functor[λ[a => Either[Int, a]]] = implicitly

// polymorphic lambda (value level)
val f = λ[Vector ~> List](_.toList)

// type lambda (type level)
trait CF2 extends Contravariant[λ[a => Function2[a, a, Double]]] {
  ...
}

// polymorphic lambda (value level)
xyz.translate(λ[F ~> G](fx => fx.flatMap(g)))
```

One pattern you might notice is that when `λ` occurs immediately
within `[]` it is referring to a type lambda (since `[]` signals a
type application), whereas when it occurs after `=` or within `()` it
usually refers to a polymorphic lambda, since those tokens usually
signal a value. (The `()` syntax for tuple and function types is an
exception to this pattern.)

The bottom line is that if you could replace a λ-expression with a
type constructor, it's a type lambda, and if you could replace it with
a value (e.g. `new Xyz[...] { ... }`) then it's a polymorphic lambda.

### Polymorphic lambdas under the hood

What follows are the gory details of the polymorphic lambda rewrite.

Polymorphic lambdas are a syntactic transformation that occurs just
after parsing (before name resolution or typechecking). Your code will
be typechecked *after* the rewrite.

Written in its most explicit form, a polymorphic lambda looks like
this:

```scala
λ[Op[F, G]].someMethod(<expr>)
```

and is rewritten into something like this:

```scala
new Op[F, G] {
  def someMethod[A](x: F[A]): G[A] = <expr>(x)
}
```

(The names `A` and `x` are used for clarity –- in practice unique
names will be used for both.)

This rewrite requires that the following are true:

 * `F` and `G` are unary type constructors (i.e. of shape `F[_]` and `G[_]`).
 * `<expr>` is an expression of type `Function1[_, _]`.
 * `Op` is parameterized on two unary type constructors.
 * `someMethod` is parametric (for any type `A` it takes `F[A]` and returns `G[A]`).

For example, `Op` might be defined like this:

```scala
trait Op[M[_], N[_]] {
  def someMethod[A](x: M[A]): N[A]
}
```

The entire λ-expression will be rewritten immediately after parsing
(and before name resolution or typechecking). If any of these
constraints are not met, then a compiler error will occur during a
later phase (likely type-checking).

Here are some polymorphic lambdas along with the corresponding code
after the rewrite:

```scala
val f = Lambda[NaturalTransformation[Stream, List]](_.toList)
val f = new NaturalTransformation[Stream, List] {
  def apply[A](x: Stream[A]): List[A] = x.toList
}

type Id[A] = A
val g = λ[Id ~> Option].run(x => Some(x))
val g = new (Id ~> Option) {
  def run[A](x: Id[A]): Option[A] = Some(x)
}

val h = λ[Either[Unit, *] Convert Option](_.fold(_ => None, a => Some(a)))
val h = new Convert[Either[Unit, *], Option] {
  def apply[A](x: Either[Unit, A]): Option[A] =
    x.fold(_ => None, a => Some(a))
}

// that last example also includes a type lambda.
// the full expansion would be:
val h = new Convert[({type Λ$[β$0$] = Either[Unit, β$0$]})#Λ$, Option] {
  def apply[A](x: ({type Λ$[β$0$] = Either[Unit, β$0$]})#Λ$): Option[A] =
    x.fold(_ => None, a => Some(a))
}
```

Unfortunately the type errors produced by invalid polymorphic lambdas
are likely to be difficult to read. This is an unavoidable consequence
of doing this transformation at the syntactic level.

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
xyz[Q[*[+_], List]]          // invalid syntax
xyz[Q[*[`+_`], List]]        // unsupported
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
of `_`, which is why we chose to use `*` instead.

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

### Community

People are expected to follow the
[Scala Code of Conduct](https://scala-lang.org/conduct/)
when discussing Kind-projector on GitHub or other venues.

The project's current maintainers are:

 * [Erik Osheim](http://github.com/non)
 * [Tomas Mikula](http://github.com/tomasmikula)
 * [Seth Tisue](https://github.com/sethtisue)
 * [Miles Sabin](https://github.com/milessabin)

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the COPYING
file.

Copyright Erik Osheim, 2011-2019.
